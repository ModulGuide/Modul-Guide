package de.eStudent.modulGuide.common;

import android.database.Cursor;

/**
 * Enthält Hilfsmethoden, die im ganzen Projekt genutzt werden können.
 */
public class Statics
{

	/** Statischer "id"-String */
	public final static String ID = "id";
	
	/** Statischer "type"-String */ 
	public final static String TYPE = "type";
	
	/** Die zugehörige Nummer des Typs "Kurs" */
	public final static int TYPE_COURSE = 1;
	
	/** Die zugehörige Nummer des Typs "University Calendar Course" */
	public final static int TYPE_UNIVERSITY_CALENDAR_COURSE = 0;
	
	/** Die zugehörige Nummer des Typs "Criterion" */
	public final static int TYPE_CRITERION = 2;
	
	/** Die zugehörige Nummer des Typs "Choosable" */
	public final static int TYPE_CHOOSABLE = 3;
	
	/** Die zugehörige Nummer des Typs "Optional" */
	public final static int TYPE_OPTIONAL = 4;

	/** Die zugehörige Nummer Für die Aktion "Bestanden" */
	public final static int ACTION_PASSED = 1;
	
	/** Die zugehörige Nummer Für die Aktion "Durchgefallen" */
	public final static int ACTION_FAILED = 2;
	
	/** Die zugehörige Nummer Für die Aktion "Angemeldet" */
	public final static int ACTION_SIGN_UP = 3;
	
	/** Die zugehörige Nummer Für die Aktion "Bearbeiten" */
	public final static int ACTION_EDIT = 4;
	
	/** Die zugehörige Nummer Für die Aktion "Details anzeigen" */
	public final static int ACTION_DETAILS = 5;
	
	/** Die zugehörige Nummer Für die Aktion "Kurs wählen" */
	public final static int ACTION_CHOOSE_COURSE = 6;
	
	/** Die zugehörige Nummer Für die Aktion "Kurs löschen" */
	public final static int ACTION_DELETE_COURSE = 7;

	/** Die zugehörige Nummer zum Flag "ohne Details" */
	public final static int FLAG_WITHOUT_DETAILS = 1;

	/**
	 * Konvertiert CP in einen optisch angepassten String.
	 * @param cp die zu konvertierenden CP
	 * @return Formatierter String der CP
	 */
	public static String convertCP(double cp)
	{
		String cpstr = "" + cp;
		if (cp - (int) cp == 0)
			cpstr = cpstr.substring(0, cpstr.length() - 2);
		return cpstr;

	}

	/**
	 * Konvertiert Noten in einen optisch angepassten String.
	 * @param grade Die zu konvertierende Note
	 * @return Formatierter String der Note
	 */
	public static String convertGrade(double grade)
	{
		return "" + ((double) Math.round(grade * 100)) / 100;
	}

	/**
	 * Erstellt einen String für Vorschläge des Musterstudienplans mit Alternativen
	 * @param c Cursor der Vorschläge
	 * @return Formatierter String für die Vorschläge
	 */
	public static String calculateAlternative(Cursor c)
	{
		if (!c.moveToFirst())
		{
			c.close();
			return "-";
		}

		int count = c.getCount();
		int tmp = 1;
		String result = "";
		while (!c.isAfterLast())
		{
			if (tmp == count)
				result += c.getInt(0);
			else if (tmp == count - 1)
				result += c.getInt(0) + " o. ";
			else
				result += c.getInt(0) + " , ";

			c.moveToNext();
			tmp++;
		}
		c.close();

		return result;
	}

	/**
	 * Erstellt einen String für Choosable-Vorschläge des Musterstudienplans mit Alternativen
	 * @param c Cursor der Choosable-Vorschläge
	 * @return Formatierter String für die Choosable-Vorschläge
	 */
	public static String calculateAlternativeForChoosable(Cursor c, double cp)
	{
		if (!c.moveToFirst())
		{
			c.close();
			return "-";
		}

		int count = c.getCount();
		int tmp = 1;
		String result = convertCP(cp) + " CP in Semester(n): ";
		while (!c.isAfterLast())
		{
			if (tmp == count)
				result += c.getInt(0);
			else if (tmp == count - 1)
				result += c.getInt(0) + " o. ";
			else
				result += c.getInt(0) + " , ";

			c.moveToNext();
			tmp++;
		}
		c.close();

		return result;
	}

	/**
	 * Erstellt einen String für Optional-Vorschläge des Musterstudienplans mit Alternativen
	 * @param c Cursor der Optional-Vorschläge
	 * @return Formatierter String für die Optional-Vorschläge
	 */
	public static String calculateAlternativeForOptional(Cursor c)
	{
		if (!c.moveToFirst())
		{
			c.close();
			return "-";
		}

		int currentPart = -1;

		int count = c.getCount();

		int[] parts = new int[count];
		for (int i = 0; i < count; i++)
		{
			parts[i] = c.getInt(2);
			c.moveToNext();
		}

		c.moveToFirst();

		int tmp = 0;
		String result = "";
		while (!c.isAfterLast())
		{
			if (currentPart != c.getInt(2))
			{
				currentPart = c.getInt(2);

				if (result.length() != 0)
					result += "\n";

				result += convertCP(c.getDouble(1)) + " CP in Semester(n): ";
			}

			if (tmp == count - 1 || parts[tmp] != parts[tmp + 1])
				result += c.getInt(0);
			else if (tmp == count - 2 || parts[tmp] != parts[tmp + 2])
				result += c.getInt(0) + " o. ";
			else
				result += c.getInt(0) + " , ";

			c.moveToNext();
			tmp++;
		}
		c.close();

		return result;
	}

}
