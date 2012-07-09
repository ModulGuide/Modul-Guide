package de.eStudent.modulGuide;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpagertabs.ViewPagerTabProvider;
import com.astuetz.viewpagertabs.ViewPagerTabs;

import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.network.UniversityCalendarLoader;
import de.eStudent.modulGuide.preferences.About;
import de.eStudent.modulGuide.preferences.ExaminationRegulationList;
import de.eStudent.modulGuide.preferences.ExampleCourseSchemeList;
import de.eStudent.modulGuide.preferences.PreferenceHelper;
import de.eStudent.modulGuide.preferences.Preferences;
import de.eStudent.modulGuide.universityCalendar.UniversityCalendarBaseActivity;
import de.eStudent.modulGuide.universityCalendar.UniversityCalendarSubjects;


/**
 * StartActivity
 * Zeigt mit Hilfe eines Viewpagers eine Übersicht, die Prüfungsordnung,
 * eine Überischt über die Semester und den ausgewählten Musterstudienplan
 *
 */
public class StudiumhelferActivity extends SherlockFragmentActivity
{

	//Der Context
	private Context context;
	
	//Viewpager
	private ViewPager viewPager;
	
	//Adapter für den Viewpager
	private MyPagerAdapter pagerAdapter;
	
	//Datahelper
	private DataHelper dataHelper;
	
	//UniversityCalendar Warning
	private RelativeLayout universityCalendarWarning;
	
	//Thread zum herunterladen des Vorlesungsverzeichnisses
	private UniversityCalendarLoader loader;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		context = this.getApplicationContext();

		//Layout der ActionBar setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);

		
		//---Layout setzen---
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setPageMargin(10);

		viewPager.setPageMarginDrawable(R.drawable.view_pager_seperator);

		pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

		pagerAdapter.addFragment("Übersicht", OverviewFragment.class);
		pagerAdapter.addFragment("Prüfungsordnung", ExaminationRegulationsFragment.class);
		pagerAdapter.addFragment("Semester", SemesterFragment.class);
		pagerAdapter.addFragment("Musterstudienplan", StudyCoursePlanFragment.class);

		viewPager.setAdapter(pagerAdapter);
		ViewPagerTabs tabs = (ViewPagerTabs) findViewById(R.id.tabs);
		tabs.setViewPager(viewPager);

		LinearLayout l = (LinearLayout) findViewById(R.id.text);
		l.setVisibility(View.VISIBLE);

		
		//überprüfen, ob Neusinstallation, oder Versions Update seid 
		//letztem Start erfolgt ist, falls ja Änderungshistorie öffnen
		try
		{
			int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			if (currentVersion != PreferenceHelper.getVersion(context))
			{
				PreferenceHelper.saveCurrentVersion(context);

				Preferences.getVersionsDialog(this).show();
			}

		} catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy()
	{

		if (loader != null)
		{
			loader.running = false;
			loader.progressDialog.dismiss();
		}

		if (dataHelper != null)
		{
			dataHelper.close();
		}

		super.onDestroy();

	}

	/**
	 * Adapter für den ViewPager
	 *
	 */
	private class MyPagerAdapter extends FragmentPagerAdapter implements ViewPagerTabProvider
	{

		private FragmentManager fragmentManager;
		private ArrayList<Class<? extends Fragment>> fragments;
		private ArrayList<String> titles;

		public MyPagerAdapter(FragmentManager fm)
		{
			super(fm);
			fragmentManager = fm;
			fragments = new ArrayList<Class<? extends Fragment>>();
			titles = new ArrayList<String>();
		}

		public void addFragment(String title, Class<? extends Fragment> fragment)
		{
			titles.add(title);
			fragments.add(fragment);
		}

		@Override
		public Fragment getItem(int position)
		{
			try
			{
				return fragments.get(position).newInstance();
			} catch (InstantiationException e)
			{
				Log.wtf("hi", e);
			} catch (IllegalAccessException e)
			{
				Log.wtf("hi", e);
			}
			return null;

		}

		@Override
		public int getCount()
		{
			return fragments.size();
		}

		@Override
		public String getTitle(int position)
		{
			return titles.get(position);
		}

	}

	
	/**
	 * OnClick Methode zum Öffnen der Ansicht für die Prüfungsordnungen
	 * @param view Der Button
	 */
	public void einstellungen(View view)
	{
		Intent preferenceActivity = new Intent(getBaseContext(), ExaminationRegulationList.class);
		startActivity(preferenceActivity);
	}

	
	/**
	 * OnClick MEhtode zum Öffnen der Ansciht für die Musterstudeinpläne
	 * @param view Der Button
	 */
	public void studyCoursePlan(View view)
	{
		if (PreferenceHelper.getRegulationName(context).equals(""))
		{
			Toast.makeText(context, "Zuerst Prüfungsordnung auswählen", Toast.LENGTH_LONG).show();
		} else
		{
			Intent intent = new Intent(getBaseContext(), ExampleCourseSchemeList.class);
			startActivity(intent);
		}
	}

	
	/**
	 * OnKlick MEthode vom UniversityCalendarWarningFragment. Startet den Thread zum herunterladen des 
	 * aktuellen Vorlesungsverzeichnisses.
	 * @param view Der Button
	 */
	public void universityCalendarWarningClick(View view)
	{
		if (!PreferenceHelper.isUniversityCalendarComplete(context))
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(this, false).execute();
		else
			loader = (UniversityCalendarLoader) new UniversityCalendarLoader(this, false).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{

		//Einstellungen öffnen
		case R.id.preferences:
			Intent preferenceActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(preferenceActivity);
			return true;
			
		//Vorlesungsverzeichnis öffnen
		case R.id.universityCalendar:
			long favourite = PreferenceHelper.getUniversityCalendarFavourite(context);
			if (PreferenceHelper.getUniversityCalendarFavouriteStart(context) && favourite != 0)
			{
				Intent universityCalendarActivity = new Intent(getBaseContext(), UniversityCalendarBaseActivity.class);
				universityCalendarActivity.putExtra(UniversityCalendarBaseActivity.ID_KEY, favourite);
				startActivity(universityCalendarActivity);
			} else
			{

				Intent universityCalendarActivity = new Intent(getBaseContext(), UniversityCalendarSubjects.class);
				startActivity(universityCalendarActivity);
			}
			return true;

		//Hilfe öffnen
		case R.id.help:
			String url = "https://sites.google.com/site/modulguidefaq/";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
			
		//Feedback geben
		case R.id.feedback:
			Intent intent = new Intent(getBaseContext(), About.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}