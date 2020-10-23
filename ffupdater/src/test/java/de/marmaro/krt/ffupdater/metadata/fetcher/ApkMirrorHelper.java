package de.marmaro.krt.ffupdater.metadata.fetcher;

import com.google.common.base.Preconditions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ApkMirrorHelper {

    public static RssFeedResponse getRssFeedResponse(String feedUrl) throws IOException, ParserConfigurationException, SAXException {
        final Document document;
        final HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(feedUrl).openConnection();
        try (InputStream original = urlConnection.getInputStream()) {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(original);
            document.getDocumentElement().normalize();
        } finally {
            urlConnection.disconnect();
        }

        final Element latestItem = getLatestItem(document);
        return new RssFeedResponse(getTitle(latestItem), getUrlToAppVersion(latestItem));
    }

    private static Element getLatestItem(Document document) {
        final NodeList items = document.getElementsByTagName("item");
        Objects.requireNonNull(items);
        final Element latestItem = ((Element) items.item(0));
        Objects.requireNonNull(latestItem);
        return latestItem;
    }

    private static String getTitle(Element latestItem) {
        return latestItem.getElementsByTagName("title").item(0).getTextContent();
    }

    private static String getUrlToAppVersion(Element latestItem) {
        return latestItem.getElementsByTagName("link").item(0).getTextContent();
    }

    public static class RssFeedResponse {
        private final String title;
        private final String urlToAppVersion;

        public RssFeedResponse(String title, String urlToAppVersion) {
            this.title = title;
            this.urlToAppVersion = urlToAppVersion;
        }

        public String getTitle() {
            return title;
        }

        public String getUrlToAppVersion() {
            return urlToAppVersion;
        }
    }

    public static String extractSha256HashFromAbiVersionPage(RssFeedResponse rssFeedResponse, Map<String, String> replacements) throws IOException {
        String[] urlParts = rssFeedResponse.getUrlToAppVersion().split("/");

        String apkName = urlParts[urlParts.length - 1];
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            apkName = apkName.replace(replacement.getKey(), replacement.getValue());
        }
        String apkUrl = rssFeedResponse.getUrlToAppVersion() + apkName;
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(apkUrl).openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            String totalResponse = response.toString();
            String textWithHash = totalResponse.split("APK file hashes")[1].split("Verify the file")[0];

            Matcher matcher = Pattern.compile("([a-z0-9]{64})").matcher(textWithHash);
            Preconditions.checkArgument(matcher.find());
            return matcher.group(0);
        } finally {
            urlConnection.disconnect();
        }
    }
}
