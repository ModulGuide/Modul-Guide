package de.eStudent.modulGuide;

import greendroid.widget.QuickActionWidget;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Semester;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;


/**
 * Ansicht für die Musterstudienpläne (Liste)
 *
 */
public class StudyCoursePlanFragment extends SherlockFragment implements DatabaseListener
{
	
	//Enthällt die für jedes Semester Vorgeschalgenen Kurse / Kriterien
	private ArrayList<Semester> data;
	
	//Der Context
	private Context context;
	
	//Der Adapter für die Liste
	private MyExpandableListAdapter adapter;
	
	//QuickactionBar für die QuickActions
	private QuickActionWidget mBar;
	
	//Die Expandable List
	private ExpandableListView l;
	
	//DataHelper
	private DataHelper dataHelper;
	
	//ViewSwitcher
	private ViewSwitcher switcher;
	
	//boolean ob sei dem letztem pausieren der Activity sich was an der Datenbank geändert hat
	private boolean dataChanged = false;

	//referenz zum Thread, um diesen vorzeitig zu beenden
	private AsyncTask task;

	
	/**
	 * Konstruktor um eine neue Instanz zu erzeugen
	 * @return Die Instanz
	 */
	public static StudyCoursePlanFragment newInstance()
	{
		StudyCoursePlanFragment pageFragment = new StudyCoursePlanFragment();
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

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//---Layout erzeugen ---
		
		View view = inflater.inflate(R.layout.fragment_study_course_plan, container, false);

		switcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);

		l = (ExpandableListView) view.findViewById(R.id.listView);

		l.setEmptyView(view.findViewById(R.id.emptyView));

		data = new ArrayList<Semester>();

		adapter = new MyExpandableListAdapter();
		l.setAdapter(adapter);

		if (data.size() == 0)
			task = new DataTask().execute();

		return view;
	}

	
	/**
	 * Thread zum laden der Daten aus der Datenbank
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
			// saveData();
			tmp = dataHelper.getExampleCourseScheme();
			return null;
		}

	}

	
	/**
	 * ListAdapter
	 *
	 */
	public class MyExpandableListAdapter extends BaseExpandableListAdapter
	{

		private LayoutInflater layoutInflater;

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

			convertView = getChild(groupPosition, childPosition).getStudyCoursePlanChildView(convertView, context, layoutInflater);

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
		public Semester getGroup(int groupPosition)
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
			return getGroup(groupPosition).getStudyCoursePlanGroupView(isExpanded, convertView, context, layoutInflater);
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
			return 5;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onResume()
	{
		super.onResume();

		// dataHelper.reOpen();

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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.study_course_plan_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
		//Tabellenansicht öffnen
		case R.id.table:
			Intent preferenceActivity = new Intent(context, StudyCoursePlanTable.class);
			startActivity(preferenceActivity);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

}
