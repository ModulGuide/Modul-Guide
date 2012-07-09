package de.eStudent.modulGuide.common;

import greendroid.widget.QuickActionGrid;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.database.DataHelper;

/**
 * Enthält alle Daten und die Darstellung von Objekten 
 * wie Kursfortsetzungen o.ä..
 */
public class Other extends ChildEntry
{
	/** Nummer des Typs "Other" */
	final static int TYPE = 2;

	/** CP, die dieses "Other" bringt */
	public double cp;

	/** Information, ob es einem Choosable oder Optional untergeordnet ist */
	public boolean isSub;

	/**
	 * Erstellt ein neues Other-Objekt, das keinem Choosable oder Optional untergeordnet ist.
	 */
	public Other()
	{
		isSub = false;
	}

	/** Der Elterneintrag, zu dem dieses Other gehört. */
	public ChildEntry parent;

	@Override
	public View getChildView(View convertView, Context context, LayoutInflater layoutInflater)
	{
		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.child_row_other, null);

			holder = new ViewHolder();
			holder.leftString = (TextView) convertView.findViewById(R.id.leftString);
			holder.rightString = (TextView) convertView.findViewById(R.id.rightString);
			holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		if (name != null)
			holder.leftString.setText(name);

		if (cp != 0)
		{
			holder.rightString.setVisibility(View.VISIBLE);
			holder.rightString.setText(Statics.convertCP(cp) + " CP");
		} else
			holder.rightString.setVisibility(View.GONE);

		if (isSub)
		{
			holder.arrow.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.color.light_child_row_bg);

		} else
		{
			holder.arrow.setVisibility(View.GONE);
			convertView.setBackgroundResource(R.color.child_row_bg);
		}

		return convertView;
	}

	private static class ViewHolder
	{
		TextView leftString;
		TextView rightString;
		ImageView arrow;

	}

	// wird beim Musterstudienplan nicht angezeigt mom.
	@Override
	public View getStudyCoursePlanChildView(View convertView, Context context, LayoutInflater layoutInflater)
	{
		return getChildView(convertView, context, layoutInflater);
	}

	@Override
	public int getStatus()
	{
		return 0;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	public QuickActionGrid getQuickActionGrid(DataHelper dataHelper, Context context, int flag)
	{
		if (parent != null)
			return parent.getQuickActionGrid(dataHelper, context, flag);

		return null;
	}

	@Override
	public Intent getOnClickIntent(Context context)
	{
		if (parent != null)
			return parent.getOnClickIntent(context);
		return null;
	}

	@Override
	public boolean countForCategory()
	{
		return false;
	}

}
