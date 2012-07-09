package de.eStudent.modulGuide.universityCalendar;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.CourseEdit;
import de.eStudent.modulGuide.CourseSignUp;
import de.eStudent.modulGuide.Details;
import de.eStudent.modulGuide.PassedCourseDialog;
import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.StudiumhelferActivity;
import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.MyAlphabetizedAdapter;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.common.Status;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;
import de.eStudent.modulGuide.database.ModulguideContentProvider;
import de.eStudent.modulGuide.network.ImportHelper;
import de.eStudent.modulGuide.network.NetworkHelper;
import de.eStudent.modulGuide.network.NetworkListener;
import de.eStudent.modulGuide.network.UniversityCalendarLoader;
import de.eStudent.modulGuide.preferences.PreferenceHelper;
import de.eStudent.modulGuide.preferences.Preferences;

public class UniversityCalendarBaseActivity extends SherlockFragmentActivity implements DatabaseListener
{

	//KEY für die im Intent zu übergebene ID des Faches für den die Kurse angezeigt werden sollen
	public static final String ID_KEY = "id";

	
	
	//---Actions für die QuickActions----
	private final static int ACTION_PASSED = 0;
	private final static int ACTION_FAILED = 1;
	private final static int ACTION_SIGN_UP = 2;
	private final static int ACTION_EDIT = 3;
	private final static int ACTION_DETAILS = 4;
	public final static String ID_KEY_FOR_SEARCH = "id";

	//Keyword zur Filterung der Kurse
	private String keyword = null;
	
	//Die ListView der anzuzeigenden Kurse
	private ListView myListView;
	
	//Datahelper
	private DataHelper dataHelper;

	//ViewSwitcher
	private ViewSwitcher viewSwitcher;
	
	//Textview für Fehlermeldungen
	private TextView errorMsg;
	
	//Aktuelle SEite des Viewpagers
	private int currentPage = 1;
	
	//boolean ob sich die Datenbank geändert hat, seitdem die Activity pausiert wurde
	private boolean dataChanged = false;
	
	//boolean, ob die Activity gerade aktiv ist
	private boolean isVisible = true;
	
	//Thread zum Herunterladen des kompletten Vorlesungsverzeichnisses
	private UniversityCalendarLoader loader;
	
	//referenz zu einem AsynkTask, um diesen vorzeitig beenden zu können
	private AsyncTask task;

	//Cursor der die Lsite der Kurse enthällt
	private Cursor myCursor;

	//Der Context
	private Context context;

	//Der zugeörige Adapter für die Liste 
	private MyAlphabetizedAdapter adapter;
	
	//quickactionBar für die Quickactions
	private QuickActionWidget mBar;
	
	//Timestamp, wann die Ansicht, das letzte Mal aktualisert wurde
	private TextView timeStamp;

