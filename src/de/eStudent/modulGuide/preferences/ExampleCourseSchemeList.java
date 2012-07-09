package de.eStudent.modulGuide.preferences;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import de.eStudent.modulGuide.XMLParser.ExampleCourseSchemeXMLHandler;
import de.eStudent.modulGuide.common.ExpandAnimation;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.network.ImportHelper;
import de.eStudent.modulGuide.network.NetworkHelper;


/**
 * Ansicht für die Musterstudienpläne
 *
 */
public class ExampleCourseSchemeList extends SherlockActivity
{
	//Der Context
	private Context context;
	
	//Die Liste für die Namen der Musterstudienpläne
	private ListView list;
	
	//Der zugehörige Adapter
	private SimpleCursorAdapter adapter;
	
	//Viewswitcher
	private ViewSwitcher viewSwitcher;
	
	//Textview für Fehlermeldungen
	private TextView errorMsg;
	
	//boolean ob der Thread zum Laden der Namen der Musterstudeinpläne gerade aktiv ist
	private boolean threadIsRunning = false;
	
	//Die aktuelle Seite des ViewPagers
	private int currentPage = 1;
	
	//Der DataHelper
	private DataHelper helper;
	
	//TimeStamp, wann die Ansicht das letzte Mal aktualsiert wurde
	private TextView timeStamp;
	
	//Der Cursor der Die Namen der Musterstudienpläne enthällt
	private Cursor myCursor;
	
