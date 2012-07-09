package de.eStudent.modulGuide;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Dialog um einen Kurs / Criterion als bestanden zu markieren
 * 
 */
public class PassedCourseDialog extends Activity
{

	//Der Context
	private Context context;

	//KEy für den Intent; Man kann ein dadurch ein vorausgewähltes Semester auswählen
	public final static String SEMESTER_KEY = "semester";

	//das aktuell ausgewählte Semester
	private int selectedSemester;
	
	//
	private int isRequired = 0;
	
	//das Semester des ausgewählten Kurses
	private int semester;
	
	//Die ID des Kurses
	private long id;
	
	//Liste der Optional / Choosable denen der Kurs zugeordnet werden kann
	private ArrayList<ChildEntry> childentries;
	
	//int, ob der Kurs bewertet oder unbewertet oder entweder oder ist
	private int isGraded;
	
	//boolean, ob der Kurs einem Choosable oder Optional angehört, oder ob es ein pflichtkurs ist
	private boolean isChoosableOrOptionalCourse = false;

	//View für die Note
	private EditText gradeView;
	
	//Checkbos um ein Kurs als unbenotet zu markieren
	private CheckBox box;
	
	//Spinner zum auswählen des Semesters
	private Spinner semesterSpinner;
	
	//Spinner zum auswählen des zugehörigen Choosables / Optionals
	private Spinner optionalChoosableSpinner;
	
	//DataHelper
	private DataHelper helper;
	
	//Ob ein Kurs, ein Vorlesungsverzeichniskurs oder ein Criterion als bestanden markiert werden soll
	private int type;
	
	//der OK Button
	private Button ok;
	
	//Fehlermeldung
	private TextView errorView;
	
