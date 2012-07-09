package de.eStudent.modulGuide.common;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.eStudent.modulGuide.CourseEdit;
import de.eStudent.modulGuide.Details;
import de.eStudent.modulGuide.PassedCourseDialog;
import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Enthält alle Daten und die Darstellung eines Kurses.
 */

public class Course extends ChildEntry
{
	/** Der Typ als Integer */
	public static final int TYPE = 1;

	/** Die CP, die dieser Kurs bringt */
	public double cp = 0;

	/** Die CP, die benötigt werden, um sich für diesen Kurs anzumelden. */
	public int necCP;

	/** Die Semesteranzahl, über die dieser Kurs läuft */
	public int duration = 1;

	/** Die VAK dieses Kurses */
	public String vak;

	/** Die Voraussetzungen, um sich für diesen Kurs anzumelden */
	public String[] requirements;

	/** Das Semester, in dem dieser Kurs eingetragen wurde */
	public int semester;

	/** Die Note, die in diesem Kurs erreicht wurde */
	public double grade;

	/** Information, ob es sich um einen Pflichtkurs handelt */
	public int requiredCourse;

	/** Der Status dieses Kurses (angemeldet, requirement missing etc.) */
	public int status;

	/** Gibt an, ob der Kurs benotet oder unbenotet ist */
	public int graded;

	/** Notengewichtung des Kurses */
	public double weight;

	/** Die CP, die dieser Kurs an das Optional gibt, in dem er eingetragen ist. */
	public double optionalcp;

	/** Aufteilung der CP, sofern mehrsemestrig */
	public double[] splitcp;

	/** Gibt die CP an, die im ersten Semester erreicht werden. */
	public double firstSemestercp;

	Context context;
	View view;

	/**
	 * Information, ob es sich um einen Kurs handelt, der z.B. einem Optional
	 * unterstellt ist
	 */
	public boolean subCourse = false;

	/**
	 * Erstelt einen neuen Kurs ohne Parameter.
	 */
	public Course()
	{
	}

	/**
	 * Erstellt einen neuen Kurs.
	 * 
	 * @param name
	 *            Der Name dieses Kurses.
	 */
	public Course(String name)
	{
		this.name = name;
	}

	/**
	 * Erstellt einen neuen Kurs.
	 * 
	 * @param id
	 *            ID des Kurses (aus dem SQL-Table)
	 * @param name
	 *            Name des Kurses
	 * @param vak
	 *            VAK des Kurses
	 * @param note
	 *            Hinweis zum Kurs
	 * @param cp
	 *            CP, die dieser Kurs bringt
	 * @param neccp
	 *            CP, die zur Anmeldung benötigt werden
	 * @param status
	 *            Der Status dieses Kurses (angemeldet usw.)
	 * @param semester
	 *            Das Semester, in dem dieser Kurs eingetragen wurde
	 * @param grade
	 *            Die Note, die für diesen Kurs erreicht wurde
	 * @param requiredCourse
	 *            Information, ob es sich um einen Pflichtkurs handelt
	 * @param duration
	 *            Die Dauer in Semestern dieses Kurses
	 * @param context
	 *            Kontext
	 */
	public Course(long id, String name, String vak, String note, double cp, int neccp, int status, int semester, double grade, int requiredCourse,
			int duration, int graded, double weight, Context context)
	{
		this.context = context;
		this.id = id;
		this.name = name;
		this.vak = vak;
		this.note = note;
		this.cp = cp;
		this.necCP = neccp;
		this.status = status;
		this.semester = semester;
		this.grade = grade;
		this.requiredCourse = requiredCourse;
		this.duration = duration;
		this.weight = weight;
		this.graded = graded;
	}

