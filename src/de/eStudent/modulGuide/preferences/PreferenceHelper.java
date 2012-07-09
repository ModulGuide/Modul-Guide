package de.eStudent.modulGuide.preferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class PreferenceHelper
{

	/**
	 * Die Keys der verschiedenen in den Preferences gespeicherten Daten.
	 */
	public static final String semesterKey = "editTextPref";
	public static final String regulationKey = "REGULATION_NAME";
	public static final String EXAMPLE_COURSE_SCHEME = "example_course_scheme";
	public static final String EXAMPLE_COURSE_SCHEME_TIMESTAMP = "example_course_scheme_timestamp";
	public static final String UNIVERSITY_CALENDAR_TIME_STAMP = "universityCalendarTimeStamp";
	public static final String UNIVERSITY_CALENDAR_FAVOURITE = "universityCalendarFavourite";
	public static final String EXAMINATION_REGULATION_TIME_STAMP = "examinatioRegulationTimeStamp";
	public static final String UNIVERSITY_CALENDAR_COMPLETE = "universityCalendarComplete";
	public static final String STUDY_COMPLETE = "complete";
	public static final String VERSION = "version";

	/**
	 * Gibt die aktuelle Version der Anwendung zurück
	 * 
	 * @param context
	 * @return die Version
	 */
	public static int getVersion(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getInt(VERSION, -1);
	}

	/**
	 * Speichert die aktuelle Version der Anwendung in den Preferences
	 * 
	 * @param context
	 */
	public static void saveCurrentVersion(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		try
		{
			editor.putInt(VERSION, context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
			editor.commit();

		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Gibt zurück, ob alle Erfordernisse für das Studium erfüllt wurden
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isStudyComplete(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getBoolean(STUDY_COMPLETE, false);
	}

	/**
	 * Speichert ob alle Erfordernisse für das Studium erfüllt sind in den
	 * Preferences
	 * 
	 * @param context
	 * @param status
	 */
	public static void setStudyComplete(Context context, boolean status)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		if (isStudyComplete(context) != status)
		{
			Editor editor = settings.edit();
			editor.putBoolean(STUDY_COMPLETE, status);
			editor.commit();
		}
	}

	/**
	 * Gibt zurück, ob das ganze Vorlesungsverzeichnis runtergeladen wurde
	 * 
	 * @param context
	 * @return true / false
	 */
	public static boolean isUniversityCalendarComplete(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getBoolean(UNIVERSITY_CALENDAR_COMPLETE, false);
	}

	/**
	 * Speichert, ob das komplette Vorlesungsverzeichnis runtergealden wurde
	 * 
	 * @param context
	 * @param status
	 *            Der zu speichernde Status
	 */
	public static void setUniversityCalendarComplete(Context context, boolean status)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		Editor editor = settings.edit();
		editor.putBoolean(UNIVERSITY_CALENDAR_COMPLETE, status);
		editor.commit();
	}

	/**
	 * Gibt das aktuelle Semester zurück
	 * 
	 * @param context
	 * @return Das Semester
	 */
	public static String getSemester(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getString(semesterKey, "1");
	}

	/**
	 * Gibt das akteulle Semester zurück
	 * 
	 * @param context
	 * @return Das Semester
	 */
	public static int getSemesterInt(Context context)
	{
		return Integer.parseInt(getSemester(context));
	}

	/**
	 * Erhöht das in den Preferences gespeicherte aktuelle Semester um eins.
	 * 
	 * @param context
	 */
	public static void incrementSemester(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		Editor editor = settings.edit();
		editor.putString(semesterKey, "" + ((getSemesterInt(context)) + 1));
		editor.commit();
	}

	/**
	 * Speichert den Namen der aktuellen Prüfungsordnung
	 * 
	 * @param name
	 *            Der Name der Prüfungsordnung
	 * @param context
	 */
	public static void saveRegulationName(String name, Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.putString(regulationKey, name);
		editor.commit();
	}

	/**
	 * Gibt den Namen der aktuellen Prüfungsordnung zurück
	 * 
	 * @param context
	 * @return Der NAme der Prüfungsordnung
	 */
	public static String getRegulationName(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getString(regulationKey, "");
	}

	/**
	 * Resetet die kompletten Preferecnes
	 * 
	 * @param context
	 */
	public static void resetAll(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.clear();
		// editor.putBoolean(UNIVERSITY_CALENDAR_COMPLETE, false);
		editor.commit();

	}

	/**
	 * Speichert den Timestamp für das Vorlesungsverzeichnis
	 * 
	 * @param context
	 * @param time
	 *            Der zu speichernde Zeitpunkt
	 */
	public static void saveUniversityCalendarSubjectsTimeStamp(Context context, long time)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.putLong(UNIVERSITY_CALENDAR_TIME_STAMP, time);
		editor.commit();
	}

	/**
	 * Gibt den in den Preferences, für das Vorlesungsverzichnis, gespeicherten
	 * Timestamp zurück.
	 * 
	 * @param context
	 * @return Der Timestamp.
	 */
	public static long getUniversityCalendarSubjectsTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getLong(UNIVERSITY_CALENDAR_TIME_STAMP, 0);
	}

	/**
	 * Gibt den in den Preferences, für das Vorlesungsverzichnis, gespeicherten
	 * Timestamp als String formatiert zurück.
	 * 
	 * @param context
	 * @return Der Timestamp.
	 */
	public static String getFormattedUniversityCalendarSubjectsTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		long time = settings.getLong(UNIVERSITY_CALENDAR_TIME_STAMP, 0);

		if (time == 0)
			return "-";
		else
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return df.format(time);
		}
	}

	/**
	 * Speichert den Timestamp für die Prüfungsordnungen
	 * 
	 * @param context
	 * @param time
	 *            Der zu speichernde Zeitpunkt
	 */
	public static void saveExaminationRegulationTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();

		long time = Calendar.getInstance().getTimeInMillis();
		editor.putLong(EXAMINATION_REGULATION_TIME_STAMP, time);
		editor.commit();
	}

	/**
	 * Gibt den in den Preferences, für die Prüfungsordnungen, gespeicherten
	 * Timestamp zurück.
	 * 
	 * @param context
	 * @return Der Timestamp.
	 */
	public static long getExaminationRegulationTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getLong(EXAMINATION_REGULATION_TIME_STAMP, 0);
	}

	/**
	 * Gibt den in den Preferences, für die Prüfungsordnungen, gespeicherten
	 * Timestamp, als String formatiert, zurück.
	 * 
	 * @param context
	 * @return Der Timestamp.
	 */
	public static String getFormattedExaminationRegulationTimeStamp(Context context)
	{
		long time = getExaminationRegulationTimeStamp(context);

		if (time == 0)
			return "-";
		else
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return df.format(time);
		}
	}

	/**
	 * Speichert die Id des Favourisierten Faches des Vorelsungsverzeichnisses
	 * 
	 * @param id
	 *            Die ID
	 * @param context
	 */
	public static void setUniversityCalendarFavourite(long id, Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.putLong(UNIVERSITY_CALENDAR_FAVOURITE, id);
		editor.commit();
	}

	/**
	 * Gibt die ID des Favouriten Fach zurück
	 * 
	 * @param context
	 * @return Die ID
	 */
	public static long getUniversityCalendarFavourite(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getLong(UNIVERSITY_CALENDAR_FAVOURITE, 0);
	}

	/**
	 * Gibt zurück, ob beim öffnen des Vorelsungsverzeichnisses direkt das
	 * Favourisierte Fach geöffnet werden soll, oder erst die Fächerübersicht.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean getUniversityCalendarFavouriteStart(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getBoolean("university_calendar_favourite", true);
	}

	/**
	 * Speichert den Namen des ausgewählten Musterstudienplanes
	 * 
	 * @param name
	 *            Der Name
	 * @param context
	 */
	public static void saveExampleCourseSchemeName(String name, Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.putString(EXAMPLE_COURSE_SCHEME, name);
		editor.commit();
	}

	/**
	 * Gibt den Namen des ausgewälten Musterstudeinplanes zurück
	 * 
	 * @param context
	 * @return Der Name
	 */
	public static String getExampleCourseSchemeName(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getString(EXAMPLE_COURSE_SCHEME, "");
	}

	/**
	 * Spe3ichert den aktuellen Zeitpunkt in den Preferences für den Timestamp
	 * der Musterstudeinpläne
	 * 
	 * @param context
	 */
	public static void saveExampleCourseSchemeTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();

		long time = Calendar.getInstance().getTimeInMillis();
		editor.putLong(EXAMPLE_COURSE_SCHEME_TIMESTAMP, time);
		editor.commit();
	}

	/**
	 * Resetet den in den Preferences. für die Musterstudeinpläne, gespeicherten
	 * Timestamp
	 * 
	 * @param context
	 */
	public static void resetExampleCourseSchemeTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();

		editor.putLong(EXAMPLE_COURSE_SCHEME_TIMESTAMP, 0);
		editor.commit();
	}

	/**
	 * Gibt den in den Preferences, für die Musterstudienpläne, gespeicherten
	 * Timestamp zurück.
	 * 
	 * @param context
	 * @return Der Timestamp.
	 */
	public static long getExampleCourseSchemeTimeStamp(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getLong(EXAMPLE_COURSE_SCHEME_TIMESTAMP, 0);
	}

	/**
	 * Gibt den in den Preferences, für die Musterstudienpläne, gespeicherten
	 * Timestamp, als String formatiert zurück.
	 * 
	 * @param context
	 * @return Der Timestamp.
	 */
	public static String getFormattedExampleCourseSchemeTimeStamp(Context context)
	{
		long time = getExampleCourseSchemeTimeStamp(context);

		if (time == 0)
			return "-";
		else
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return df.format(time);
		}
	}

}
