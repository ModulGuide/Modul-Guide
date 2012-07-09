package de.eStudent.modulGuide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragment;

import de.eStudent.modulGuide.common.MainInfo;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Klasse für die Übersicht Ansicht im ViewPager
 * 
 */
public class OverviewFragment extends SherlockFragment implements DatabaseListener
{

	//die aktuellen erreichten CP
	private double currentCP;
	
	//die aktuellen CP für die man sich momentan angemeldet hat
	private double signedUpCP;
	
	//MainInfo; Die wichtigsten Daten zum StudiumFach
	private MainInfo mainInfo;
	
	//Die aktuelle Gesamtnote
	private double grade;
	
	//Der Context
	private Context context;
	
	//DataHelper
	private DataHelper helper;
	
	//Fachbereichsnummer
	private String facultyNr;
	
	//aktelles Semester
	private int semester;
	
	//boolean, bo sich seid dem letzten pausieren der Activity sich die Datenbank geändert hat
	private boolean dataChanged = false;
	
	//Listener für die Preferences
	private SharedPreferences.OnSharedPreferenceChangeListener listener;
	
	//Die aktuelle Ansicht des viewPagers
	private int currentPage;


	/**
	 * Erzeugt eine neue Instantz der Klasse
	 * 
	 * @return
	 */
	public static OverviewFragment newInstance()
	{
		OverviewFragment pageFragment = new OverviewFragment();

		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		context = this.getActivity().getApplicationContext();
		helper = new DataHelper(context);
		helper.addDataBaseListener(this);

		
		//Falls sich das aktuelle Semester ändert oder das Studium beendet wurde Ansicht updaten
		listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			@Override
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
			{
				if (key.equals("editTextPref") || key.equals(PreferenceHelper.STUDY_COMPLETE))
				{
					update();
				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);

	}

	
	/**
	 * LAyout Elemente für die Ansicht
	 */
	private TextView subject;
	private TextView semesterTextView;
	private TextView fortschritt;
	private TextView note;
	private ProgressBar progressBar;
	private TextView neccessarSemsForGraduation;
	private ImageButton button;
	private TextView progressHeaderAndPercentage;
	private ViewSwitcher switcher;
	private View studyComplete;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//---Layout erzeugen ----
		currentPage = 1;

		View view = inflater.inflate(R.layout.fragment_overview, container, false);

		switcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);
		subject = (TextView) view.findViewById(R.id.overviewCourseOfStudy);
		fortschritt = ((TextView) view.findViewById(R.id.overviewCourseOfStudyCP));
		semesterTextView = ((TextView) view.findViewById(R.id.overviewCourseOfStudySemester));
		note = ((TextView) view.findViewById(R.id.overviewCourseOfStudyGrade));
		progressBar = ((ProgressBar) view.findViewById(R.id.overviewProgressBar));
		neccessarSemsForGraduation = (TextView) view.findViewById(R.id.overviewCourseOfStudyGraduationDate);
		studyComplete = view.findViewById(R.id.studyComplete);

		button = (ImageButton) view.findViewById(R.id.overviewButton);
		progressHeaderAndPercentage = (TextView) view.findViewById(R.id.overviewCourseOfStudyProgress);

		setData();

		return view;
	}

	/**
	 * Zeigt die eigentliche Übersicht Ansicht dar
	 */
	private void shwowOverview()
	{
		if (currentPage != 1)
		{
			switcher.showPrevious();
			currentPage = 1;
		}
	}

	/**
	 * Ruft die Alternative Ansicht auf für den Fall das noch keine
	 * Prüfungsordnung ausgewählt wurde
	 */
	private void showOtherView()
	{
		if (currentPage != 2)
		{
			switcher.showNext();
			currentPage = 2;
		}
	}

	/**
	 * Setzt die aktuellen Daten in die Übersichts Ansicht
	 */
	private void setData()
	{

		mainInfo = helper.getCourseOfStudies(0);
		if (mainInfo != null)
		{
			if (mainInfo.facultyNr >= 10)
				facultyNr = "" + mainInfo.facultyNr;
			else
				facultyNr = "0" + mainInfo.facultyNr;

			semester = PreferenceHelper.getSemesterInt(context);

			subject.setText("FB" + facultyNr + ": "+mainInfo.degree+" "+ mainInfo.subject);

			semesterTextView.setText("Aktuelles Semester: " + semester);

			currentCP = helper.getCurrentCP();
			signedUpCP = helper.getSignedUpCP();

			int percentage = (int) ((1.0 * currentCP / mainInfo.requiredCp) * 100);
			int signedUpPercentage = (int) ((1.0 * signedUpCP / mainInfo.requiredCp) * 100);

			if (signedUpPercentage == 0)
				progressHeaderAndPercentage.setText("Fortschritt: (" + percentage + "%)");
			else
			{
				SpannableString str = new SpannableString("Fortschritt: (" + percentage + "+" + signedUpPercentage + "%)");
				str.setSpan(new ForegroundColorSpan(Color.rgb(190, 153, 35)), str.length() - (String.valueOf(signedUpPercentage).length() + 3),
						str.length() - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				progressHeaderAndPercentage.setText(str);
			}

			fortschritt.setText("Fortschritt: " + currentCP + " +" + signedUpCP + "/" + mainInfo.requiredCp + " CP  (" + percentage + " +" + signedUpPercentage
					+ "%)");

			String curCp = "" + currentCP;
			if (currentCP - (int) currentCP == 0)
				curCp = curCp.substring(0, curCp.length() - 2);

			if (signedUpPercentage == 0)
			{

				fortschritt.setText(curCp + "/" + mainInfo.requiredCp + "CP");

			}

			else
			{

				String signCp = "" + signedUpCP;
				if (signedUpCP - (int) signedUpCP == 0)
					signCp = signCp.substring(0, signCp.length() - 2);

				SpannableString str = new SpannableString(curCp + "+" + signCp + "/" + mainInfo.requiredCp + " CP");
				str.setSpan(new ForegroundColorSpan(Color.rgb(190, 153, 35)), curCp.length(), curCp.length() + signCp.length() + 1,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				fortschritt.setText(str);
			}

			grade = (double) ((int) (helper.getGrade() * 100)) / 100;

			if (grade != 0)
				note.setText("" + grade);
			else
				note.setText("-");

			progressBar.setMax(mainInfo.requiredCp);
			progressBar.setIndeterminate(false);
			progressBar.setProgress((int) currentCP);
			progressBar.setSecondaryProgress((int) (currentCP + signedUpCP));

			double averageCpForSemester = helper.averageCPForSemester();
			if (averageCpForSemester != 0)
			{
				int neccessarySem = (int) (Math.ceil((1.0 * mainInfo.requiredCp - helper.getCurrentCPWithSignedUpCourses()) / averageCpForSemester));
				neccessarSemsForGraduation.setText("In " + neccessarySem + " Semestern");
			} else
				neccessarSemsForGraduation.setText("-");

			if (semester > 1)
			{
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						Intent intent = new Intent(context, Charts.class);
						startActivity(intent);
					}
				});
			} else
			{
				button.setVisibility(View.GONE);
			}

			if (PreferenceHelper.isStudyComplete(context))
				studyComplete.setVisibility(View.VISIBLE);
			else
				studyComplete.setVisibility(View.GONE);

			shwowOverview();

		} else
			showOtherView();

	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (dataChanged)
		{
			setData();
			dataChanged = false;

		}

	}

	@Override
	public void update()
	{
		if (isResumed())
			setData();
		else
			dataChanged = true;
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();

		if (helper != null)
		{
			helper.close();
		}
	}

}
