package de.eStudent.modulGuide.common;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Enthält Daten, die übergeordnete Klassen wie Kategorien benötigen 
 * (bisher lediglich Kindobjekte).
 */
public abstract class GroupEntry
{

	public ArrayList<? extends ChildEntry> childs = new ArrayList<ChildEntry>();

	public abstract View getGroupView(boolean isExpanded, View convertView, Context context, LayoutInflater layoutInflater);

}
