package de.eStudent.modulGuide.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

/**
 * Ställt Hilfsmethoden für das Internet zur verfügung
 * 
 * @author Jan Rahlf
 * 
 */
public class NetworkHelper {

	/**
	 * Überprüft, ob das Handy momentan über eine Internetverbindung verfügt.
	 * 
	 * @param context
	 *            Der Context
	 * @return Ja / Nein
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	
	/**
	 * Überprüft das vorhandensein einer SD-Karte
	 * @param requireWriteAccess
	 * @return Ja / Nein
	 */
	public static boolean hasStorage(boolean requireWriteAccess) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

}