	//Die ID des Faches für den gerade die Kurse angezeigt werden
	private long id;

	
	//boolean, ob der Thread zum laden der Kurse gerade aktiv ist
	private boolean loadDataThreadIsRunning = false;

	
	/**
	 * Für die Suche
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		// suche
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH))
		{
			keyword = intent.getStringExtra(SearchManager.QUERY);

			final RelativeLayout searchBar = (RelativeLayout) findViewById(R.id.searchCancelBar);

			searchBar.setVisibility(View.VISIBLE);

			TextView searchKeyword = (TextView) findViewById(R.id.searchKeyword);
			searchKeyword.setText(keyword);

			ImageButton cancelSearch = (ImageButton) findViewById(R.id.cancelSearch);
			cancelSearch.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					keyword = null;
					searchBar.setVisibility(View.GONE);
					task = new FetchDataTask(null, false).execute();
				}
			});

			task = new FetchDataTask(keyword, false).execute();

			//es wurde direkt ein Element der Kursvorschläge ausgewählt
		} else if (Intent.ACTION_VIEW.equals(intent.getAction()))
		{

			Intent courseOverviewIntent = new Intent(this, Details.class);

			courseOverviewIntent.setData(intent.getData());
			courseOverviewIntent.putExtra(Statics.ID, Long.parseLong(intent.getData().getLastPathSegment()));
			courseOverviewIntent.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);

			startActivity(courseOverviewIntent);
			finish();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.university_calendar_main);

		
		//ActionBar Layout setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		context = this;

		dataHelper = new DataHelper(context);
		dataHelper.addDataBaseListener(this);

		
		//---Layout setzen ---
		
		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		errorMsg = (TextView) findViewById(R.id.errorText);

		myListView = (ListView) findViewById(R.id.universityCalendarList);

		
		//Bei einem ShortClick die Details des Kurses aufrufen
		myListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
			{

				myCursor.moveToPosition(adapter.getPositionInCursor(position));

				Intent intent = new Intent(context, Details.class);
				intent.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
				intent.putExtra(Statics.ID, myCursor.getLong(0));
				startActivity(intent);

			}

		});

		
		//Bei einem Logpress die Quickaktions öffnen
		myListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
			{

				myCursor.moveToPosition(adapter.getPositionInCursor(position));

				final int status = myCursor.getInt(2);

				mBar = new QuickActionGrid(context);
				if (status != Status.STATUS_PASSED && status != Status.STATUS_REQUIREMENTS_MISSING)
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_passed, R.string.passed, ACTION_PASSED));

				if (status == Status.STATUS_SIGNED_UP)
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_failed, R.string.failed, ACTION_FAILED));

				if (status != Status.STATUS_SIGNED_UP && status != Status.STATUS_PASSED && status != Status.STATUS_REQUIREMENTS_MISSING)
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_sign_up, R.string.sign_in, ACTION_SIGN_UP));

				if (status == Status.STATUS_PASSED || status == Status.STATUS_SIGNED_UP)
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_edit, R.string.edit, ACTION_EDIT));

				mBar.addQuickAction(new QuickAction(context, R.drawable.action_details, R.string.details, ACTION_DETAILS));

				mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
				{

					@Override
					public void onQuickActionClicked(QuickActionWidget widget, int qposition)
					{

						switch (qposition)
						{
						//Kurs besatanden
						case ACTION_PASSED:

							Intent intent = new Intent(context, PassedCourseDialog.class);
							intent.putExtra(Statics.ID, myCursor.getLong(0));
							intent.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
							startActivity(intent);
							break;
						//Durs durchgefallen
						case ACTION_FAILED:

							dataHelper.setCourseFailed(dataHelper.getCourseFromUniversityCalendar(myCursor.getLong(0)));

							break;
						//Für Kurs anmelden
						case ACTION_SIGN_UP:
							int requiredCourse = dataHelper.isRequiredCourse(myCursor.getLong(0));
							if (requiredCourse != 1 && !dataHelper.isChoosableOrOptionalUniversityCalendarCourse(myCursor.getLong(0)))
							{
								Intent intent4 = new Intent(context, CourseSignUp.class);
								intent4.putExtra(Statics.ID, myCursor.getLong(0));
								String semester = PreferenceHelper.getSemester(context);
								intent4.putExtra(CourseEdit.SEMESTER_KEY, Integer.parseInt(semester));
								startActivity(intent4);
							} else
							{
								ChildEntry entry = null;
								dataHelper.setUniversityCalendarCourseSignedUp(myCursor.getLong(0), Integer.parseInt(PreferenceHelper.getSemester(context)),
										entry);
							}
							break;

						//Kurs editieren
						case ACTION_EDIT:
							Intent intent2 = new Intent(context, CourseEdit.class);
							intent2.putExtra(Statics.ID, myCursor.getLong(0));
							intent2.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
							startActivity(intent2);
							break;

						//Kurs Details aufrufen
						case ACTION_DETAILS:
							Intent intent3 = new Intent(context, Details.class);
							intent3.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);// UniversityCalendarCourseOverview.UNIVERSITY_CALENDAR_COURSE_ID);
							intent3.putExtra(Statics.ID, myCursor.getLong(0));
							startActivity(intent3);
						}
					}
				});

				if (PreferenceHelper.getRegulationName(context).length() == 0)
					Toast.makeText(context, "Wähle zuerst eine Pürfungsordnung aus!", Toast.LENGTH_LONG).show();
				else
					mBar.show(view);

				return true;
			}
		});

		Intent intent = getIntent();

		Bundle extras = intent.getExtras();
		id = extras.getLong(ID_KEY, 0);
		timeStamp = (TextView) findViewById(R.id.universityCalendarTimestamp);

		// Fall alle Kurse
		if (id == 0)
		{
			actionBar.setTitle("Alle Kurse");
			timeStamp.setText("Letzte Aktualisierung: " + PreferenceHelper.getFormattedExaminationRegulationTimeStamp(context));
		}
		// Fall alleKurse eines bestimmten Faches
		else
		{
			actionBar.setTitle(dataHelper.getUniversityCalendarSubjectName(id));
			timeStamp.setText("Letzte Aktualisierung: " + dataHelper.getFormattedUniversityCalendarSubjectTimestamp(id));
		}

		task = new FetchDataTask(null, false).execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.university_calendar_base_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		//startansicht aufrufen
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;
			
		//Suche
		case R.id.search:
			onSearchRequested();
			return true;
		//Liste der Fächer öffnen
		case R.id.universityCalendarSubjects:
			Intent intent = new Intent(getBaseContext(), UniversityCalendarSubjects.class);
			startActivity(intent);
			return true;
			
		//Ansicht aktualisieren
		case R.id.refresh:
			if (!loadDataThreadIsRunning)
				task = new FetchDataTask(true).execute();
			return true;
			
		//Einstellungen öffnen
		case R.id.preferences:
			Intent preferenceActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(preferenceActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	/**
	 * Thread zum laden aller Kurse eines Faches 
	 * */
	private class FetchDataTask extends AsyncTask<Object, Integer, Integer> implements NetworkListener
	{
	
		private final static int NO_INET_ACCESS = -1;
		private final static int FAIL = -2;
		private String keyword;
		private boolean reload = false;
		private boolean refresh = false;

