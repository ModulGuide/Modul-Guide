package de.eStudent.modulGuide;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Semester;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;
import de.eStudent.modulGuide.preferences.PreferenceHelper;


/**
 * Ansicht für  die Emesterübersicht.
 * (Welche Kurse, Kriterion und wie viwl CP habe ich in Semester X erlangt)
 *
 */
public class SemesterFragment extends SherlockFragment implements DatabaseListener
{

	//Die Daten der anzuzeigenden Semester
	private ArrayList<Semester> data;
	
	//Der Context
	private Context context;
	
	//Der Adapter der Liste
	private MyExpandableListAdapter adapter;
	
	//Die QuickactionBar für die QuickActions
	private QuickActionWidget mBar;
	
	//Expandable List für die ganzen Semester
	private ExpandableListView l;
	
	//DataHelper
	private DataHelper dataHelper;
	
	//Viewswitcher
	private ViewSwitcher switcher;
	
	//boolean, ob seid dem pausieren und wieder fortsetzen der Activity sich die Datenbank geändert hat
	boolean dataChanged = false;
	
	//Listener für die Preferences
	private SharedPreferences.OnSharedPreferenceChangeListener listener;

	
	//Referenz zum Thread, um diesen vorzeitig beenden zu können
	private AsyncTask task;

	
	/**
	 * Konstruktor
	 * Erzeugt eine neue Instanz der Klasse
	 * @return Die Instanz
	 */
	public static SemesterFragment newInstance()
	{

		SemesterFragment pageFragment = new SemesterFragment();
		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		context = this.getActivity().getApplicationContext();
		dataHelper = new DataHelper(context);
		dataHelper.addDataBaseListener(this);

		
		//Falls sich das aktuelle Semester ändert, Ansicht updaten.
		listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			@Override
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
			{
				if (key.equals("editTextPref"))
				{
					update();
				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.semester_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
		//aktuelles Semester inkrementieren
		case R.id.addSemester:
			PreferenceHelper.incrementSemester(context);
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	
		//---Layout setzen
		View view = inflater.inflate(R.layout.fragment_semester, container, false);

		switcher = (ViewSwitcher) view.findViewById(R.id.semesterFragmentViewSwitcher);

		l = (ExpandableListView) view.findViewById(R.id.semesterListView);

		data = new ArrayList<Semester>();

		adapter = new MyExpandableListAdapter();
		l.setAdapter(adapter);

		if (data.size() == 0)
			task = new DataTask().execute();

		return view;
	}

	
	/**
	 * Thread zum laden der Daten
	 *
	 */
	private class DataTask extends AsyncTask
	{
		ArrayList<Semester> tmp;

		// can use UI thread here
		@Override
		protected void onPreExecute()
		{
			switcher.showNext();
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Object result)
		{
			data = tmp;
			adapter.notifyDataSetChanged();
			switcher.showPrevious();
			task = null;
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Object doInBackground(Object... params)
		{
			tmp = dataHelper.getAllSemester();
			return null;
		}

	}

	
	/**
	 * Adapter für die Liste
	 *
	 */
	public class MyExpandableListAdapter extends BaseExpandableListAdapter
	{

		LayoutInflater layoutInflater;

		public MyExpandableListAdapter()
		{
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		// Komponenten für den Dialog des manuellen Hinzufügens
		Spinner gradeSpinner;
		EditText courseName, courseVAK, courseCP;
		CheckBox coursePassed;

		// Aktuell ausgewähltes Semester zum Speichern eines neuen Kurses
		int currentGroupPosition;

		@Override
		public ChildEntry getChild(int groupPosition, int childPosition)
		{
			return data.get(groupPosition).childs.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return childPosition;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{

			convertView = data.get(groupPosition).childs.get(childPosition).getChildView(convertView, context, layoutInflater);

			convertView.setOnLongClickListener(new OnLongClickListener()
			{

				@Override
				public boolean onLongClick(final View v)
				{

					ChildEntry o = adapter.getChild(groupPosition, childPosition);

					mBar = o.getQuickActionGrid(dataHelper, context, 0);
					if (mBar == null)
						return false;
					mBar.show(v);

					return true;
				}
			});

			convertView.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					ChildEntry o = adapter.getChild(groupPosition, childPosition);
					Intent intent = o.getOnClickIntent(context);
					if (intent == null)
						return;
					startActivity(intent);

				}
			});

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition)
		{
			return data.get(groupPosition).childs.size();
		}

		@Override
		public Object getGroup(int groupPosition)
		{
			return data.get(groupPosition);
		}

		@Override
		public int getGroupCount()
		{
			return data.size();
		}

		@Override
		public long getGroupId(int groupPosition)
		{
			return groupPosition;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			//---Layout für Semester setzen
			Semester semester = data.get(groupPosition);
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.group_row_semester, null);

			if (semester.childs.isEmpty())
			{
				ImageView image = (ImageView) convertView.findViewById(R.id.semester_indicator);
				image.setVisibility(View.INVISIBLE);
			} else if (isExpanded)
			{
				ImageView image = (ImageView) convertView.findViewById(R.id.semester_indicator);
				image.setImageResource(R.drawable.group_indicator_arrrow_down);
			}

			TextView semesterName = ((TextView) convertView.findViewById(R.id.semesterName));
			semesterName.setText(semester.semester + ". Semester");

			TextView semesterGrade = ((TextView) convertView.findViewById(R.id.semesterGrade));
			if (semester.grade == 0 || Double.isNaN(semester.grade))
				semesterGrade.setText("Note: -");
			else
				semesterGrade.setText("Note: " + Statics.convertGrade(semester.grade));

			TextView semesterCP = ((TextView) convertView.findViewById(R.id.semesterCP));
			if (semester.outstandingCp != 0)
			{
				SpannableString str = new SpannableString(Statics.convertCP(semester.cp) + "+" + Statics.convertCP(semester.outstandingCp) + " CP");
				str.setSpan(new ForegroundColorSpan(Color.rgb(190, 153, 35)), String.valueOf(semester.cp).length(), str.length() - 2,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				semesterCP.setText(str);
			} else
				semesterCP.setText(Statics.convertCP(semester.cp) + " CP");

			convertView.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					mBar = new QuickActionBar(context);
					mBar.addQuickAction(new QuickAction(context, android.R.drawable.ic_menu_view, R.string.course_offers, 99));

					// Dialog zum manuellen Hinzufügen eines Kurses
					mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
					{
						@Override
						public void onQuickActionClicked(QuickActionWidget widget, int position)
						{

							Toast.makeText(context, "Noch nciht implementiert", Toast.LENGTH_LONG).show();
						}
					});
					mBar.show(v);
					return true;
				}
			});

			convertView.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{

					if (l.isGroupExpanded(groupPosition))
						l.collapseGroup(groupPosition);
					else
						l.expandGroup(groupPosition);

				}
			});

			return convertView;
		}

		@Override
		public boolean hasStableIds()
		{
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return false;
		}

		@Override
		public int getChildType(int groupPosition, int childPosition)
		{
			return getChild(groupPosition, childPosition).getType();
		}

		@Override
		public int getChildTypeCount()
		{
			return 3;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onResume()
	{
		super.onResume();

		if (dataChanged)
		{
			task = new DataTask().execute();
			dataChanged = false;

		}

	}

	@Override
	public void onDestroy()
	{

		if (task != null)
			task.cancel(true);

		if (dataHelper != null)
		{
			dataHelper.close();
		}

		super.onDestroy();

	}

	@Override
	public void update()
	{
		if (isResumed())
			task = new DataTask().execute();
		else
			dataChanged = true;
	}

}