	//ViewSwitcher
	private ViewSwitcher viewSwitcher;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//---Layout setzen ---
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_passed_dialog);

		context = this;
		helper = new DataHelper(context);

		Intent intent = getIntent();

		Bundle extras = intent.getExtras();

		semester = PreferenceHelper.getSemesterInt(context);

		viewSwitcher = (ViewSwitcher) findViewById(R.id.profileSwitcherPassed);

		errorView = (TextView) findViewById(R.id.coursePassedError);

		ok = (Button) findViewById(R.id.coursePassedOk);
		ok.setEnabled(false);

		type = extras.getInt(Statics.TYPE);
		id = extras.getLong(Statics.ID);

		selectedSemester = extras.getInt(SEMESTER_KEY);
		if (selectedSemester == 0)
			selectedSemester = semester;

		gradeView = (EditText) findViewById(R.id.coursePassedGradeTextField);

		ChildEntry c = null;

		TextView titleView = (TextView) findViewById(R.id.title);
		TextView subtitleView = (TextView) findViewById(R.id.subtitle);
		subtitleView.setVisibility(View.VISIBLE);

		switch (type)
		{
		case Statics.TYPE_COURSE:

			titleView.setText("Kurs bestanden:");

			c = helper.getCourse(id);
			subtitleView.setText(((Course) c).name);
			subtitleView.setText(c.name);
			isRequired = ((Course) c).requiredCourse;
			isGraded = ((Course) c).graded;
			break;

		case Statics.TYPE_CRITERION:
			titleView.setText("Kriterium bestanden:");
			subtitleView.setText(helper.getCriteriumName(id));

			c = helper.getCriterion(id);
			subtitleView.setText(((Criterion) c).name);
			isRequired = -1;
			isGraded = ((Criterion) c).graded;
			break;
		case Statics.TYPE_UNIVERSITY_CALENDAR_COURSE:
			titleView.setText("Kurs bestanden:");
			// id = helper.getCourseFromUniversityCalendar(id);
			c = helper.getMergedCourse(id, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
			subtitleView.setText(c.name);

			isRequired = ((Course) c).requiredCourse;// helper.isRequiredCourse(id);
			isGraded = ((Course) c).graded;
			isChoosableOrOptionalCourse = helper.isChoosableOrOptionalUniversityCalendarCourse(id);
			break;

		default:

			titleView.setVisibility(View.INVISIBLE);
		}

		box = (CheckBox) findViewById(R.id.coursePassedCheckbox);

		box.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					gradeView.setEnabled(false);
					errorView.setVisibility(View.INVISIBLE);
					ok.setEnabled(true);
				} else
				{
					gradeView.setEnabled(true);

					if (gradeView.getText().length() == 0)
						ok.setEnabled(false);
					else
					{
						double grade;
						try
						{
							grade = Double.parseDouble(gradeView.getText().toString());
						} catch (Exception e)
						{
							grade = 0;
						}
						if (grade < 1 || grade > 4)
						{
							ok.setEnabled(false);
							errorView.setVisibility(View.VISIBLE);
						} else
							ok.setEnabled(true);

					}

				}

			}
		});

		gradeView.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void afterTextChanged(Editable arg0)
			{
				if (arg0.length() == 0)
				{
					ok.setEnabled(false);
					errorView.setVisibility(View.INVISIBLE);

				} else
				{
					double grade;
					try
					{
						grade = Double.parseDouble(arg0.toString());
					} catch (Exception e)
					{
						grade = 0;
					}

					if (grade < 1 || grade > 4)
					{
						ok.setEnabled(false);
						errorView.setVisibility(View.VISIBLE);
					} else
					{
						ok.setEnabled(true);
						errorView.setVisibility(View.INVISIBLE);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}

		});

		semesterSpinner = (Spinner) findViewById(R.id.coursePassedSemesterSpinner);
		optionalChoosableSpinner = (Spinner) findViewById(R.id.coursePassedOptionalChoosableSpinner);

		if (isGraded == 0)
		{
			TableRow row = (TableRow) findViewById(R.id.coursePassedGradeRow);
			row.setVisibility(View.GONE);

			box.setChecked(true);
			box.setClickable(false);

			errorView.setVisibility(View.GONE);
			ok.setEnabled(true);
		} else if (isGraded == 1)
		{
			TableRow row = (TableRow) findViewById(R.id.coursePassedCheckboxRow);
			row.setVisibility(View.GONE);
		}

		if (type == Statics.TYPE_COURSE || type == Statics.TYPE_CRITERION || isChoosableOrOptionalCourse)
		{
			TableRow row = (TableRow) findViewById(R.id.categoryRow);
			row.setVisibility(View.GONE);
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
		ArrayAdapter<String> adapter2;

		for (int i = 1; i <= semester; i++)
		{
			adapter.add(i + ". Semester");
		}

		adapter.notifyDataSetChanged();
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		semesterSpinner.setAdapter(adapter);
		semesterSpinner.setSelection(selectedSemester - 1);

		if (isRequired == 1 || (isRequired == 2 && type == Statics.TYPE_COURSE))
		{
			adapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
			adapter2.add(((Course) c).name);
			adapter2.notifyDataSetChanged();
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			optionalChoosableSpinner.setAdapter(adapter2);

		} else if (type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE)
		{
			// Course course = helper.getCourse(id);
			// final ArrayList<Optional> al =
			// helper.getPossibleOptionalChoices(id);
			childentries = helper.getPossibleOptionalAndChoosableChoicesForUCC(id);
			ArrayList<String> catlist = new ArrayList<String>();

			if (!childentries.isEmpty())
			{
				for (ChildEntry ca : childentries)
					catlist.add(ca.name);

				adapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, catlist);

				adapter2.notifyDataSetChanged();
				adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				optionalChoosableSpinner.setAdapter(adapter2);
				optionalChoosableSpinner.setSelection(-1);
				optionalChoosableSpinner.setSelection(0);
			} else
			{
				viewSwitcher.showNext();
				ok.setEnabled(false);
			}
		}

		ok.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				double grade = 0;

				if (isGraded != 0 && !box.isChecked() && !gradeView.getText().toString().equals(""))
					grade = Double.parseDouble(gradeView.getText().toString());

				if (isGraded != 0 && !box.isChecked() && (grade < 1 || grade > 4))
				{
					errorView.setVisibility(View.VISIBLE);
				} else
				{
					if (type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE && isRequired == 0 && !isChoosableOrOptionalCourse)
						id = helper.saveUniversityCalendarCourse(id);
					else if (type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE)
						id = helper.getCourseFromUniversityCalendar(id);
					errorView.setVisibility(View.INVISIBLE);

					int semester = (int) semesterSpinner.getSelectedItemId() + 1;

					ChildEntry entry = null;
					if (isRequired != 1 && type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE && !isChoosableOrOptionalCourse)
						entry = childentries.get((int) optionalChoosableSpinner.getSelectedItemId());
					else if (isRequired == 0)
						entry = helper.getOptionalObjectByCourseId(id);
					else if (isRequired == 2)
						entry = helper.getChoosableObjectByCourseId(id);

					if (type == Statics.TYPE_COURSE || type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE)
						// helper.setCoursePassed(id, semester, grade);
						helper.setCoursePassed(id, semester, grade, isRequired, entry);
					else if (type == Statics.TYPE_CRITERION)
						helper.setCriterionPassed(id, semester, grade);
					finish();
				}

			}
		});

		Button cancel = (Button) findViewById(R.id.coursePassedCancel);
		cancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
			
				finish();

			}
		});

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (helper != null)
		{
			helper.close();
		}
	}
	

}
