package de.eStudent.modulGuide;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import de.eStudent.modulGuide.common.MyBaseAlphabetizedAdapter;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.common.Status;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.ModulguideContentProvider;

/**
 * Ansicht zur Auswahl eines Kurses aus einer Liste von möglichen Kursen
 * 
 */
public class CourseChoiceDialog extends SherlockActivity
{

	Context context;
	Cursor cursor;
	MyCursorAdapter adapter;
	DataHelper helper;
	long id;
	int type;

	ListView list;

	TextView hintText;
	AsyncTask thread;

	/**
	 * Im Falle einer Suche
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		// Es wurde eine Suche durchgeführt -> Ergebnis anzeigen
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH))
		{
			String searchKeywords = intent.getStringExtra(SearchManager.QUERY);

			final RelativeLayout searchBar = (RelativeLayout) findViewById(R.id.searchCancelBar);
			searchBar.setVisibility(View.VISIBLE);

			TextView searchKeyword = (TextView) findViewById(R.id.searchKeyword);
			searchKeyword.setText(searchKeywords);

			ImageButton cancelSearch = (ImageButton) findViewById(R.id.cancelSearch);
			cancelSearch.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					searchBar.setVisibility(View.GONE);
					thread = new DataTask().execute();
				}
			});

			thread = new DataTask(searchKeywords).execute();

			// Es wurde ein Ergebnis in den Suchvorschlägen direkt angeklickt ->
			// dieses auswählen
		} else if (Intent.ACTION_VIEW.equals(intent.getAction()))
		{
			if (type == Statics.TYPE_CHOOSABLE)
				helper.setChoosableCourseChoice(id, Long.parseLong(intent.getData().getLastPathSegment()));
			else if (type == Statics.TYPE_OPTIONAL)
				helper.addUniversityCourseToOptional(id, Long.parseLong(intent.getData().getLastPathSegment()));
			finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.course_choice_dialog);

		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		context = this;

		helper = new DataHelper(context);

		Intent intent = getIntent();

		Bundle extras = intent.getExtras();

		id = extras.getLong(Statics.ID);
		type = extras.getInt(Statics.TYPE);
		// selectedCourseId = extras.getLong(SELECTED_COURSE_KEY);

		list = (ListView) findViewById(R.id.list);

		hintText = (TextView) findViewById(R.id.empty);
		list.setEmptyView(findViewById(R.id.empty));

		// Falls es vorgeschlagene Kurse gibt, diese im List HEader anzeigen
		String suggestedCourses = null;
		if (type == Statics.TYPE_CHOOSABLE)
			suggestedCourses = helper.getChoosableCourseSuggestion(id);
		else if (type == Statics.TYPE_OPTIONAL)
			suggestedCourses = helper.getOptionalCourseSuggestion(id);

		if (suggestedCourses != null)
		{
			LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.suggested_course_list_header, null, false);
			TextView course = (TextView) layout.findViewById(R.id.courses);
			course.setText(suggestedCourses);

			list.addHeaderView(layout);
		}

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, final int position, final long DO_NOT_USE)
			{
				/*
				 * der Header soll nicht klickable sein Da wenn es kein HEader
				 * gibt als erstes einen Index View gibt, der auch nciht
				 * ancklickbar sein soll Funktioniert dies
				 */
				if (position == 0)
					return;

