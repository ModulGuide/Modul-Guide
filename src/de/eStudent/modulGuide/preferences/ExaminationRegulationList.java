package de.eStudent.modulGuide.preferences;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.StudiumhelferActivity;
import de.eStudent.modulGuide.XMLParser.ExaminationRegulationXMLHandler;
import de.eStudent.modulGuide.common.MyAlphabetizedAdapter;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.network.ImportHelper;
import de.eStudent.modulGuide.network.NetworkHelper;


/**
 * Ansicht für das auswählen einer Prüfungsordnung
 *
 */
public class ExaminationRegulationList extends SherlockActivity
{
	
	//Der Cursor der die Liste der Prüfungsordnungen beinhaltet
	private Cursor myCursor;
	
	//Der Context
	private Context context;
	
	//Die ListView der Prüfungsordnungen
	private ListView list;
	
	//Der zugehörige Adapter
	private MyAlphabetizedAdapter adapter;
	
	//Der name der ausgewählten Prüfungsordnung
	private String name;
	
	//Der Viewswitcher
	private ViewSwitcher viewSwitcher;
	
	//Textview für Fehlermeldungen
	private TextView errorMsg;
	
	//boolean ob der Thread zum laden der Prüfungsordnungsnamen läuft
	private boolean gettingRegulationNamesThreadIsRunning = false;
	
	//integer, welche Seite des Viewpagers gerade aktiv ist
	private int currentPage = 1;
	
	//Der Datahelper
	private DataHelper helper;
	
	//Der Timestamp, wann die Ansicht, das letzte mal aktualisiert wurde
	private TextView timeStamp;
	
	//Anyctask, zum vorzeitigen beenden
	private AsyncTask task;
	
	//keyword nach dem die Liste der Prüfungsordnungen gefiltert werden soll
	private String keyword;