	//refereny eiens threads um ihn vorzeitig beenden zu können
	private AsyncTask task;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.example_course_scheme_list);

		context = this;

		//Layout der Actionbar setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		helper = new DataHelper(context);
		
		
		//-- ab heir LAyout setzen ---

		list = (ListView) findViewById(R.id.list);

		list.setEmptyView(findViewById(R.id.emptyView));

		
		//Bei einem LongKlick den Musterstudienplan auswählen
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long id)
			{
				myCursor.moveToPosition(position);
				final String name = myCursor.getString(2);

				QuickActionWidget mBar = new QuickActionBar(context);
				mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_passed, "auswählen"));

				mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
				{
					@Override
					public void onQuickActionClicked(QuickActionWidget widget, int position)
					{

						switch (position)
						{
						//Musterstudeinplan auswählen
						case 0:

							if (PreferenceHelper.getExampleCourseSchemeName(context).equals(name))
								Toast.makeText(context, "Musterstudienplan bereits ausgewählt", Toast.LENGTH_LONG).show();
							else if (!PreferenceHelper.getExampleCourseSchemeName(context).equals(""))
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setMessage("Sicher dass ein neuer Musterstudienplan gewählt werden soll?").setCancelable(false)
										.setPositiveButton("Ja", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int id)
											{
												dialog.cancel();

												task = new getCourseSchemeFromServer().execute(myCursor.getString(2));
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
								task = new getCourseSchemeFromServer().execute(myCursor.getString(2));

							break;
						}
					}

				});

				mBar.show(view);
				return true;
			}

		});

		
		//Bei einem Short Klick Die Description des angeklickten Musterstudienplanes ausfahren
		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
			{
				LinearLayout layout = (LinearLayout) view.findViewById(R.id.descriptionLayout);
				ImageView expander = (ImageView) view.findViewById(R.id.expandImage);
				TextView expandText = (TextView) view.findViewById(R.id.expandText);

				ExpandAnimation anim = new ExpandAnimation(layout, 500);
				myCursor.moveToPosition(position);

				if (layout.getAnimation() != null && !layout.getAnimation().hasEnded())
				{
					return;
				}

				if (layout.getVisibility() == View.VISIBLE)
				{
					expander.setImageResource(R.drawable.expander_open);
					expandText.setVisibility(View.VISIBLE);
				} else
				{
					expander.setImageResource(R.drawable.expander_close);
					expandText.setVisibility(View.INVISIBLE);
				}
				layout.startAnimation(anim);
				layout.requestLayout();

			}
		});

		timeStamp = (TextView) findViewById(R.id.timestamp);

		// timeStamp.setText("Letzte Aktualisierung: " +
		// PreferenceHelper.getFormattedExaminationRegulationTimeStamp(context));

		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		errorMsg = (TextView) findViewById(R.id.errorText);

		task = new LoadDataTask().execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.example_course_scheme_list_menu, menu);

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
			
		//Anischt aktualisieren
		case R.id.refresh:
			if (!threadIsRunning)
				new LoadDataTask(true).execute();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Initalisiert die Liste für die Namen aller Musterstudienpläne.
	 */
	private void initList()
	{

		adapter = new SimpleCursorAdapter(

		getApplicationContext(),

		R.layout.example_course_scheme_list_list_item,

		myCursor,

		new String[] { "name", "description", "originalName" },// from

				new int[] { R.id.name, R.id.description, R.id.selected });

		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder()
		{

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex)
			{
				if (columnIndex == 2)
				{

					if (cursor.getString(columnIndex).equals(PreferenceHelper.getExampleCourseSchemeName(context)))
						view.setVisibility(View.VISIBLE);
					else
						view.setVisibility(View.INVISIBLE);

					return true;

				}
				return false;
			}
		});

		list.setAdapter(adapter);

		adapter.notifyDataSetChanged();
	}

	// Thread um die Namen aller Musterstudeinpläne zu laden
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

			threadIsRunning = true;

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
				PreferenceHelper.saveExampleCourseSchemeTimeStamp(context);

			case 1:
				timeStamp.setText("Letzte Aktualisierung: " + PreferenceHelper.getFormattedExampleCourseSchemeTimeStamp(context));
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
			threadIsRunning = false;
			task = null;

		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Integer doInBackground(Object... params)
		{

			if (!refresh)
				myCursor = helper.getCiriculumNames();

			try
			{
				if (refresh || myCursor.getCount() == 0)
				{

					if (!NetworkHelper.isOnline(context))
						return NO_INET_ACCESS;

					ImportHelper i = new ImportHelper();

					i.getCurriculum(context);
					refresh = true;
					myCursor = helper.getCiriculumNames();
					return 2;

				}
			} catch (Exception e)
			{

				Log.e("fail", "internet fail", e);
				e.printStackTrace();

				// Es gab einen Fehler
				return FAIL;
			}

			return 1;
		}

	}

	// Thread um eine spezifische XML runterzuladen
	public class getCourseSchemeFromServer extends AsyncTask<String, Integer, Integer>
	{

		ProgressDialog progressDialog;
		final static int NO_INET_ACCESS = -1;
		final static int FAIL = -2;
		String xml;
		String XMLName;

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
				new ParseStudyCoursePlan(xml, XMLName).execute();

				break;
			}

			adapter.notifyDataSetChanged();
			progressDialog.dismiss();

		}

		@Override
		protected Integer doInBackground(String... values)
		{

			if (!NetworkHelper.isOnline(context))
				return NO_INET_ACCESS;

			ImportHelper i = new ImportHelper();
			XMLName = values[0];
			try
			{
				xml = i.getExampleCourseScheme(XMLName, this);
				helper.deleteRecommendedPlan();
			} catch (IOException e)
			{
				Log.d("ExampleCourseScheme", "XML runterladen", e);
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

	
	/**
	 * Thread zum Parsen eines Musterstudienplanes
	 *
	 */
	private class ParseStudyCoursePlan extends AsyncTask<Object, Object, Integer>
	{

		String plan;
		String XMLName;
		ProgressDialog dialog;

		public ParseStudyCoursePlan(String plan, String XMLName)
		{
			this.plan = plan;
			this.XMLName = XMLName;
		}

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
				Toast.makeText(context, "Musterstudienplan fehlerhaft", Toast.LENGTH_LONG).show();

			dialog.dismiss();
			adapter.notifyDataSetChanged();
		}

		@Override
		protected Integer doInBackground(Object... params)
		{
			try
			{
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				ExampleCourseSchemeXMLHandler myXMLHandler = new ExampleCourseSchemeXMLHandler();
				xr.setContentHandler(myXMLHandler);

				InputSource is = new InputSource(new ByteArrayInputStream(plan.getBytes("UTF-8")));
				xr.parse(is);
				helper.addRecommendedPlan(myXMLHandler.list);
				PreferenceHelper.saveExampleCourseSchemeName(XMLName, context);

			} catch (Exception e)
			{

				e.printStackTrace();
				return 1;
			}

			return 0;
		}

	}

	
	/**
	 * Ansicht für die Namen der Musterstudienpläne
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
	 * Fehleransicht, die eine Fehlernachricht anzeigt
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
