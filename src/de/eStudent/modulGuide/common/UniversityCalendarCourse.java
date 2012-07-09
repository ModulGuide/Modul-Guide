package de.eStudent.modulGuide.common;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.content.Context;
import android.content.Intent;
import de.eStudent.modulGuide.CourseEdit;
import de.eStudent.modulGuide.CourseSignUp;
import de.eStudent.modulGuide.Details;
import de.eStudent.modulGuide.PassedCourseDialog;
import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Enthält alle Daten und die Darstellung eines 
 * Kurses aus dem Vorlesungsverzeichnis. 
 */
public class UniversityCalendarCourse extends Course
{

	/** Dozenten usw., die den Kurs leiten */
	public String staff;

	/** Beschreibung des Kurses */
	public String description;

	/** Die Veranstaltungszeiten */
	public String date;

	/** Das Fach, zu dem dieser Kurs gehört */
	public long courseOfStudies;

	public long courseId;

	/**
	 * Erstellt ein neues UniversityCalendarCourse-Objekt
	 * 
	 * @param id
	 *            ID des UC-Kurses
	 * @param name
	 *            Name des UC-Kurses
	 * @param vak
	 *            Die VAK des UC-Kurses
	 * @param cp
	 *            Die CP, die dieser UC-Kurs bringt
	 * @param date
	 *            Die Zeiten, in denen Veranstaltungen stattfinden
	 * @param staff
	 *            Die Dozenten usw., die den Kurs leiten
	 * @param description
	 *            Die Beschreibung des Kurses
	 * @param courseOfStudiesId
	 *            Das Fach, zu dem dieser Kurs gehört
	 */
	public UniversityCalendarCourse(long id, String name, String vak, double cp, String date, String staff, String description, long courseOfStudiesId)
	{
		this.id = id;
		this.name = name;
		this.vak = vak;
		this.cp = cp;
		this.date = date;
		this.staff = staff;
		this.description = description;
		this.courseOfStudies = courseOfStudiesId;
	}

	/**
	 * Erstellt ein neues UniversityCalendarCourse-Objekt.
	 * @param id ID des UC-Kurses
	 * @param name Name des UC-Kurses
	 * @param vak VAK des UC-Kurses
	 * @param note Anmerkungen
	 * @param cp CP des UC-Kurses
	 * @param neccp Nötige CP, um sich für diesen Kurs anzumelden
	 * @param status Status des UC-Kurses
	 * @param semester Semester, in dem dieser UC-Kurs eingetragen ist
	 * @param grade Note des UC-Kurses
	 * @param requiredCourse Kursvoraussetzungen des UC-Kurses
	 * @param duration Dauer des UC-Kurses
	 * @param graded Gibt an, ob der UC-Kurs benotet/unbenotet ist
	 */
	public UniversityCalendarCourse(long id, String name, String vak, String note, double cp, int neccp, int status, int semester, double grade,
			int requiredCourse, int duration, int graded)
	{
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
		this.graded = graded;
	}

	/**
	 * Ein neues UC-Kursobjekt erstellen.
	 */
	public UniversityCalendarCourse()
	{
	}

	@Override
	public QuickActionGrid getQuickActionGrid(final DataHelper dataHelper, final Context context, int flag)
	{

		if (courseId != 0)
			return super.getQuickActionGrid(dataHelper, context, flag);

		final QuickActionGrid mBar = new QuickActionGrid(context);

		if (status != Status.STATUS_PASSED && status != Status.STATUS_REQUIREMENTS_MISSING)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_passed, R.string.passed, Statics.ACTION_PASSED));

		if (status == Status.STATUS_SIGNED_UP)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_failed, R.string.failed, Statics.ACTION_FAILED));
		if (status != Status.STATUS_SIGNED_UP && status != Status.STATUS_PASSED && status != Status.STATUS_REQUIREMENTS_MISSING)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_sign_up, R.string.sign_in, Statics.ACTION_SIGN_UP));

		if (subCourse)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_delete_course, "Kurs löschen", Statics.ACTION_DELETE_COURSE));

		if(courseId !=0)
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
					intent.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
					break;
				case Statics.ACTION_SIGN_UP:
					requiredCourse = dataHelper.isRequiredCourse(id);
					if (requiredCourse != 1 && !dataHelper.isChoosableOrOptionalUniversityCalendarCourse(id))
					{
						Intent intent4 = new Intent(context, CourseSignUp.class);
						intent4.putExtra(Statics.ID, id);
						String semester = PreferenceHelper.getSemester(context);
						intent4.putExtra(CourseEdit.SEMESTER_KEY, Integer.parseInt(semester));
						intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent4);
					} else
					{
						ChildEntry entry = null;
						dataHelper.setUniversityCalendarCourseSignedUp(id, Integer.parseInt(PreferenceHelper.getSemester(context)), entry);
					}
					break;
				case Statics.ACTION_FAILED:
					dataHelper.setCourseFailed(courseId);
					break;
				case Statics.ACTION_DETAILS:
					Intent intent2 = new Intent(context, Details.class);
					intent2.putExtra(Statics.ID, id);
					intent2.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
					intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent2);
					break;
				case Statics.ACTION_EDIT:
					Intent intent3 = new Intent(context, CourseEdit.class);
					intent3.putExtra(Statics.ID, id);
					String semester = PreferenceHelper.getSemester(context);
					intent3.putExtra(CourseEdit.SEMESTER_KEY, Integer.parseInt(semester));
					intent3.putExtra(Statics.TYPE, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE);
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

}