	private File dir;

	
	/**
	 * Für die Suche
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		
		//Suche
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH))
		{
			keyword = intent.getStringExtra(SearchManager.QUERY);

			final RelativeLayout searchBar = (RelativeLayout) findViewById(R.id.searchCancelBar);

			searchBar.setVisibility(View.VISIBLE);

			TextView searchKeyword = (TextView) findViewById(R.id.searchKeyword);
			searchKeyword.setText(keyword);

			
			//Zum beenden der gefilterten Suche
			ImageButton cancelSearch = (ImageButton) findViewById(R.id.cancelSearch);
			cancelSearch.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					keyword = null;
					searchBar.setVisibility(View.GONE);
					task = new LoadDataTask().execute();
				}
			});

			new LoadSearchResultFromDatabaseTask().execute(keyword);

		//es wurde direkt ein Eintrag der Suchvorschläge ausgewählt
		} else if (Intent.ACTION_VIEW.equals(intent.getAction()))
		{
			new LoadSearchResultFromDatabaseTask().execute(intent.getData());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.examination_regulation_list);

		context = this;

		//Layout der Actionbar setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		
		//Ordner zum speichern der heruntergeladenen PDFs
		 dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		 dir.mkdirs();

		
		//--Ab hier Layout setzen ---
		helper = new DataHelper(context);

		list = (ListView) findViewById(R.id.examinationRegulationListList);
		timeStamp = (TextView) findViewById(R.id.timestamp);

		viewSwitcher = (ViewSwitcher) findViewById(R.id.examinationRegulationListSwitcher);
		errorMsg = (TextView) findViewById(R.id.examinationRegulationListErrorText);

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, final int position, long id)
			{

				myCursor.moveToPosition(adapter.getPositionInCursor(position));
				name = myCursor.getString(3);

				QuickActionWidget mBar = new QuickActionBar(context);
				mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_passed, "auswählen"));
				mBar.addQuickAction(new QuickAction(context, R.drawable.action_pdf, "PDF auswählen"));
				if ((new File(dir, myCursor.getString(4))).exists())
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_delete_pdf, "PDF löschen"));

				mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
				{
					@Override
					public void onQuickActionClicked(QuickActionWidget widget, int position)
					{

						switch (position)
						{
						// Prüfungsordnung xml runterladen parsen speichern
						case 0:

							if (PreferenceHelper.getRegulationName(context).equals(name))
								Toast.makeText(context, "Prüfungsordnung bereits ausgewählt", Toast.LENGTH_LONG).show();
							else if (!PreferenceHelper.getRegulationName(context).equals(""))
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setMessage("Sicher dass eine neue Prüfungsordnung gewählt werden soll? Der ganze Fortschritt wird damit gelöscht.")
										.setCancelable(false).setPositiveButton("Ja", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int id)
											{
												dialog.cancel();

												new getRegulationXMLFromServer().execute(myCursor.getString(3));
											}
										}).setNegativeButton("Nein", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int id)
											{
												dialog.cancel();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}

							else
								new getRegulationXMLFromServer().execute(myCursor.getString(3));
							break;

						// PDF der Prüfungsordnung runterladen -> speichern ->
						// öffnen
						case 1:
							if(NetworkHelper.hasStorage(true))
								new getRegulationPDFromServer(myCursor.getString(4)).execute();
							else
								Toast.makeText(context, "SD_Karte benötigt", Toast.LENGTH_LONG).show();
							break;

						//PDF löschen
						case 2:
							File file = new File(dir, myCursor.getString(4));
							boolean success = file.delete();
							if (!success)
								Toast.makeText(context, "Löschen fehlgeschlagen", Toast.LENGTH_SHORT);
							else
								adapter.notifyDataSetChanged();

							break;
						}

					}
				});

				mBar.show(view);

			}

		});

		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				list.getOnItemClickListener().onItemClick(arg0, arg1, arg2, arg3);
				return true;
			}
		});

		if (!gettingRegulationNamesThreadIsRunning)
			task = new LoadDataTask().execute();

	}

	
	/**
	 * Starte Suche
	 */
	private void startSearch()
	{
		if (myCursor != null && myCursor.getCount() > 0)
			onSearchRequested();

		else
			Toast.makeText(context, "Nichts zu durchsuchen", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.examination_regulation_list_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		//Startansicht öffnen
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;
		
		//Suche
		case R.id.search:
			startSearch();
			return true;
			
		//Liste der Prüfungsordnungen aktualisieren
		case R.id.refresh:
			if (!gettingRegulationNamesThreadIsRunning)
				task = new LoadDataTask(true).execute();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	/**
	 * Initialisiert die Lsiten für die Namen der Prüfungsordnungen
	 */
	private void initList()
	{

		adapter = new MyAlphabetizedAdapter(

		getApplicationContext(),

		R.layout.examination_regulation_list_list_item,

		myCursor,

		new String[] { "name", "date", "originalName", "pdfName" },// from

				new int[] { R.id.examinationRegulationListListItemtextView1, R.id.examinationRegulationListListItemtextView2,
						R.id.examinationRegulationListListItemImage, R.id.examinationRegulationListListItemPdf });

		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder()
		{

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex)
			{
				if (columnIndex == 3)
				{

					if (cursor.getString(columnIndex).equals(PreferenceHelper.getRegulationName(context)))
						view.setVisibility(View.VISIBLE);
					else
						view.setVisibility(View.INVISIBLE);

					return true;

				} else if (columnIndex == 4)
				{
					if ((new File(dir, cursor.getString(4)).exists()))
						view.setVisibility(View.VISIBLE);
					else
						view.setVisibility(View.GONE);

					return true;
				}
				return false;
			}
		});

		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	// Thread um eine Suche in der Datenbank nach Regulations zu machen
	private class LoadSearchResultFromDatabaseTask extends AsyncTask<Object, Object, Integer>
	{

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{
			gettingRegulationNamesThreadIsRunning = true;

			setSupportProgressBarIndeterminateVisibility(true);

			list.setAdapter(null);

			showListView();

		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{

			initList();

			setSupportProgressBarIndeterminateVisibility(false);

			gettingRegulationNamesThreadIsRunning = false;

		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Integer doInBackground(Object... params)
		{
			if (params[0] instanceof String)
				myCursor = helper.searchInRegulationBuffer((String) params[0]);
			else if (params[0] instanceof Uri)
				myCursor = helper.searchIdInRegulationBuffer(((Uri) params[0]).toString());

			return 0;
		}

	}

	// Thread um die NAmen aller Prüfungsordnungen zu laden
	private class LoadDataTask extends AsyncTask<Object, Object, Integer>
	{

		final static int NO_INET_ACCESS = -1;
		final static int FAIL = -2;

		boolean refresh = false;

		public LoadDataTask(boolean refresh)
		{
			this.refresh = refresh;
		}

		public LoadDataTask()
		{
		}

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{
			showErrorView();
			errorMsg.setText("Loading Data ...");

			gettingRegulationNamesThreadIsRunning = true;

			setSupportProgressBarIndeterminateVisibility(true);

			list.setAdapter(null);

		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{

			switch (result)
			{
			case 2:
				PreferenceHelper.saveExaminationRegulationTimeStamp(context);

			case 1:
				timeStamp.setText("Letzte Aktualisierung: " + PreferenceHelper.getFormattedExaminationRegulationTimeStamp(context));
				initList();
				showListView();

				break;

			case NO_INET_ACCESS:

				showErrorView();
				errorMsg.setText("Keine Verbindung");

				break;

			case FAIL:
				showErrorView();

				errorMsg.setText("Verbindungs Fehler");

				break;
			}

			setSupportProgressBarIndeterminateVisibility(false);

			gettingRegulationNamesThreadIsRunning = false;
			task = null;

		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Integer doInBackground(Object... params)
		{

			if (!refresh)
				myCursor = helper.getRegulationBuffer();

			try
			{
				if (refresh || myCursor.getCount() == 0)
				{

					if (!NetworkHelper.isOnline(context))
						return NO_INET_ACCESS;

					ImportHelper i = new ImportHelper();

					i.getRegulations(context);
					refresh = true;
					myCursor = helper.getRegulationBuffer();
					return 2;

				}
			} catch (Exception e)
			{

				Log.e("fail", "internet fail", e);

				// Es gab einen Fehler
				return FAIL;
			}

			return 1;
		}

	}

	// Thread um eine spezifische PDF runterzuladen
	public class getRegulationPDFromServer extends AsyncTask<String, Integer, Integer>
	{
		String regulationXML;
		final static int NO_INET_ACCESS = -1;
		final static int FAIL = -2;

		ProgressDialog progressDialog;

		private String name;
		boolean fileExists;

		getRegulationPDFromServer(String name)
		{
			this.name = name;
			if ((new File(dir, name)).exists())
				fileExists = true;
		}

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{

			if (!fileExists)
			{
				progressDialog = new ProgressDialog(context);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setMessage("Loading PDF...");
				progressDialog.setCancelable(true);
				progressDialog.show();
			}
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{
			if (!fileExists)
				progressDialog.dismiss();

			switch (result)
			{
			case NO_INET_ACCESS:
				Toast.makeText(context, "Keine Verbindung", Toast.LENGTH_LONG).show();
				break;
			case FAIL:
				Toast.makeText(context, "Verbindungsfehler", Toast.LENGTH_LONG).show();

				break;
			}

			adapter.notifyDataSetChanged();

		}

		@Override
		protected Integer doInBackground(String... values)
		{

			ImportHelper i = new ImportHelper();

			Uri path;

			if (!fileExists)
			{
				if (!NetworkHelper.isOnline(context))
					return NO_INET_ACCESS;

				try
				{
					path = i.getRegulationPDF(name, this);

				} catch (IOException e)
				{
					Log.d("ExaminationRegulationList", "PDF runterladen", e);
					return FAIL;

				}

			} else
				path = Uri.fromFile(new File(dir, name));
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(path, "application/pdf");
			startActivity(Intent.createChooser(intent, "PDF öffnen..."));

			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			progressDialog.setProgress(values[0]);
		}

		// @Override
		public void publishProgress(int progress)
		{
			super.publishProgress(progress);
		}

	}

	// Thread um eine spezifische XML runterzuladen
	public class getRegulationXMLFromServer extends AsyncTask<String, Integer, Integer>
	{
		String regulationXML;

		final static int NO_INET_ACCESS = -1;
		final static int FAIL = -2;

		ProgressDialog progressDialog;

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{

			progressDialog = new ProgressDialog(context);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{
			switch (result)
			{
			case NO_INET_ACCESS:
				Toast.makeText(context, "Keine Verbindung", Toast.LENGTH_LONG).show();
				break;
			case FAIL:
				Toast.makeText(context, "Verbindungs Fehler", Toast.LENGTH_LONG).show();
				break;
			default:
				new ParseAndSaveExaminationRegualtionTask().execute(regulationXML);
				break;
			}

			progressDialog.dismiss();

		}

		@Override
		protected Integer doInBackground(String... values)
		{

			if (!NetworkHelper.isOnline(context))
				return NO_INET_ACCESS;

			ImportHelper i = new ImportHelper();

			try
			{
				regulationXML = i.getRegulationXML(values[0], this);
			} catch (IOException e)
			{
				Log.d("ExaminationRegulationList", "XML runterladen", e);
				return FAIL;
			}

			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			progressDialog.setProgress(values[0]);
		}

		public void publishProgress(int progress)
		{
			super.publishProgress(progress);
		}

	}

	// Thread um eine spezifische XML in der Datenbank zu speichern
	private class ParseAndSaveExaminationRegualtionTask extends AsyncTask<String, Integer, Integer>
	{

		ProgressDialog dialog;

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{
			dialog = ProgressDialog.show(context, "", "Parse and Saving. Please wait...", true);
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{
			if (result == 1)
				Toast.makeText(context, "Prüfungsordnung fehlerhaft", Toast.LENGTH_LONG).show();

			dialog.dismiss();
			adapter.notifyDataSetChanged();
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Integer doInBackground(String... params)
		{

			helper.deleteExaminationRegulationData();
			PreferenceHelper.saveExampleCourseSchemeName("", context);
			PreferenceHelper.resetExampleCourseSchemeTimeStamp(context);

			try
			{
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				ExaminationRegulationXMLHandler myXMLHandler = new ExaminationRegulationXMLHandler();
				xr.setContentHandler(myXMLHandler);

				InputSource is = new InputSource(new ByteArrayInputStream(params[0].getBytes("UTF-8")));
				xr.parse(is);
				helper.addCategories(myXMLHandler.listofCategory);
				helper.addCourseOfStudies(myXMLHandler.mainInfo);
				PreferenceHelper.saveRegulationName(name, context);

			} catch (Exception e)
			{
				e.printStackTrace();
				return 1;

			}

			return 0;
		}

	}

	
	/**
	 * Ruft die normale Listenasicht mit den Namen der Prüfungsordnungena 
	 * auf
	 */
	private void showListView()
	{
		if (currentPage != 1)
		{
			viewSwitcher.showPrevious();
			currentPage = 1;
		}
	}

	
	/**
	 * Ruft die äFEhleransicht auf, die eine Fehlernachricht zeigt
	 */
	private void showErrorView()
	{
		if (currentPage != 2)
		{
			viewSwitcher.showNext();
			currentPage = 2;
		}
	}

	@Override
	public void onDestroy()
	{
		if (task != null)
			task.cancel(true);

		if (myCursor != null)
			myCursor.close();

		if (helper != null)
		{
			helper.close();
		}

		super.onDestroy();

	}

}