		public FetchDataTask(boolean reload)
		{
			this.reload = reload;
		}

		public FetchDataTask(String keyword, boolean refresh)
		{
			this.keyword = keyword;
			this.refresh = refresh;
		}

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{
			if (!refresh)
			{

				showErrorView();
				errorMsg.setText("Loading Data ...");

				setSupportProgressBarIndeterminateVisibility(true);

				loadDataThreadIsRunning = true;

			}

			setSupportProgressBarVisibility(true);
			setSupportProgress(0);
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{
			switch (result)
			{

			case 1:
				if (reload)
				{
					timeStamp.setText("Letzte Aktualisierung: " + dataHelper.getFormattedUniversityCalendarSubjectTimestamp(id));
					initList(true);
				}
				if (refresh)
					adapter.changeCursor(myCursor);
				else
				{
					initList(true);
					showListView();
				}
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
			setSupportProgressBarVisibility(false);

			loadDataThreadIsRunning = false;

			task = null;

		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Integer doInBackground(Object... params)
		{

			if (!reload)
			{
				if (keyword == null)
					myCursor = dataHelper.getUniversityCalendarCoursesCursor(id);
				else
				{
					myCursor = dataHelper.getFilteredUniversityCalendarCoursesCursor(id, keyword);

				}
			}

			if ((reload || myCursor.getCount() == 0) && keyword == null)
			{
				if (!NetworkHelper.isOnline(context))
					return NO_INET_ACCESS;

				ImportHelper i = new ImportHelper();
				try
				{
					i.saveLectureXML(dataHelper.getOriginalUniversityCalendarSubjectName(id), id, this, context);
					dataHelper.setUniversityCalendarSubjectDownloadedStatus(id, true);
					dataHelper.setUniversityCalendarSubjectTimeStamp(id);
					reload = true;
					myCursor = dataHelper.getUniversityCalendarCoursesCursor(id);

				} catch (Exception e)
				{
					Log.d("tag", "Daten Laden", e);
					return FAIL;
				}
			}

			return 1;

		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			setSupportProgress((Window.PROGRESS_END - Window.PROGRESS_START) / 100 * values[0]);
		}

		@Override
		public void update(int value)
		{
			publishProgress(value);

		}

		
		/**
		 * Initialsiert die Liste für die Kurse
		 * @param dataSetChanged OB sich die Kursliste geändert hat
		 */
		private void initList(boolean dataSetChanged)
		{
			if (dataSetChanged || adapter == null)
			{

				adapter = new MyAlphabetizedAdapter(

				getApplicationContext(),

				R.layout.university_calendar_row,

				myCursor,

				new String[] { "name", "status" },// from

						new int[] { R.id.universityCalendarName, R.id.universityCalendarStatus });

				adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder()
				{

					@Override
					public boolean setViewValue(View view, Cursor cursor, int columnIndex)
					{

						if (columnIndex == 2)
						{

							ImageView image = (ImageView) view.findViewById(R.id.universityCalendarStatus);

							int status = cursor.getInt(columnIndex);

							if (status != 0)
							{
								image.setVisibility(View.VISIBLE);

								image.setImageResource(de.eStudent.modulGuide.common.Status.getIcon(status));

							} else
								image.setVisibility(View.GONE);

							return true;

						}

						return false;
					}
				});
				myListView.setAdapter(adapter);

			}
			adapter.notifyDataSetChanged();
			adapter.notifyDataSetInvalidated();

		}
	}

	/**
	 * zeigt die Lsitenansicht für die Kurse an
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
	 * ZEigt die Fehelrsnischt mit einer Fehelermeldung an
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

		if (loader != null)
		{
			loader.running = false;
			loader.progressDialog.dismiss();
		}

		if (myCursor != null)
			myCursor.close();

		if (dataHelper != null)
		{
			dataHelper.close();
		}

		super.onDestroy();

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		isVisible = false;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		isVisible = true;

		if (dataChanged)
		{
			task = new FetchDataTask(keyword, true).execute();
			dataChanged = false;

		}

	}

	
	@Override
	public boolean onSearchRequested()
	{

		ContentProviderClient client = getContentResolver().acquireContentProviderClient("de.eStudent.modulGuide.database.ModulguideContentProvider");
		ModulguideContentProvider provider = (ModulguideContentProvider) client.getLocalContentProvider();
		provider.setSubjectSearchId(id);

		Bundle appData = new Bundle();
		appData.putLong(UniversityCalendarBaseActivity.ID_KEY_FOR_SEARCH, id);
		startSearch(null, false, appData, false);
		return true;
	}

	@Override
	public void update()
	{
		if (isVisible)
			task = new FetchDataTask(keyword, true).execute();
		else
			dataChanged = true;

	}

	/**
	 * OnClick MEthode für das UniversityCalendarWarningFragment
	 * @param view
	 */
	public void universityCalendarWarningClick(View view)
	{
		if (!PreferenceHelper.isUniversityCalendarComplete(context))
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(this, false).execute();
		else
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(this, false).execute();
	}

}
