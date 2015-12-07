package net.galmiza.android.spectrogram;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		// Init values?
	    
	    // Run update
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		UpdateConfiguration(R.array.preferences_configuration);
	}
	
	/**
	 * UpdateConfiguration
	 * Map items of ressourceId to several arrays string[3] = type, key, pattern
	 * Thus array dimension should be multiple of 3
	 * Replaces summary of preference 'key' by 'pattern' taking current value as input 
	 */
	@SuppressWarnings("deprecation")
	private void UpdateConfiguration(int ressourceId) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String[] cf = getResources().getStringArray(ressourceId);
		
		for (int i=0; i<cf.length/4; i++) {
			String type = cf[4*i+0];
			String object = cf[4*i+1];
			String key = cf[4*i+2];
			String pattern = cf[4*i+3];
			
			Preference preference = (Preference) findPreference(key);
			if (preference == null) continue;
			
			String text = "";
			if (type.equals("string")) text = String.format(pattern, sharedPreferences.getString(key, ""));
			if (type.equals("integer")) text = String.format(pattern, sharedPreferences.getInt(key, 100));
			
			if (object.equals("summary")) preference.setSummary(text);
			if (object.equals("title")) preference.setTitle(text);
		}
	}
	
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		// Whatever the change is, update all!
		UpdateConfiguration(R.array.preferences_configuration);
	}
}
