package de.eStudent.modulGuide.common;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
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
 * Enthält alle Daten und die Darstellung eines Optionals 
 * (Wahlpflicht über das Vorlesungsverzeichnis).
 */
public class Optional extends ChildEntry

{

	/** Der Optional-Typ als Integer. */
	public static final int TYPE = 3;

	/** Die CP, die in diesem Optional erreicht werden müssen. */
	public double cp;

	/** Die CP, die in diesem Optional bereits erreicht wurden. */
	public double acquiredcp;

	/** Die Gesamt-CP aller Kurse, die für dieses Optional gewählt wurden. */
	public double chosencp;

	/** Beschreibt, ab wieviel CP dieses Optional startet (z.B. bei 6 von 18) */
	public double cpRange;

	/** VAK-Kriterium, das dieses Optional stellt */
	public String vak;

	/** VAK des Kurses, der für dieses Optional vorgeschlagen wird */
	public String coursevak;

	/** Name des Kurses, der für dieses Optional vorgeschlagen wird */
	public String coursename;

	/** Gewichtung aller Kurse, die in diesem Optional hinzugefügt wurden. */
	public double weight;

	/** Die Kurse, die Voraussetzung für alle gewählten Kurse dieses Optionals sind */
	public String[] requirements;

	/** Die benötigten CP, um sich für alle gewählten Kurse dieses Optionals anmelden zu können */
	public int necCp;

	/** Beschreibt, ob Kurse in diesem Optional benotet sind. */ 
	public int graded;

	/** Sagt aus, ob dieses Optional CP-Überhänge zugeschrieben bekommt. */
	public boolean transmitcp;

	/** Beschreibt, welcher Teil eines gesamten Optionals dieser ist. */
	public int part;

	/** Die Dauer aller gewählten Kurse dieses Optionals */
	public int duration;

	/** Beschreibt, wie die CP dieses Optionals aufgeteilt sind */
	public double[] splitcp;

	/** CP des ersten Semesters, in dem das Optional vorgeschlagen wurde. */
	public double firstSemestercp;

	/**
	 * Erstellt ein neues Optional-Objekt.
	 * 
	 * @param name
	 *            Der Name des Optionals
	 */
	public Optional(String name)
	{
		this.name = name;
	}

	/**
	 * Erstellt ein neues Optional-Objekt.
	 * 
	 * @param id
	 *            ID des Optionals (aus dem SQL-Table)
	 * @param name
	 *            Der Name des Optionals
	 * @param vak
	 *            Die VAK-Vorgaben, die dieses Optional hat
	 * @param cp
	 *            Die CP, die in diesem Optional erreicht werden müssen
	 * @param note
	 *            Der Hinweis zu diesem Optional
	 * @param acquiredcp
	 *            Die erreichten CP
	 * @param chosencp
	 *            die CP aller Kurse, die für dieses Optional gewählt wurden
	 */
	public Optional(long id, String name, String vak, double cp, String note, double acquiredcp, double chosencp, double weight, int necCp)
	{
		this.id = id;
		this.name = name;
		this.vak = vak;
		this.cp = cp;
		this.note = note;
		this.acquiredcp = acquiredcp;
		this.weight = weight;
		this.necCp = necCp;

		chosencp = chosencp - acquiredcp;
		this.chosencp = chosencp;

	}

	/**
	 * Erstellt ein neues Optional-Objekt
	 * 
	 * @param id
	 *            ID des Optionals
	 * @param name
	 *            Name des Optionals
	 */
	public Optional(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/**
	 * Erstellt die Ansicht eines Optionals
	 */
	@Override
	public View getChildView(View convertView, final Context context, LayoutInflater layoutInflater)
	{

		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.child_row_optional, null);

			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.optionalName);
			holder.note = (ImageView) convertView.findViewById(R.id.optionalNote);
			holder.cp = (TextView) convertView.findViewById(R.id.optionalCP);
			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText("Wahl: " + name);

		String strCp = "" + Statics.convertCP(cp);
		String strAquiredCp = "" + Statics.convertCP(acquiredcp);
		String strChosenCp = "" + Statics.convertCP(chosencp);

		if (chosencp == 0)
		{
			holder.cp.setText(strAquiredCp + "/" + strCp + " CP");
		} else
		{
			SpannableString str = new SpannableString(strAquiredCp + "+" + strChosenCp + "/" + strCp + " CP");
			str.setSpan(new ForegroundColorSpan(Color.rgb(244, 63, 14)), strAquiredCp.length(), str.length() - (4 + strCp.length()),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			holder.cp.setText(str);
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

		holder.name.setSelected(true);

		String strCp = "" + Statics.convertCP(cp - cpRange < 0 ? 0 : cp - cpRange);
		String strAquiredCp = "" + Statics.convertCP(acquiredcp - cpRange < 0 ? 0 : acquiredcp - cpRange);
		String strChosenCp = "" + Statics.convertCP(chosencp - cpRange < 0 ? 0 : chosencp - cpRange);

		if (chosencp - cpRange <= 0)
		{
			holder.cp.setText(strAquiredCp + "/" + strCp + " CP");
		} else
		{
			SpannableString str = new SpannableString(strAquiredCp + "+" + strChosenCp + "/" + strCp + " CP");
			str.setSpan(new ForegroundColorSpan(Color.rgb(244, 63, 14)), strAquiredCp.length(), str.length() - (4 + strCp.length()),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			holder.cp.setText(str);
		}

		if (alternative.length() == 0)
			holder.alternative.setText("alt.: -");
		else
			holder.alternative.setText("alt.: " + alternative + " Sem.");

		if (coursename == null)
			holder.courseLayout.setVisibility(View.GONE);
		else
		{
			holder.courseLayout.setVisibility(View.VISIBLE);
			holder.courseName.setText(coursename);
			holder.courseName.setSelected(true);
			holder.courseVak.setText(coursevak);
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
	 * Gibt den Status des Optionals zurück.
	 * 
	 * @return Status des Optionals
	 */
	@Override
	public int getStatus()
	{
		if (acquiredcp - cpRange >= cp)
			return Status.STATUS_PASSED;
		else if (chosencp - cpRange >= cp)
			return Status.STATUS_SIGNED_UP;

		return Status.STATUS_NOT_PASSED;
	}

	/**
	 * Gibt den Optional-Typ zurück.
	 * 
	 * @return Optional-Typ
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

		if ((acquiredcp + chosencp) < cp)
			mBar.addQuickAction(new QuickAction(context, R.drawable.action_course_choose, "Kurs auswählen", Statics.ACTION_CHOOSE_COURSE));

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
					intent.putExtra(Statics.TYPE, Statics.TYPE_OPTIONAL);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);

					break;
				case Statics.ACTION_DETAILS:
					Intent intent2 = new Intent(context, Details.class);
					intent2.putExtra(Statics.ID, id);
					intent2.putExtra(Statics.TYPE, Statics.TYPE_OPTIONAL);
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
		intent.putExtra(Statics.TYPE, Statics.TYPE_OPTIONAL);
		return intent;
	}

	@Override
	public boolean countForCategory()
	{
		return true;
	}

}
