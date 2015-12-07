package net.galmiza.android.spectrogram;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class Misc {
	
	// Map for associative array
	private static HashMap<String,Object> map = new HashMap<String,Object>();
	public static Object getAttribute(String s)	{ return map.get(s); }
	public static void setAttribute(String s, Object o)	{ map.put(s, o); }
	public static void resetAttributes() { map = new HashMap<String,Object>(); }

    // Sleep
    public static void sleep(int ms) {
    	try { Thread.sleep(ms); } catch (InterruptedException e) { e.printStackTrace(); }
    }
   
	/////////////
    // Message //
	/////////////
    public static void Toast(final Activity a, final String message, final int duration) {
		a.runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	Toast.makeText(a.getBaseContext(), message, duration).show();
	        }
		});
    }
    public static void Toast(final String message, final int duration) {
    	Toast(getActivity(), message, duration);
    }
    
    
    // String
    public static String unicodeEscape(String s) {
    	final char[] hexChar = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
		    if ((c >> 7) > 0) {
				sb.append("\\u");
				sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
				sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
				sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
				sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
		    } else {
		    	sb.append(c);
		    }
		}
		return sb.toString();
    }
    

    // Android
    public static boolean isIntentAvailable(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    

	public static void getOverflowMenu(Context context) {
		try {
			ViewConfiguration config = ViewConfiguration.get(context);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {	e.printStackTrace(); } // Should never happen since field above is valid
	}
	
	
	
	
	////////////
	// DIALOGS
	////////////
	public static interface OnSimpleInputDialogListener {
        void onInput(String input) throws Exception;
    }
	public static void onSimpleInputDialog(final Context context, String title, String message, String text, final OnSimpleInputDialogListener listener) {
		
		// Create dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(title);
		if (message != null) alert.setMessage(message);
		final EditText input = new EditText(context);
		if (text!=null) {
			input.setText(text);
			alert.setView(input);
		}

		// Handle button actions
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String text = input.getText().toString().trim();	
				try {
					listener.onInput(text);
				} catch (Exception e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
	
		alert.setNegativeButton("Cancel", null);
		alert.show();
	}
	public static interface OnMultiInputHintDialogListener {
        void onInput(String[] inputs) throws Exception;
    }
	public static void onMultiInputHintDialog(final Context context, String title, String message, String[] hints, String[] texts, final OnMultiInputHintDialogListener listener) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(context);  
		
		// Dynamically create dialog
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);
		if (message != null) alert.setMessage(message);
		final EditText[] edits = new EditText[hints.length];
		for (int i=0; i<edits.length; i++) {
			edits[i] = new EditText(context);
			if (texts != null) if (texts[i] != null) edits[i].setText(texts[i]);
			edits[i].setHint(hints[i]);
			ll.addView(edits[i]);
		}
		alert.setView(ll);
        alert.setTitle(title);

        // Handle button actions
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		String[] texts = new String[edits.length];
        		for (int i=0; i<edits.length; i++)
        			texts[i]=edits[i].getText().toString().trim();
        		try {
					listener.onInput(texts);
				} catch (Exception e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
        	}
        });                 
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {     
        	public void onClick(DialogInterface dialog, int whichButton) {   
        		dialog.cancel();  
        	}    
        });         
        alert.create();
        alert.show();
	}

	//////////////
	// PROGRESS
	//////////////
	public static ProgressDialog progressDialog(Context context, String title, String message, boolean cancelable, boolean canceledOnTouchOutside) {
		ProgressDialog progress = new ProgressDialog(context);
		progress.setTitle(title);
		progress.setMessage(message);
		progress.setCancelable(cancelable);
		progress.setCanceledOnTouchOutside(canceledOnTouchOutside);
		return progress;
	}
	

	////////////////
	// PREFERENCES
	////////////////
	public static void setPreference(Activity a, String key, boolean value) {	PreferenceManager.getDefaultSharedPreferences(a).edit().putBoolean(key, value).commit();	}
	public static void setPreference(Activity a, String key, float value) {		PreferenceManager.getDefaultSharedPreferences(a).edit().putFloat(key, value).commit();	}
	public static void setPreference(Activity a, String key, int value) {		PreferenceManager.getDefaultSharedPreferences(a).edit().putInt(key, value).commit();	}
	public static void setPreference(Activity a, String key, long value) {		PreferenceManager.getDefaultSharedPreferences(a).edit().putLong(key, value).commit();	}
	public static void setPreference(Activity a, String key, String value) {	PreferenceManager.getDefaultSharedPreferences(a).edit().putString(key, value).commit();	}
	
	public static boolean getPreference(Activity a, String key, boolean def) {	return PreferenceManager.getDefaultSharedPreferences(a).getBoolean(key, def);	}
	public static float getPreference(Activity a, String key, float def) {		return PreferenceManager.getDefaultSharedPreferences(a).getFloat(key, def);	}
	public static int getPreference(Activity a, String key, int def) {			return PreferenceManager.getDefaultSharedPreferences(a).getInt(key, def);	}
	public static long getPreference(Activity a, String key, long def) {		return PreferenceManager.getDefaultSharedPreferences(a).getLong(key, def);	}
	public static String getPreference(Activity a, String key, String def) {	return PreferenceManager.getDefaultSharedPreferences(a).getString(key, def);	}

	public static void setPreference(String key, boolean value) {	setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, float value) {		setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, int value) {		setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, long value) {		setPreference(getActivity(), key, value);	}
	public static void setPreference(String key, String value) {	setPreference(getActivity(), key, value);	}
	
	public static boolean getPreference(String key, boolean def) {	return getPreference(getActivity(), key, def);	}
	public static float getPreference(String key, float def) {		return getPreference(getActivity(), key, def);	}
	public static int getPreference(String key, int def) {			return getPreference(getActivity(), key, def);	}
	public static long getPreference(String key, long def) {		return getPreference(getActivity(), key, def);	}
	public static String getPreference(String key, String def) {	return getPreference(getActivity(), key, def);	}
	
	/*	public static void setPreference(Activity a, String key, boolean value) {	a.getPreferences(Context.MODE_PRIVATE).edit().putBoolean(key, value).commit();	}
	public static void setPreference(Activity a, String key, float value) {		a.getPreferences(Context.MODE_PRIVATE).edit().putFloat(key, value).commit();	}
	public static void setPreference(Activity a, String key, int value) {		a.getPreferences(Context.MODE_PRIVATE).edit().putInt(key, value).commit();	}
	public static void setPreference(Activity a, String key, long value) {		a.getPreferences(Context.MODE_PRIVATE).edit().putLong(key, value).commit();	}
	public static void setPreference(Activity a, String key, String value) {	a.getPreferences(Context.MODE_PRIVATE).edit().putString(key, value).commit();	}
	
	public static boolean getPreference(Activity a, String key, boolean def) {	return a.getPreferences(Context.MODE_PRIVATE).getBoolean(key, def);	}
	public static float getPreference(Activity a, String key, float def) {		return a.getPreferences(Context.MODE_PRIVATE).getFloat(key, def);	}
	public static int getPreference(Activity a, String key, int def) {			return a.getPreferences(Context.MODE_PRIVATE).getInt(key, def);	}
	public static long getPreference(Activity a, String key, long def) {		return a.getPreferences(Context.MODE_PRIVATE).getLong(key, def);	}
	public static String getPreference(Activity a, String key, String def) {	return a.getPreferences(Context.MODE_PRIVATE).getString(key, def);	} */
	
	public static Activity getActivity() {
		return (Activity) getAttribute("activity");
	}
}
