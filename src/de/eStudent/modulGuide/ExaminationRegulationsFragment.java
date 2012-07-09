package de.eStudent.modulGuide;

import greendroid.widget.QuickActionWidget;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragment;

import de.eStudent.modulGuide.XMLParser.ExaminationRegulationXMLHandler;
import de.eStudent.modulGuide.common.Category;
import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Semester;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Prüfungsordnungsansicht im Viewpager
 * 
 */
public class ExaminationRegulationsFragment extends SherlockFragment implements DatabaseListener

{

	//Die anzuzeigenden Daten der Prüfungsordnung
	private ArrayList<Category> data;
	
	//Der Context
	private Context context;
	
	//Der DataHelper
	private DataHelper dataHelper;
	
	//ViewSwitcher
	private ViewSwitcher switcher;
	
	//Adapter für die ExpandableLsit
	private MyExpandableListAdapter adapter;
	
	//boolean, ob seid dem letzten Pasuieren der Activty sich die Datenbank geänder hat
	boolean dataChanged = false;
	
	//QuickActionbar für die QuickActions
	private QuickActionWidget mBar;
	
	//Referenz von Thread zum vorzeitigen Beenden
	private AsyncTask task;

	/**
	 * Erzeug eine neue Instanz der Klasse
	 * 
	 * @return Die neie Instanz
	 */
	public static ExaminationRegulationsFragment newInstance()
	{
		ExaminationRegulationsFragment pageFragment = new ExaminationRegulationsFragment();
		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this.getActivity().getApplicationContext();

		data = new ArrayList<Category>();
		dataHelper = new DataHelper(context);
		dataHelper.addDataBaseListener(this);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//---Layout erzeugen ---
		View view = inflater.inflate(R.layout.fragment_examinationregulation, container, false);

		switcher = (ViewSwitcher) view.findViewById(R.id.examinationRegulationViewSwitcher);

		ExpandableListView l = (ExpandableListView) view.findViewById(R.id.exRegListView);

		l.setEmptyView(view.findViewById(R.id.examinationRegulationEmptyView));

		adapter = new MyExpandableListAdapter();
		l.setAdapter(adapter);

		if (data.size() == 0)
		{
			task = new DataTask().execute();

		}
		return view;
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

	/**
	 * Thread zum Laden der nötigen Daten
	 * 
	 */
	private class DataTask extends AsyncTask
	{
		
		ArrayList<Category> tmp;


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

			if (!data.isEmpty())
			{
				// überprüfe, ob Studium fertig
				int size = data.size();
				boolean b = true;
				Category c;
				for (int i = 0; i < size; i++)
				{
					c = data.get(i);
					if (c.items != c.passedItems)
						b = false;
				}

				if (b && !PreferenceHelper.isStudyComplete(context))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Studium bestanden").setMessage("Herzlichen Glückwunsch zum Bestehen des Studiums!").setCancelable(false)
							.setPositiveButton("Ok", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									PreferenceHelper.setStudyComplete(context, true);
									dialog.cancel();
								}
							});

					AlertDialog alert = builder.create();
					alert.show();
				} else if (!b)
					PreferenceHelper.setStudyComplete(context, false);

			}
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Object doInBackground(Object... params)
		{
			// saveData();
			tmp = dataHelper.getCategories(0);			
			return null;
		}

	}

	/**
	 * Listadapter für die Liste
	 * 
	 */
	public class MyExpandableListAdapter extends BaseExpandableListAdapter
	{

		LayoutInflater layoutInflater;

		public MyExpandableListAdapter()
		{
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
					startActivity(o.getOnClickIntent(context));

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
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			return data.get(groupPosition).getGroupView(isExpanded, convertView, context, layoutInflater);
		}

		@Override
		public boolean hasStableIds()
		{
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return true;
		}

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