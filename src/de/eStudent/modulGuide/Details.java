package de.eStudent.modulGuide;

import greendroid.widget.QuickActionGrid;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Choosable;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.Optional;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.common.Status;
import de.eStudent.modulGuide.common.UniversityCalendarCourse;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DatabaseListener;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Ansicht für die Details von Course / Criterion, Optional, Choosable
 * 
 */
public class Details extends SherlockActivity implements DatabaseListener
{

	//Der Context
	private Context context;
	
	//Die ID
	private long id;
	
	//der Typ (Course / Criterion / Optional / Choosable)
	private int type;
	
	//Datahelper
	private DataHelper helper;
	
	//Der Childentry von dem die Details angezeigt werden
	private ChildEntry childEntry;

	//der Course von dem die Details angezeigt werden
	private Course c = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = getBaseContext();

		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);

		Intent intent = getIntent();
		id = intent.getExtras().getLong(Statics.ID, 0);
		type = intent.getExtras().getInt(Statics.TYPE);
		helper = new DataHelper(getBaseContext());
		helper.addDataBaseListener(this);

		// JE nach Typ ein anderes LAyout setzen
		switch (type)
		{
		case Statics.TYPE_COURSE:
		case Statics.TYPE_UNIVERSITY_CALENDAR_COURSE:
			setContentView(R.layout.university_calendar_course_overview);
			break;
		case Statics.TYPE_CRITERION:
			setContentView(R.layout.criterion_overview);
			break;
		case Statics.TYPE_CHOOSABLE:
			setContentView(R.layout.choosable_overview);
			break;
		case Statics.TYPE_OPTIONAL:
			setContentView(R.layout.optional_overview);
			break;
		}
		setContent();

		// In der ActionBar einen Buttons zum aufruden der Actions hinzufügen
		View customView = LayoutInflater.from(this).inflate(R.layout.custom_action_item, null);

		final ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT
				| Gravity.CENTER_VERTICAL);

		customView.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				QuickActionGrid mBar = childEntry.getQuickActionGrid(helper, context, Statics.FLAG_WITHOUT_DETAILS);

				if (PreferenceHelper.getRegulationName(context).length() == 0)
					Toast.makeText(context, "Wähle zuerst eine Pürfungsordnung aus!", Toast.LENGTH_LONG).show();
				else if (mBar != null)
					mBar.show(v);

			}
		});

		actionBar.setCustomView(customView, layoutParams);

	}

	private void setContent()
	{
		// Je nach Typ unterschiedlcihen Content setzen
		switch (type)
		{
		case Statics.TYPE_COURSE:
		case Statics.TYPE_UNIVERSITY_CALENDAR_COURSE:

			UniversityCalendarCourse course = helper.getMergedCourse(id, type);
			childEntry = course;

			TextView name = (TextView) findViewById(R.id.universityCalendarCourseOverviewName);
			TextView staff = (TextView) findViewById(R.id.universityCalendarCourseOverviewProfValue);
			TextView date = (TextView) findViewById(R.id.universityCalendarCourseOverviewTimeValue);
			TextView duration = (TextView) findViewById(R.id.universityCalendarCourseOverviewDurationValue);
			TextView desc = (TextView) findViewById(R.id.universityCalendarCourseOverviewDescriptionValue);
			TextView cp = (TextView) findViewById(R.id.universityCalendarCourseOverviewCPValue);
			TextView grade = (TextView) findViewById(R.id.universityCalendarCourseOverviewGradeValue);
			TextView vak = (TextView) findViewById(R.id.universityCalendarCourseOverviewVak);
			TextView note = (TextView) findViewById(R.id.universityCalendarCourseOverviewNoteValue);
			TextView semester = (TextView) findViewById(R.id.universityCalendarCourseOverviewSemesterValue);
			TextView requirements = (TextView) findViewById(R.id.universityCalendarCourseOverviewRequirementsValue);
			ImageView status = (ImageView) findViewById(R.id.universityCalendarCourseOverviewStatus);
			TextView alternative = (TextView) findViewById(R.id.universityCalendarCourseOverviewAlternative);
			TextView weight = (TextView) findViewById(R.id.universityCalendarCourseOverviewWeightValue);

			LinearLayout layout2 = (LinearLayout) findViewById(R.id.layout2);

			name.setText(course.name);
			vak.setText(course.vak);
			status.setImageResource(Status.getIcon(course.status));
			if (course.staff != null && !course.staff.equals(""))
			{
				staff.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewProf).setVisibility(View.VISIBLE);
				staff.setText(course.staff);
			}
			if (course.note != null && !course.note.equals(""))
			{
				note.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewNote).setVisibility(View.VISIBLE);
				note.setText("" + course.note);
			}

			if (course.duration == 0)
			{
				duration.setText("1 Semester");
			} else
				duration.setText(course.duration + " Semester");

			if (course.date != null && !course.date.equals(""))
			{
				layout2.setVisibility(View.VISIBLE);
				date.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewTime).setVisibility(View.VISIBLE);
				date.setText(course.date);
			}
			if (course.description != null && !course.description.equals(""))
			{
				layout2.setVisibility(View.VISIBLE);
				desc.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewDescription).setVisibility(View.VISIBLE);
				desc.setText(course.description);
			}
			if (course.cp > 0)
				cp.setText("" + Statics.convertCP(course.cp));

			if (course.grade < 1 && course.status == Status.STATUS_PASSED)
			{
				grade.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewGrade).setVisibility(View.VISIBLE);
				grade.setText("unbenotet");
			} else if (course.grade >= 1)
			{
				grade.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewGrade).setVisibility(View.VISIBLE);
				grade.setText("" + course.grade);
			}
			if (course.semester >= 1)
			{
				semester.setVisibility(View.VISIBLE);
				findViewById(R.id.universityCalendarCourseOverviewSemester).setVisibility(View.VISIBLE);
				semester.setText("" + course.semester);
			}

			if (course.alternative != null && course.alternative.length() != 0)
				alternative.setText(course.alternative);

			course.requirements = helper.getCourseRequirementsAsHtml(id);
			String requirementString = "";
			if (course.necCP != 0)
			{
				if (helper.getCurrentCP() >= course.necCP)
					requirementString = "- <font color=green>" + course.necCP + " CP \u2714 </font>";
				else
					requirementString = "- <font color=red>" + course.necCP + " CP </font>";
			}
			if (course.requirements.length > 0)
			{
				for (String req : course.requirements)
				{
					if (requirementString.length() == 0)
						requirementString = "- " + req;
					else
						requirementString = requirementString + "<br>- " + req;
				}
			}
			if (requirementString.length() != 0)
			{
				requirements.setText(Html.fromHtml(requirementString));
			}
			if (course.weight == 1)
			{
				weight.setVisibility(View.GONE);
				(findViewById(R.id.universityCalendarCourseOverviewWeight)).setVisibility(View.GONE);
			} else
				weight.setText("" + course.weight + "x");
			break;
		case Statics.TYPE_CRITERION:

			Criterion criterion = helper.getCriterion(id);
			childEntry = criterion;

			TextView critName = (TextView) findViewById(R.id.criterionName);
			ImageView critStatus = (ImageView) findViewById(R.id.criterionStatus);
			TextView critNote = (TextView) findViewById(R.id.criterionNoteValue);
			TextView critSem = (TextView) findViewById(R.id.criterionSemesterValue);
			TextView critCP = (TextView) findViewById(R.id.criterionCPValue);
			TextView critGrade = (TextView) findViewById(R.id.criterionGradeValue);
			TextView critReq = (TextView) findViewById(R.id.criterionRequirementsValue);
			TextView critAlternative = (TextView) findViewById(R.id.criterionAlternative);
			TextView critWeight = (TextView) findViewById(R.id.criterionWeightValue);

			critName.setText(criterion.name);
			critStatus.setImageResource(Status.getIcon(criterion.status));
			if (criterion.note != null)
				critNote.setText(criterion.note);
			if (criterion.semester > 0)
				critSem.setText("" + criterion.semester);
			else
				critSem.setText("-");
			critCP.setText("" + Statics.convertCP(criterion.cp));
			if (criterion.graded != 0 && criterion.grade > 0)
				critGrade.setText("" + criterion.grade);
			else if (criterion.graded != 1 || (criterion.status == Status.STATUS_PASSED && criterion.grade == 0))
				critGrade.setText("unbenotet");
			else
				critGrade.setText("-");

			if (criterion.alternative != null && criterion.alternative.length() != 0)
				critAlternative.setText(criterion.alternative);

			criterion.requirements = helper.getCriterionRequirementsAsHtml(id);
			String requirementString2 = "";

			if (criterion.neccp != 0)
			{
				if (helper.getCurrentCP() > criterion.neccp)
					requirementString2 = "- <font color=green>" + criterion.neccp + " CP \u2714 </font>";
				else
					requirementString2 = "- <font color=red>" + criterion.neccp + " CP </font>";
			}
			if (criterion.requirements.length > 0)
			{
				for (String req : criterion.requirements)
				{
					if (requirementString2.length() == 0)
						requirementString2 = "- " + req;
					else
						requirementString2 = requirementString2 + "<br>- " + req;
				}
			}
			if (requirementString2.length() != 0)
			{
				critReq.setText(Html.fromHtml(requirementString2));
			}
			if (criterion.weight == 1)
			{
				critWeight.setVisibility(View.GONE);
				(findViewById(R.id.criterionWeight)).setVisibility(View.GONE);
			} else
				critWeight.setText("" + criterion.weight + "x");

			break;
		case Statics.TYPE_CHOOSABLE:

			Choosable choosable = helper.getChoosableObjectById(id);
			childEntry = choosable;

			TextView choosName = (TextView) findViewById(R.id.choosableName);
			TextView choosNote = (TextView) findViewById(R.id.choosableNoteValue);
			TextView choosCP = (TextView) findViewById(R.id.choosableCPValue);
			TextView choosAlternative = (TextView) findViewById(R.id.choosableAlternative);
			TextView choosRecommendeCourse = (TextView) findViewById(R.id.choosableRecommendedCourse);

			if (choosable.selectedCourseId != 0)
			{
				c = helper.getCourse(choosable.selectedCourseId);
				c.subCourse = true;
			}
			choosName.setText(choosable.name);
			if (choosable.note != null)
				choosNote.setText(choosable.note);
			if (choosable.selectedCourseId != 0 && c.status == Status.STATUS_PASSED)
				choosCP.setText("" + Statics.convertCP(choosable.cp) + "/" + Statics.convertCP(choosable.cp));
			else
				choosCP.setText("0/" + Statics.convertCP(choosable.cp));

			if (choosable.alternative.length() != 0)
				choosAlternative.setText(choosable.alternative);

			if (choosable.reccomendedCourseName != null)
				choosRecommendeCourse.setText(choosable.reccomendedCourseName + " (" + choosable.reccomendedCourseVak + ")");

			LinearLayout mainLayout = (LinearLayout) findViewById(R.id.choosableLayout);
			mainLayout.removeAllViews();
			if (choosable.selectedCourseId != 0)
			{

				LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = vi.inflate(R.layout.child_row_subject, null);
				LinearLayout layout = (LinearLayout) v.findViewById(R.id.courseLayout);
				TextView courseName = (TextView) v.findViewById(R.id.subjectName);
				TextView courseDur = (TextView) v.findViewById(R.id.subjectDuration);
				ImageView courseStatus = (ImageView) v.findViewById(R.id.subjectImage);
				TextView courseGrade = (TextView) v.findViewById(R.id.subjectGrade);
				TextView courseCP = (TextView) v.findViewById(R.id.subjectCP);

				layout.setPadding(0, 0, 0, 0);
				v.setBackgroundDrawable(null);
				courseName.setText(c.name);
				courseName.setSelected(true);
				if (c.duration > 1)
					courseDur.setText("" + c.duration);
				courseStatus.setImageResource(Status.getIcon(c.status));
				if (c.grade > 0)
					courseGrade.setText("Note: " + c.grade);
				courseCP.setText(Statics.convertCP(c.cp) + " CP");
				View v2 = new View(context);
				v2.setBackgroundColor(Color.BLACK);
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				v2.setLayoutParams(lp);
				mainLayout.addView(v2);

				v.setClickable(true);

				v.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{

						startActivity(c.getOnClickIntent(context));
					}
				});

				v.setOnLongClickListener(new OnLongClickListener()
				{

					@Override
					public boolean onLongClick(View v)
					{

						c.getQuickActionGrid(helper, context, 0).show(v);

						return true;
					}
				});

				v.setBackgroundResource(R.color.child_row_bg);

				mainLayout.addView(v);
				View v3 = new View(context);
				v3.setBackgroundColor(Color.BLACK);
				LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				v3.setLayoutParams(lp2);
				mainLayout.addView(v3);
			} else
			{
				TextView tv = new TextView(context);
				tv.setText("Keine");
				tv.setTextColor(Color.BLACK);
				mainLayout.addView(tv);
			}

			break;

		case Statics.TYPE_OPTIONAL:

			Optional optional = helper.getOptionalObjectById(id);
			childEntry = optional;

			TextView optName = (TextView) findViewById(R.id.optionalName);
			TextView optNote = (TextView) findViewById(R.id.optionalNoteValue);
			TextView optCP = (TextView) findViewById(R.id.optionalCPValue);
			LinearLayout mainLayoutOpt = (LinearLayout) findViewById(R.id.optionalLayout);

			TextView optAlternative = (TextView) findViewById(R.id.optionalAlternative);
			TextView optRecommendedCourse = (TextView) findViewById(R.id.optionalRecommendedCourse);
			TextView optWeight = (TextView) findViewById(R.id.optionalWeightValue);

			// ListView optList = (ListView)
			// findViewById(R.id.optionalCourseListView);

			ArrayList<Course> data = new ArrayList<Course>();
			data = helper.getCoursesFromOptional(id);

			optName.setText(optional.name);
			if (optional.note != null)
				optNote.setText(optional.note);

			String acquiredcp = Statics.convertCP(optional.acquiredcp);
			String chosencp = Statics.convertCP(optional.chosencp);
			String cpstr = Statics.convertCP(optional.cp);

			if (optional.chosencp != 0)
			{
				SpannableString str = new SpannableString(acquiredcp + "+" + chosencp + "/" + cpstr);
				str.setSpan(new ForegroundColorSpan(Color.rgb(244, 63, 14)), acquiredcp.length(), str.length() - cpstr.length() - 1,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				optCP.setText(str);
			} else
				optCP.setText(acquiredcp + "/" + cpstr);

			if (optional.alternative.length() != 0)
				optAlternative.setText(optional.alternative);

			if (optional.coursename != null)
				optRecommendedCourse.setText(optional.coursename);

			optWeight.setVisibility(View.GONE);
			(findViewById(R.id.optionalWeight)).setVisibility(View.GONE);

			mainLayoutOpt.removeAllViews();

			if (data.size() == 0)
			{
				TextView tv = new TextView(context);
				tv.setText("Keine");
				tv.setTextColor(Color.BLACK);
				mainLayoutOpt.addView(tv);
			}

			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (int i = 0; i < data.size(); i++)
			{
				View v = vi.inflate(R.layout.child_row_subject, null);
				LinearLayout layout = (LinearLayout) v.findViewById(R.id.courseLayout);
				TextView courseName = (TextView) v.findViewById(R.id.subjectName);
				TextView courseDur = (TextView) v.findViewById(R.id.subjectDuration);
				ImageView courseStatus = (ImageView) v.findViewById(R.id.subjectImage);
				TextView courseGrade = (TextView) v.findViewById(R.id.subjectGrade);
				TextView courseCP = (TextView) v.findViewById(R.id.subjectCP);
				Course c2 = data.get(i);

				layout.setPadding(0, 0, 0, 0);
				v.setBackgroundDrawable(null);
				courseName.setText(c2.name);
				courseName.setSelected(true);
				if (c2.duration > 1)
					courseDur.setText(c2.duration);
				courseStatus.setImageResource(Status.getIcon(c2.status));
				if (c2.grade > 0)
					courseGrade.setText("Note: " + c2.grade);
				courseCP.setText(Statics.convertCP(c2.cp) + " CP");
				View v2 = new View(context);
				v2.setBackgroundColor(Color.BLACK);
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				v2.setLayoutParams(lp);
				mainLayoutOpt.addView(v2);

				v.setBackgroundResource(R.color.child_row_bg);
				v.setTag(c2);

				v.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						startActivity(((Course) v.getTag()).getOnClickIntent(context));
					}
				});

				v.setOnLongClickListener(new OnLongClickListener()
				{

					@Override
					public boolean onLongClick(View v)
					{
						QuickActionGrid mBar = ((Course) v.getTag()).getQuickActionGrid(helper, context, 0);
						mBar.show(v);
						return false;
					}
				});

				mainLayoutOpt.addView(v);
				if (i + 1 == data.size())
				{
					View v3 = new View(context);
					v3.setBackgroundColor(Color.BLACK);
					LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
					v3.setLayoutParams(lp2);
					mainLayoutOpt.addView(v3);
				}

			}
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		//Startansicht öffnen
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean running = true;
	private boolean update = false;

	@Override
	protected void onPause()
	{
		running = false;
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		running = true;
		if (update)
			setContent();
		update = false;
		super.onResume();
	}

	@Override
	public void update()
	{
		if (!helper.db.isOpen())
			helper.reOpen();
		if (running)
			setContent();
		else
			update = true;

	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("tag", "helper destroyed");
		if (helper != null)
		{
			helper.close();
		}
	}

}
