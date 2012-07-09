package de.eStudent.modulGuide;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.common.UniversityCalendarCourse;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Dialog zum Anmelden für einen Kurs
 * 
 */
public class CourseSignUp extends Activity
{

	Context context;

	public final static String SEMESTER_KEY = "semester";
	private int selectedSemester;
	private int semester;
	private long id;
	private UniversityCalendarCourse c;
	// private boolean deleteCourse;
	private ArrayList<ChildEntry> childentries;

	EditText gradeView;
	CheckBox box;
	Spinner semesterSpinner;
	Spinner optionalChoosableSpinner;
	DataHelper helper;
	int type;
	Button ok;
	ViewSwitcher viewSwitcher;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up_dialog);

		viewSwitcher = (ViewSwitcher) findViewById(R.id.profileSwitcherSignUp);
		context = this;
		helper = new DataHelper(context);

		Intent intent = getIntent();

		Bundle extras = intent.getExtras();

		semester = PreferenceHelper.getSemesterInt(context);

		ok = (Button) findViewById(R.id.courseSignedUpOk);
		// fullOk = (Button) findViewById(R.id.courseSignedUpOkFull);

		id = extras.getLong(Statics.ID);

		selectedSemester = extras.getInt(SEMESTER_KEY);
		if (selectedSemester == 0)
			selectedSemester = semester;

		TextView titleView = (TextView) findViewById(R.id.title);
		TextView subtitleView = (TextView) findViewById(R.id.subtitle);
		subtitleView.setVisibility(View.VISIBLE);

		titleView.setText("Kursanmeldung:");

		c = helper.getUniversityCalendarCourse(id);
		subtitleView.setText(c.name);

		optionalChoosableSpinner = (Spinner) findViewById(R.id.courseSignedUpOptionalChoosableSpinner);

		ArrayAdapter<String> adapter2;

		childentries = helper.getPossibleOptionalAndChoosableChoicesForUCC(id);
		if (!childentries.isEmpty())
		{
			ArrayList<String> catlist = new ArrayList<String>();

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

		ok.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				ChildEntry entry = null;
				Log.d("sign", "up");

				entry = childentries.get((int) optionalChoosableSpinner.getSelectedItemId());

				helper.setUniversityCalendarCourseSignedUp(id, semester, entry);
				finish();
			}
		});

		Button cancel = (Button) findViewById(R.id.courseSignedUpCancel);
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
		// TODO Auto-generated method stub
		super.onDestroy();

		if (helper != null)
		{
			helper.close();
		}
	}

}
