package de.eStudent.modulGuide.common;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.eStudent.modulGuide.CourseChoiceDialog;
import de.eStudent.modulGuide.Details;
import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.database.DataHelper;

/**
 * Enthält alle Daten und die Darstellung eines Choosables 
 * (Wahlpflicht mit voreingetragenen Kursen).
 */
public class Choosable extends ChildEntry
{
	/** Der Typ als Integer */
	public static final int TYPE = 4;

	/** Die CP, die durch dieses Choosable erreicht werden. */
	public double cp;

	/** Die Kursbedingungen aller Kurse des Choosables */
	public String[] requirements;

	/** Gibt an, ob alle Kurse des Choosables benotet oder unbenotet sind */ 
	public int graded;

	/** Gibt an, welche Gewichtung alle Kurse dieses Choosables haben. */
	public double weight;

	/** Gibt an, wie lange Kurse dieses Choosables dauern. */
	public int duration;

	/** Gibt an, wie die CP aller Kurse dieses Choosables aufgeteilt sind, wenn mehrsemestrig */
	public double[] splitcp;

	/** Nötige CP, um sich für Kurse dieses Choosables anmelden zu können. */
	public int necCP;

	/** Die CP, die im ersten Semester dieses Choosables erreicht werden. */
	public double firstSemestercp;

	/**
	 * Die ID des Kurses, der in diesem Choosable gewählt wurde (aus dem
	 * SQL-Table)
	 */
	public long selectedCourseId;

	/** Der Kurs, der für dieses Choosable ausgewählt wurde. */
	public Course selectedCourse;

	public String reccomendedCourseName;
	public String reccomendedCourseVak;

	/**
	 * Erstellt ein neues Choosable-Objekt
	 * 
	 * @param id
	 *            Die im SQL-Table definierte ID
	 * @param name
	 *            Der Name
	 * @param note
	 *            Hinweis zur Wahl
	 * @param selectedCourseId
	 *            SQL-Table ID des Kurses, der für dieses Choosable gewählt
	 *            wurde
	 * @param cp
	 *            die CP, die man durch Kurse dieser Wahl bekommt
	 */
	public Choosable(long id, String name, String note, long selectedCourseId, double cp)
	{
		this.id = id;
		this.name = name;
		this.note = note;
		this.selectedCourseId = selectedCourseId;
		this.cp = cp;
	}

	/**
	 * Erstellt ein neues Choosable-Objekt ohne Parameterinformationen.
	 */
	public Choosable()
	{
	}

	public Choosable(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/**
	 * @uml.property name="subjects"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 *                     elementType="de.eStudent.modulGuide.common.Course"
	 */
	public ArrayList<Course> subjects = new ArrayList<Course>();

	/**
	 * Erstellt die Ansicht eines Choosable-Elements.
	 */
	@Override
	public View getChildView(View convertView, final Context context, LayoutInflater layoutInflater)
	{

		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.child_row_choosable, null);

			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.choosableName);
			holder.note = (ImageView) convertView.findViewById(R.id.choosableNote);
			holder.cp = (TextView) convertView.findViewById(R.id.choosableCP);
			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText("Wahl: " + name);

