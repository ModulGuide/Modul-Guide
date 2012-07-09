package de.eStudent.modulGuide;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Choosable;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.Optional;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.common.Status;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Ansicht zum Editieren von Kursen / Criterions
 * 
 */
public class CourseEdit extends SherlockActivity
{
	DataHelper helper;
	Context context;
	EditText gradeView;
	EditText cpView;
	TextView catText;
	TextView errorView;
	CheckBox graded;
	CheckBox failed;
	Spinner semesterSpinner;
	Spinner categorySpinner;
	Spinner statusSpinner;
	MenuItem item;

	ArrayList<ChildEntry> childentries;
	long id = -1;
	int semester = 0;
	int type;
	int choice;
	int status;
	int dummystatus;
	int requiredCourse;
	double grade;
	double cp;
	int isGraded;
	String name = "";

	/** CP-Fehler */
	boolean cpErr = false;
	/** Notenfehler */
	boolean gradeErr = false;

	public final static String SEMESTER_KEY = "semester";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		context = this;
		helper = new DataHelper(context);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_edit);

		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		id = -1;
		// semester = PreferenceHelper.getSemesterInt(context);
		if (intent.getExtras() != null)
		{
			id = intent.getExtras().getLong(Statics.ID);
			// semester = intent.getExtras().getInt(SEMESTER_KEY);
			type = intent.getExtras().getInt(Statics.TYPE);
		}

		if (type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE)
			id = helper.getCourseFromUniversityCalendar(id);

		// final Course c = helper.getCourse(id);

		errorView = (TextView) findViewById(R.id.courseEditError);
		TextView courseText = (TextView) findViewById(R.id.courseEditCourseText);
		gradeView = (EditText) findViewById(R.id.courseEditGradeTextField);
		cpView = (EditText) findViewById(R.id.courseEditCPTextField);
		semesterSpinner = (Spinner) findViewById(R.id.courseEditSemesterSpinner);
		categorySpinner = (Spinner) findViewById(R.id.courseEditChoiceSpinner);
		TextView catText = (TextView) findViewById(R.id.courseEditChoice);
		statusSpinner = (Spinner) findViewById(R.id.courseEditStatusSpinner);
		graded = (CheckBox) findViewById(R.id.courseEditCheckboxGraded);

		ChildEntry c = null;
		if (type == Statics.TYPE_CRITERION)
		{
			c = helper.getCriterion(id);
			semester = ((Criterion) c).semester;
			grade = ((Criterion) c).grade;
			cp = ((Criterion) c).cp;
			status = ((Criterion) c).status;
			dummystatus = ((Criterion) c).status;
			isGraded = ((Criterion) c).graded;
			name = ((Criterion) c).name;
		} else
		{
			c = helper.getCourse(id);
			semester = ((Course) c).semester;
			grade = ((Course) c).grade;
			cp = ((Course) c).cp;
			status = ((Course) c).status;
			dummystatus = ((Course) c).status;
			requiredCourse = ((Course) c).requiredCourse;
			isGraded = ((Course) c).graded;
			name = ((Course) c).name;
		}

		if (isGraded == 0)
		{
			graded.setVisibility(View.GONE);
			graded.setChecked(true);
		} else if (isGraded == 1)
		{
			graded.setVisibility(View.GONE);
			graded.setChecked(false);
		} else
		{
			graded.setVisibility(View.VISIBLE);
		}

		if (semester < 1)
			semester = PreferenceHelper.getSemesterInt(context);

		if (status == Status.STATUS_PASSED && grade == 0)
		{
			graded.setChecked(true);
			gradeView.setEnabled(false);
		}
		courseText.setText(name);

		cpView.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void afterTextChanged(Editable arg0)
			{
				if (arg0.length() == 0)
				{
					setEnabledAction(false);
					errorView.setText("Ungültige CP");
					cpErr = true;
					errorView.setVisibility(View.VISIBLE);

				} else
				{
					double newCP;
					try
					{
						newCP = Double.parseDouble(arg0.toString());
					} catch (Exception e)
					{
						newCP = -1;
					}

					if (newCP < 0)
					{
						setEnabledAction(false);
						errorView.setText("Ungültige CP");
						cpErr = true;
						errorView.setVisibility(View.VISIBLE);
					} else
					{
						cpErr = false;
						setEnabledAction(true);
						errorView.setVisibility(View.INVISIBLE);
						if (gradeErr && !graded.isChecked())
						{
							errorView.setText("Ungültige Note: [1.0 - 4.0]);");
							errorView.setVisibility(View.VISIBLE);
							setEnabledAction(false);
						}
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

		gradeView.setText("");

		gradeView.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void afterTextChanged(Editable arg0)
			{
				if (arg0.length() == 0)
				{
					setEnabledAction(false);
					errorView.setText("Ungültige Note: [1.0 - 4.0]);");
					gradeErr = true;
					errorView.setVisibility(View.VISIBLE);

				} else
				{
					double newGrade;
					try
					{
						newGrade = Double.parseDouble(arg0.toString());
					} catch (Exception e)
					{
						newGrade = 0;
					}

					if (newGrade < 1 || newGrade > 4)
					{
						setEnabledAction(false);
						errorView.setText("Ungültige Note: [1.0 - 4.0]");
						gradeErr = true;
						errorView.setVisibility(View.VISIBLE);
					} else
					{
						setEnabledAction(true);
						gradeErr = false;
						errorView.setVisibility(View.INVISIBLE);
						if (cpErr)
						{
							errorView.setText("Ungültige CP");
							errorView.setVisibility(View.VISIBLE);
							setEnabledAction(false);
						}
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

		if (grade > 0)
		{
			gradeView.setHint("" + grade);
			gradeView.setText("" + grade);
		}

		cpView.setText("" + cp);
		cpView.setHint("" + cp);

		graded.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					gradeView.setEnabled(false);
					if (cpErr)
					{
						errorView.setVisibility(View.VISIBLE);
						errorView.setText("Ungültige CP");
						setEnabledAction(false);
					} else
					{
						errorView.setVisibility(View.INVISIBLE);
						setEnabledAction(true);
					}
				} else
				{
					gradeView.setEnabled(true);

					if (gradeView.getText().length() == 0)
					{
						setEnabledAction(false);
						errorView.setText("Ungültige Note: [1.0 - 4.0]");
						errorView.setVisibility(View.VISIBLE);
						gradeErr = true;
					} else
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
							setEnabledAction(false);
							errorView.setText("Ungültige Note: [1.0 - 4.0]");
							gradeErr = true;
							errorView.setVisibility(View.VISIBLE);
						} else
						{
							setEnabledAction(true);
							gradeErr = false;
							if (cpErr)
							{
								errorView.setText("Ungültige CP");
								errorView.setVisibility(View.VISIBLE);
								setEnabledAction(false);
							}
						}
					}
				}
			}
		});

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
		int semesterdummy = Integer.parseInt(PreferenceHelper.getSemester(context));
		for (int i = 1; i <= semesterdummy; i++)
		{
			adapter.add(i + ". Semester");
		}

		adapter.notifyDataSetChanged();
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		semesterSpinner.setAdapter(adapter);
		semesterSpinner.setSelection(semester - 1);

		if (type != Statics.TYPE_CRITERION && requiredCourse != 1)
		{
			childentries = helper.getPossibleOptionalAndChoosableChoicesFromCourse(id);

			ArrayList<String> catlist = new ArrayList<String>();

			for (ChildEntry ca : childentries)
				catlist.add(ca.name);

			ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, catlist);
			adapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, catlist);

			adapter2.notifyDataSetChanged();
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			categorySpinner.setAdapter(adapter2);

			long catId = 0;

			catId = helper.getOptional(id);
			for (ChildEntry e : childentries)
			{
				if ((e instanceof Choosable && ((Choosable) e).selectedCourseId == id) || e instanceof Optional && ((Optional) e).id == catId)
				{
					categorySpinner.setSelection(childentries.indexOf(e));
					break;
				}
			}

		} else
		{
			catText.setVisibility(View.GONE);
			categorySpinner.setVisibility(View.GONE);
		}

		ArrayAdapter<CharSequence> adapter3 = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);

		adapter3.add("Bestanden");
		adapter3.add("Nicht bestanden");
		if (type != Statics.TYPE_CRITERION)
			adapter3.add("Angemeldet");

		adapter3.notifyDataSetChanged();
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		statusSpinner.setAdapter(adapter3);
		switch (status)
		{
		case Status.STATUS_PASSED:
			statusSpinner.setSelection(0);
			gradeView.setVisibility(View.VISIBLE);
			graded.setVisibility(View.VISIBLE);
			// findViewById(R.id.courseEditGrade).setVisibility(View.GONE);
			if (!graded.isChecked() && !gradeView.getText().toString().equals(""))
				setEnabledAction(false);
			else
				setEnabledAction(true);
			break;
		case Status.STATUS_NOT_PASSED:
			statusSpinner.setSelection(1);
			gradeView.setVisibility(View.GONE);
			graded.setVisibility(View.GONE);
			break;
		case Status.STATUS_SIGNED_UP:
			statusSpinner.setSelection(2);
			gradeView.setVisibility(View.GONE);
			graded.setVisibility(View.GONE);
			break;
		case Status.STATUS_REQUIREMENTS_MISSING:
			statusSpinner.setSelection(1);
			gradeView.setVisibility(View.GONE);
			graded.setVisibility(View.GONE);
			break;
		default:
			statusSpinner.setSelection(2);
			gradeView.setVisibility(View.GONE);
			graded.setVisibility(View.GONE);
		}

		statusSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
			{
				if (position == 0 && status == Status.STATUS_REQUIREMENTS_MISSING)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Einige Bedingungen für diesen Kurs wurden noch nicht erfüllt. Soll dieser Kurs dennoch als bestanden markiert werden?")
							.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									statusSpinner.setSelection(0);
									dialog.cancel();
								}
							}).setNegativeButton("Nein", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									switch (status)
									{
									case Status.STATUS_PASSED:
										statusSpinner.setSelection(0);
									case Status.STATUS_NOT_PASSED:
										statusSpinner.setSelection(1);
									case Status.STATUS_SIGNED_UP:
										statusSpinner.setSelection(2);
									case Status.STATUS_REQUIREMENTS_MISSING:
										statusSpinner.setSelection(1);
									}
									dialog.cancel();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
				if (position == 0 && isGraded == 2) // 0 = bestanden, 2 = egal
				{
					gradeView.setVisibility(View.VISIBLE);
					graded.setVisibility(View.VISIBLE);
					findViewById(R.id.courseEditGrade).setVisibility(View.VISIBLE);
					if (gradeView.getText().length() == 0 && !graded.isChecked())
					{
						errorView.setText("Ungültige Note: [1.0 - 4.0]");
						errorView.setVisibility(View.VISIBLE);
						gradeErr = true;
						setEnabledAction(false);
					} else
						setEnabledAction(true);
				} else if (position == 0 && isGraded == 1) // 1 = nur benotet
				{
					gradeView.setVisibility(View.VISIBLE);
					graded.setVisibility(View.GONE);
					graded.setChecked(false);
					findViewById(R.id.courseEditGrade).setVisibility(View.VISIBLE);
					if (gradeView.getText().length() == 0)
					{
						errorView.setText("Ungültige Note: [1.0 - 4.0]");
						errorView.setVisibility(View.VISIBLE);
						gradeErr = true;
						setEnabledAction(false);
					} else
						setEnabledAction(true);
				}

				else
				{
					gradeView.setVisibility(View.GONE);
					graded.setVisibility(View.GONE);
					findViewById(R.id.courseEditGrade).setVisibility(View.GONE);
					if (!cpErr)
					{
						errorView.setVisibility(View.INVISIBLE);
						setEnabledAction(true);
					}
				}
				int dummy = statusSpinner.getSelectedItemPosition();
				switch (dummy)
				{
				case 0:
					status = Status.STATUS_PASSED;
					break;
				case 1:
					if (dummystatus == Status.STATUS_REQUIREMENTS_MISSING)
						status = Status.STATUS_REQUIREMENTS_MISSING;
					else
						status = Status.STATUS_NOT_PASSED;
					break;
				case 2:
					status = Status.STATUS_SIGNED_UP;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView)
			{
				// your code here
			}

		});
	}

	private boolean tmp;

	private void setEnabledAction(boolean enabled)
	{
		if (item == null)
			tmp = enabled;
		else
		{
			item.setEnabled(enabled);
			if (enabled)
				item.setIcon(R.drawable.actionbar_accept);
			else
				item.setIcon(R.drawable.actionbar_accept_disabled);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.course_edit_menu, menu);

		item = menu.findItem(R.id.save);
		setEnabledAction(tmp);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;
		case R.id.save:
			errorView.setVisibility(View.INVISIBLE);
			int semester = (int) semesterSpinner.getSelectedItemId() + 1;
			double newGrade = 0;
			if (gradeView.isShown() && !graded.isChecked() && !gradeView.getText().toString().equals(""))
				newGrade = Double.parseDouble(gradeView.getText().toString());
			double newCP = 0;
			if (!cpView.getText().toString().equals(""))
				newCP = Double.parseDouble(cpView.getText().toString());

			if (requiredCourse != 1 && type != Statics.TYPE_CRITERION)
			{
				ChildEntry optChoosChoice = null;
				// TODO: WTF?
				optChoosChoice = childentries.get((int) categorySpinner.getSelectedItemId());

				helper.setCourseEdited(id, semester, newGrade, optChoosChoice, newCP, status);
			} else
			{
				// TODO: cp ändern ohne note bei nicht bestandenem
				// fach
				// fuehrt zum bestehen
				if (type == Statics.TYPE_COURSE)
					helper.setCourseEdited(id, semester, newGrade, null, newCP, status);
				else if (type == Statics.TYPE_CRITERION)
					helper.setCriterionEdited(id, semester, newGrade, newCP, status); // category);
			}
			finish();
			return true;

		case R.id.cancel:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
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
