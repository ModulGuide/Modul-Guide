package de.eStudent.modulGuide.common;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.eStudent.modulGuide.R;

/**
 * Enthält alle Daten und die Darstellung eines Semesters.
 */
public class Semester
{
	/**
	 * Liste aller Kurse und Kriterien, die man in diesem Semester eingetragen
	 * hat
	 */
	public ArrayList<? extends ChildEntry> childs = new ArrayList<ChildEntry>();

	/** Zahl des Semesters */
	public int semester;

	/** Die Durchschnittsnote, die in diesem Semester erreicht wurde. */
	public double grade;

	/** Die CP, die in diesem Semester erreicht wurden. */
	public double cp = 0;

	/** Die CP, die eingetragen, aber nicht erhalten wurden. */
	public int outstandingCp = 0;

	/** Die CP, die in diesem Semester laut Musterstudienplan vorgesehen sind. */
	public int plannedCp;

	/**
	 * Erstellt ein neues Semester-Objekt.
	 * 
	 * @param childs
	 *            Die Kurse und Kriterien, die diesem Semester zugewiesen
	 *            wurden.
	 * @param semester
	 *            Die Zahl des Semesters
	 * @param grade
	 *            Die Durchschnittsnote dieses Semesters
	 * @param cp
	 *            Die CP, die in diesem Semester erreicht wurden
	 */
	public Semester(ArrayList<Course> childs, int semester, double grade, int cp)
	{
		this.semester = semester;
		this.grade = grade;
		this.cp = cp;
		this.childs = childs;
	}

	/**
	 * Erstellt ein neues Semester-Objekt.
	 * 
	 * @param semester
	 */
	public Semester(int semester)
	{
		this.semester = semester;
		childs = new ArrayList<Course>();
	}

	public View getStudyCoursePlanGroupView(boolean isExpanded, View convertView, Context context, LayoutInflater layoutInflater)
	{
		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.group_row_study_course_plan, null);

			holder = new ViewHolder();
			holder.semester = ((TextView) convertView.findViewById(R.id.name));
			holder.cp = ((TextView) convertView.findViewById(R.id.cp));
			holder.indicator = ((ImageView) convertView.findViewById(R.id.indicator));

			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		if (childs.isEmpty())
		{
			holder.indicator.setVisibility(View.INVISIBLE);
		} else if (isExpanded)
		{
			holder.indicator.setVisibility(View.VISIBLE);
			holder.indicator.setImageResource(R.drawable.group_indicator_arrrow_down);
		} else
		{
			holder.indicator.setImageResource(R.drawable.group_indicator_arrrow_right);
			holder.indicator.setVisibility(View.VISIBLE);

		}

		holder.semester.setText(semester + ". Semester");
		holder.cp.setText(Statics.convertCP(plannedCp) + " CP \nempfohlen");

		return convertView;

	}

	private static class ViewHolder
	{
		TextView semester;
		TextView cp;
		ImageView indicator;

	}

}