				// QuickActions
				QuickActionBar mBar = new QuickActionBar(context);
				mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_choose, "auswählen"));
				mBar.addQuickAction(new QuickAction(context, R.drawable.action_details, "Details"));

				mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
				{
					@Override
					public void onQuickActionClicked(QuickActionWidget widget, int qPosition)
					{
						cursor.moveToPosition(adapter.getPositionInCursor(position));
						switch (qPosition)
						{

						case 0:
							if (type == Statics.TYPE_CHOOSABLE)
								helper.setChoosableCourseChoice(id, cursor.getLong(0));
							else if (type == Statics.TYPE_OPTIONAL)
								helper.addUniversityCourseToOptional(id, cursor.getLong(0), cursor.getDouble(3));
							finish();
							break;

						case 1:
							Intent intent = new Intent(getBaseContext(), Details.class);
							intent.putExtra(Statics.ID, cursor.getLong(0));
							if (type == Statics.TYPE_CHOOSABLE)
							{
								intent.putExtra(Statics.TYPE, type = Statics.TYPE_COURSE);
							} else if (type == Statics.TYPE_OPTIONAL)
								intent.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);

							startActivity(intent);

						}

					}
				});

				mBar.show(view);

			}
		});

		thread = new DataTask().execute();

	}

	// Thread zum laden der Daten
	private class DataTask extends AsyncTask
	{

		String keywords;

		public DataTask()
		{
		}

		public DataTask(String keywords)
		{
			this.keywords = keywords;
		}

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{

			setSupportProgressBarIndeterminateVisibility(true);

			hintText.setText("Kursvorschläge werden geladen ...");
			if (adapter != null)
			{
				adapter = null;
				list.setAdapter(null);
				cursor.close();
			}

		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Object result)
		{
			if (cursor.getCount() == 0)
				hintText.setText("Keine passenden Kurse gefunden");

			adapter = new MyCursorAdapter(context, cursor);
			list.setAdapter(adapter);

			adapter.notifyDataSetChanged();
			setSupportProgressBarIndeterminateVisibility(false);

		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Object doInBackground(Object... params)
		{
			if (type == Statics.TYPE_CHOOSABLE)
				cursor = helper.getCourseChoicesForChoosable(id, keywords);
			else if (type == Statics.TYPE_OPTIONAL)
				cursor = helper.getCoursesForOptional(id, keywords);
			return null;
		}

	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		if (thread != null)
			thread.cancel(true);

		if (helper != null)
		{
			if (cursor != null && !cursor.isClosed())
				cursor.close();

			helper.close();
		}
	}

	// Adapter für die Liste
	private class MyCursorAdapter extends MyBaseAlphabetizedAdapter
	{
		public MyCursorAdapter(Context context, Cursor c)
		{
			super(context, c);
		}

		@Override
		public View getCustomView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;

			cursor.moveToPosition(position);

			if (convertView == null)
			{
				convertView = layoutInflater.inflate(R.layout.course_choice_list_item, null);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.subjectName);
				holder.hint = (ImageView) convertView.findViewById(R.id.subjectHint);
				holder.duration = (TextView) convertView.findViewById(R.id.subjectDuration);
				holder.status = (ImageView) convertView.findViewById(R.id.subjectImage);
				holder.vak = (TextView) convertView.findViewById(R.id.subjectVAK);
				holder.cp = (TextView) convertView.findViewById(R.id.subjectCP);
				convertView.setTag(holder);

			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			if (type == Statics.TYPE_CHOOSABLE)
			{
				holder.status.setVisibility(View.VISIBLE);
				holder.name.setText(cursor.getString(1));
				holder.name.setSelected(true);
				holder.vak.setText(cursor.getString(2));
				if (cursor.getString(3) != null)
					holder.hint.setVisibility(View.VISIBLE);

				double cp = cursor.getDouble(4);
				if (cp - (int) cp == 0)
					holder.cp.setText((int) cp + " CP");
				else
					holder.cp.setText(cp + " CP");
				holder.status.setImageResource(Status.getIcon(cursor.getInt(5)));
				if (cursor.getInt(6) != 1)

					holder.duration.setText("" + cursor.getInt(6));

			} else if (type == Statics.TYPE_OPTIONAL)
			{
				holder.status.setVisibility(View.GONE);

				holder.name.setText(cursor.getString(1));
				holder.name.setSelected(true);
				holder.vak.setText(cursor.getString(2));

				double cp = cursor.getDouble(3);
				if (cp - (int) cp == 0)
					holder.cp.setText((int) cp + " CP");
				else
					holder.cp.setText(cp + " CP");
			}

			return convertView;
		}

	}

	// ViewHolder für die Listeneinträge
	private static class ViewHolder
	{
		TextView name;
		ImageView hint;
		TextView duration;
		ImageView status;
		TextView vak;
		TextView cp;
	}

	/**
	 * Suche
	 */
	@Override
	public boolean onSearchRequested()
	{

		ContentProviderClient client = getContentResolver().acquireContentProviderClient("de.eStudent.modulGuide.database.ModulguideContentProvider");
		ModulguideContentProvider provider = (ModulguideContentProvider) client.getLocalContentProvider();

		if (type == Statics.TYPE_CHOOSABLE)
			provider.setCourseChoiceDialogSearchParameters(Statics.TYPE_CHOOSABLE, id);
		else
			provider.setCourseChoiceDialogSearchParameters(Statics.TYPE_OPTIONAL, id);
		startSearch(null, false, null, false);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.course_choice_menu, menu);

		if (type == Statics.TYPE_CHOOSABLE)
			menu.removeItem(R.id.add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{

		// Start Activity starten
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;

			// Suche
		case R.id.search:
			onSearchRequested();
			return true;

			// Eigenen Kurs erstellen
		case R.id.add:
			Intent intent = new Intent(context, CreateCustomCourseDialog.class);
			intent.putExtra(Statics.ID, id);
			finish();
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

}
