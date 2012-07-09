package de.eStudent.modulGuide.common;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.eStudent.modulGuide.R;

/**
 * Enthält alle Daten und die Darstellung einer Kategorie.
 */
public class Category extends GroupEntry
{
	/** Die ID der Kategorie (aus dem SQL-Table) */
	public long id;

	/** Der Name der Kategorie */
	public String name;

	/** Die Anzahl der bestandenen Kurse, Optionals etc. */
	public int passedItems = 0;

	/** Die Anzahl der Kurse, Optionals etc. */
	public int items = 0;
	/** Die ID des Studienganges, zu dem diese Kategorie gehört. */
	public long courseOfStudiesId;

	/** Liste der Semester */
	public ArrayList<Semester> semester;

	/**
	 * Erstellt eine neue Kategorie.
	 * 
	 * @param name
	 *            Der Name der Kategorie
	 */
	public Category(String name)
	{
		this.name = name;
	}

	/**
	 * Erstellt eine neue Kategorie
	 * 
	 * @param id
	 *            die im SQL-Table definierte ID der Kategorie
	 * @param name
	 *            Der Name der Kategorie
	 */
	public Category(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/**
	 * Erstellt die Ansicht einer Kategorie
	 */
	@Override
	public View getGroupView(boolean isExpanded, View convertView, Context context, LayoutInflater layoutInflater)
	{

		ViewHolder holder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.group_row, null);

			holder = new ViewHolder();
			holder.name = ((TextView) convertView.findViewById(R.id.categoryName));
			holder.indicator = (ImageView) convertView.findViewById(R.id.category_indicator);
			holder.itemCount = ((TextView) convertView.findViewById(R.id.categoryItemCount));
			holder.checkImage = (ImageView) convertView.findViewById(R.id.categoryImage);

			convertView.setTag(holder);

		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setSelected(true);
		holder.name.setText(name);

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

		LayoutParams params = (RelativeLayout.LayoutParams) holder.name.getLayoutParams();
		if (items == passedItems)
		{
			holder.itemCount.setVisibility(View.GONE);
			holder.checkImage.setVisibility(View.VISIBLE);

			params.addRule(RelativeLayout.LEFT_OF, R.id.categoryImage);
			holder.name.setLayoutParams(params);
		} else
		{
			holder.checkImage.setVisibility(View.GONE);
			holder.itemCount.setVisibility(View.VISIBLE);

			params.addRule(RelativeLayout.LEFT_OF, R.id.categoryItemCount);
			holder.name.setLayoutParams(params);

			holder.itemCount.setText(passedItems + "/" + items);
		}

		return convertView;
	}

	private static class ViewHolder
	{
		TextView name;
		ImageView indicator;
		TextView itemCount;
		ImageView checkImage;
	}
}