		if (note != null)
		{
			holder.note.setVisibility(View.VISIBLE);

			holder.note.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{

					Toast.makeText(context, note, Toast.LENGTH_LONG).show();

				}
			});
		} else
			holder.note.setVisibility(View.INVISIBLE);

		String srCp = Statics.convertCP(cp);
		if (selectedCourseId == 0)
		{

			holder.cp.setText("0/" + srCp + " CP");

		} else if (selectedCourse.status == Status.STATUS_PASSED)
		{
			holder.cp.setText(cp + "/" + srCp + " CP");
		} else
		{
			SpannableString str = new SpannableString("0+" + srCp + "/" + srCp + " CP");
			str.setSpan(new ForegroundColorSpan(Color.rgb(244, 63, 14)), 1, str.length() - (4 + srCp.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			holder.cp.setText(str);
		}

		return convertView;
	}

	@Override
	public View getStudyCoursePlanChildView(View convertView, final Context context, LayoutInflater layoutInflater)
	{
		ViewHolder2 holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.child_row_study_course_plan_optional, null);

			holder = new ViewHolder2();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.note = (ImageView) convertView.findViewById(R.id.note);
			holder.cp = (TextView) convertView.findViewById(R.id.cp);
			holder.alternative = (TextView) convertView.findViewById(R.id.alternative);
			holder.courseLayout = (LinearLayout) convertView.findViewById(R.id.courseLayout);
			holder.courseName = (TextView) convertView.findViewById(R.id.courseName);
			holder.courseVak = (TextView) convertView.findViewById(R.id.courseVak);

			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder2) convertView.getTag();
		}

		if (firstSemestercp == 0)
			holder.name.setText("Wahl: " + name);
		else
			holder.name.setText("Wahl: " + name + " (" + Statics.convertCP(firstSemestercp) + "CP)");

		String srCp = Statics.convertCP(cp);
		if (selectedCourseId == 0)
		{
			holder.cp.setText("0/" + srCp + " CP");

		} else if (selectedCourse.status == Status.STATUS_PASSED)
		{
			holder.cp.setText(cp + "/" + srCp + " CP");
		} else
		{
			SpannableString str = new SpannableString("0+" + srCp + "/" + srCp + " CP");
			str.setSpan(new ForegroundColorSpan(Color.rgb(244, 63, 14)), 1, str.length() - (4 + srCp.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			holder.cp.setText(str);
		}

		if (alternative.length() == 0)
			holder.alternative.setText("alt.: -");
		else
			holder.alternative.setText("alt.: " + alternative + " Sem.");

		if (reccomendedCourseName == null)
			holder.courseLayout.setVisibility(View.GONE);
		else
		{
			holder.courseLayout.setVisibility(View.VISIBLE);
			holder.courseName.setText(reccomendedCourseName);
			holder.courseName.setSelected(true);
			holder.courseVak.setText(reccomendedCourseVak);
		}

		if (note != null)
		{
			holder.note.setVisibility(View.VISIBLE);

			holder.note.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{

					Toast.makeText(context, note, Toast.LENGTH_LONG).show();

				}
			});
		} else
			holder.note.setVisibility(View.INVISIBLE);

		return convertView;

	}

	private static class ViewHolder
	{
		TextView name;
		ImageView note;
		TextView cp;
	}

	private static class ViewHolder2
	{
		TextView name;
		ImageView note;
		TextView cp;
		TextView alternative;

		LinearLayout courseLayout;
		TextView courseName;
		TextView courseVak;
	}

	/**
	 * Gibt den Status des ausgewühlten Kurses zurück
	 * 
	 * @return Status des ausgewählten Kurses, -1 wenn kein Kurs gewählt wurde
	 */
	@Override
	public int getStatus()
	{
		if (selectedCourseId == 0)
			return -1;
		else
			return selectedCourse.status;
	}

	/**
	 * Gibt den Typ des ausgewählten Kurses zurück.
	 * 
	 * @return Typ des Kurses
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

		if (selectedCourseId == 0)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_passed, "Kurs auswählen", Statics.ACTION_CHOOSE_COURSE));

		if (flag != Statics.FLAG_WITHOUT_DETAILS)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_details, R.string.details, Statics.ACTION_DETAILS));

		if (mBar.getQuickActionCount() == 0)
		{
			Toast.makeText(context, "Keine Aktionen möglich", Toast.LENGTH_LONG).show();
			return null;
		}

		mBar.setOnQuickActionClickListener(new OnQuickActionClickListener()
		{

			@Override
			public void onQuickActionClicked(QuickActionWidget widget, int position)
			{

				switch (position)
				{
				case Statics.ACTION_CHOOSE_COURSE:

					Intent intent = new Intent(context, CourseChoiceDialog.class);
					intent.putExtra(Statics.ID, id);
					intent.putExtra(Statics.TYPE, Statics.TYPE_CHOOSABLE);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);

					break;

				case Statics.ACTION_DETAILS:
					Intent intent2 = new Intent(context, Details.class);
					intent2.putExtra(Statics.ID, id);
					intent2.putExtra(Statics.TYPE, Statics.TYPE_CHOOSABLE);
					intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent2);
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
		intent.putExtra(Statics.TYPE, Statics.TYPE_CHOOSABLE);
		return intent;
	}

	@Override
	public boolean countForCategory()
	{
		return true;
	}

}
