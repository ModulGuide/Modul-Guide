package de.eStudent.modulGuide.universityCalendar;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.app.SearchManager;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.actionbarsherlock.view.Window;

import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.StudiumhelferActivity;
import de.eStudent.modulGuide.common.MyAlphabetizedAdapter;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;
import de.eStudent.modulGuide.network.ImportHelper;
import de.eStudent.modulGuide.network.NetworkHelper;
import de.eStudent.modulGuide.network.NetworkListener;
import de.eStudent.modulGuide.network.UniversityCalendarLoader;
import de.eStudent.modulGuide.preferences.PreferenceHelper;
import de.eStudent.modulGuide.preferences.Preferences;

public class UniversityCalendarSubjects extends SherlockFragmentActivity implements DatabaseListener
{

	Context context;
	Cursor cursor;
	ListView list;
	DataHelper helper;
	TextView timeStamp;
	int currentPage = 1;
	ViewSwitcher viewSwitcher;
	TextView errorMsg;
	long favouriteId = 0;
	String keyword = null;
	boolean isVisible = true;
	boolean dataChanged = false;
	private UniversityCalendarLoader loader;

	private AsyncTask task;

	MyAlphabetizedAdapter adapter;
	boolean gettingLectureNamesThreadIsRunning = false;

	private static final int ACTION_DOWNLOAD = 0;
	private static final int ACTION_DELETE = 1;
	private static final int ACTION_FAVOURITE = 2;
	private static final int ACTION_DELETE_FAVOURITE = 3;

	private final static String TAG = "UniversityCalendarSubjects";

