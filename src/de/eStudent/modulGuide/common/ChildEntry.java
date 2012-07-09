package de.eStudent.modulGuide.common;

import greendroid.widget.QuickActionGrid;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import de.eStudent.modulGuide.database.DataHelper;

/**
 * Enthält alle Daten und abstrakten Funktionen, die ein Großteil 
 * der Datenklassen gemeinsam haben (name, id etc.) 
 * - wird u.a. von Course, Criterion usw. benutzt.
 */
public abstract class ChildEntry
{
	/** Der Name des Kurses, Optionals etc. */
	public String name;

	/** Ein Hinweis zum Kurs, Optional etc. */
	public String note;

	/** Die ID des Kurses, Optionals etc. (aus dem SQL-Table) */
	public long id;

	/** Die Kategorie, zu der der Kurs, Optional etc. gehört */
	public long category;

	/** Alternativen */
	public String alternative;

	public abstract View getChildView(View convertView, Context context, LayoutInflater layoutInflater);

	public abstract View getStudyCoursePlanChildView(View convertView, Context context, LayoutInflater layoutInflater);

	public abstract int getStatus();

	public abstract int getType();

	public abstract QuickActionGrid getQuickActionGrid(final DataHelper dataHelper, final Context context, int flag);

	public abstract Intent getOnClickIntent(final Context context);

	/** Zählt die Elemente für die Kategorieanzeige */
	public abstract boolean countForCategory();

}
