package de.marmaro.krt.ffupdater.app.impl

import de.marmaro.krt.ffupdater.network.github.GithubConsumer
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockKExtension::class)
internal class FirefoxFocusBetaIT : BaseAppIT() {

    @Test
    fun findAppUpdateStatus() {
        val firefoxFocusBeta = FirefoxFocusBeta(GithubConsumer.INSTANCE, deviceAbiExtractor)
        val result = runBlocking { firefoxFocusBeta.findAppUpdateStatus(context) }
        verifyThatDownloadLinkAvailable(result.downloadUrl)
        val releaseDate = ZonedDateTime.parse(result.publishDate, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val age = Duration.between(releaseDate, ZonedDateTime.now())
        assertTrue(age.toDays() < 3 * 7) { "${age.toDays()} days is too old" }
    }
}