	@Override
	protected void onNewIntent(Intent intent)
	{
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
					task = new LoadDataTask().execute();
				}
			});

			task = new LoadDataTask(keyword).execute();

		} else if (Intent.ACTION_VIEW.equals(intent.getAction()))
		{
			Intent courseListIntent = new Intent(this, UniversityCalendarBaseActivity.class);
			courseListIntent.putExtra(UniversityCalendarBaseActivity.ID_KEY, Long.parseLong(intent.getData().toString()));
			startActivity(courseListIntent);
			finish();

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.university_calendar_subjects);

		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		context = this;

		helper = new DataHelper(context);
		helper.addDataBaseListener(this);

		timeStamp = (TextView) findViewById(R.id.timestamp);
		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		errorMsg = (TextView) findViewById(R.id.errorText);

		favouriteId = PreferenceHelper.getUniversityCalendarFavourite(context);

		list = (ListView) findViewById(R.id.list);

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
			{
				cursor.moveToPosition(adapter.getPositionInCursor(position));

				if (cursor.getInt(2) == 1)
				{
					Intent universityCalendarActivity = new Intent(getBaseContext(), UniversityCalendarBaseActivity.class);
					universityCalendarActivity.putExtra(UniversityCalendarBaseActivity.ID_KEY, cursor.getLong(0));
					startActivity(universityCalendarActivity);
				} else
				{

					new saveLectureXMLFromServer(cursor.getLong(0), true).execute(cursor.getString(3));
				}
			}

		});

		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, final View view, final int position, final long id)
			{
				cursor.moveToPosition(adapter.getPositionInCursor(position));

				QuickActionBar mBar = new QuickActionBar(context);

				if (cursor.getLong(0) != PreferenceHelper.getUniversityCalendarFavourite(context))
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_favorite, "Favorit", ACTION_FAVOURITE));
				else
					mBar.addQuickAction(new QuickAction(context, R.drawable.action_delete_favorite, "Favorit entfernen", ACTION_DELETE_FAVOURITE));

				mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
				{

					@Override
					public void onQuickActionClicked(QuickActionWidget widget, int action)
					{
						switch (action)
						{
						case ACTION_FAVOURITE:
							PreferenceHelper.setUniversityCalendarFavourite(cursor.getLong(0), context);
							favouriteId = cursor.getLong(0);
							adapter.notifyDataSetChanged();
							break;
						case ACTION_DELETE_FAVOURITE:
							PreferenceHelper.setUniversityCalendarFavourite(0, context);
							favouriteId = 0;
							adapter.notifyDataSetChanged();
							break;
						}
					}
				});

				mBar.show(view);
				return true;
			}
		});

		task = new LoadDataTask().execute();

	}

	private void initList()
	{

		// downloaded.clear();

		adapter = new MyAlphabetizedAdapter(

		getApplicationContext(),

		R.layout.university_calendar_subjects_list_item,

		cursor,

		new String[] { "name", "loaded", "_id" },// from

				new int[] { R.id.name, R.id.imageArrow, R.id.image });

		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder()
		{

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex)
			{

				if (columnIndex == 2)
				{

					if (cursor.getInt(2) == 1)
					{
						((ImageView) view).setImageResource(R.drawable.right_arrow);

					} else
						((ImageView) view).setImageResource(R.drawable.right_arrow_disabled);

					return true;

				} else if (columnIndex == 0)
				{
					if (cursor.getLong(0) == favouriteId)

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

	// _id, name, loaded, educ AS section

	// Thread um eine spezifische XML runterzuladen
	public class saveLectureXMLFromServer extends AsyncTask<String, Integer, Integer> implements NetworkListener
	{

		final static int NO_INET_ACCESS = -1;
		final static int FAIL = -2;

		ProgressBar bar;
		long id;
		boolean open;

		public saveLectureXMLFromServer(long id, boolean open)
		{
			this.bar = bar;
			this.id = id;
			this.open = open;
		}

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{

			setSupportProgressBarVisibility(true);
			setSupportProgress(0);
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{

			switch (result)
			{

			case 0:

				helper.notifyDatabaseListeners();

				if (open)
				{

					Intent universityCalendarActivity = new Intent(getBaseContext(), UniversityCalendarBaseActivity.class);
					universityCalendarActivity.putExtra(UniversityCalendarBaseActivity.ID_KEY, id);
					startActivity(universityCalendarActivity);
				}
				break;

			case NO_INET_ACCESS:
				Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_LONG).show();

				break;

			case FAIL:
				Toast.makeText(context, "Verbindungsfehler", Toast.LENGTH_LONG).show();

				break;
			}
			// bar.setVisibility(View.INVISIBLE);
			setSupportProgressBarVisibility(false);
		}

		@Override
		protected Integer doInBackground(String... values)
		{
			if (!NetworkHelper.isOnline(context))
				return NO_INET_ACCESS;

			ImportHelper i = new ImportHelper();
			try
			{
				i.saveLectureXML(values[0], id, this, context);
				helper.setUniversityCalendarSubjectDownloadedStatus(id, true);
				helper.setUniversityCalendarSubjectTimeStamp(id);

			} catch (Exception e)
			{
				Log.d("tag", "xml runterladen fehler", e);
				return FAIL;
			}

			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			setSupportProgress((android.view.Window.PROGRESS_END - android.view.Window.PROGRESS_START) / 100 * values[0]);
		}

		@Override
		public void update(int value)
		{
			publishProgress(value);

		}

	}

	// Thread um die NAmen aller F�cher zu laden
	private class LoadDataTask extends AsyncTask<Object, Object, Integer>
	{
		boolean update = false;
		String keyword;

		public LoadDataTask()
		{
			// TODO Auto-generated constructor stub
		}

		public LoadDataTask(boolean update)
		{
			this.update = update;
		}

		public LoadDataTask(String keyword)
		{
			this.keyword = keyword;
		}

		final static int NO_INET_ACCESS = -1;
		final static int FAIL = -2;

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{

			setSupportProgressBarIndeterminateVisibility(true);
			if (!update)
			{
				showErrorView();
				errorMsg.setText("Loading Data ...");

				gettingLectureNamesThreadIsRunning = true;
				setSupportProgressBarIndeterminateVisibility(true);

				// refreshButton.startAnimation(AnimationUtils.loadAnimation(context,
				// R.anim.rotate));

			}
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result)
		{

			switch (result)
			{

			case 1:

				if (update)
				{
					adapter.changeCursor(cursor);
				} else
				{
					showListView();
					initList();
					timeStamp.setText("Letzte Aktualisierung: " + PreferenceHelper.getFormattedUniversityCalendarSubjectsTimeStamp(context));
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

			// refreshButton.setAnimation(null);
			setSupportProgressBarIndeterminateVisibility(false);

			gettingLectureNamesThreadIsRunning = false;

			task = null;

		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Integer doInBackground(Object... params)
		{

			ImportHelper i = new ImportHelper();

			cursor = helper.getUniversityCalendarSubjectsList(keyword);
			try
			{
				if (cursor.getCount() == 0)
				{

					if (!NetworkHelper.isOnline(context))
						return NO_INET_ACCESS;

					i.getLections(context);
					cursor = helper.getUniversityCalendarSubjectsList(null);

				}

			} catch (Exception e)
			{

				Log.e(TAG, "Namen runterladen", e);

				// Es gab einen Fehler
				return FAIL;
			}

			return 1;
		}

	}

	private void showListView()
	{
		if (currentPage != 1)
		{
			viewSwitcher.showPrevious();
			currentPage = 1;
		}
	}

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

		if (cursor != null)
			cursor.close();

		if (helper != null)
		{
			helper.close();
		}

		super.onDestroy();
	}

	private void startSearch()
	{
		if (cursor != null && cursor.getCount() > 0)
			onSearchRequested();
		else
			Toast.makeText(context, "Nichts zu durchsuchen", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.university_calendar_subjects, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{

		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.refresh:
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(context, true).execute();
			return true;
		case R.id.preferences:
			Intent preferenceActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(preferenceActivity);
			return true;
			/*
			 * case R.id.allCourses: Intent intent = new
			 * Intent(getBaseContext(), UniversityCalendarBaseActivity.class);
			 * intent.putExtra(UniversityCalendarBaseActivity.ID_KEY, (long) 0);
			 * startActivity(intent); return true;
			 */
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	public void universityCalendarWarningClick(View view)
	{
		if (!PreferenceHelper.isUniversityCalendarComplete(context))
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(this, false).execute();
		else
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(this, false).execute();
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
			task = new LoadDataTask(keyword).execute();

			dataChanged = false;

		}

	}

	@Override
	public void update()
	{
		if (isVisible)
			task = new LoadDataTask(keyword).execute();

		else
			dataChanged = true;
	}

}
