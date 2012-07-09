package de.eStudent.modulGuide;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.eStudent.modulGuide.network.UniversityCalendarLoader;
import de.eStudent.modulGuide.preferences.PreferenceHelper;


/**
 * Ansicht für eine Warning, falls das Vorlesungsverzeichnis nicht vollstädnig oder veraltet sit
 * @author Jan Rahlf
 *
 */
public class UniversityCalendarWarningFragment extends Fragment
{

	//Context
	private Context context;
	
	//Listener für die Preferences
	private SharedPreferences.OnSharedPreferenceChangeListener listener;
	
	//View der gesamten Ansciht
	private View view;

	//boolean on das Vorlesungsverzeichnis vollstädnig ist
	private boolean universityCalendarComplete;
	
	//boolean ob das Vorlesungsverzeichnis aktuell ist
	private boolean universityCalendarUpToDate;

	//Referenz um Thread vorzeitig beenden zu wollen
	public UniversityCalendarLoader task;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		context = this.getActivity().getApplicationContext();
		universityCalendarComplete = PreferenceHelper.isUniversityCalendarComplete(context);
		universityCalendarUpToDate = isUniversityCalendarUpToDate();

		
		//Listener für de Preferences zuweisen
		listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			@Override
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
			{

				if (key.equals(PreferenceHelper.UNIVERSITY_CALENDAR_COMPLETE) || key.equals(PreferenceHelper.UNIVERSITY_CALENDAR_TIME_STAMP))
				{
					
					if (key.equals(PreferenceHelper.UNIVERSITY_CALENDAR_COMPLETE))
					{
						if (PreferenceHelper.isUniversityCalendarComplete(context))
							universityCalendarComplete = true;
						else
							universityCalendarComplete = false;

					} else if (key.equals(PreferenceHelper.UNIVERSITY_CALENDAR_TIME_STAMP))
					{
						if (isUniversityCalendarUpToDate())
							universityCalendarUpToDate = true;
						else
							universityCalendarUpToDate = true;
					}

					if (!universityCalendarComplete || !universityCalendarUpToDate)
					{
						view.setVisibility(View.VISIBLE);
						TextView warning = (TextView) view.findViewById(R.id.warning);

						if (!universityCalendarComplete)
							warning.setText("Das Vorlesungsverzeichnis ist nicht vollständig.");
						else
							warning.setText("Vorlesungsverzeichnis ist veraltet.");

					} else
						view.setVisibility(View.GONE);
				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
	}

	
	/**
	 * Methode gibt zurück, ob das Vorlesungsverzeichnis aktuell ist
	 * @return ture / false
	 */
	private boolean isUniversityCalendarUpToDate()
	{

		long lastUpdate = PreferenceHelper.getUniversityCalendarSubjectsTimeStamp(context);
		Calendar c = Calendar.getInstance();
		long now = c.getTimeInMillis();
		int thisYear = c.get(Calendar.YEAR);
		c.set(Calendar.MONTH, Calendar.APRIL);
		c.set(Calendar.DAY_OF_MONTH, 1);
		long time2 = c.getTimeInMillis();
		c.set(Calendar.MONTH, Calendar.AUGUST);
		c.set(Calendar.DAY_OF_MONTH, 1);
		long time3 = c.getTimeInMillis();
		c.set(Calendar.YEAR, thisYear - 1);
		long time4 = c.getTimeInMillis();

		if ((now > time2 && !(lastUpdate > time2)) || (now > time3 && !(lastUpdate > time3)) || (now > time4 && !(lastUpdate > time4)))
			return false;
		else
			return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.university_calendar_warning, container, false);
		this.view = view;

		if (!universityCalendarComplete || !universityCalendarUpToDate)
		{
			view.setVisibility(View.VISIBLE);
			TextView warning = (TextView) view.findViewById(R.id.warning);
			TextView subWarning = (TextView) view.findViewById(R.id.subWarning);

			if (!universityCalendarComplete)
				warning.setText("Das Vorlesungsverzeichnis ist nicht vollständig.");
			else
				warning.setText("Vorlesungsverzeichnis ist veraltet.");

			subWarning.setText("Kursvorschläge können fehlerhaft sein.");

		} else
			view.setVisibility(View.GONE);

		return view;
	}

}
