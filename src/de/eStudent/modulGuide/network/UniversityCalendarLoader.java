package de.eStudent.modulGuide.network;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import de.eStudent.modulGuide.XMLParser.CompleteUniversityCalendarXMLHandler;
import de.eStudent.modulGuide.common.StopParsingException;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;


/**
 * Thread zum herunterladen des komplettenn Vorlesungsverzeichnisses
 *
 */
public class UniversityCalendarLoader extends AsyncTask<String, Integer, Integer>
{

	//Der Context
	private Context context;
	
	//ProgressDialog
	public ProgressDialog progressDialog;
	
	//Referenz zum Thread, um diesen vorzeitig beenden zu können
	public UniversityCalendarLoader thread;
	
	//DataHelper
	private DataHelper helper;
	
	//boolean ob der Thread gerade aktiv ist
	public volatile boolean running = true;

	
	//---- statics für den Erfolg beim Herunterladen des Vorlesungsverzeichnisses
	private final static int NO_INET_ACCESS = -1;
	private final static int FAIL = -2;
	private final static int SUCCESSFULL = 0;
	private final static int STOP = -3;

	
	/**
	 * Konstructor
	 * @param context
	 * @param completly boolean ob, Das komplette Vorlesungsverzeichnis heruntergeladen werden soll oder ggf. nur der fehlende Teil
	 */
	public UniversityCalendarLoader(Context context, boolean completly)
	{
		this.context = context;
		thread = this;
		helper = new DataHelper(context);
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		progressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Integer result)
	{
		switch (result)
		{
		case STOP:
			// nothing special to do
			break;

		case SUCCESSFULL:

			PreferenceHelper.setUniversityCalendarComplete(context, true);
			PreferenceHelper.saveUniversityCalendarSubjectsTimeStamp(context, helper.calculateUniversityCalendarTimestamp());
			break;

		case NO_INET_ACCESS:
			Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_LONG).show();

			break;

		case FAIL:
			Toast.makeText(context, "Verbindungsfehler", Toast.LENGTH_LONG).show();

			break;
		}

		progressDialog.dismiss();
		
		helper.close();
		helper.notifyDatabaseListeners();


		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute()
	{

		PreferenceHelper.setUniversityCalendarComplete(context, false);

		//Progress Dialog erstellen
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage("Downloading...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(false);
		
		/*
		progressDialog.setOnCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				thread.cancel(true);
			}
		});
		
		*/

		progressDialog.show();
	}

	@Override
	protected void onCancelled(Integer result)
	{
		Log.d("hier", "bcancel");
		helper.notifyDatabaseListeners();
		if(helper.db.inTransaction())
			helper.db.endTransaction();
		helper.close();
		running = false;
		super.onCancelled(result);
	}

	@Override
	protected void onCancelled()
	{
		Log.d("hier", "bcancel");

		helper.notifyDatabaseListeners();
		if(helper.db.inTransaction())
			helper.db.endTransaction();
		helper.close();
		running = false;
		super.onCancelled();
	}

	@Override
	protected Integer doInBackground(String... params)
	{

		if (!NetworkHelper.isOnline(context))
			return NO_INET_ACCESS;

		ImportHelper i = new ImportHelper();
		try
		{

			helper.db.beginTransaction();
			try
			{
				URL url = new URL(ImportHelper.LECTURESPATH + "all_lectures.xml");

				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				CompleteUniversityCalendarXMLHandler myXMLHandler = new CompleteUniversityCalendarXMLHandler(helper, this);
				xr.setContentHandler(myXMLHandler);

				InputSource is = new InputSource(url.openStream());
				xr.parse(is);

				helper.db.setTransactionSuccessful();

			} finally
			{
				if(helper.db.inTransaction())
					helper.db.endTransaction();
			}

		} catch (StopParsingException e)
		{
			return STOP;
		}

		catch (Exception e)
		{
			e.printStackTrace();
			return FAIL;
		}

		return SUCCESSFULL;
	}

	public void publishProgress(int progress)
	{
		super.publishProgress(progress);
	}

	
}