	/**
	 * Erstellt die Ansicht dieses Kurses.
	 */
	@Override
	public View getChildView(View convertView, final Context context, LayoutInflater layoutInflater)
	{

		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.child_row_subject, null);
			holder = new ViewHolder();
			holder.name = ((TextView) convertView.findViewById(R.id.subjectName));
			holder.grade = ((TextView) convertView.findViewById(R.id.subjectGrade));
			holder.duration = ((TextView) convertView.findViewById(R.id.subjectDuration));
			holder.status = ((ImageView) convertView.findViewById(R.id.subjectImage));
			holder.cp = ((TextView) convertView.findViewById(R.id.subjectCP));
			holder.hint = ((ImageView) convertView.findViewById(R.id.subjectHint));
			holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		if (subCourse)
		{
			holder.arrow.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.color.light_child_row_bg);

		} else
		{
			holder.arrow.setVisibility(View.GONE);

			convertView.setBackgroundResource(R.color.child_row_bg);
		}

		if (graded == 0 || (grade == 0 && status == Status.STATUS_PASSED))
			holder.grade.setText("unbenotet");
		else if (grade != 0)
			holder.grade.setText("Note: " + grade);
		else
			holder.grade.setText("Note: -");

		if (firstSemestercp == 0)
			holder.name.setText(name);
		else
			holder.name.setText(name + " - Teil 1 (" + Statics.convertCP(firstSemestercp) + " CP)");
		holder.name.setSelected(true);

		holder.cp.setText(Statics.convertCP(cp) + " CP");

		if (duration > 1)
		{
			holder.duration.setText("" + duration);
			holder.duration.setVisibility(View.VISIBLE);
		} else
			holder.duration.setVisibility(View.GONE);

		holder.status.setImageResource(Status.getIcon(status));

		if (note != null)
		{
			holder.hint.setVisibility(View.VISIBLE);

			holder.hint.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{

					Toast.makeText(context, note, Toast.LENGTH_LONG).show();

				}
			});
		} else
			holder.hint.setVisibility(View.INVISIBLE);

		return convertView;
	}

	private static class ViewHolder
	{
		TextView name;
		ImageView hint;
		TextView duration;
		ImageView status;
		TextView grade;
		TextView cp;
		ImageView arrow;
	}

	@Override
	public View getStudyCoursePlanChildView(View convertView, Context context, LayoutInflater layoutInflater)
	{
		convertView = getChildView(convertView, context, layoutInflater);
		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (alternative.length() == 0)
			holder.grade.setText("alt.: -");
		else
			holder.grade.setText("alt.: " + alternative);

		return convertView;
	}

	/**
	 * Gibt den Status dieses Kurses zurück (angemeldet etc.).
	 * 
	 * @return Status des Kurses
	 */
	@Override
	public int getStatus()
	{
		return status;
	}

	/**
	 * Gibt die Information zurück, dass es sich um ein Element vom Typ "Kurs"
	 * handelt.
	 * 
	 * @return Der Kurs-Typ als Integer
	 */
	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	public QuickActionGrid getQuickActionGrid(final DataHelper dataHelper, final Context context, int flag)
	{
		final QuickActionGrid mBar = new QuickActionGrid(context);

		if (status != Status.STATUS_PASSED && status != Status.STATUS_REQUIREMENTS_MISSING)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_passed, R.string.passed, Statics.ACTION_PASSED));

		if (status == Status.STATUS_SIGNED_UP)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_failed, R.string.failed, Statics.ACTION_FAILED));
		if (status != Status.STATUS_SIGNED_UP && status != Status.STATUS_PASSED && status != Status.STATUS_REQUIREMENTS_MISSING)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_sign_up, R.string.sign_in, Statics.ACTION_SIGN_UP));

		if (subCourse)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_delete_course, "Kurs löschen", Statics.ACTION_DELETE_COURSE));

		mBar.addQuickAction(new QuickAction(context, R.drawable.action_edit, R.string.edit, Statics.ACTION_EDIT));

		if (flag != Statics.FLAG_WITHOUT_DETAILS)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_details, R.string.details, Statics.ACTION_DETAILS));

		mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
		{

			@Override
			public void onQuickActionClicked(QuickActionWidget widget, int position)
			{

				switch (position)
				{
				case Statics.ACTION_PASSED:
					Intent intent = new Intent(context, PassedCourseDialog.class);
					intent.putExtra(Statics.ID, id);
					intent.putExtra(Statics.TYPE, Statics.TYPE_COURSE);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
					break;
				case Statics.ACTION_SIGN_UP:
					dataHelper.setCourseSignedUp(id, PreferenceHelper.getSemesterInt(context));
					break;
				case Statics.ACTION_FAILED:
					dataHelper.setCourseFailed(id);
					break;
				case Statics.ACTION_DETAILS:
					Intent intent2 = new Intent(context, Details.class);
					intent2.putExtra(Statics.ID, id);
					intent2.putExtra(Statics.TYPE, Statics.TYPE_COURSE);
					intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent2);
					break;
				case Statics.ACTION_EDIT:
					Intent intent3 = new Intent(context, CourseEdit.class);
					intent3.putExtra(Statics.ID, id);
					String semester = PreferenceHelper.getSemester(context);
					intent3.putExtra(CourseEdit.SEMESTER_KEY, Integer.parseInt(semester));
					intent3.putExtra(Statics.TYPE, Statics.TYPE_COURSE);
					intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent3);
					break;

				case Statics.ACTION_DELETE_COURSE:

					dataHelper.deleteCourseFromOptionalOrChoosable(id);

					break;
				}

			}
		});

		return mBar;
	}

	@Override
	public Intent getOnClickIntent(Context context)
	{
		Intent intent = new Intent(context, Details.class);
		intent.putExtra(Statics.ID, id);
		intent.putExtra(Statics.TYPE, Statics.TYPE_COURSE);
		return intent;
	}

	@Override
	public boolean countForCategory()
	{
		if (subCourse)
			return false;
		return true;
	}

}
