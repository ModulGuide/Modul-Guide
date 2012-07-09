package de.eStudent.modulGuide.database;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import de.eStudent.modulGuide.preferences.PreferenceHelper;


/**
 * Thread zum Löschen / Resetten der Datenbank.
 *
 */
public class DeleteDatabaseTask extends AsyncTask
{
	//ProgressDialog
	private ProgressDialog dialog;
	
	//Context
	private Context context;
	
	//DataHelper
	private DataHelper dataHelper;

	/**
	 * Konstruktor
	 * @param context
	 */
	public DeleteDatabaseTask(Context context)
	{
		this.context = context;
	}

	// can use UI thread here
	@Override
	protected void onPreExecute()
	{
		dialog = ProgressDialog.show(context, "", "Deleting. Please wait...", true);
	}

	// can use UI thread here
	@Override
	protected void onPostExecute(Object result)
	{
		dialog.dismiss();
	}

	// automatically done on worker thread (separate from UI thread)
	@Override
	protected Object doInBackground(Object... params)
	{

		try
		{
			dataHelper = new DataHelper(context);
			dataHelper.deleteExaminationRegulationData();

			// Auch Preferences zurücksetzen
			PreferenceHelper.resetAll(context);

		} finally
		{
			dataHelper.close();
		}
		return null;
	}

}