package de.eStudent.modulGuide.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

/**
 * WIRD IM MOMENT NICHT BENUTZT KOMMT IN DER ZUKUNFT
 * 
 * Klasse um die Datenbak und die Preferences in der Cloud zu speichern
 */
public class MyBackupAgent extends BackupAgentHelper
{

	// A key to uniquely identify the set of backup data
	static final String PREFS_BACKUP_KEY = "prefs";
	static final String PREFS_FILE_NAME = "de.eStudent.modulGuide_preferences";

	@Override
	public void onCreate()
	{
		Log.d("test", "vll wurde was geamcht???");

		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS_FILE_NAME);
		addHelper(PREFS_BACKUP_KEY, helper);
		Log.d("test", "die zweite");
	}
}
