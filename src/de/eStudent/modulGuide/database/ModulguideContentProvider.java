package de.eStudent.modulGuide.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.common.Status;


/**
 * Contentprovider für unsere Datenbank, der von dem SearchInterface verwendet wird
 *
 */
public class ModulguideContentProvider extends ContentProvider
{
	//Name dieses ContentProviders
	public static final String PROVIDER_NAME = "de.eStudent.modulGuide.provider";

	//datenbank
	private SQLiteDatabase db;

	//unused
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	//unused
	@Override
	public String getType(Uri uri)
	{
		return null;
	}

	
	//unused
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	//unused
	@Override
	public boolean onCreate()
	{
		return true;
	}

	
	/**
	 * Gibt je nach Suchanfrage den entsprechenden Cursor mit den Suchergebnissen zurück
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{

		String path = uri.getEncodedPath();
		
		//Suche innerhalb der Musterstudienpläne
		if (path.equals("/regulationList/search_suggest_query"))
		{
			db = getContext().openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READONLY, null);
			Cursor c = db.rawQuery("SELECT _id AS "+BaseColumns._ID+", name AS "+SearchManager.SUGGEST_COLUMN_TEXT_1 +", date AS "+SearchManager.SUGGEST_COLUMN_TEXT_2+", _id AS "+BaseColumns._ID+" FROM RegulationBuffer  WHERE name LIKE \"%" + selectionArgs[0] + "%\" ORDER BY Upper(name)  COLLATE LOCALIZED ",
					null);

			return c;
			
		//Suche innerhalb der Fächer des Vorlesungsverzeichnisses
		} else if (path.equals("/universityCalendarSubjects/search_suggest_query"))
		{
			db = getContext().openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READONLY, null);
			Cursor c = db.rawQuery("SELECT _id, name AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ", _id AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
					+ " FROM UniversityCalendarCourseOfStudies WHERE name LIKE \"%" + selectionArgs[0] + "%\" ORDER BY Upper(name)  COLLATE LOCALIZED, date", null);

			return c;
		}

		//Suche innerhalb der Kursvorschläge
		else if (path.equals("/courseChoiceDialog/search_suggest_query"))
		{
			db = getContext().openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READONLY, null);
			if (type == 0)
			{
				String sql = "SELECT C._id, C.name AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ", C._id AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
						+ " FROM Course AS C, CourseChoices AS A WHERE A.choosableId = " + id + " AND A.courseId = C._id AND C.name LIKE \"%"
						+ selectionArgs[0] + "%\" ORDER BY UPPER(C.name) COLLATE LOCALIZED ";
				return db.rawQuery(sql, null);
			} else
			{
				String sql = "Select vak FROM Optional WHERE _id = " + id;
				Cursor c = db.rawQuery(sql, null);
				c.moveToFirst();

				String condition = "";
				String optionalsql = "Select courseId FROM SelectedCourses WHERE optionalId = " + id;
				Cursor optionalc = db.rawQuery(optionalsql, null);
				if (optionalc.moveToFirst())
				{
					condition = "AND ( U._id != " + optionalc.getLong(0);
					optionalc.moveToNext();
					while (!optionalc.isAfterLast())
					{
						condition = condition + " AND U._id != " + optionalc.getLong(0);
						optionalc.moveToNext();
					}
					condition = condition + " OR U._id IS NULL)";
				}

				String sql2 = "Select C._id, C.name AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ", C._id AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
						+ " FROM UniversityCalendar AS C LEFT OUTER JOIN Course AS U ON C.vak=U.vak " + "WHERE (" + c.getString(0) + ") AND C.name LIKE \"%"
						+ selectionArgs[0] + "%\" AND (U.requiredCourse = 0 OR U.requiredCourse IS NULL) AND (U.status != " + Status.STATUS_PASSED
						+ " AND U.status != " + Status.STATUS_SIGNED_UP + " OR U.status IS NULL) " + condition
						+ " GROUP BY C.vak ORDER BY UPPER(C.name) COLLATE LOCALIZED ";
				c.close();
				return db.rawQuery(sql2, null);
			}

		}

		//Suche innerhalb der Kurse eines Faches des Vorlesungsverzeichnisses
		else
		{

			db = getContext().openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READONLY, null);
			String sql;

			if (subjectId == 0)
				sql = "SELECT U._id, U.name, C.status FROM UniversityCalendar AS U LEFT OUTER JOIN Course AS C ON U.vak=C.vak WHERE U.name LIKE \"%"
						+ selectionArgs[0] + "%\" GROUP BY U.vak ORDER BY Upper(U.name) ASC COLLATE LOCALIZED ";
			else
				sql = "SELECT U._id, U.name, C.status FROM UniversityCalendar AS U LEFT OUTER JOIN Course AS C ON U.vak=C.vak WHERE U.courseOfStudiesId = "
						+ subjectId + " AND U.name LIKE \"%" + selectionArgs[0] + "%\" GROUP BY U.vak ORDER BY Upper(U.name) COLLATE LOCALIZED ";

			Cursor c = db.rawQuery(sql, null);

			String[] columns = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_2,
					SearchManager.SUGGEST_COLUMN_INTENT_DATA };

			c.moveToFirst();
			String icon = "";
			MatrixCursor cursor = new MatrixCursor(columns);
			int status = 0;
			for (int i = 0; i < c.getCount(); i++)
			{
				String s = c.getString(2);
				if (s == null)
					status = -1;
				else
					status = c.getInt(2);

				
				//Je nach Status, anderes Bild setzen
				switch (status)
				{
				case Status.STATUS_NOT_PASSED:
					icon = "android.resource://de.eStudent.modulGuide/" + R.drawable.blue_icon;

					break;

				case Status.STATUS_PASSED:
					icon = "android.resource://de.eStudent.modulGuide/" + R.drawable.green_icon;

					break;

				case Status.STATUS_REQUIREMENTS_MISSING:
					icon = "android.resource://de.eStudent.modulGuide/" + R.drawable.red_icon;

					break;

				case Status.STATUS_SIGNED_UP:
					icon = "android.resource://de.eStudent.modulGuide/" + R.drawable.yellow_icon;

					break;

				default:
					icon = null;
				}

				String[] tmp = { Integer.toString(c.getInt(0)), c.getString(1), icon, Integer.toString(c.getInt(0)) };
				cursor.addRow(tmp);
				c.moveToNext();
			}
			c.close();
			db.close();

			return cursor;
		}

	}

	
	//unused
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	
	/**
	 * setzt die Id des Faches, welches durchsucht werden soll
	 * @param id Die Id des Faches
	 */
	public void setSubjectSearchId(long id)
	{
		subjectId = id;
	}

	
	/**
	 * Setzt die Parameter der Kursvorschläge, welche durchsucht werden sollen
	 * @param type Der Typ (Optional / Choosable)
	 * @param id Deren ID
	 */
	public void setCourseChoiceDialogSearchParameters(int type, long id)
	{
		// 0:= Choosable
		// 1:=Optional
		this.type = type;
		this.id = id;
	}

	public static final String DATABASE_NAME = "modulguide.db";
	private long subjectId;
	private int type;
	private long id;

}
