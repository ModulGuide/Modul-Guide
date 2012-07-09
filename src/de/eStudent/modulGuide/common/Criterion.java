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

/**
 * Enthält alle Daten und die Darstellung eines Kriteriums.
 */
public class Criterion extends ChildEntry
{
	/** Der Criterion-Typ als Integer */
	public static final int TYPE = 0;

	/** Die Note des Kriteriums. */
	public float grade;

	/** Die CP, die dieses Kriterium bringt. */
	public double cp;

	/** Die CP, die benötigt werden, um sich für dieses Kriterium anzunelden. */
	public int neccp;

	/** Der Status dieses Kriteriums */
	public int status;

	/** Das Semester, in dem man das Kriterium bestanden hat. */
	public int semester;

	/** Die Voraussetzungen, um dieses Kriterium angehen zu können. */
	public String[] requirements;

	/** Gibt an, ob das Kriterium benotet oder unbenotet ist */
	public int graded;

	/** Notengewichtung des Kriteriums */
	public double weight;

	/**
	 * Erstellt ein neues Kriterium.
	 * 
	 * @param name
	 *            Name des Kriteriums
	 */
	public Criterion(String name)
	{
		this.name = name;

	}

	/**
	 * Erstellt ein neues Kriterium.
	 * 
	 * @param id
	 *            ID des Kriteriums
	 * @param name
	 *            Name des Kriteriums
	 * @param grade
	 *            Note, die im Kriterium erreicht wurde
	 * @param note
	 *            Hinweis zu diesem Kriterium
	 * @param cp
	 *            CP, die dieses Kriterium bringt
	 * @param neccp
	 *            CP, die für dieses Kriterium benötigt werden
	 * @param status
	 *            Status, den dieses Kriterium hat
	 * @param semester
	 *            Semester, in dem dieses Kriterium bestanden wurde
	 */
	public Criterion(long id, String name, float grade, String note, int cp, int neccp, int status, int semester, int graded, double weight)
	{
		this.id = id;
		this.name = name;
		this.grade = grade;
		this.note = note;
		this.cp = cp;
		this.neccp = neccp;
		this.status = status;
		this.semester = semester;
		this.weight = weight;
		this.graded = graded;

	}

	/**
	 * Erstellt die Ansicht dieses Kriteriums.
	 */
	@Override
	public View getChildView(View convertView, final Context context, LayoutInflater layoutInflater)
	{
		if (graded != 0)
		{

			convertView = layoutInflater.inflate(R.layout.child_row_subject, null);

			convertView.setBackgroundResource(R.color.child_row_bg);

			TextView subjectName = ((TextView) convertView.findViewById(R.id.subjectName));
			subjectName.setText(name);
			subjectName.setSelected(true);

			ImageView statusImage = (ImageView) convertView.findViewById(R.id.subjectImage);
			statusImage.setImageResource(Status.getIcon(status));

			TextView gradeView = ((TextView) convertView.findViewById(R.id.subjectGrade));
			if (grade != 0)
				gradeView.setText("" + grade);

			TextView subjectCP = ((TextView) convertView.findViewById(R.id.subjectCP));
			subjectCP.setText(cp + " CP");

			ImageView imageHint = ((ImageView) convertView.findViewById(R.id.subjectHint));

			if (note != null)
			{
				imageHint.setVisibility(View.VISIBLE);

				imageHint.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{

						Toast.makeText(context, note, Toast.LENGTH_LONG).show();

					}
				});
			} else
				imageHint.setVisibility(View.INVISIBLE);

		} else
		{
			convertView = layoutInflater.inflate(R.layout.child_row_checkentry, null);

			TextView subjectName = ((TextView) convertView.findViewById(R.id.checkEntryName));
			subjectName.setText(name);
			subjectName.setSelected(true);

			ImageView statusImage = (ImageView) convertView.findViewById(R.id.criterion_Image);
			statusImage.setImageResource(Status.getIcon(status));

			ImageView imageHint = ((ImageView) convertView.findViewById(R.id.criterionNote));

			if (note != null)
			{
				imageHint.setVisibility(View.VISIBLE);

				imageHint.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{

						Toast.makeText(context, note, Toast.LENGTH_LONG).show();

					}
				});
			} else
				imageHint.setVisibility(View.INVISIBLE);

		}

		return convertView;

	}

	@Override
	public View getStudyCoursePlanChildView(View convertView, final Context context, LayoutInflater layoutInflater)
	{
		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.child_row_subject, null);
			holder = new ViewHolder();
			holder.name = ((TextView) convertView.findViewById(R.id.subjectName));
			holder.alternative = ((TextView) convertView.findViewById(R.id.subjectGrade));
			holder.status = ((ImageView) convertView.findViewById(R.id.subjectImage));
			holder.cp = ((TextView) convertView.findViewById(R.id.subjectCP));
			holder.hint = ((ImageView) convertView.findViewById(R.id.subjectHint));
			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText(name);
		holder.name.setSelected(true);
		if (cp == 0)
			holder.cp.setText("Keine CP");
		else
			holder.cp.setText(Statics.convertCP(cp) + " CP");

		holder.status.setImageResource(Status.getIcon(status));

		if (alternative.length() == 0)
			holder.alternative.setText("alt.: -");
		else
			holder.alternative.setText("alt.: " + alternative);

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
		ImageView status;
		ImageView hint;
		TextView cp;
		TextView alternative;

	}

	/**
	 * Gibt den Status des Kriteriums zurück
	 * 
	 * @return Status des Kriteriums
	 */
	@Override
	public int getStatus()
	{
		return status;
	}

	/**
	 * Gibt den Kriterium-Typ zurück.
	 * 
	 * @return der Kriterium-Typ
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
					intent.putExtra(Statics.TYPE, Statics.TYPE_CRITERION);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);

					break;
				case Statics.ACTION_DETAILS:
					Intent intent2 = new Intent(context, Details.class);
					intent2.putExtra(Statics.ID, id);
					intent2.putExtra(Statics.TYPE, Statics.TYPE_CRITERION);
					intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent2);
					break;

				case Statics.ACTION_EDIT:
					Intent intent3 = new Intent(context, CourseEdit.class);
					intent3.putExtra(Statics.ID, id);
					intent3.putExtra(Statics.TYPE, Statics.TYPE_CRITERION);
					intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent3);
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
		intent.putExtra(Statics.TYPE, Statics.TYPE_CRITERION);
		return intent;
	}

	@Override
	public boolean countForCategory()
	{
		return true;
	}

}
