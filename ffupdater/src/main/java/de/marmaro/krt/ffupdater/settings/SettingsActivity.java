package de.marmaro.krt.ffupdater.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.marmaro.krt.ffupdater.R;
import de.marmaro.krt.ffupdater.background.UpdateChecker;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MainPreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);
            Preference prefCheckInterval = findPreference(getString(R.string.pref_check_interval));
            prefCheckInterval.setOnPreferenceChangeListener(reconfigureUpdateCheckerOnChange);

            Preference prefBuild = findPreference(getString(R.string.pref_build));
            prefBuild.setOnPreferenceChangeListener(displayWarningOnSwitchingToUnsafeBuild);
        }

        private Preference.OnPreferenceChangeListener reconfigureUpdateCheckerOnChange = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String valueAsString = (String) newValue;
                int value = Integer.parseInt(valueAsString);
                UpdateChecker.registerOrUnregister(value);
                return true;
            }
        };

        private Preference.OnPreferenceChangeListener displayWarningOnSwitchingToUnsafeBuild = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Context context = getContext();
                String value = (String) newValue;
                if (context.getString(R.string.default_pref_build).equals(value)) {
                    return true;
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(context.getString(R.string.switch_to_unsafe_build_title))
                        .setMessage(context.getString(R.string.switch_to_unsafe_build_message))
                        .setPositiveButton(context.getText(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        };
    }
}
