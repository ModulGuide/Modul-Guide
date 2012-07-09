package de.eStudent.modulGuide.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import de.eStudent.modulGuide.common.Category;
import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Choosable;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.MainInfo;
import de.eStudent.modulGuide.common.Optional;
import de.eStudent.modulGuide.common.Other;
import de.eStudent.modulGuide.common.Semester;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.common.Status;
import de.eStudent.modulGuide.common.UniversityCalendarCourse;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Diese Klasse ist für jegliche SQLite-Datenbankankbefehle zuständig.
 */
public class DataHelper
{

	/** Name der Datenbank */
	public static final String DATABASE_NAME = "modulguide.db";

	/** Version */
	public static final int TABLE_VERSION = 2;

	/**
	 * CourseOfStudies-Table - enthält Informationen über das Fach, in dem man
	 * studiert.
	 */
	public static final String CREATE_COURSE_OF_STUDIES_TABLE = "CREATE TABLE " + "CourseOfStudies" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "subject TEXT," + "facultyNr INTEGER," + "regulationDate TEXT," + "cp INTEGER," + "requiredCp INTERGER," + "semester INTEGER, degree TEXT)";

	/**
	 * Category-Table - enthält alle Kategorien, die in der ausgewählten
	 * Prüfungsordnung definiert sind.
	 */
	public static final String CREATE_CATEGORY_TABLE = "CREATE TABLE " + "Category" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "courseOfStudiesId INTEGER)";

	/**
	 * Course-Table - enthält alle Kurse, mit denen in irgendeiner Weise bereits
	 * gearbeitet wurde.
	 */
	public static final String CREATE_COURSE_TABLE = "CREATE TABLE " + "Course" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "categoryId INTEGER," + "vak TEXT," + "note TEXT," + "cp REAL," + "neccp INTEGER," + "status INTEGER," + "semester INTEGER," + "grade REAL,"
			+ "requiredCourse INTEGER," + "duration INTEGER, graded INTEGER, " + "weight REAL, optionalcp REAL)"; // boolean!!!!!

	/**
	 * Criterion-Table - enthält alle Kriterien, die in der Prüfungsordnung
	 * definiert sind.
	 */
	public static final String CREATE_CRITERION_TABLE = "CREATE TABLE " + "Criterion" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "categoryId INTEGER," + "grade REAL," + "note TEXT," + "cp REAL," + "neccp INTEGER," + "status INTEGER," + "semester INTEGER,"
			+ "graded INTEGER, " + "weight REAL)";

	/** Requirements-Table - enthält alle Kurs- und Kriteriumsanforderungen. */
	public static final String CREATE_REQUIREMENTS_TABLE = "CREATE TABLE " + "Requirements" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "requiredCourseId INTEGER," + "requiredCriterionId INTEGER," + "criterionId INTEGER," + "courseId INTERGER,"
			+ "passed INTEGER, optionalId INTEGER)";

	/** Optional-Table - enthält alle Wahlpflichten mit VAK-Filtern. */
	public static final String CREATE_OPTIONAL_TABLE = "CREATE TABLE " + "Optional" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT," + "vak TEXT,"
			+ "cp REAL," + "acquiredcp REAL," + "chosencp REAL," + "note TEXT," + "categoryId INTEGER, " + "weight REAL, "
			+ "transmitcp INTEGER, neccp INTEGER, graded INTEGER, duration INTEGER)";

	/** SelectedCourses-Table - enthält alle ausgewählten Kurse eines Optionals. */
	public static final String CREATE_SELECTEDCOURSES_TABLE = "CREATE TABLE " + "SelectedCourses" + "(optionalId INTEGER," + "courseId INTEGER)";

	/** Choosable-Table - enthält alle Wahlpflichten mit vordefinierten Kursen. */
	public static final String CREATE_CHOOSABLE_TABLE = "CREATE TABLE " + "Choosable" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "note TEXT," + "selectedCourseId INTEGER," + "categoryId INTEGER," + "cp REAL, duration INTEGER)";

	/** CourseChoices-Table - enthält alle Wahlmöglichkeiten der Choosables. */
	public static final String CREATE_COURSECHOICES_TABLE = "CREATE TABLE " + "CourseChoices" + "(choosableId INTEGER," + "courseId INTEGER)";

	/**
	 * UniversityCalendar-Table - enthält alle Kurse des
	 * Vorlesungsverzeichnisses.
	 */
	public static final String CREATE_UNIVERSITY_CALENDAR = "CREATE TABLE " + "UniversityCalendar" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "vak TEXT," + "cp REAL," + "date TEXT," + "staff TEXT," + "description TEXT," + "courseOfStudiesId INTEGER)";

	/**
	 * UniversityCalendarCourseOfStudies-Table - enthält alle Fache des
	 * Vorlesungsverzeichnisses
	 */
	public static final String CREATE_UNIVERSITY_CALENDAR_COURSEOFSTUDIES = "CREATE TABLE " + "UniversityCalendarCourseOfStudies"
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT," + "loaded int, timeStamp INTEGER, originalName TEXT)"; // boolean

	/**
	 * RegulationBuffer-Table - enthält alle zuletzt erkannten
	 * Prüfungsordnungen.
	 */
	public static final String CREATE_REGULATION_BUFFER = "CREATE TABLE " + "RegulationBuffer" + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "date TEXT," + "originalName TEXT, pdfName TEXT)";

	/** CurriculumNames-Table - enthält Musterstudienplan-Informationen */
	public static final String CREATE_CURRICULUM_NAMES_TABLE = "CREATE TABLE " + "CurriculumNames" + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "originalName TEXT, description TEXT)";

	/** Semester-Table - enthält Anzahl der zu erreichenden cp im Semester x */
	public static final String CREATE_SEMESTER_TABLE = "CREATE TABLE " + "Semester" + "(num INTEGER PRIMARY KEY," + "cp INTEGER)";

	/**
	 * Recommendation-Table - enthält Kurse und Kriterien mit Vermerk, in
	 * welchen Semestern sie erfüllt werden sollen und ordnet sie ggf einem
	 * Choosable zu
	 */
	public static final String CREATE_RECOMMENDATION_TABLE = "CREATE TABLE " + "Recommendation" + "(sem INTEGER," + "courseId INTEGER,"
			+ "criterionId INTEGER," + "alternative TEXT, choosableId INTEGER)";

	/** */
	public static final String CREATE_OPTIONALRECOMMENDATION_TABLE = "CREATE TABLE " + "OptionalRecommendation" + "(sem INTEGER," + "optionalId INTEGER,"
			+ "alternative TEXT," + "cp REAL," + "coursevak TEXT," + "coursename TEXT," + "cprange REAL, part INTEGER)";

	/**
	 * CPOverhang Table - enthält die überschüsse der einzelnen kurse
	 */
	public static final String CREATE_CPOVERHANG_TABLE = "CREATE TABLE " + "CPOverhang " + "(courseId INTEGER, cp REAL, additionalGradedCP REAL)";

	public static final String CREATE_LONGCOURSECP_TABLE = "CREATE TABLE LongCourseCp (courseId INTEGER, part INTEGER, cp REAL, optionalId INTEGER, choosableId INTEGER)";

	private Context context;
	public SQLiteDatabase db;
	DBHelper helper;

	/**
	 * Konstruktor des DataHelpers - benötigt immer den Kontext.
	 * 
	 * @param context
	 *            Kontext der aufrufenden Activity
	 */
	public DataHelper(Context context)
	{
		this.context = context;
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
		db.setLocale(new Locale("de_DE"));
	}

	/**
	 * Prüft, ob die Datenbank offen ist und öffnet sie ggf. neu
	 */
	public void reOpen()
	{
		if (!db.isOpen())
			db = helper.getWritableDatabase();
	}

	/**
	 * Schließt die Datenbank.
	 */
	public synchronized void close()
	{
		if (db != null)
		{
			db.close();
		}
	}

	/*
	 * Curriculum und UniversityCalendarSubject Methoden
	 */

	/**
	 * Holt sich die am weitesten zurückliegende update-Zeit des
	 * UniversityCalendars.
	 * 
	 * @return am weitesten zurückliegende update-Zeit
	 */
	public long calculateUniversityCalendarTimestamp()
	{
		String sql = "SELECT MIN(timeStamp) FROM UniversityCalendarCourseOfStudies";
		Cursor c = db.rawQuery(sql, null);
		long timeStamp = 0;

		if (c.moveToFirst())
		{
			timeStamp = c.getLong(0);
			;
		}
		c.close();
		return timeStamp;
	}

	/**
	 * Löscht alle Musterstudienplannamen.
	 */
	public void emptyCurriculumNames()
	{
		db.delete("CurriculumNames", null, null);
	}

	/**
	 * Fügt Musterstudienplaninformationen hinzu.
	 * 
	 * @param originalName
	 *            Dateiname
	 * @param name
	 *            Gut lesbarer Name
	 * @param description
	 *            Beschreibung des Musterstudienplans
	 */
	public void insertCiriculumName(String originalName, String name, String description)
	{
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("originalName", originalName);
		values.put("description", description);

		db.insert("CurriculumNames", null, values);
	}

	/**
	 * Holt sich alle Namen der Musterstudienpläne.
	 * 
	 * @return Cursor mit allen Musterstudienplaninformationen
	 */
	public Cursor getCiriculumNames()
	{
		String sql = "SELECT * FROM CurriculumNames";
		return db.rawQuery(sql, null);
	}

	/**
	 * Löscht ein Fach aus dem UniversityCalendar-Table.
	 * 
	 * @param id
	 *            ID des Faches
	 */
	public void deleteUniversityCalendarForASubject(long id)
	{
		db.delete("UniversityCalendar", "courseOfStudiesId = " + id, null);
	}

	/**
	 * Fügt ein Fach ins Vorlesungsverzeichnis hinzu.
	 * 
	 * @param name
	 *            Formatierter Name des Faches
	 * @param originalName
	 *            Dateiname des Faches
	 * @param educ
	 */
	public long addUniversityCalendarSubject(String name, String originalName)
	{
		if (name != null)
		{
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("originalName", originalName);
			return db.insert("UniversityCalendarCourseOfStudies", null, values);
		} else
			return -1;

	}

	/**
	 * Holt sich den Namen eines Faches
	 * 
	 * @param id
	 *            ID des Faches
	 * @return Name des Faches
	 */
	public String getUniversityCalendarSubjectName(long id)
	{
		Cursor c = db.rawQuery("SELECT name FROM UniversityCalendarCourseOfStudies WHERE _id = " + id, null);
		c.moveToFirst();
		String name = c.getString(0);
		c.close();
		return name;

	}

	/**
	 * Holt sich den Originalnamen eines Faches (z.B. zum Downloaden)
	 * 
	 * @param id
	 *            ID des Faches
	 * @return Originalname des Faches
	 */
	public String getOriginalUniversityCalendarSubjectName(long id)
	{
		Cursor c = db.rawQuery("SELECT originalName FROM UniversityCalendarCourseOfStudies WHERE _id = " + id, null);
		c.moveToFirst();
		String name = c.getString(0);
		c.close();
		return name;

	}

	/**
	 * Holt sich den Zeitpunkt des letzten Updates eines Faches.
	 * 
	 * @param id
	 *            ID des Faches
	 * @return Zeitpunkt des letzten Updates
	 */
	public long getUniversityCalendarSubjectTimeStamp(long id)
	{
		Cursor c = db.rawQuery("SELECT timeStamp FROM UniversityCalendarCourseOfStudies WHERE _id = " + id, null);
		c.moveToFirst();
		long timeStamp = c.getLong(0);
		c.close();
		return timeStamp;
	}

	/**
	 * Gibt den Zeitpunkt des letzten Updates eines Kurses formatiert zurück
	 * 
	 * @param id
	 *            ID des Kurses
	 * @return Formatiertes Datum des letzten Updtes
	 */
	public String getFormattedUniversityCalendarSubjectTimestamp(long id)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(getUniversityCalendarSubjectTimeStamp(id));
	}

	/**
	 * Gibt alle Fächer gefiltert zurück.
	 * 
	 * @param selectionArg
	 *            Filter für die Auswahl
	 * @return Cursor mit allen zutreffenden Fächern
	 */
	public Cursor getUniversityCalendarSubjectsList(String selectionArg)
	{
		String sql;
		if (selectionArg == null)
			sql = "SELECT _id, name, loaded, originalName, timeStamp FROM UniversityCalendarCourseOfStudies ORDER BY Upper(name) COLLATE LOCALIZED";
		else
			sql = "SELECT _id, name, loaded, originalName, timeStamp FROM UniversityCalendarCourseOfStudies WHERE name LIKE \"%" + selectionArg
					+ "%\" ORDER BY Upper(name) COLLATE LOCALIZED";
		return db.rawQuery(sql, null);
	}

	/**
	 * Prüft, ob bereits Kurse des Vorlesungsverzeichnisses in der Datenbank
	 * enthalten sind.
	 * 
	 * @return Kurse enthalten?
	 */
	public boolean areUniversityCalendarSubjectsNamesLoaded()
	{
		String sql = "SELECT COUNT (*) FROM UniversityCalendar";
		Cursor c = db.rawQuery(sql, null);
		c.moveToPosition(0);
		int count = c.getInt(0);
		c.close();
		if (count == 0)
			return false;

		return true;
	}

	/**
	 * Setzt den Downloadstatus eines Fachs
	 * 
	 * @param id
	 *            ID des Fachs
	 * @param status
	 *            zu setzender Status
	 */
	public void setUniversityCalendarSubjectDownloadedStatus(long id, boolean status)
	{
		ContentValues values = new ContentValues();
		if (status)
			values.put("loaded", 1);
		else
			values.put("loaded", 0);

		db.update("UniversityCalendarCourseOfStudies", values, "_id=" + id, null);
		// notifyDatabaseListeners();
	}

	/**
	 * Setzt die zuletzt geupdatete Zeit eines Fachs
	 * 
	 * @param id
	 *            ID des Faches
	 */
	public void setUniversityCalendarSubjectTimeStamp(long id)
	{
		ContentValues values = new ContentValues();
		values.put("timeStamp", Calendar.getInstance().getTimeInMillis());

		db.update("UniversityCalendarCourseOfStudies", values, "_id=" + id, null);
		// notifyDatabaseListeners();
	}

	/**
	 * Wirft alle Fächer aus der Datenbank.
	 */
	public void emptyUniversityCalendarSubjects()
	{
		db.delete("UniversityCalendarCourseOfStudies", null, null);
	}

	/*
	 * Saemtliche add-Methoden (ausser UCS)
	 */

	/**
	 * Fügt ein Fach hinzu, zu dem die Prüfungsordnung gehört.
	 * 
	 * @param name
	 *            Name des Faches
	 * @param facultyNr
	 *            Fachbereichsnummer
	 * @param regulationDate
	 *            Datum der Prüfungsordnung
	 * @param semester
	 *            Semester
	 */
	/*
	public void addCourseOfStudies(String name, int facultyNr, String regulationDate, int semester)
	{
		db.beginTransaction();
		try
		{
			String sql = "INSERT INTO CourseOfStudies (name, facultyNr, regulationDate, semester) VALUES (\"" + name + " \",\"" + facultyNr + " \",\""
					+ regulationDate + " \",\"" + semester + " \")";
			db.execSQL(sql);

			db.setTransactionSuccessful();
		} finally
		{
			db.endTransaction();
		}
	}*/

	/**
	 * Fügt Kategorien zur Datenbank hinzu.
	 * 
	 * @param listofCategories
	 *            Liste der Kategorien
	 */
	public void addCategories(ArrayList<Category> listofCategories)
	{
		db.beginTransaction();
		try
		{
			for (int i = 0; i < listofCategories.size(); i++)
				addCategory(listofCategories.get(i));

			addAllRequirements(listofCategories);

			db.setTransactionSuccessful();

		} finally
		{
			db.endTransaction();
		}

		checkForNecCP();

		notifyDatabaseListeners();
	}

	/**
	 * Fügt Anforderungen eines Kurses oder Kriteriums hinzu.
	 * 
	 * @param o
	 *            Kurs oder Kriterium
	 */
	private void addRequirementsForCourseOrCriterionOrOptional(Object o)
	{
		if (o instanceof Course)
		{
			Course c = (Course) o;

			if (c.requirements != null)
				for (int x = 0; x < c.requirements.length; x++)
				{
					Cursor cursor = db.query("Course", new String[] { "_id" }, "vak='" + c.requirements[x] + "'", null, null, null, null);

					if (cursor.getCount() != 0)
					{
						cursor.moveToFirst();
						long requiredCourseId = cursor.getLong(0);
						addRequirement(requiredCourseId, null, c.id, null, null);
					} else
					{
						Cursor cursor2 = db.query("Criterion", new String[] { "_id" }, "name='" + c.requirements[x] + "'", null, null, null, null);
						if (cursor2.getCount() != 0)
						{
							cursor2.moveToFirst();

							long requiredCriterionId = cursor2.getLong(0);
							addRequirement(null, requiredCriterionId, c.id, null, null);
						}
						cursor2.close();

					}
					cursor.close();

				}
		} else if (o instanceof Criterion)
		{
			Criterion c = (Criterion) o;

			if (c.requirements != null)
				for (int x = 0; x < c.requirements.length; x++)
				{
					Cursor cursor = db.query("Course", new String[] { "_id" }, "vak='" + c.requirements[x] + "'", null, null, null, null);

					if (cursor.getCount() != 0)
					{
						cursor.moveToFirst();

						long requiredCourseId = cursor.getLong(0);
						addRequirement(requiredCourseId, null, null, c.id, null);
					} else
					{
						Cursor cursor2 = db.query("Criterion", new String[] { "_id" }, "name='" + c.requirements[x] + "'", null, null, null, null);
						if (cursor2.getCount() != 0)
						{
							cursor2.moveToFirst();

							long requiredCriterionId = cursor2.getLong(0);
							addRequirement(null, requiredCriterionId, null, c.id, null);
						}
						cursor2.close();
					}
					cursor.close();
				}
		} else if (o instanceof Optional)
		{
			Optional opt = (Optional) o;

			if (opt.requirements != null)
				for (int x = 0; x < opt.requirements.length; x++)
				{
					Cursor cursor = db.query("Course", new String[] { "_id" }, "vak='" + opt.requirements[x] + "'", null, null, null, null);

					if (cursor.getCount() != 0)
					{
						cursor.moveToFirst();

						long requiredCourseId = cursor.getLong(0);
						addRequirement(requiredCourseId, null, null, null, opt.id);
					} else
					{
						Cursor cursor2 = db.query("Criterion", new String[] { "_id" }, "name='" + opt.requirements[x] + "'", null, null, null, null);
						if (cursor2.getCount() != 0)
						{
							cursor2.moveToFirst();

							long requiredCriterionId = cursor2.getLong(0);
							addRequirement(null, requiredCriterionId, null, null, opt.id);
						}
						cursor2.close();
					}
					cursor.close();
				}
		}
	}

	/**
	 * Fügt alle Anforderungen aus einer Liste von Kategorien hinzu.
	 * 
	 * @param listofCategories
	 *            Liste von Kategorien
	 */
	public void addAllRequirements(ArrayList<Category> listofCategories)
	{
		for (int i = 0; i < listofCategories.size(); i++)
		{
			Category category = listofCategories.get(i);
			for (int j = 0; j < category.childs.size(); j++)
			{
				Object o = category.childs.get(j);

				if (o instanceof Choosable)
				{
					for (int k = 0; k < ((Choosable) o).subjects.size(); k++)
					{
						addRequirementsForCourseOrCriterionOrOptional(((Choosable) o).subjects.get(k));
					}
				} else
					addRequirementsForCourseOrCriterionOrOptional(o);
			}
		}
	}

	/**
	 * Fügt eine Kategorie zur Datenbank hinzu.
	 * 
	 * @param category
	 *            Hinzuzufügende Kategorie
	 */
	public void addCategory(Category category)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("name", category.name);
		values.put("courseOfStudiesId", category.courseOfStudiesId);
		id = db.insert("Category", null, values);
		if (id != -1)
			category.id = id;
		else
			throw new Error("Datenbnakanfrage schlägt fehl");

		for (int i = 0; i < category.childs.size(); i++)
		{
			Object o = category.childs.get(i);
			if (o instanceof Course)
			{
				((Course) o).category = id;
				addCourse((Course) o);
			} else if (o instanceof Criterion)
			{
				((Criterion) o).category = id;
				addCriterion((Criterion) o);
			} else if (o instanceof Choosable)
			{
				((Choosable) o).category = id;
				addChoosable((Choosable) o);
			} else if (o instanceof Optional)
			{
				((Optional) o).category = id;
				addOptional((Optional) o);
			}
		}
	}

	/**
	 * Trägt deinen Kurs in die Datenbank ein.
	 * 
	 * @param course
	 *            Course-Objekt, das eingetragen werden soll
	 * @return die von der SQL-Datenbank zugewiesene Id
	 */
	public long addCourse(Course course)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("name", course.name);
		values.put("vak", course.vak);
		values.put("cp", course.cp);
		values.put("neccp", course.necCP);
		if (course.requirements == null)
			values.put("status", Status.STATUS_NOT_PASSED);
		else
			values.put("status", Status.STATUS_REQUIREMENTS_MISSING);
		values.put("categoryId", course.category);
		values.put("requiredCourse", course.requiredCourse);
		values.put("duration", course.duration);
		values.put("graded", course.graded);
		values.put("note", course.note);

		values.put("weight", course.weight);
		id = db.insert("Course", null, values);

		course.id = id;

		if (course.splitcp != null)
		{
			for (int i = 0; i < course.splitcp.length; i++)
			{
				values.clear();
				values.put("courseId", course.id);
				values.put("part", i + 1);
				values.put("cp", course.splitcp[i]);
				id = db.insert("LongCourseCp", null, values);

			}
		}

		notifyDatabaseListeners();

		return course.id;
	}

	/**
	 * Fügt eine Liste von Kursen der Datenbank hinzu.
	 * 
	 * @param listOfCourses
	 *            Liste der Kurse
	 * @param id
	 *            ID des Faches, zu dem die Kurse gehören
	 */
	public void addListOfCourses(LinkedList<UniversityCalendarCourse> listOfCourses, long id)
	{
		db.beginTransaction();
		try
		{
			ContentValues values;
			while (!listOfCourses.isEmpty())
			{
				UniversityCalendarCourse c = listOfCourses.removeFirst();
				if (c.name != null)
				{
					values = new ContentValues();
					values.put("name", c.name);
					values.put("vak", c.vak);
					values.put("cp", c.cp);
					values.put("staff", c.staff);
					values.put("date", c.date);
					values.put("description", c.description);
					values.put("courseOfStudiesId", id);

					db.insert("UniversityCalendar", null, values);
				}
			}
			db.setTransactionSuccessful();

		} finally
		{
			db.endTransaction();
		}

	}

	public void addUniversityCalendarCourse(UniversityCalendarCourse course, long universityCalendarSubjectId)
	{
		
			ContentValues values;

			values = new ContentValues();
			values.put("name", course.name);
			values.put("vak", course.vak);
			values.put("cp", course.cp);
			values.put("staff", course.staff);
			values.put("date", course.date);
			values.put("description", course.description);
			values.put("courseOfStudiesId", universityCalendarSubjectId);

			db.insert("UniversityCalendar", null, values);
	}

	/**
	 * Fügt eine Anforderung der Datenbank hinzu.
	 * 
	 * @param requiredCourseId
	 *            Kursanforderung
	 * @param requiredCriterionId
	 *            Kriteriumsanforderung
	 * @param courseId
	 *            Kurs, der diese Anforderung besitzt
	 * @param criterionId
	 *            Kriterium, das diese Anforderung besitzt
	 */
	public void addRequirement(Long requiredCourseId, Long requiredCriterionId, Long courseId, Long criterionId, Long optionalId)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("requiredCourseId", requiredCourseId);
		values.put("requiredCriterionId", requiredCriterionId);
		values.put("criterionId", criterionId);
		values.put("courseId", courseId);
		values.put("optionalId", optionalId);
		values.put("passed", 0);

		id = db.insert("Requirements", null, values);
		if (id == -1)
			throw new Error("Datenbnakanfrage schlägt fehl");

	}

	/**
	 * Fügt ein Kriterium der Datenbank hinzu.
	 * 
	 * @param criterion
	 *            Hinzuzufügendes Kriterium
	 */
	public void addCriterion(Criterion criterion)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("name", criterion.name);
		values.put("categoryId", criterion.category);
		values.put("status", Status.STATUS_NOT_PASSED);
		values.put("cp", criterion.cp);
		values.put("neccp", criterion.neccp);
		values.put("note", criterion.note);
		values.put("graded", criterion.graded);
		values.put("weight", criterion.weight);
		id = db.insert("Criterion", null, values);

		criterion.id = id;

	}

	/**
	 * Fügt ein Optional (VAK-gefilterte Wahlpflicht) der Datenbank hinzu.
	 * 
	 * @param optional
	 *            Hinzuzufügendes Optional
	 */
	public void addOptional(Optional optional)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("name", optional.name);
		values.put("categoryId", optional.category);
		values.put("cp", optional.cp);
		values.put("vak", optional.vak);
		values.put("note", optional.note);
		values.put("weight", optional.weight);
		values.put("neccp", optional.necCp);
		values.put("graded", optional.graded);
		values.put("duration", optional.duration);

		if (optional.transmitcp)
			values.put("transmitcp", 1);
		else
			values.put("transmitcp", 0);

		id = db.insert("Optional", null, values);

		optional.id = id;

		if (optional.splitcp != null)
		{
			for (int i = 0; i < optional.splitcp.length; i++)
			{
				values.clear();
				values.put("optionalId", optional.id);
				values.put("part", i + 1);
				values.put("cp", optional.splitcp[i]);
				id = db.insert("LongCourseCp", null, values);

			}
		}

	}

	/**
	 * Fügt ein Choosable (Wahlpflicht mit vordefinierten Kursen) der Datenbank
	 * hinzu.
	 * 
	 * @param choosable
	 *            Hinzuzufügendes Choosable
	 */
	public void addChoosable(Choosable choosable)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("name", choosable.name);
		values.put("categoryId", choosable.category);
		values.put("cp", choosable.cp);
		values.put("duration", choosable.duration);

		id = db.insert("Choosable", null, values);

		choosable.id = id;

		if (choosable.splitcp != null)
		{
			for (int i = 0; i < choosable.splitcp.length; i++)
			{
				values.clear();
				values.put("choosableId", choosable.id);
				values.put("part", i + 1);
				values.put("cp", choosable.splitcp[i]);
				id = db.insert("LongCourseCp", null, values);

			}
		}

		for (int i = 0; i < choosable.subjects.size(); i++)
		{
			Course c = choosable.subjects.get(i);
			addCourse(c);
			addCourseChoice(choosable.id, c.id);

		}

	}

	/**
	 * Fügt einen gewählten Kurs (eines Optionals) der Datenbank hinzu.
	 * 
	 * @param optionalId
	 *            ID des Optionals
	 * @param courseId
	 *            ID des Kurses
	 * @param cp
	 *            CP des Kurses
	 */
	public void addSelectedCourse(long optionalId, long courseId, double cp)
	{
		ContentValues values = new ContentValues();
		values.put("optionalId", optionalId);
		values.put("courseId", courseId);

		if (db.insert("SelectedCourses", null, values) < 0)
			throw new Error("Datenbnakanfrage schlägt fehl");

		String sql = "Select chosencp, weight, cp, graded, transmitcp FROM Optional WHERE _id = " + optionalId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
		{
			ContentValues values3 = new ContentValues();
			if (c.getDouble(0) + cp <= c.getDouble(2))
			{
				values3.put("chosencp", c.getDouble(0) + cp);
				db.update("Optional", values3, "_id = " + optionalId, null);

				if (c.getInt(4) != 1)
				{
					values3.clear();
					values3.put("optionalcp", cp);
					db.update("Course", values3, "_id = " + courseId, null);
				}
			} else
			{
				values3.put("chosencp", c.getDouble(2));
				db.update("Optional", values3, "_id = " + optionalId, null);
				String sql2 = "Select _id, chosencp FROM Optional WHERE transmitcp = 1";

				Cursor c2 = db.rawQuery(sql2, null);
				if (c2.moveToFirst())
				{
					values3.clear();
					values3.put("chosencp", c2.getDouble(1) + ((c.getDouble(0) + cp) - c.getDouble(2)));
					db.update("Optional", values3, "_id = " + c2.getLong(0), null);

					if (c.getInt(4) != 1)
					{
						values3.clear();
						if(c.getDouble(2) >= cp)
							values3.put("optionalcp", ((c.getDouble(0) + cp) - c.getDouble(2)));
						else
							values3.put("optionalcp", cp - ((c.getDouble(0) + cp) - c.getDouble(2)));
						db.update("Course", values3, "_id = " + courseId, null);

						values3.clear();
						values3.put("cp", cp);
						if(c.getDouble(2) >= cp)
							values3.put("additionalGradedCp", cp - ((c.getDouble(0) + cp) - c.getDouble(2)));
						else
							values3.put("additionalGradedCp", ((c.getDouble(0) + cp) - c.getDouble(2)));
						values3.put("courseId", courseId);
						db.insert("CPOverhang", null, values3);
					}
				}
			}
		}
		/*
		 * String sql = "Select chosencp FROM Optional WHERE _id = " +
		 * optionalId; Cursor c = db.rawQuery(sql, null); if (c.moveToFirst()) {
		 * ContentValues values3 = new ContentValues(); values3.put("chosencp",
		 * c.getDouble(0) + cp); db.update("Optional", values3, "_id = " +
		 * optionalId, null); }
		 */
		c.close();
	}

	/**
	 * Fügt einen gewählten Kurs (eines Choosables) der Datenbank hinzu.
	 * 
	 * @param choosableId
	 *            ID des Choosables
	 * @param courseId
	 *            ID des Kurses
	 */
	public void addCourseChoice(long choosableId, long courseId)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("choosableId", choosableId);
		values.put("courseId", courseId);

		id = db.insert("CourseChoices", null, values);
		if (id == -1)
			throw new Error("Datenbnakanfrage schlägt fehl");

	}

	/**
	 * Fügt einen Studiengang der Datenbank hinzu.
	 * 
	 * @param mainInfo
	 *            Der Studiengang
	 */
	public void addCourseOfStudies(MainInfo mainInfo)
	{
		long id;

		ContentValues values = new ContentValues();
		values.put("subject", mainInfo.subject);
		values.put("facultyNr", mainInfo.facultyNr);
		values.put("regulationDate", mainInfo.regulationDate);
		values.put("requiredCp", mainInfo.requiredCp);
		values.put("cp", 0);
		values.put("degree", mainInfo.degree);

		id = db.insert("CourseOfStudies", null, values);

		mainInfo.id = id;

	}

	/**
	 * Fuegt mit addUniversityCourseToOptional(long optionalId, long uccId,
	 * double cp) einen UniversityCalendarCourse einem Optional hinzu. Der Kurs
	 * wird ggf in Course hingefügt
	 * 
	 * @param optionalId
	 * @param uccId
	 */
	public void addUniversityCourseToOptional(long optionalId, long uccId)
	{
		String sql = "SELECT cp FROM UniversityCalendar WHERE _id = " + uccId;
		Cursor c = db.rawQuery(sql, null);

		double cp = 0;

		if (c.moveToFirst())
			cp = c.getDouble(0);
		c.close();
		addUniversityCourseToOptional(optionalId, uccId, cp);
	}

	/**
	 * Fügt einen UniversityCalendar-Kurs einem Optional hinzu.
	 * 
	 * @param uccId
	 *            ID des Kurses im UniversityCalendar
	 * @param optionalId
	 *            ID des Optionals
	 */
	public void addUniversityCourseToOptional(long optionalId, long uccId, double cp)
	{
		long id = 0;
		String sql = "SELECT * FROM UniversityCalendar WHERE _id = " + uccId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();

		String sql2 = "SELECT vak, status, _id, cp FROM Course WHERE vak = '" + c.getString(2) + "'";
		Cursor c2 = db.rawQuery(sql2, null);

		if (!c2.moveToFirst())
		{
			int flag = 1;
			ContentValues values = new ContentValues();
			values.put("name", c.getString(1));
			values.put("vak", c.getString(2));
			values.put("cp", c.getDouble(3));
			values.put("status", Status.STATUS_NOT_PASSED);
			values.put("neccp", 0);
			values.put("requiredCourse", 0);
			values.put("duration", 1);
			id = db.insert("Course", null, values);
			

			c2.close();
			sql2 = "Select * FROM Requirements WHERE optionalId = " + optionalId;
			c2 = db.rawQuery(sql2, null);
			if (c2.moveToFirst())
			{
				ContentValues reqValues = new ContentValues();

				if (!c2.isAfterLast())
				{
					if (!c2.isNull(1))
						reqValues.put("requiredCourseId", c2.getLong(1));
					if (!c2.isNull(2))
						reqValues.put("requiredCriterionId", c2.getLong(2));
					if (!c2.isNull(3))
						reqValues.put("criterionId", c2.getLong(3));
					Log.d("id = ", "" + id);
					reqValues.put("courseId", id);
					reqValues.put("passed", c2.getInt(5));

					flag = c2.getInt(5) * flag;
					c2.moveToNext();
				}

				db.insert("Requirements", null, reqValues);

				if (flag == 0)
				{
					values.clear();
					values.put("status", Status.STATUS_REQUIREMENTS_MISSING);
					db.update("Course", values, "_id = " + id, null);
				}
			}

			ContentValues values2 = new ContentValues();
			values2.put("optionalId", optionalId);
			values2.put("courseId", id);
			db.insert("SelectedCourses", null, values2);
		} else if (c2.getInt(1) == Status.STATUS_NOT_PASSED)
		{
			cp = c2.getDouble(3);
			id = c2.getLong(2);
			ContentValues values2 = new ContentValues();
			values2.put("optionalId", optionalId);
			values2.put("courseId", c2.getLong(2));
			db.insert("SelectedCourses", null, values2);
		} else
			id = c2.getLong(2);
		c2.close();
		c.close();
		double weight = 0;
		int graded = 0;
		double[] params = adjustCPForOptionalOrChoosable(optionalId, id, cp, weight, graded);

		
		
		
		//weitere Attribute vererben
		ContentValues values4 = new ContentValues();
		
		if(params[3] > 1)
		{
			values4.put("cp", params[2]);
			
			//splitcp des Optionals an den Kurs vererben
			
			passOptionalSplitCPToCourse(optionalId, id);
		}
		values4.put("weight", params[1]);
		values4.put("graded", params[0]);
		db.update("Course", values4, "_id = " + id, null);
		c.close();
		c2.close();
		notifyDatabaseListeners();
	}
	
	public double[] adjustCPForOptionalOrChoosable(Long optionalId, long id, double cp, double weight, int graded)
	{
		double[] params = new double[4];

		String sql = "Select chosencp, weight, cp, graded, transmitcp, duration FROM Optional WHERE _id = " + optionalId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
		{
			//falls duration > 1 nehme cp von Optional
			if(c.getInt(5) > 1)
				cp = c.getDouble(2);
			
			weight = c.getDouble(1);
			graded = c.getInt(3);
			params[2] = c.getDouble(2);
			params[3] = c.getInt(5);
			ContentValues values3 = new ContentValues();
			if (c.getDouble(0) + cp <= c.getDouble(2))
			{
				values3.put("chosencp", c.getDouble(0) + cp);
				db.update("Optional", values3, "_id = " + optionalId, null);

				if (c.getInt(4) != 1)
				{
					values3.clear();
					values3.put("optionalcp", cp);
					db.update("Course", values3, "_id = " + id, null);
				}
			} else
			{
				values3.put("chosencp", c.getDouble(2));
				db.update("Optional", values3, "_id = " + optionalId, null);
				String sql2 = "Select _id, chosencp FROM Optional WHERE transmitcp = 1";

				Cursor c2 = db.rawQuery(sql2, null);
				if (c2.moveToFirst())
				{
					values3.clear();
					values3.put("chosencp", c2.getDouble(1) + ((c.getDouble(0) + cp) - c.getDouble(2)));
					db.update("Optional", values3, "_id = " + c2.getLong(0), null);

					if (c.getInt(4) != 1)
					{
						values3.clear();
						if(c.getDouble(2) >= cp)
							values3.put("optionalcp", ((c.getDouble(0) + cp) - c.getDouble(2)));
						else
							values3.put("optionalcp", cp - ((c.getDouble(0) + cp) - c.getDouble(2)));
						db.update("Course", values3, "_id = " + id, null);

						values3.clear();
						values3.put("cp", cp);
						if(c.getDouble(2) >= cp)
							values3.put("additionalGradedCp", cp - ((c.getDouble(0) + cp) - c.getDouble(2)));
						else
							values3.put("additionalGradedCp", ((c.getDouble(0) + cp) - c.getDouble(2)));
						values3.put("courseId", id);
						db.insert("CPOverhang", null, values3);
					}
				}
				c2.close();
			}
		}
		c.close();
		params[0] = graded;
		params[1] = weight;
		return params;
	}

	/**
	 * Fügt einen selbst erstellten Kurs einem Optional hinzu.
	 * 
	 * @param optionalId
	 *            ID des Optionals
	 * @param course
	 *            Der neu erstellte Kurs
	 */
	public void addCustomCourseToOptional(long optionalId, Course course)
	{
		//Zu vererbende Attribute des Optionals laden und dem Course zuweisen
		
		String sql = "SELECT weight, neccp, graded, duration, cp FROM Optional WHERE _id = "+optionalId;
		Cursor c = db.rawQuery(sql, null);
		
		if(c.moveToFirst())
		{
			course.weight = c.getDouble(0);
			course.necCP = c.getInt(1);
			course.graded = c.getInt(2);
			course.duration = c.getInt(3);
			
			//Bei duration >1 sind keine anderen CP als ie vom Optional erlaubt
			if(course.duration > 1)
				course.cp = c.getDouble(4);
		}
		c.close();
		
		long id = addCourse(course);
		
		//falls duration >2 splitcp vom optional an den Kurs vererben
		
		passOptionalSplitCPToCourse(optionalId, id);
		
		
		//requirements des Optional an den Kurs vererben
		int flag=1;
		String sql2 = "Select * FROM Requirements WHERE optionalId = " + optionalId;
		Cursor c2 = db.rawQuery(sql2, null);
		if (c2.moveToFirst())
		{
			ContentValues reqValues = new ContentValues();

			if (!c2.isAfterLast())
			{
				if (!c2.isNull(1))
					reqValues.put("requiredCourseId", c2.getLong(1));
				if (!c2.isNull(2))
					reqValues.put("requiredCriterionId", c2.getLong(2));
				if (!c2.isNull(3))
					reqValues.put("criterionId", c2.getLong(3));
				reqValues.put("courseId", id);
				reqValues.put("passed", c2.getInt(5));

				flag = c2.getInt(5) * flag;
				c2.moveToNext();
			}

			db.insert("Requirements", null, reqValues);

			if (flag == 0)
			{
				reqValues.clear();
				reqValues.put("status", Status.STATUS_REQUIREMENTS_MISSING);
				db.update("Course", reqValues, "_id = " + id, null);
			}
		}

		addSelectedCourse(optionalId, id, course.cp);

	}
	
	
	public void passOptionalSplitCPToCourse(long optionalId, long courseId)
	{
		ContentValues values = new ContentValues();
		values.put("courseId", courseId);
		
		db.update("LongCourseCp", values, "optionalId = " + optionalId, null);

	}

	public ArrayList<Category> getExampleCourseSchemeTable()
	{

		ArrayList<Semester> semester = getStudyCoursePlanSemester();
		int semesterCount = semester.size();

		// alle Categorien laden
		ArrayList<Category> categories = getCategoryNamesAndId();

		int size = categories.size();
		for (int i = 0; i < size; i++)
		{
			Category cat = categories.get(i);

			cat.semester = new ArrayList<Semester>();

			for (int y = 1; y <= semesterCount; y++)
				cat.semester.add(new Semester(y));

			cat.semester = getStudyCoursePlanSuggestions(cat.semester, cat.id);
		}

		// Leere Categoríen rausschmeißen
		for (int i = 0; i < size; i++)
		{
			Category cat = categories.get(i);

			boolean notEmpty = false;
			for (int x = 0; x < cat.semester.size() && !notEmpty; x++)
			{
				if (!cat.semester.get(x).childs.isEmpty())
					notEmpty = true;
			}

			if (!notEmpty)
			{
				categories.remove(i);
				size--;
				i--;
			}

		}

		// planned CP und cp in der ersten Category speichern
		for (int i = 0; i < semesterCount; i++)
		{
			categories.get(0).semester.get(i).plannedCp = semester.get(i).plannedCp;
			categories.get(0).semester.get(i).cp = semester.get(i).semester;

		}

		return categories;
	}

	public ArrayList<Semester> getExampleCourseScheme()
	{

		ArrayList<Semester> list = getStudyCoursePlanSemester();

		if (list.size() > 0)
			getStudyCoursePlanSuggestions(list, 0);

		return list;
	}

	public ArrayList<Semester> getStudyCoursePlanSemester()
	{
		ArrayList<Semester> list = new ArrayList<Semester>();

		// Alle Semester hinzufügen

		String sql = "SELECT * FROM Semester";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Semester sem = new Semester(c.getInt(0));
			sem.plannedCp = c.getInt(1);
			list.add(sem);
			// sem.cp = getCPForSemester(sem.semester);

			sem.childs = new ArrayList<ChildEntry>();

			c.moveToNext();
		}
		c.close();
		return list;
	}

	public ArrayList<Semester> getStudyCoursePlanSuggestions(ArrayList<Semester> list, long categoryId)
	{

		// Alle Kurse laden und den entsprechenden Semester zuweisen

		String sql2;
		if (categoryId == 0)
			sql2 = "SELECT C._id, C.name, C.cp, C.status, (SELECT IFNULL(L.cp, 0) FROM LongCourseCp AS L WHERE L.courseId = C._id AND L.part = 1), C.duration, C.note FROM Course AS C WHERE C.requiredCourse=1";
		else
			sql2 = "SELECT C._id, C.name, C.cp, C.status, (SELECT IFNULL(L.cp, 0) FROM LongCourseCp AS L WHERE L.courseId = C._id AND L.part = 1), C.duration, C.note FROM Course AS C WHERE C.requiredCourse=1 AND C.categoryId = "
					+ categoryId;

		Cursor c2 = db.rawQuery(sql2, null);
		c2.moveToFirst();
		while (!c2.isAfterLast())
		{

			String sql3 = "SELECT sem, alternative FROM Recommendation WHERE courseId = " + c2.getLong(0);
			Cursor c3 = db.rawQuery(sql3, null);
			c3.moveToFirst();
			while (!c3.isAfterLast())
			{
				Course course = new Course();
				course.id = c2.getLong(0);
				course.name = c2.getString(1);
				course.cp = c2.getDouble(2);
				course.status = c2.getInt(3);
				course.alternative = c3.getString(1);
				course.firstSemestercp = c2.getDouble(4);
				course.duration = c2.getInt(5);
				course.note = c2.getString(6);
				((ArrayList<Course>) (list.get(c3.getInt(0) - 1).childs)).add(course);

				// CourseParts suchen
				if (course.firstSemestercp != 0)
				{
					ArrayList<ChildEntry> others = getLongCourseParts(0, course, false);
					for (int i = 0; i < others.size(); i++)
					{
						((ArrayList<ChildEntry>) list.get(c3.getInt(0) + i).childs).add(others.get(i));

					}
				}

				c3.moveToNext();

			}
			c3.close();

			c2.moveToNext();
		}

		c2.close();

		// Alle Criteriosn laden und dem entsprechenden Semester hinzufügen

		String sql3;
		if (categoryId == 0)
			sql3 = "SELECT _id, name, cp, status, note FROM Criterion";
		else
			sql3 = "SELECT _id, name, cp, status, note FROM Criterion WHERE categoryId = " + categoryId;

		Cursor c3 = db.rawQuery(sql3, null);
		c3.moveToFirst();

		while (!c3.isAfterLast())
		{
			String sql4 = "SELECT sem, alternative FROM Recommendation WHERE criterionId = " + c3.getLong(0);
			Cursor c4 = db.rawQuery(sql4, null);
			c4.moveToFirst();
			while (!c4.isAfterLast())
			{

				Criterion criterion = new Criterion(c3.getString(1));
				criterion.id = c3.getLong(0);
				criterion.cp = c3.getDouble(2);
				criterion.status = c3.getInt(3);
				criterion.note = c3.getString(4);
				criterion.alternative = c4.getString(1);
				((ArrayList<Criterion>) (list.get(c4.getInt(0) - 1).childs)).add(criterion);
				c4.moveToNext();
			}
			c4.close();

			c3.moveToNext();
		}

		c3.close();

		// Alle Choosables laden und den entsprechenden Semestern hinzufügen

		String sql4;
		if (categoryId == 0)
			sql4 = "SELECT C._id, C.name, C.cp, C.selectedCourseId, (SELECT IFNULL(L.cp, 0) FROM LongCourseCp AS L WHERE L.choosableId = C._id AND L.part = 1), duration, note FROM Choosable AS C";
		else
			sql4 = "SELECT C._id, C.name, C.cp, C.selectedCourseId, (SELECT IFNULL(L.cp, 0) FROM LongCourseCp AS L WHERE L.choosableId = C._id AND L.part = 1), duration, note FROM Choosable AS C WHERE C.categoryId = "
					+ categoryId;

		Cursor c4 = db.rawQuery(sql4, null);
		c4.moveToFirst();

		while (!c4.isAfterLast())
		{

			Course selectedCourse;
			if (c4.getLong(3) == 0)
				selectedCourse = null;
			else
				selectedCourse = getCourse(c4.getLong(3));

			String sql5 = "SELECT R.sem, R.alternative, C.name, C.vak FROM Recommendation AS R LEFT OUTER JOIN Course AS C ON R.courseId = C._id WHERE R.choosableId = "
					+ c4.getLong(0);
			Cursor c5 = db.rawQuery(sql5, null);
			c5.moveToFirst();
			while (!c5.isAfterLast())
			{
				Choosable choosable = new Choosable(c4.getLong(0), c4.getString(1));
				choosable.cp = c4.getDouble(2);
				choosable.alternative = c5.getString(1);
				choosable.reccomendedCourseName = c5.getString(2);
				choosable.reccomendedCourseVak = c5.getString(3);
				choosable.selectedCourseId = c4.getLong(3);
				choosable.selectedCourse = selectedCourse;
				choosable.firstSemestercp = c4.getDouble(4);
				choosable.duration = c4.getInt(5);
				choosable.note = c4.getString(6);
				((ArrayList<Choosable>) (list.get(c5.getInt(0) - 1).childs)).add(choosable);

				// ChoosableParts suchen
				if (choosable.firstSemestercp != 0)
				{
					ArrayList<ChildEntry> others = getChoosableParts(choosable);
					for (int i = 0; i < others.size(); i++)
					{
						((ArrayList<ChildEntry>) list.get(c5.getInt(0) + i).childs).add(others.get(i));

					}
				}

				c5.moveToNext();
			}
			c5.close();

			c4.moveToNext();
		}

		c4.close();

		// Alle Optionals laden und den entsprechenden Semestern hinzufügen

		String sql5;
		if (categoryId == 0)
			sql5 = "SELECT O._id, O.name, O.acquiredcp, O.chosencp, O.cp, (SELECT IFNULL(L.cp, 0) FROM LongCourseCp AS L WHERE L.optionalId = O._id AND L.part = 1), duration, note FROM Optional AS O";
		else
			sql5 = "SELECT O._id, O.name, O.acquiredcp, O.chosencp, O.cp, (SELECT IFNULL(L.cp, 0) note FROM LongCourseCp AS L WHERE L.optionalId = O._id AND L.part = 1), duration, note FROM Optional AS O WHERE O.categoryId = "
					+ categoryId;

		Cursor c5 = db.rawQuery(sql5, null);
		c5.moveToFirst();

		while (!c5.isAfterLast())
		{
			String sql6 = "SELECT sem, alternative, cp, coursevak, coursename, cprange, part, (SELECT SUM(cprange) FROM OptionalRecommendation WHERE optionalId = "
					+ c5.getLong(0) + " ) FROM OptionalRecommendation WHERE optionalId = " + c5.getLong(0);
			Cursor c6 = db.rawQuery(sql6, null);
			c6.moveToFirst();

			Log.d("bla", c5.getString(1) + "|" + c6.getCount());

			while (!c6.isAfterLast())
			{
				Optional optional = new Optional(c5.getString(1));

				optional.id = c5.getLong(0);
				optional.acquiredcp = c5.getDouble(2);
				optional.chosencp = c5.getDouble(3);
				optional.duration = c5.getInt(6);
				optional.note = c5.getString(7);

				optional.alternative = c6.getString(1);
				optional.cp = c6.getDouble(2);
				optional.coursevak = c6.getString(3);
				optional.coursename = c6.getString(4);
				optional.cpRange = c6.getDouble(5);
				if (c6.getDouble(7) != 0)
					optional.name += " Teil " + c6.getInt(6);

				optional.firstSemestercp = c5.getDouble(5);

				((ArrayList<Optional>) (list.get(c6.getInt(0) - 1).childs)).add(optional);

				// OptionalParts suchen
				if (optional.firstSemestercp != 0)
				{
					ArrayList<ChildEntry> others = getOptionalParts(optional);
					for (int i = 0; i < others.size(); i++)
					{
						((ArrayList<ChildEntry>) list.get(c6.getInt(0) + i).childs).add(others.get(i));

					}
				}

				c6.moveToNext();
			}
			c6.close();

			c5.moveToNext();
		}

		c5.close();

		return list;

	}

	public void addRecommendedPlan(Semester[] semesters)
	{
		ContentValues contentValues;

		for (int i = 0; i < semesters.length; i++)
		{
			contentValues = new ContentValues();
			contentValues.put("num", semesters[i].semester);
			contentValues.put("cp", semesters[i].plannedCp);

			db.insert("Semester", null, contentValues);

			for (ChildEntry child : semesters[i].childs)
			{
				if (child instanceof Optional)
				{
					Cursor c = db.rawQuery("SELECT _id FROM Optional WHERE name = '" + ((Optional) child).name + "'", null);
					if (c.moveToFirst())
					{
						contentValues = new ContentValues();
						contentValues.put("optionalId", c.getLong(0));
						contentValues.put("alternative", ((Optional) child).alternative);
						contentValues.put("sem", semesters[i].semester);
						contentValues.put("cp", ((Optional) child).cp);
						contentValues.put("coursevak", ((Optional) child).coursevak);
						contentValues.put("coursename", ((Optional) child).coursename);
						contentValues.put("cprange", ((Optional) child).cpRange);
						contentValues.put("part", ((Optional) child).part);
						db.insert("OptionalRecommendation", null, contentValues);
					}
					c.close();

				} else if (child instanceof Choosable)
				{
					Cursor c = db.rawQuery("SELECT _id FROM Choosable WHERE name = '" + ((Choosable) child).name + "'", null);
					if (c.moveToFirst())
					{
						contentValues = new ContentValues();
						contentValues.put("choosableId", c.getLong(0));
						contentValues.put("alternative", ((Choosable) child).alternative);
						contentValues.put("sem", semesters[i].semester);

						if (((Choosable) child).reccomendedCourseName != null)
						{
							Cursor c2 = db.rawQuery("SELECT _id FROM Course WHERE requiredCourse = 2 AND name = '" + ((Choosable) child).reccomendedCourseName
									+ "'", null);
							if (c2.moveToFirst())
								contentValues.put("courseId", c2.getLong(0));
							c2.close();
						}

						db.insert("Recommendation", null, contentValues);
					}
					c.close();

				} else if (child instanceof Course)
				{

					Cursor c = db.rawQuery("SELECT _id FROM Course WHERE name = '" + ((Course) child).name + "'", null);
					if (c.moveToFirst())
					{
						contentValues = new ContentValues();
						contentValues.put("courseId", c.getLong(0));
						contentValues.put("alternative", ((Course) child).alternative);
						contentValues.put("sem", semesters[i].semester);

						db.insert("Recommendation", null, contentValues);
					}
					c.close();
				} else if (child instanceof Criterion)
				{
					Cursor c = db.rawQuery("SELECT _id FROM Criterion WHERE name = '" + ((Criterion) child).name + "'", null);
					if (c.moveToFirst())
					{
						contentValues = new ContentValues();
						contentValues.put("criterionId", c.getLong(0));
						contentValues.put("alternative", ((Criterion) child).alternative);
						contentValues.put("sem", semesters[i].semester);

						db.insert("Recommendation", null, contentValues);
					}
					c.close();
				}
			}
		}

		notifyDatabaseListeners();
	}

	/**
	 * Fügt einen Kurs des UniversityCalendars im Course-Table hinzu.
	 * 
	 * @param id
	 *            ID des Kurses im UniversityCalendar
	 * @return ID des Kurses im Course-Table
	 */
	public long saveUniversityCalendarCourse(long id)
	{
		UniversityCalendarCourse ucc = getUniversityCalendarCourse(id);
		Course c = new Course(0, ucc.name, ucc.vak, ucc.note, ucc.cp, ucc.necCP, ucc.status, ucc.semester, ucc.grade, 0, ucc.duration, 1, ucc.weight, context);
		return addCourse(c);
	}

	/*
	 * Alle get-Methoden, nach Objekten sortiert
	 */

	/**
	 * Gibt eine Liste aller Kategorien eines Studienganges zurück.
	 * 
	 * @param courseOfStudies
	 *            ID des Studienganges
	 * @return Liste aller Kategorien
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Category> getCategories(long courseOfStudies)
	{
		ArrayList<Category> categories = new ArrayList<Category>();

		String sql = "SELECT * FROM Category";
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Category category = new Category(c.getLong(0), c.getString(1));
			c.moveToNext();

			category.childs = getAllCoursesFromCategory(category.id);
			((ArrayList<ChildEntry>) category.childs).addAll(getAllOptionalChildrenInCategory(category.id));
			((ArrayList<ChildEntry>) category.childs).addAll(getAllChoosableChildrenInCategory(category.id));
			((ArrayList<Criterion>) category.childs).addAll(getAllCriterionsFromCategory(category.id));

			for (int i = 0; i < category.childs.size(); i++)
			{
				if (category.childs.get(i).countForCategory())
				{
					category.items++;
					if (category.childs.get(i).getStatus() == Status.STATUS_PASSED)
						category.passedItems++;
				}

			}

			categories.add(category);

		}
		c.close();
		return categories;
	}

	/**
	 * Holt ein Category-Objekt anhand seiner ID.
	 * 
	 * @param id
	 *            ID der Category in der Datenbank
	 * @return Die gewünschte Kategorie
	 */
	public Category getCategory(long id)
	{
		Category cat = null;
		String sql = "SELECT * FROM Category where _id = " + id;
		Cursor c = db.rawQuery(sql, null);

		if (c.moveToFirst())
			cat = new Category(c.getLong(0), c.getString(1));

		c.close();
		return cat;
	}

	public ArrayList<Category> getCategoryNamesAndId()
	{
		String sql = "SELECT _id, name FROM Category";
		Cursor c = db.rawQuery(sql, null);

		ArrayList<Category> cats = new ArrayList<Category>();

		if (!c.moveToFirst())
		{
			c.close();
			return cats;
		}

		for (int i = 0; !c.isAfterLast(); i++)
		{
			Category cat = new Category(c.getString(1));
			cat.id = c.getLong(0);
			cats.add(cat);
			c.moveToNext();
		}
		c.close();
		return cats;

	}

	/**
	 * Gibt alle Semester der Datenbank zurück.
	 * 
	 * @return Liste der Semester
	 */
	public ArrayList<Semester> getAllSemester()
	{
		ArrayList<Semester> semester = new ArrayList<Semester>();

		int count = Integer.parseInt(PreferenceHelper.getSemester(context));
		for (int i = 1; i <= count; i++)
		{
			semester.add(getSemester(i));
		}

		return semester;
	}

	/**
	 * Gibt das Semester-Objekt anhand der Semesternummer zurück.
	 * 
	 * @param semester
	 *            Semesterzahl
	 * @return das entsprechende Semesterobjekt
	 */
	public Semester getSemester(int semester)
	{
		Semester sem = new Semester(semester);

		String sql = "SELECT C.*, (SELECT IFNULL(L.cp, 0) FROM LongCourseCp AS L WHERE L.courseId = C._id AND L.part = 1) FROM Course AS C WHERE C.semester = "
				+ semester + " AND (C.status = " + Status.STATUS_PASSED + " OR C.status = " + Status.STATUS_SIGNED_UP + ")";

		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Course course = new Course(c.getLong(0), c.getString(1), c.getString(3), c.getString(4), c.getDouble(5), c.getInt(6), c.getInt(7), c.getInt(8),
					c.getDouble(9), c.getInt(10), c.getInt(11), c.getInt(12), c.getDouble(13), context);
			course.firstSemestercp = c.getDouble(15);

			((ArrayList<Course>) sem.childs).add(course);
			c.moveToNext();
		}
		c.close();

		sql = "SELECT * FROM Criterion WHERE semester = " + semester + " AND status = " + Status.STATUS_PASSED;

		c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Criterion criterion = new Criterion(c.getLong(0), c.getString(1), c.getFloat(3), c.getString(4), c.getInt(5), c.getInt(6), c.getInt(7),
					c.getInt(8), c.getInt(9), c.getDouble(10));

			((ArrayList<Criterion>) sem.childs).add(criterion);

			c.moveToNext();
		}

		((ArrayList<ChildEntry>) (sem.childs)).addAll(getLongCourseParts(semester, null, true));

		sem.cp = getCPForSemester(semester);

		sem.grade = getGradeForSemester(semester);

		c.close();

		return sem;
	}

	public ArrayList<ChildEntry> getLongCourseParts(int semester, Course course, boolean minSignedUp)
	{
		ArrayList<ChildEntry> list = new ArrayList<ChildEntry>();

		String sql;
		if (minSignedUp && semester != 0)
			sql = "SELECT L.part, L.cp, C.name, C._id FROM LongCourseCp AS L LEFT OUTER JOIN Course AS C ON L.courseId = C._id WHERE L.part!=1 AND C.semester - 1 + L.part = "
					+ semester + " AND (C.status = " + Status.STATUS_SIGNED_UP + " OR C.status = " + Status.STATUS_PASSED + ")";
		else if (semester != 0)
			sql = "SELECT L.part, L.cp, C.name, C._id FROM LongCourseCp AS L LEFT OUTER JOIN Course AS C ON L.courseId = C._id WHERE L.part  != 1 AND C.semester - 1 + L.part = "
					+ semester;
		else
			sql = "SELECT L.part, L.cp, C.name FROM LongCourseCp AS L LEFT OUTER JOIN Course AS C ON L.courseId = C._id WHERE L.part != 1 AND C._id = "
					+ course.id;

		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Other other = new Other();
			other.name = "" + c.getString(2) + " Teil " + c.getInt(0);
			other.cp = c.getDouble(1);

			if (course != null)
				other.parent = course;

			else
				other.parent = getCourse(c.getLong(3));

			list.add(other);
			c.moveToNext();
		}
		
		c.close();

		return list;
	}

	public ArrayList<ChildEntry> getChoosableParts(Choosable choosable)
	{
		ArrayList<ChildEntry> list = new ArrayList<ChildEntry>();

		String sql = "SELECT L.part, L.cp, C.name FROM LongCourseCp AS L LEFT OUTER JOIN Choosable AS C ON L.choosableId = C._id WHERE L.part != 1 AND C._id = "
				+ choosable.id;

		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Other other = new Other();
			other.name = "" + c.getString(2) + " Teil " + c.getInt(0);
			other.cp = c.getDouble(1);
			other.parent = choosable;
			list.add(other);
			c.moveToNext();
		}

		return list;
	}

	public ArrayList<ChildEntry> getOptionalParts(Optional optional)
	{
		ArrayList<ChildEntry> list = new ArrayList<ChildEntry>();

		String sql = "SELECT L.part, L.cp, O.name FROM LongCourseCp AS L LEFT OUTER JOIN Optional AS O ON L.optionalId = O._id WHERE L.part != 1 AND O._id = "
				+ optional.id;

		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Other other = new Other();
			other.name = "" + c.getString(2) + " Teil " + c.getInt(0);
			other.cp = c.getDouble(1);
			other.parent = optional;
			list.add(other);
			c.moveToNext();
		}

		return list;
	}

	/**
	 * Die Methode liefert alle Kurse, deren VAK der VAK des Optionals
	 * entsprechen, die aber noch nicht einem Optional zugewiesen wurden oder
	 * bereits angemeldet/bestanden sind
	 * 
	 * @param optionalId
	 *            Id des Optionals
	 * @param keywords
	 *            Namensfilterung
	 * @return Cursor des Ergebnisses
	 */
	public Cursor getCoursesForOptional(long optionalId, String keywords)
	{
		String condition = "";
		String sql = "Select vak FROM Optional WHERE _id = " + optionalId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();

		String optionalsql = "Select courseId FROM SelectedCourses";
		Cursor optionalc = db.rawQuery(optionalsql, null);

		String optionalsql2 = "Select selectedCourseId FROM Choosable";
		Cursor optionalc2 = db.rawQuery(optionalsql2, null);

		if (optionalc.moveToFirst())
		{
			Log.d("BLUBBERMAN", Long.toString(optionalc.getLong(0)));
			condition = "AND ( U._id != " + optionalc.getLong(0);
			optionalc.moveToNext();
			while (!optionalc.isAfterLast())
			{
				condition = condition + " AND U._id != " + optionalc.getLong(0);
				optionalc.moveToNext();
			}

			if (optionalc2.moveToFirst())
				while (!optionalc2.isAfterLast())
				{
					condition = condition + " AND U._id != " + optionalc2.getLong(0);
					optionalc2.moveToNext();
				}
			condition = condition + " OR U._id IS NULL)";
		} else if (optionalc2.moveToFirst())
		{
			condition = " AND (U._id != " + optionalc2.getLong(0);
			optionalc2.moveToNext();
			while (!optionalc2.isAfterLast())
			{
				condition = condition + " AND U._id != " + optionalc2.getLong(0);
				optionalc2.moveToNext();
			}
			condition = condition + " OR U._id IS NULL)";
		}

		String sql2;

		if (keywords == null)
		{
			sql2 = "Select C._id, C.name, C.vak, C.cp FROM UniversityCalendar AS C LEFT OUTER JOIN Course AS U ON U.vak=C.vak " + "WHERE (" + c.getString(0)
					+ ") AND (U.requiredCourse = 2 OR U.requiredCourse = 0 OR U.requiredCourse IS NULL) AND (U.status != " + Status.STATUS_PASSED
					+ " AND U.status != " + Status.STATUS_SIGNED_UP + " OR U.status IS NULL) " + condition
					+ " GROUP BY C.vak ORDER BY UPPER(C.name) COLLATE LOCALIZED";
			Log.d("SQLDD", sql2 );
		} else
			sql2 = "Select C._id, C.name, C.vak, C.cp FROM UniversityCalendar AS C LEFT OUTER JOIN Course AS U ON U.vak=C.vak " + "WHERE (" + c.getString(0)
					+ ") AND C.name LIKE \"%" + keywords + "%\" AND (U.requiredCourse = 0 OR U.requiredCourse IS NULL) AND (U.status != "
					+ Status.STATUS_PASSED + " AND U.status != " + Status.STATUS_SIGNED_UP + " OR U.status IS NULL) " + condition
					+ " GROUP BY C.vak ORDER BY UPPER(C.name) COLLATE LOCALIZED ";

		Cursor c2 = db.rawQuery(sql2, null);
		c2.moveToFirst();
		while (!c2.isAfterLast())
		{
			c2.moveToNext();
		}
		c.close();
		optionalc.close();
		optionalc2.close();
		return c2;
	}

	/**
	 * Gibt eine Liste aller Optionals (Wahlpflicht mit VAK-Filterung) zurück.
	 * 
	 * @return Liste der Optionals
	 */
	public ArrayList<Optional> getAllOptionals()
	{
		ArrayList<Optional> arrayList = new ArrayList<Optional>();

		String sql = "SELECT * FROM Optional";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();

		while (c.isAfterLast() == false)
		{
			Optional o;
			o = new Optional(c.getLong(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(6), c.getDouble(4), c.getDouble(5), c.getDouble(8),
					c.getInt(10));
			arrayList.add(o);
			c.moveToNext();
		}
		c.close();
		return arrayList;
	}

	/**
	 * Gibt die ID des Optionals zurück, zu dem ein Kurs zugewiesen ist.
	 * 
	 * @param courseId
	 *            ID des Kurses
	 * @return -1 bei nicht gefundenem Optional, sonst ID des Optionals
	 */
	public long getOptional(long courseId)
	{
		String sql = "Select optionalId FROM SelectedCourses WHERE courseId = " + courseId;
		Cursor c = db.rawQuery(sql, null);
		long id;
		if (c.moveToFirst())
			id = c.getLong(0);
		else
			id = -1;
		c.close();
		return id;
	}

	/**
	 * Sucht anhand der courseId das Optional, in welchem sich der Kurs befindet
	 * 
	 * @param courseId
	 *            ID des Kurses
	 * @return Optional-Objekt zu diesem Kurs
	 */
	public Optional getOptionalObjectByCourseId(long courseId)
	{
		String sql = "Select * FROM Optional WHERE _id = (Select optionalId FROM SelectedCourses WHERE courseId = " + courseId + ")";
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
		{
			Optional o = new Optional(c.getLong(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(6), c.getDouble(4), c.getDouble(5),
					c.getDouble(8), c.getInt(10));
			o.graded = c.getInt(11);
			c.close();
			return o;
		}
		c.close();
		return null;
	}

	/**
	 * Sucht das Optional anhand der Id
	 * 
	 * @param optionalId
	 *            ID des Optionals
	 * @return DDas dazugehörige Optional-Objekt
	 */
	public Optional getOptionalObjectById(long optionalId)
	{
		Optional o = null;

		String sql = "Select * FROM Optional WHERE _id = " + optionalId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
		{
			o = new Optional(c.getLong(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(6), c.getDouble(4), c.getDouble(5), c.getDouble(8),
					c.getInt(10));
			o.graded = c.getInt(11);
			c.close();
		}
		c.close();

		// Kursvorschlag laden
		String sql2 = "SELECT coursename, coursevak FROM OptionalRecommendation WHERE coursename IS NOT NULL AND optionalId = " + optionalId;
		Cursor c2 = db.rawQuery(sql2, null);
		c2.moveToFirst();

		while (!c2.isAfterLast())
		{
			if (o.coursename == null)
				o.coursename = c2.getString(0) + " (" + c2.getString(1) + ")";

			else if (c2.getString(0) != null)
				o.coursename += "\n" + c2.getString(0) + " (" + c2.getString(1) + ")";

			c2.moveToNext();
		}
		c2.close();

		// Vorgeschlagene Semester laden
		String sql3 = "SELECT sem, cp, part FROM OptionalRecommendation WHERE optionalId = " + optionalId + " ORDER BY part";
		o.alternative = Statics.calculateAlternativeForOptional(db.rawQuery(sql3, null));

		return o;
	}

	/**
	 * Gibt eine Liste aller Kurse eines Optionals zurück.
	 * 
	 * @param optionalId
	 *            ID des Optionals
	 * @return Eine Liste mit allen Kursen des gegebenen Optionals
	 */
	public ArrayList<Course> getCoursesFromOptional(long optionalId)
	{
		ArrayList<Course> al = new ArrayList<Course>();
		String sql = "SELECT courseId FROM SelectedCourses WHERE optionalId = " + optionalId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		String sql2;
		Cursor c2;
		while (!c.isAfterLast())
		{
			sql2 = "SELECT * FROM Course WHERE _id = " + c.getLong(0);
			c2 = db.rawQuery(sql2, null);
			if (c2.moveToFirst())
			{
				Course course = new Course(c2.getLong(0), c2.getString(1), c2.getString(3), c2.getString(4), c2.getDouble(5), c2.getInt(6), c2.getInt(7),
						c2.getInt(8), c2.getDouble(9), c2.getInt(10), c2.getInt(11), c2.getInt(12), c2.getDouble(13), context);
				course.subCourse = true;
				al.add(course);
			}
			c2.close();
			c.moveToNext();
		}

		c.close();
		return al;
	}

	/**
	 * Gibt eine Liste aller Optionals einer Kategorie zurück.
	 * 
	 * @param categoryId
	 *            ID der Kategorie
	 * @return Liste aller Optionals
	 */
	public ArrayList<ChildEntry> getAllOptionalChildrenInCategory(long categoryId)
	{
		ArrayList<ChildEntry> al = new ArrayList<ChildEntry>();
		String sql = "SELECT * FROM OPTIONAL WHERE categoryId = " + categoryId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Optional o = new Optional(c.getLong(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(6), c.getDouble(4), c.getDouble(5),
					c.getDouble(8), c.getInt(10));
			al.add(o);
			al.addAll(getCoursesFromOptional(o.id));

			if (c.getInt(9) == 1)
				al.addAll(getCPOverhangForOptional());

			c.moveToNext();
		}
		c.close();
		return al;
	}

	public ArrayList<ChildEntry> getCPOverhangForOptional()
	{
		String sql = "SELECT O.additionalGradedCP, C._id, C.name, C.status FROM CPOverhang AS O LEFT OUTER JOIN Course AS C ON O.courseId = C._id WHERE O.additionalGradedCP > 0 ";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();

		ArrayList<ChildEntry> list = new ArrayList<ChildEntry>();

		while (!c.isAfterLast())
		{
			Other other = new Other();
			other.isSub = true;
			other.name = "CP Überhang von: \n" + c.getString(2);
			other.cp = c.getDouble(0);
			other.parent = new Course();
			other.parent.id = c.getLong(1);
			other.parent.name = c.getString(2);
			((Course) other.parent).status = c.getInt(3);

			list.add(other);
			c.moveToNext();
		}
		
		c.close();

		return list;

	}

	/**
	 * Gibt alle möglchen Optionals und Choosables zurück, denen ein Kurs
	 * zugewiesen werden kann.
	 * 
	 * @param id
	 *            ID des Kurses
	 * @return Liste aller Optionals
	 */
	public ArrayList<ChildEntry> getPossibleOptionalAndChoosableChoicesFromCourse(long id)
	{
		ArrayList<ChildEntry> al = new ArrayList<ChildEntry>();
		String sql = "Select _id, name FROM Choosable WHERE (selectedCourseId IS NULL AND _id = (Select choosableId FROM CourseChoices WHERE courseId = " + id
				+ ")) OR selectedCourseId = " + id;// (Select C._id FROM Course
													// AS C WHERE C.vak =
													// (Select U.vak FROM
													// UniversityCalendar AS U
													// WHERE _id = "+ id + "))";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			al.add(new Choosable(c.getLong(0), c.getString(1)));
			c.moveToNext();
		}
		c.close();
		String sqloptional = "Select O.* FROM Optional AS O WHERE O.cp > COALESCE(O.chosencp, 0) OR O._id = (Select optionalId FROM SelectedCourses WHERE courseId = "
				+ id + ")";
		Cursor coptional = db.rawQuery(sqloptional, null);
		coptional.moveToFirst();
		// String sqloptional2 =
		// "Select optionalId FROM SelectedCourses WHERE courseId = " +
		// getCourseFromUniversityCalendar(id);
		// String sqloptional2 =
		// "Select optionalId FROM SelectedCourses WHERE courseId = " + id;

		// Cursor coptional2 = db.rawQuery(sqloptional2, null);
		String vak = "";
		while (!coptional.isAfterLast())
		{
			vak = coptional.getString(2);
			// if (coptional.getDouble(5) < coptional.getDouble(3) ||
			// coptional2.moveToFirst())
			// {
			// sql = "Select C._id FROM UniversityCalendar AS C WHERE C._id = "
			// + id + " AND (" + vak + ")";
			sql = "Select C._id FROM Course AS C WHERE C._id = " + id + " AND (" + vak + ")";
			c = db.rawQuery(sql, null);
			String sql2 = "Select optionalId FROM SelectedCourses WHERE courseId = " + id + " AND optionalId = " + coptional.getLong(0);
			Cursor c2 = db.rawQuery(sql2, null);
			if (c.moveToFirst() || c2.moveToFirst())
			{
				Optional o = new Optional(coptional.getLong(0), coptional.getString(1));
				o.weight = coptional.getDouble(8);
				o.graded = coptional.getInt(11);
				o.necCp = coptional.getInt(10);
				o.chosencp = coptional.getDouble(5);
				o.acquiredcp = coptional.getDouble(4);
				al.add(o);
				c.moveToNext();
			}
			c.close();
			// }

			coptional.moveToNext();
		}
		coptional.close();

		// String sql2 =
		// "Select O._id, O.name FROM Optional AS O WHERE ( O.chosencp IS NULL OR O.chosencp < O.cp) AND (Select _id FROM UniversityCalendar AS C WHERE C._id == '"
		// + id + "' AND C.vak LIKE O.vak) IS NOT NULL";

		return al;
	}

	/**
	 * Sucht alle möglichen Optionals und Choosables zu einem Kurs im
	 * UniversityCalendar-Table heraus.
	 * 
	 * @param universityCalendarCourseId
	 *            ID des Kurses im UniversityCalendar
	 * @return Liste aller Optionals und Choosables, die zutreffend sind
	 */
	public ArrayList<ChildEntry> getPossibleOptionalAndChoosableChoicesForUCC(long universityCalendarCourseId)
	{
		ArrayList<ChildEntry> al = new ArrayList<ChildEntry>();
		String sql = "Select _id, name FROM Choosable WHERE (selectedCourseId IS NULL AND _id = (Select choosableId FROM CourseChoices WHERE courseId = (Select C._id FROM Course AS C WHERE C.vak = (Select U.vak FROM UniversityCalendar AS U WHERE _id = "
				+ universityCalendarCourseId
				+ ")))) OR selectedCourseId = (Select C._id FROM Course AS C WHERE C.vak = (Select U.vak FROM UniversityCalendar AS U WHERE _id = "
				+ universityCalendarCourseId + "))";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			al.add(new Choosable(c.getLong(0), c.getString(1)));
			c.moveToNext();
		}
		c.close();
		String sqloptional = "Select * FROM Optional";
		Cursor coptional = db.rawQuery(sqloptional, null);
		coptional.moveToFirst();
		String sqloptional2 = "Select optionalId FROM SelectedCourses WHERE courseId = "
				+ getMergedCourse(universityCalendarCourseId, Statics.TYPE_UNIVERSITY_CALENDAR_COURSE).id;// getCourseFromUniversityCalendar(universityCalendarCourseId);

		Cursor coptional2 = db.rawQuery(sqloptional2, null);
		String vak = "";
		while (!coptional.isAfterLast())
		{
			Optional o;
			vak = coptional.getString(2);
			if (coptional.getDouble(5) < coptional.getDouble(3) || coptional2.moveToFirst())
			{
				sql = "Select C._id FROM UniversityCalendar AS C WHERE C._id = " + universityCalendarCourseId + " AND (" + vak + ")";
				c = db.rawQuery(sql, null);
				if (c.moveToFirst())
				{
					o = new Optional(coptional.getLong(0), coptional.getString(1));
					o.graded = coptional.getInt(11);
					o.weight = coptional.getDouble(8);
					al.add(o);

					c.moveToNext();
				}
				c.close();
			}

			coptional.moveToNext();
		}
		coptional.close();
		coptional2.close();

		return al;
	}

	/**
	 * Holt das Choosable Objekt des zugehörigen Kurses
	 * 
	 * @param courseId
	 *            ID des Kurses
	 * @return Choosable-Objekt, zu dem der Kurs gehört
	 */
	public Choosable getChoosableObjectByCourseId(long courseId)
	{
		String sql = "Select * FROM Choosable WHERE _id = (Select choosableId FROM courseChoices WHERE courseId = " + courseId + ")";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		Choosable ch = new Choosable(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3), c.getDouble(5));
		c.close();
		return ch;
	}

	/**
	 * Gibt das Choosable-Objekt anhand dessen ID zurück
	 * 
	 * @param choosableId
	 *            ID des Choosables
	 * @return Objekt des Choosables
	 */
	public Choosable getChoosableObjectById(long choosableId)
	{
		Choosable ch = null;
		String sql = "Select * FROM Choosable WHERE _id = " + choosableId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
		{
			ch = new Choosable(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3), c.getDouble(5));
			c.close();
		}
		c.close();

		// Kursvorschlag laden
		String sql2 = "SELECT C.name, C.vak FROM Recommendation AS R LEFT OUTER JOIN Course AS C ON R.courseId = C._id WHERE courseId IS NOT NULL AND R.choosableId = "
				+ choosableId;
		Cursor c2 = db.rawQuery(sql2, null);
		if (c2.moveToFirst())
		{
			ch.reccomendedCourseName = c2.getString(0);
			ch.reccomendedCourseVak = c2.getString(1);
		}
		c2.close();

		// Vorgeschlagene Semester laden
		String sql3 = "SELECT sem FROM Recommendation WHERE choosableId  =" + choosableId;
		ch.alternative = Statics.calculateAlternativeForChoosable(db.rawQuery(sql3, null), ch.cp);

		return ch;
	}

	/**
	 * Gibt alle Choosables einer Kategorie zurück
	 * 
	 * @param categoryId
	 *            ID der Kategorie
	 * @return Liste der Choosables dieser Kategorie
	 */
	public ArrayList<Choosable> getAllChoosablesFromCategory(long categoryId)
	{
		ArrayList<Choosable> choosables = new ArrayList<Choosable>();

		String sql = "SELECT * FROM Choosable WHERE categoryId = " + categoryId;
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Choosable choosable = new Choosable(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3), c.getDouble(5));

			c.moveToNext();

			choosables.add(choosable);

			if (choosable.selectedCourseId != 0)
				choosable.selectedCourse = getCourse(choosable.selectedCourseId);

		}
		c.close();
		return choosables;
	}

	/**
	 * Gibt eine Liste aller Choosables zurück
	 * 
	 * @return Alle Choosables der Datenbank
	 */
	public ArrayList<Choosable> getAllChoosables()
	{
		ArrayList<Choosable> choosables = new ArrayList<Choosable>();

		String sql = "SELECT * FROM Choosable";
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Choosable choosable = new Choosable(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3), c.getDouble(5));

			c.moveToNext();

			choosables.add(choosable);

			if (choosable.selectedCourseId != 0)
				choosable.selectedCourse = getCourse(choosable.selectedCourseId);

		}
		c.close();
		return choosables;
	}

	/**
	 * An sich ist die Methode unnoetig, da hier nach einem Kurs anhand der Id
	 * gesucht wird Dieser Kurs ist auch der Einzige, der in der Arraylist
	 * zurueckgegeben wird
	 * 
	 * @param selectedCourseId
	 *            Id des gesuchten Kurses
	 * @return Kurs des Choosables
	 */
	public Course getCourseFromChoosable(long selectedCourseId)
	{
		Course course = getCourse(selectedCourseId);
		course.subCourse = true;

		return course;
	}

	/**
	 * Gibt alle Choosables und deren Kurse einer Kategorie zurück
	 * 
	 * @param categoryId
	 *            ID der Kategorie
	 * @return Arraylist, welche sowohl die Choosables als auch ihre kurse
	 *         enthaelt
	 */
	public ArrayList<ChildEntry> getAllChoosableChildrenInCategory(long categoryId)
	{
		ArrayList<ChildEntry> al = new ArrayList<ChildEntry>();
		String sql = "SELECT * FROM Choosable WHERE categoryId = " + categoryId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Choosable ch = new Choosable(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3), c.getDouble(5));
			ch.category = categoryId;

			al.add(ch);
			if (ch.selectedCourseId != 0)
			{
				al.add(getCourseFromChoosable(ch.selectedCourseId));
				ch.selectedCourse = getCourse(ch.selectedCourseId);

			}
			c.moveToNext();
		}
		c.close();
		return al;
	}

	/**
	 * Gibt alle Kurswahlmöglichkeiten eines Choosables zurück
	 * 
	 * @param id
	 *            ID des Choosables
	 * @param keywords
	 *            Kursnamenfilterung
	 * @return Cursor, der die Kurse enthält
	 */
	public Cursor getCourseChoicesForChoosable(long id, String keywords)
	{
		String condition = "";
		String sql;
		String sql2 = "Select S.courseId FROM SelectedCourses AS S LEFT OUTER JOIN CourseChoices AS C ON S.courseId = C.courseId";
		Cursor c = db.rawQuery(sql2, null);
		if (c.moveToFirst())
		{
			condition = " AND C._id != " + c.getLong(0);
			c.moveToNext();
			while (!c.isAfterLast())
			{
				condition = condition + " AND C._id != " + c.getLong(0);
				c.moveToNext();
			}
		}

		if (keywords == null)
			sql = "SELECT C._id, C.name, C.vak, C.note, C.cp, C.status, C.duration FROM Course AS C, CourseChoices AS A WHERE A.choosableId = " + id
					+ " AND A.courseId = C._id " + condition + " ORDER BY UPPER(C.name) COLLATE LOCALIZED";
		else
			sql = "SELECT C._id, C.name, C.vak, C.note, C.cp, C.status, C.duration FROM Course AS C, CourseChoices AS A WHERE A.choosableId = " + id
					+ " AND A.courseId = C._id " + condition + " AND C.name LIKE \"%" + keywords + "%\" ORDER BY UPPER(C.name) COLLATE LOCALIZED";
		return db.rawQuery(sql, null);
	}

	/**
	 * Gibt die Summe der CP zurück, die erreicht wurden.
	 * 
	 * @return Summe der CP
	 */
	public int getCurrentCP()
	{
		/*
		 * SQLiteStatement stmt =
		 * db.compileStatement("SELECT SUM(cp) FROM Course WHERE status = " +
		 * Status.STATUS_PASSED); SQLiteStatement stmt2 =
		 * db.compileStatement("SELECT SUM(cp) FROM Criterion WHERE status = " +
		 * Status.STATUS_PASSED);
		 * 
		 * return ((int) stmt.simpleQueryForLong() + (int)
		 * stmt2.simpleQueryForLong());
		 */
		String sql = "SELECT SUM(cp) FROM Course WHERE status = " + Status.STATUS_PASSED;
		String sql2 = "SELECT SUM(cp) FROM Criterion WHERE status = " + Status.STATUS_PASSED;
		Cursor c = db.rawQuery(sql, null);
		Cursor c2 = db.rawQuery(sql2, null);
		c.moveToFirst();
		c2.moveToFirst();
		double retval = c.getDouble(0) + c2.getDouble(0);
		c.close();
		c2.close();
		return (int) retval;
	}

	/**
	 * Gibt die Summe der CP zurück, für die sich angemeldet wurde
	 * 
	 * @return Summe der CP
	 */
	public int getSignedUpCP()
	{
		/*
		 * SQLiteStatement stmt =
		 * db.compileStatement("SELECT SUM(cp) FROM Course WHERE status = " +
		 * Status.STATUS_SIGNED_UP); SQLiteStatement stmt2 =
		 * db.compileStatement("SELECT SUM(cp) FROM Criterion WHERE status = " +
		 * Status.STATUS_SIGNED_UP);
		 * 
		 * return ((int) stmt.simpleQueryForLong() + (int)
		 * stmt2.simpleQueryForLong());
		 */
		String sql = "SELECT SUM(cp) FROM Course WHERE status = " + Status.STATUS_SIGNED_UP;
		String sql2 = "SELECT SUM(cp) FROM Criterion WHERE status = " + Status.STATUS_SIGNED_UP;
		Cursor c = db.rawQuery(sql, null);
		Cursor c2 = db.rawQuery(sql2, null);
		c.moveToFirst();
		c2.moveToFirst();
		double retval = c.getDouble(0) + c2.getDouble(0);
		c.close();
		c2.close();
		return (int) retval;
	}

	/**
	 * Gibt die Summe der CP zurück, die bestanden oder angemeldet wurden.
	 * 
	 * @return Summe der CP
	 */
	public double getCurrentCPWithSignedUpCourses()
	{
		/*
		 * SQLiteStatement stmt =
		 * db.compileStatement("SELECT SUM(cp) FROM Course WHERE status = " +
		 * Status.STATUS_PASSED + " OR status =" + Status.STATUS_SIGNED_UP);
		 * SQLiteStatement stmt2 =
		 * db.compileStatement("SELECT SUM(cp) FROM Criterion WHERE status = " +
		 * Status.STATUS_PASSED);
		 * 
		 * return ((int) stmt.simpleQueryForLong() + (int)
		 * stmt2.simpleQueryForLong());
		 */
		String sql = "SELECT SUM(cp) FROM Course WHERE status = " + Status.STATUS_PASSED + " OR status = " + Status.STATUS_SIGNED_UP;
		String sql2 = "SELECT SUM(cp) FROM Criterion WHERE status = " + Status.STATUS_PASSED + " OR  status = " + Status.STATUS_SIGNED_UP;
		Cursor c = db.rawQuery(sql, null);
		Cursor c2 = db.rawQuery(sql2, null);
		c.moveToFirst();
		c2.moveToFirst();
		double retval = c.getDouble(0) + c2.getDouble(0);
		c.close();
		c2.close();
		return retval;
	}

	/**
	 * Gibt den Notendurchschnitt zurück.
	 * 
	 * @return Notendurchschnitt
	 */
	public double getGrade()
	{
		String sql = "SELECT SUM((cp*weight)*grade), SUM((cp*weight)) FROM Course WHERE requiredCourse = 1 AND duration = 1 AND status =" + Status.STATUS_PASSED
				+ " AND grade != 0 ";
		String sql2 = "SELECT SUM((L.cp*C.weight)*C.grade), SUM(L.cp*C.weight) FROM LongCourseCp AS L LEFT OUTER JOIN Course AS C ON L.courseId = C._id WHERE C.status = " + Status.STATUS_PASSED + " AND C.grade != 0";
		String sql3 = "SELECT SUM((cp*weight)*grade), SUM((cp*weight)) FROM Criterion WHERE status =" + Status.STATUS_PASSED
				+ " AND grade != 0";

		String sql4 = "SELECT SUM(((C.optionalcp + O.additionalGradedCp)*C.weight)*C.grade), SUM(((C.optionalcp + O.additionalGradedCp)*C.weight)) FROM Course AS C LEFT JOIN CPOverhang AS O ON C._id = O.courseId WHERE C.requiredCourse = 0 AND C.duration = 1 AND C.status =" + Status.STATUS_PASSED
				+ " AND C.grade != 0 ";
		
		Cursor c = db.rawQuery(sql, null);
		Cursor c2 = db.rawQuery(sql2, null);
		Cursor c3 = db.rawQuery(sql3, null);
		Cursor c4 = db.rawQuery(sql4, null);

		double retval;

		if (c.moveToFirst() && c2.moveToFirst() && c3.moveToFirst() && c4.moveToFirst())
		{
			retval = (c.getDouble(0) + c2.getDouble(0) + c3.getDouble(0) +c4.getDouble(0)) / (c.getDouble(1) + c2.getDouble(1) + c3.getDouble(1) + c4.getDouble(1));
		} else
			retval = 0;

		c.close();
		c2.close();
		c3.close();
		c4.close();
		return retval;
	}

	/**
	 * Gibt den CP-Durchschnitt pro Semester zurück.
	 * 
	 * @return CP-Durchschnitt pro Semester
	 */
	public double averageCPForSemester()
	{

		double average = getCurrentCPWithSignedUpCourses() / PreferenceHelper.getSemesterInt(context);

		return average;
	}

	/**
	 * Gibt die Summe aller CP eines Semesters zurück.
	 * 
	 * @param semester
	 *            Semesterzahl
	 * @return Summe aller CP des Semesters
	 */
	public double getCPForSemester(int semester)
	{
		String sql3 = " + (SELECT IFNULL(SUM(cp), 0) FROM Criterion WHERE semester = " + semester + " AND ( status = " + Status.STATUS_PASSED + " OR status = "
				+ Status.STATUS_SIGNED_UP + " ) )";
		
		String sql2 = " + (SELECT IFNULL(SUM(L.cp), 0) FROM LongCourseCp AS L LEFT OUTER JOIN Course AS C ON L.courseId = C._id WHERE C.semester - 1 + L.part = "
				+ semester + " AND (C.status = " + Status.STATUS_SIGNED_UP + " OR C.status = " + Status.STATUS_PASSED + ") )";
		
		String sql = "SELECT IFNULL(SUM(cp), 0) " + sql2 + sql3 + " FROM Course WHERE ( status = " + Status.STATUS_PASSED + " OR status = "
				+ Status.STATUS_SIGNED_UP + " ) AND semester = " + semester + " AND duration = 1";

		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		double cp = c.getDouble(0);
		c.close();
		return cp;
	}

	/**
	 * Gibt den Notendurchschnitt eines Semesters zurück.
	 * 
	 * @param semester
	 *            Semesterzahl
	 * @return Notendurchschnitt des Semesters
	 */
	public double getGradeForSemester(int semester)
	{
		String sql = "SELECT SUM((cp*weight)*grade), SUM((cp*weight)) FROM Course WHERE duration = 1 AND status =" + Status.STATUS_PASSED
				+ " AND grade != 0 AND semester = " + semester;
		String sql2 = "SELECT SUM((L.cp*C.weight)*C.grade), SUM(L.cp*C.weight) FROM LongCourseCp AS L LEFT OUTER JOIN Course AS C ON L.courseId = C._id WHERE C.semester -1 + L.part = "
				+ semester + " AND C.status = " + Status.STATUS_PASSED + " AND C.grade != 0";
		String sql3 = "SELECT SUM((cp*weight)*grade), SUM((cp*weight)) FROM Criterion WHERE status =" + Status.STATUS_PASSED
				+ " AND grade != 0 AND semester = " + semester;

		Cursor c = db.rawQuery(sql, null);
		Cursor c2 = db.rawQuery(sql2, null);
		Cursor c3 = db.rawQuery(sql3, null);

		double retval;

		if (c.moveToFirst() && c2.moveToFirst() && c3.moveToFirst())
		{
			retval = (c.getDouble(0) + c2.getDouble(0) + c3.getDouble(0)) / (c.getDouble(1) + c2.getDouble(1) + c3.getDouble(1));
		} else
			retval = 0;

		c.close();
		c2.close();
		c3.close();
		return retval;
	}

	/**
	 * Gibt die erreichten CP eines Optionals zurück
	 * 
	 * @param id
	 *            ID des Optionals
	 * @return Erreichte CP des Optionals
	 */
	public double getAcquiredCPFromOptional(long id)
	{
		double cp;
		String sql = "SELECT acquiredcp FROM Optional WHERE _id = " + id;
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		cp = c.getDouble(0);
		c.close();
		return cp;
	}

	/**
	 * Gibt alle Kurse einer Kategorie zurück
	 * 
	 * @param categoryId
	 *            ID der Kategorie
	 * @return Liste aller Kurse dieser Kategorie
	 */
	public ArrayList<Course> getAllCoursesFromCategory(long categoryId)
	{
		ArrayList<Course> courses = new ArrayList<Course>();

		String sql = "SELECT * FROM Course WHERE categoryId = " + categoryId;
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Course course = new Course(c.getLong(0), c.getString(1), c.getString(3), c.getString(4), c.getDouble(5), c.getInt(6), c.getInt(7), c.getInt(8),
					c.getDouble(9), c.getInt(10), c.getInt(11), c.getInt(12), c.getDouble(13), context);
			course.category = c.getLong(2);
			c.moveToNext();

			courses.add(course);

		}
		c.close();
		return courses;
	}

	/**
	 * Holt ein Kursobjekt anhand seiner ID
	 * 
	 * @param courseId
	 *            ID des Kurses
	 * @return Objekt des Kurses
	 */
	public Course getCourse(long courseId)
	{
		String sql = "SELECT * FROM Course WHERE _id =" + courseId;
		Cursor c = db.rawQuery(sql, null);

		if (c.moveToFirst())
		{
			Course course = new Course(c.getLong(0), c.getString(1), c.getString(3), c.getString(4), c.getDouble(5), c.getInt(6), c.getInt(7), c.getInt(8),
					c.getDouble(9), c.getInt(10), c.getInt(11), c.getInt(12), c.getDouble(13), context);
			course.category = c.getLong(2);
			course.optionalcp = c.getDouble(14);
			c.close();
			return course;
		}
		c.close();
		return null;

	}

	/**
	 * Gibt den Namen eines Kurses anhand seiner ID zurück
	 * 
	 * @param courseId
	 *            ID des Kurses
	 * @return Kursname
	 */
	public String getCourseName(long courseId)
	{
		String sql = "SELECT name FROM Course WHERE _id = " + courseId;
		Cursor c = db.rawQuery(sql, null);

		if (c.moveToFirst())
		{
			String name = c.getString(0);
			c.close();
			return name;
		}
		c.close();
		return null;
	}

	/**
	 * Gibt einen Kurs anhand seiner VAK zurück.
	 * 
	 * @param vak
	 *            VAK des Kurses
	 * @return Objekt des Kurses
	 */
	public Course getCourseByVAK(String vak)
	{
		String sql = "SELECT * FROM Course WHERE vak = '" + vak + "'";
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();

		Course course = null;
		if (!c.isAfterLast())
		{
			course = new Course(c.getLong(0), c.getString(1), c.getString(3), c.getString(4), c.getDouble(5), c.getInt(6), c.getInt(7), c.getInt(8),
					c.getDouble(9), c.getInt(10), c.getInt(11), c.getInt(12), c.getDouble(13), context);
			course.category = c.getLong(2);
		}
		c.close();
		return course;

	}

	/**
	 * Erstellt ggf einen neuen Kurs Ist vllt depricated
	 * 
	 * @param id
	 * @return id des Kurses in Course
	 */
	public long getCourseFromUniversityCalendar(long id)
	{
		String sql = "Select C._id FROM Course AS C WHERE C.vak = (SELECT U.vak FROM UniversityCalendar AS U WHERE _id = " + id + ")";
		Cursor c = db.rawQuery(sql, null);
		long retval;
		if (c.moveToFirst())
			retval = c.getLong(0);
		else
		{
			UniversityCalendarCourse ucc = getUniversityCalendarCourse(id);
			Course course = new Course(0, ucc.name, ucc.vak, ucc.note, ucc.cp, ucc.necCP, ucc.status, ucc.semester, ucc.grade, ucc.requiredCourse,
					ucc.duration, 1, ucc.weight, context);
			retval = addCourse(course);
		}
		c.close();
		return retval;
	}

	/**
	 * Erstellt keinen neuen Kurs
	 * 
	 * @param id
	 *            ID des Kurses im UniversityCalendar
	 * @return id des Kurses in Course
	 */
	public long getCourseFromUniversityCalendarWithoutCreation(long id)
	{
		String sql = "Select C._id FROM Course AS C WHERE C.vak = (SELECT U.vak FROM UniversityCalendar AS U WHERE _id = " + id + ")";
		Cursor c = db.rawQuery(sql, null);
		long retval;
		if (c.moveToFirst())
			retval = c.getLong(0);
		else
			retval = -1;
		c.close();
		return retval;
	}

	/**
	 * Gibt alle Anforderungen eines Kurses HTML-formatiert zurück (Bestanden:
	 * Grün mit Haken, nicht bestanden: rot).
	 * 
	 * @param id
	 *            Id des Kriteriums
	 * @return String-Liste mit HTML-formatierten Anforderungen
	 */
	public String[] getCourseRequirementsAsHtml(long id)
	{
		// TODO: Geht sicher schöner

		// Erst Kurse
		String sql = "SELECT name, status FROM Requirements AS R LEFT OUTER JOIN Course AS C ON R.requiredCourseId=C._id WHERE R.courseId =" + id
				+ " AND R.requiredCourseId IS NOT NULL";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		String[] reqs = new String[c.getCount()];
		int i = 0;
		while (!c.isAfterLast())
		{
			if (c.getInt(1) == Status.STATUS_PASSED)
				reqs[i] = "<font color=green>" + c.getString(0) + " \u2714 </font>"; // (10004),
																						// Häkchen
			else
				reqs[i] = "<font color=red>" + c.getString(0) + "</font>";
			i++;
			c.moveToNext();
		}
		c.close();

		// Dann Kriterien
		sql = "SELECT name, status FROM Requirements AS R LEFT OUTER JOIN Criterion AS C ON R.requiredCriterionId=C._id WHERE R.courseId =" + id
				+ " AND R.requiredCriterionId IS NOT NULL";
		c = db.rawQuery(sql, null);
		c.moveToFirst();
		String[] reqs2 = new String[c.getCount()];
		i = 0;
		while (!c.isAfterLast())
		{
			if (c.getInt(1) == Status.STATUS_PASSED)
				reqs2[i] = "<font color=green>" + c.getString(0) + " \u2714 </font>";
			else
				reqs2[i] = "<font color=red>" + c.getString(0) + "</font>";
			i++;
			c.moveToNext();
		}
		c.close();

		// Zusammenführen
		String[] reqsFinal = new String[reqs.length + reqs2.length];
		i = 0;
		for (String str : reqs)
		{
			reqsFinal[i] = str;
			i++;
		}
		for (String str : reqs2)
		{
			reqsFinal[i] = str;
			i++;
		}
		return reqsFinal;
	}

	/*
	 * depricated
	 * 
	 * public ArrayList<Optional> getAllOptionals(long categoryId) {
	 * ArrayList<Optional> optionals = new ArrayList<Optional>();
	 * 
	 * String sql = "SELECT * FROM Optional WHERE categoryId = " + categoryId;
	 * Cursor c = db.rawQuery(sql, null);
	 * 
	 * c.moveToFirst(); while (c.isAfterLast() == false) {
	 * 
	 * Optional optional = new Optional(c.getLong(0), c.getString(1),
	 * c.getString(2), c.getInt(3), c.getString(4));
	 * 
	 * c.moveToNext();
	 * 
	 * optionals.add(optional);
	 * 
	 * } c.close(); return optionals; }
	 */

	/**
	 * Gibt den Namen eines Kriteriums anhand seiner ID zurück
	 * 
	 * @param criterionId
	 *            ID des Kriteriums
	 * @return Name des Kriteriums
	 */
	public String getCriteriumName(long criterionId)
	{
		String sql = "SELECT name FROM Criterion WHERE _id =" + criterionId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		String name = c.getString(0);
		c.close();
		return name;
	}

	/**
	 * Gibt das Kriterium-Objekt anhand seiner ID zurück
	 * 
	 * @param id
	 *            ID des Kriteriums
	 * @return Objekt des Kriteriums
	 */
	public Criterion getCriterion(long id)
	{
		String sql = "SELECT * FROM Criterion WHERE _id = " + id;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
		{
			Criterion criterion = new Criterion(c.getLong(0), c.getString(1), c.getFloat(3), c.getString(4), c.getInt(5), c.getInt(6), c.getInt(7),
					c.getInt(8), c.getInt(9), c.getDouble(10));
			c.close();

			String sql2 = "SELECT sem FROM Recommendation WHERE criterionId = " + id;
			criterion.alternative = Statics.calculateAlternative(db.rawQuery(sql2, null));

			return criterion;
		}
		c.close();

		return null;
	}

	/**
	 * Gibt alle Kriterien einer Kategorie zurück.
	 * 
	 * @param categoryId
	 *            ID der Kategorie
	 * @return Liste aller Kriterien-Objekte
	 */
	public ArrayList<Criterion> getAllCriterionsFromCategory(long categoryId)
	{
		ArrayList<Criterion> criterions = new ArrayList<Criterion>();

		String sql = "SELECT * FROM Criterion WHERE categoryId = " + categoryId;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{

			Criterion criterion = new Criterion(c.getLong(0), c.getString(1), c.getFloat(3), c.getString(4), c.getInt(5), c.getInt(6), c.getInt(7),
					c.getInt(8), c.getInt(9), c.getDouble(10));

			c.moveToNext();

			criterions.add(criterion);

		}
		c.close();
		return criterions;
	}

	/**
	 * Gibt alle Anforderungen eines Kriteriums HTML-formatiert zurück
	 * (Bestanden: Grün mit Haken, nicht bestanden: rot).
	 * 
	 * @param id
	 *            Id des Kriteriums
	 * @return String-Liste mit HTML-formatierten Anforderungen
	 */
	public String[] getCriterionRequirementsAsHtml(long id)
	{
		// TODO: Geht sicher schöner

		// Erst Kurse
		String sql = "SELECT name, status FROM Requirements AS R LEFT OUTER JOIN Course AS C ON R.requiredCourseId=C._id WHERE R.criterionId =" + id
				+ " AND R.requiredCourseId IS NOT NULL";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		String[] reqs = new String[c.getCount()];
		int i = 0;
		while (!c.isAfterLast())
		{
			if (c.getInt(1) == Status.STATUS_PASSED)
				reqs[i] = "<font color=green>" + c.getString(0) + " \u2714 </font>";
			else
				reqs[i] = "<font color=red>" + c.getString(0) + "</font>";
			i++;
			c.moveToNext();
		}
		c.close();

		// Dann Kriterien
		sql = "SELECT name, status FROM Requirements AS R LEFT OUTER JOIN Criterion AS C ON R.requiredCriterionId=C._id WHERE R.criterionId =" + id
				+ " AND R.requiredCriterionId IS NOT NULL";
		c = db.rawQuery(sql, null);
		c.moveToFirst();
		String[] reqs2 = new String[c.getCount()];
		i = 0;
		while (!c.isAfterLast())
		{
			if (c.getInt(1) == Status.STATUS_PASSED)
				reqs2[i] = "<font color=green>" + c.getString(0) + " \u2714 </font>";
			else
				reqs2[i] = "<font color=red>" + c.getString(0) + "</font>";
			i++;
			c.moveToNext();
		}
		c.close();

		// Zusammenühren
		String[] reqsFinal = new String[reqs.length + reqs2.length];
		i = 0;
		for (String str : reqs)
		{
			reqsFinal[i] = str;
			i++;
		}
		for (String str : reqs2)
		{
			reqsFinal[i] = str;
			i++;
		}
		return reqsFinal;
	}

	/**
	 * Gibt ein Studienfach-Objekt anhand seiner ID zurück
	 * 
	 * @param Id
	 *            ID des Studienfaches
	 * @return Objekt des Studienfaches
	 */
	public MainInfo getCourseOfStudies(int Id)
	{
		String sql = "SELECT * FROM CourseOfStudies";
		Cursor c = db.rawQuery(sql, null);

		MainInfo mi = null;
		c.moveToFirst();
		if (!c.isAfterLast())
			mi = new MainInfo(c.getLong(0), c.getString(1), c.getInt(2), c.getString(3), c.getInt(4), c.getInt(5), c.getInt(6), c.getString(7));
		c.close();
		return mi;
	}

	/**
	 * Gibt die ID des Studienfaches zurück
	 * 
	 * @return ID des Studienfaches
	 */
	public long getCourseOfStudiesId()
	{
		String sql = "SELECT _id FROM CourseOfStudies";
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		long retval = c.getLong(0);
		c.close();
		return retval;
	}

	/**
	 * Gibt einen UniversityCalendakurs-Objekt anhand seiner ID zurück.
	 * 
	 * @param id
	 *            ID des Kurses im UC
	 * @return Objekt des Kurses
	 */
	public UniversityCalendarCourse getUniversityCalendarCourse(long id)
	{
		String sql = "SELECT * FROM UniversityCalendar WHERE _id = " + id;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		UniversityCalendarCourse ucc = null;
		if (!c.isAfterLast())
		{
			ucc = new UniversityCalendarCourse(c.getLong(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4), c.getString(5), c.getString(6),
					c.getLong(7));
			ucc.weight = 1.0;
		}
		c.close();
		return ucc;
	}

	/**
	 * Gibt vereinte Informationen eines Kurses (UniversityCalendar und Course
	 * Table) zurück.
	 * 
	 * @param id
	 *            ID des Kurses
	 * @param type
	 *            gibt an, ob ID vom UniversityCalendar oder vom Course Table
	 *            ist.
	 * @return Objekt mit vereinten Informationen
	 */
	public UniversityCalendarCourse getMergedCourse(long id, int type)
	{
		String sql = "";

		if (type == Statics.TYPE_COURSE)
			sql = "SELECT C.* , U.date, U.staff, U.description FROM Course AS C LEFT OUTER JOIN UniversityCalendar AS U ON U.vak=C.vak WHERE C._id = " + id
					+ " GROUP BY U.vak";
		else
			sql = "SELECT C.*, U._id, U.note, U.cp, U.neccp, U.status, U.semester, U.grade, U.requiredCourse, U.duration, COALESCE(U.graded, 1) FROM UniversityCalendar AS C LEFT OUTER JOIN Course AS U ON U.vak=C.vak WHERE C._id = "
					+ id + " GROUP BY U.vak";

		Cursor c = db.rawQuery(sql, null);
		if (!c.moveToFirst())
		{
			c.close();
			return null;
		} else
		{
			UniversityCalendarCourse course = null;

			if (type == Statics.TYPE_COURSE)
			{
				course = new UniversityCalendarCourse(c.getLong(0), c.getString(1), c.getString(3), c.getString(4), c.getDouble(5), c.getInt(6), c.getInt(7),
						c.getInt(8), c.getDouble(9), c.getInt(10), c.getInt(11), c.getInt(12));

				course.weight = c.getDouble(13);
				course.date = c.getString(14);
				course.staff = c.getString(15);
				course.description = c.getString(16);
				course.courseId = course.id;

			} else if (type == Statics.TYPE_UNIVERSITY_CALENDAR_COURSE)
			{
				course = new UniversityCalendarCourse(c.getLong(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(4), c.getString(5),
						c.getString(6), c.getLong(7));

				course.courseId = c.getLong(8);
				course.note = c.getString(9);
				course.cp = c.getDouble(10);
				course.necCP = c.getInt(11);
				course.status = c.getInt(12);
				course.semester = c.getInt(13);
				course.grade = c.getDouble(14);
				course.requiredCourse = c.getInt(15);
				course.duration = c.getInt(16);
				course.graded = c.getInt(17);

				if (c.isNull(18))
					course.weight = 1.0;
				else
					course.weight = c.getDouble(18);

				id = course.courseId;

			}
			c.close();

			if (id != 0)
			{
				// Semestervorschläge laden
				String sql2 = "SELECT sem FROM Recommendation WHERE courseId = " + id;
				course.alternative = Statics.calculateAlternative(db.rawQuery(sql2, null));

			}

			return course;
		}
	}

	public Cursor getUniversityCalendarCoursesCursor(long id)
	{
		String sql;

		if (id == 0)
			sql = "SELECT U._id, U.name, C.status FROM UniversityCalendar AS U LEFT OUTER JOIN Course AS C ON U.vak=C.vak GROUP BY U.vak ORDER BY Upper(U.name) COLLATE LOCALIZED";
		else
			sql = "SELECT U._id, U.name, C.status FROM UniversityCalendar AS U LEFT OUTER JOIN Course AS C ON U.vak=C.vak WHERE U.courseOfStudiesId = " + id
					+ " GROUP BY U.vak ORDER BY Upper(U.name) COLLATE LOCALIZED";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getFilteredUniversityCalendarCoursesCursor(long id, String selectionArg)
	{
		String sql;

		if (id == 0)
			sql = "SELECT U._id, U.name, C.status FROM UniversityCalendar AS U LEFT OUTER JOIN Course AS C ON U.vak=C.vak WHERE U.name LIKE \"%" + selectionArg
					+ "%\" GROUP BY U.vak ORDER BY Upper(U.name) COLLATE LOCALIZED";
		else
			sql = "SELECT U._id, U.name, C.status FROM UniversityCalendar AS U LEFT OUTER JOIN Course AS C ON U.vak=C.vak WHERE U.courseOfStudiesId = " + id
					+ " AND U.name LIKE \"%" + selectionArg + "%\" GROUP BY U.vak ORDER BY Upper(U.name) COLLATE LOCALIZED";

		return db.rawQuery(sql, null);

	}

	public boolean getUniversityCalendarCourseDownloaded(long id)
	{
		String sql = "SELECT loaded FROM UniversityCalendarCourseOfStudies WHERE _id = " + id;
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		if (c.getInt(0) == 0)
		{
			c.close();
			return false;
		} else
		{
			c.close();
			return true;
		}
	}

	public String getUniversityCalendarCourseName(long id)
	{
		String sql = "SELECT name FROM UniversityCalendarCourseOfStudies WHERE _id = " + id;
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		String retval = c.getString(0);
		c.close();
		return retval;
	}

	/*
	 * Alle set-Methoden
	 */

	/**
	 * Setzt den Status eines Kurses auf "Bestanden" und füllt die Datenbank
	 * entsprechend
	 * 
	 * @param id
	 *            courseId
	 * @param semester
	 *            semester
	 * @param grade
	 *            gewuenschte note
	 * @param type
	 *            isRequired
	 * @param entry
	 *            Optional oder Choosable oder null
	 */
	public void setCoursePassed(long id, int semester, double grade, int type, ChildEntry entry)
	{
		ContentValues values = new ContentValues();
		values.put("semester", semester);
		values.put("grade", grade);
		values.put("status", Status.STATUS_PASSED);
		db.update("Course", values, "_id=" + id, null);

		// String optionalsql =
		// "Select optionalId FROM SelectedCourses WHERE courseId =" + id;
		// Cursor optionalc = db.rawQuery(optionalsql, null);
		if (type != 1)
		{
			if (entry instanceof Optional)
			{
				String opsql = "Select courseId FROM SelectedCourses WHERE courseId = " + id;
				Cursor opc = db.rawQuery(opsql, null);
				if (opc.getCount() < 1)
				{
					ContentValues op = new ContentValues();
					op.put("courseId", id);
					op.put("optionalId", ((Optional) entry).id);
					db.insert("SelectedCourses", null, op);
				}

				setAcquiredCPAndChosenCPOptional(entry, id);
				values.clear();
				values.put("weight", ((Optional) entry).weight);
				values.put("neccp", ((Optional) entry).necCp);
				values.put("graded", ((Optional) entry).graded);
				values.put("status", Status.STATUS_PASSED);
				db.update("Course", values, "_id=" + id, null);

				opc.close();
			} else if (entry instanceof Choosable)
			{
				db.update("Course", values, "_id=" + id, null);
				ContentValues ch = new ContentValues();
				ch.put("selectedCourseId", id);
				db.update("Choosable", ch, "_id = " + ((Choosable) entry).id, null);
			}
		} else
			db.update("Course", values, "_id=" + id, null);

		ContentValues req = new ContentValues();
		req.put("passed", 1);
		if (db.update("Requirements", req, "requiredCourseId = " + id, null) == 0)
			Log.e("nichts", "gefunden");

		checkForCourseRequirements(id);

		checkForNecCP();
		notifyDatabaseListeners();

		Toast.makeText(context, "Kurs wurde bestanden", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Markiert einen Kurs als nicht bestanden und resettet den wert in
	 * Requirements
	 * 
	 * @param id
	 */
	public void setCourseFailed(long id)
	{
		ContentValues values = new ContentValues();
		values.put("grade", 0);
		values.put("status", Status.STATUS_NOT_PASSED);
		if (db.update("Course", values, "_id=" + id, null) == 0)
			Log.e("nichts", "gefunden");

		ContentValues values2 = new ContentValues();
		values2.put("passed", 0);

		db.update("Requirements", values2, "requiredCourseId=" + id, null);
		checkForCourseRequirements(id);

		checkForNecCP();
		notifyDatabaseListeners();
	}

	/**
	 * Setzt den Status eines Kriteriums auf "Bestanden" inkl. Note wenn
	 * vorhanden
	 * 
	 * @param id
	 *            ID des Kriteriums
	 * @param semester
	 *            Semesterzahl
	 * @param grade
	 *            Note
	 */
	public void setCriterionPassed(long id, int semester, double grade)
	{
		ContentValues values = new ContentValues();
		values.put("semester", semester);
		values.put("grade", grade);
		values.put("status", Status.STATUS_PASSED);
		if (db.update("Criterion", values, "_id=" + id, null) == 0)
			Log.e("nichts", "gefunden");

		checkForCriterionRequirements(id);

		checkForNecCP();

		notifyDatabaseListeners();

		Toast.makeText(context, "Kriterium wurde erfüllt", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Setzt den Status eines Kriteriums auf "Nicht bestanden" zurück.
	 * 
	 * @param id
	 *            ID des Kriteriums
	 */
	public void setCriterionFailed(long id)
	{
		ContentValues values = new ContentValues();
		values.put("grade", 0);
		values.put("status", Status.STATUS_NOT_PASSED);
		if (db.update("Criterion", values, "_id=" + id, null) == 0)
			Log.e("nichts", "gefunden");

		ContentValues values2 = new ContentValues();
		values2.put("passed", 0);

		db.update("Requirements", values2, "requiredCriterionId=" + id, null);

		checkForCriterionRequirements(id);

		checkForNecCP();
		notifyDatabaseListeners();
	}

	/**
	 * Setzt den Status eines UniversityCalendar-Kurses auf "Angemeldet"
	 * 
	 * @param uccId
	 *            ID des Kurses im UniversityCalendar
	 * @param semester
	 *            Semesterzahl
	 * @param entry
	 *            Optional oder Choosable, zu dem der Kurs gehört
	 */
	public void setUniversityCalendarCourseSignedUp(long uccId, int semester, ChildEntry entry)
	{
		long id = 0;
		UniversityCalendarCourse ucc = getUniversityCalendarCourse(uccId);
		ContentValues values = new ContentValues();
		values.put("name", ucc.name);
		values.put("vak", ucc.vak);
		values.put("status", Status.STATUS_SIGNED_UP);
		values.put("semester", semester);

		if (entry instanceof Optional)
		{
			// values.put("requiredCourse", 0);

			if (db.update("Course", values, "vak = '" + ucc.vak + "'", null) != 1)
			{
				values.put("cp", ucc.cp);
				values.put("neccp", ((Optional) entry).necCp);
				values.put("graded", ((Optional) entry).graded);
				values.put("weight", ((Optional) entry).weight);
				id = db.insert("Course", null, values);
			}
			// id = getCourseFromUniversityCalendar(uccId);
			ContentValues values2 = new ContentValues();
			values2.put("courseId", id);
			values2.put("optionalId", ((Optional) entry).id);
			db.insert("SelectedCourses", null, values2);

			// optionalc.getLong(0);
			setAcquiredCPAndChosenCPOptional(entry, id);

		} else if (entry instanceof Choosable)
		{
			// values.put("requiredCourse", 2);
			id = db.update("Course", values, "vak = '" + ucc.vak + "'", null);
			if (id != 1)
			{
				values.put("cp", ucc.cp);
				db.insert("Course", null, values);
			}
			id = getCourseFromUniversityCalendar(uccId);
			ContentValues values2 = new ContentValues();
			values2.put("selectedCourseId", id);
			db.update("Choosable", values2, "_id = " + ((Choosable) entry).id, null);
		} else
			id = db.update("Course", values, "vak = '" + ucc.vak + "'", null);

		notifyDatabaseListeners();
	}

	/**
	 * funktioniert...Setzt die cp für das optional und überhangszp für freie
	 * wahl
	 */
	void setAcquiredCPAndChosenCPOptional(ChildEntry entry, long courseId)
	{
		double additionaloverhang = 0; 
		double chosencp = 0;
		double diff = 0;  
		double acquiredoptional = 0;
		double chosenoptional = 0;

		String optionalInfo = "Select acquiredcp, chosencp, _id, transmitcp, cp FROM Optional WHERE _id = " + ((Optional) entry).id
				+ " OR transmitcp = 1 ORDER BY CASE WHEN transmitcp is null then 0 else 1 end, transmitcp";
		Cursor coptionalInfo = db.rawQuery(optionalInfo, null);
		coptionalInfo.moveToFirst();

		String optionalAcquiredSum = "Select SUM(cp), (Select SUM(cp) FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE optionalId = (Select _id FROM Optional WHERE transmitcp = 1) AND status = "
				+ Status.STATUS_PASSED
				+ ") FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE optionalId = "
				+ ((Optional) entry).id
				+ " AND status = " + Status.STATUS_PASSED;
		Cursor coptionalAcquiredSum = db.rawQuery(optionalAcquiredSum, null);
		coptionalAcquiredSum.moveToFirst();
		Log.d("summe acquired", "" + coptionalAcquiredSum.getDouble(0));

		String optionalChosenSum = "Select SUM(cp), (Select SUM(cp) FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE optionalId = (Select _id FROM Optional WHERE transmitcp = 1)) FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE optionalId = "
				+ ((Optional) entry).id;
		Cursor coptionalChosenSum = db.rawQuery(optionalChosenSum, null);
		coptionalChosenSum.moveToFirst();

		String overhang = "Select SUM(additionalGradedCP) FROM CPOverhang";
		Cursor coverhang = db.rawQuery(overhang, null);
		coverhang.moveToFirst();

		String overhang2 = "Select SUM(additionalGradedCP) FROM CPOverhang LEFT OUTER JOIN Course ON courseId = _id WHERE status = " + Status.STATUS_PASSED;
		Cursor coverhang2 = db.rawQuery(overhang2, null);
		coverhang2.moveToFirst();

		String overhang3 = "Select SUM(C.additionalGradedCP) FROM CPOverhang AS C LEFT OUTER JOIN SelectedCourses AS S ON S.courseId = C.courseId WHERE S.optionalId != "
				+ ((Optional) entry).id;
		Cursor coverhang3 = db.rawQuery(overhang3, null);
		coverhang3.moveToFirst();

		String overhang4 = "Select SUM(C.additionalGradedCP) FROM CPOverhang AS C LEFT OUTER JOIN SelectedCourses AS S ON S.courseId = C.courseId WHERE S.optionalId != "
				+ ((Optional) entry).id + " AND S.courseId = (Select _id FROM Course WHERE status = " + Status.STATUS_PASSED + ")";
		Cursor coverhang4 = db.rawQuery(overhang4, null);
		coverhang4.moveToFirst();

		String status = "Select status, cp FROM Course WHERE _id = " + courseId;
		Cursor cstatus = db.rawQuery(status, null);
		cstatus.moveToFirst();

		String other = "Select _id, cp FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE optionalId = " + ((Optional) entry).id
				+ " AND status != " + Status.STATUS_PASSED;
		Cursor cother = db.rawQuery(other, null);
		cother.moveToFirst();

		String overhangcourse = "Select additionalGradedCP FROM CPOverhang WHERE courseId = " + courseId;
		Cursor coverhangcourse = db.rawQuery(overhangcourse, null);

		if (coptionalInfo.getInt(coptionalInfo.getColumnIndex("transmitcp")) != 1)
		{
			if (coptionalInfo.getDouble(coptionalInfo.getColumnIndex("cp")) < coptionalChosenSum.getDouble(0))
			{
				chosenoptional = coptionalInfo.getDouble(4);
				chosencp = coptionalChosenSum.getDouble(0) - coptionalInfo.getDouble(1);
				acquiredoptional = coptionalAcquiredSum.getDouble(0);
				if (cstatus.getInt(0) != Status.STATUS_PASSED)
				{
					if (!coverhangcourse.moveToFirst())
					{
						ContentValues values = new ContentValues();
						values.put("cp", cstatus.getDouble(1));
						values.put("courseId", courseId);
						Log.d("asdf", "" + coptionalChosenSum.getDouble(0) + " " + coptionalInfo.getDouble(1));
						values.put("additionalGradedCP", coptionalChosenSum.getDouble(0) - coptionalInfo.getDouble(4));
						db.insert("CPOverhang", null, values);
						values.clear();
						values.put("optionalcp", cstatus.getDouble(1) - (coptionalChosenSum.getDouble(0) - coptionalInfo.getDouble(4)));
						db.update("Course", values, "_id = " + courseId, null);
					}
					if(coptionalInfo.getDouble(4) >= coptionalAcquiredSum.getDouble(0))
					{
						String checkforotheroverhang = "Select C.additionalGradedCP, C.courseId FROM CPOverhang AS C LEFT OUTER JOIN SelectedCourses AS S  " +
													   " ON C.courseId = S.courseId WHERE C.courseId != " + courseId + " AND S.optionalId = " + ((Optional)entry).id;
						Cursor ccheckforotheroverhang = db.rawQuery(checkforotheroverhang, null);
						if(ccheckforotheroverhang.moveToFirst())
							while(!ccheckforotheroverhang.isAfterLast())
							{
								double overhangcheck = coptionalChosenSum.getDouble(0) - coptionalInfo.getDouble(coptionalInfo.getColumnIndex("cp"));
								if(ccheckforotheroverhang.getDouble(0) == overhangcheck)
								{
									db.delete("CPOverhang", "courseId = " + ccheckforotheroverhang.getLong(1), null);
									String s = "Select optionalcp FROM Course WHERE _id = " + ccheckforotheroverhang.getLong(1);
									Cursor c = db.rawQuery(s, null);
									c.moveToFirst();
									ContentValues vals = new ContentValues();
									vals.put("optionalcp", ccheckforotheroverhang.getDouble(0) + c.getDouble(0));
									c.close();
									db.update("Course", vals, "_id = " + ccheckforotheroverhang.getLong(1), null);
									ccheckforotheroverhang.moveToLast();
								}
								ccheckforotheroverhang.moveToNext();
							}
						ccheckforotheroverhang.close();
					}
					else
					{
						acquiredoptional = coptionalInfo.getDouble(4);
					}
				}
				else if (cstatus.getInt(0) == Status.STATUS_PASSED && coptionalInfo.getDouble(4) >= coptionalAcquiredSum.getDouble(0))
				{
					int dummy = db.delete("CPOverhang", "courseId = " + courseId, null);

					additionaloverhang = coptionalChosenSum.getDouble(0) - coptionalInfo.getDouble(4);
					if (!cother.isAfterLast())// && dummy == 1)
					{
						ContentValues values = new ContentValues();
						values.put("cp", cother.getDouble(1));
						values.put("courseId", cother.getLong(0));
						if (additionaloverhang <= cother.getDouble(1))
						{
							values.put("additionalGradedCP", additionaloverhang);
							db.insert("CPOverhang", null, values);

							values.clear();
							values.put("optionalcp", cother.getDouble(1) - additionaloverhang);
							db.update("Course", values, "_id = " + cother.getLong(0), null);
							cother.moveToLast();
							cother.moveToNext();
						} else
						{
							values.put("additionalGradedCP", cother.getDouble(1));
							db.insert("CPOverhang", null, values);

							values.clear();
							values.put("optionalcp", 0);
							db.update("Course", values, "_id = " + cother.getLong(0), null);
							additionaloverhang -= cother.getDouble(1);
							cother.moveToNext();
						}
					}

					

					cother.close();
					acquiredoptional = coptionalAcquiredSum.getDouble(0);
				} else if (coptionalInfo.getDouble(4) < coptionalAcquiredSum.getDouble(0))
					acquiredoptional = coptionalInfo.getDouble(4);
				
				if (getCurrentCP() > getCourseOfStudies((int) getCourseOfStudiesId()).requiredCp)
				{
					double additionaloroptionalcp = 0;
					boolean additional = false;
					ContentValues values = new ContentValues();

					diff = getCurrentCP() - getCourseOfStudies((int) getCourseOfStudiesId()).requiredCp;
					String othersql = "Select _id, additionalGradedCP, grade FROM CPOverhang LEFT OUTER JOIN Course ON courseId =_id ORDER BY grade DESC";
					Cursor cothersql = db.rawQuery(othersql, null);
					cothersql.moveToFirst();
					String othersql2 = "Select _id, optionalcp, grade FROM Course WHERE _id = (Select courseId FROM SelectedCourses WHERE optionalId = (Select _id FROM Optional WHERE transmitcp = 1)) ORDER BY grade DESC";
					Cursor cothersql2 = db.rawQuery(othersql2, null);
					Log.d("test", "test");
					cothersql2.moveToFirst();
					while (diff > 0)
					{
						if (!cothersql.isAfterLast())
						{
							additionaloroptionalcp = cothersql.getDouble(1);
							additional = true;
							if (!cothersql2.isAfterLast() && cothersql.getDouble(2) <= cothersql2.getDouble(2))
							{
								additionaloroptionalcp = cothersql2.getDouble(1);
								additional = false;
							}
						} else if(!cothersql2.isAfterLast())
						{
							additionaloroptionalcp = cothersql2.getDouble(1);
							additional = false;
						}
						if(!cothersql.isAfterLast() || !cothersql2.isAfterLast()){
						if (diff - additionaloroptionalcp >= 0)
						{
							if (additional)
							{
								db.delete("CPOverhang", "courseId = " + cothersql.getLong(0), null);
								cothersql.moveToNext();
							} else
							{
								values.put("optionalcp", cothersql2.getDouble(1) - additionaloroptionalcp);
								db.update("Course", values, "_id = " + cothersql2.getLong(0), null);
								cothersql2.moveToNext();
							}
							diff -= additionaloroptionalcp;
						} else
						{
							diff = additionaloroptionalcp - diff;
							Log.d("diff", "" + diff);
							Log.d("additionaloroptionalcp", "" + additionaloroptionalcp);
							if (additional)
							{
								values.put("additionalGradedCP", diff);
								db.update("CPOverhang", values, "courseId = " + cothersql.getLong(0), null);
							} else
							{
								values.put("optionalcp", diff);
								db.update("Course", values, "_id = " + cothersql2.getLong(0), null);
							}
							values.clear();
							diff = 0;
						}
						}
						else
							diff=0;
					}
				}
			} else
			{
				chosenoptional = coptionalChosenSum.getDouble(0);
				acquiredoptional = coptionalAcquiredSum.getDouble(0);
			}

			ContentValues optionalValues = new ContentValues();
			optionalValues.put("acquiredcp", acquiredoptional);
			optionalValues.put("chosencp", chosenoptional);
			db.update("Optional", optionalValues, "_id = " + ((Optional) entry).id, null);
		} 
		else
		{
			ContentValues values = new ContentValues();
			values.put("optionalcp", cstatus.getDouble(1));
			db.update("Course", values, "_id = " + courseId, null);
			if (getCurrentCP() > getCourseOfStudies((int) getCourseOfStudiesId()).requiredCp)
			{
				double additionaloroptionalcp = 0;
				boolean additional = false;

				diff = getCurrentCP() - getCourseOfStudies((int) getCourseOfStudiesId()).requiredCp;
				String othersql = "Select _id, additionalGradedCP, grade FROM CPOverhang LEFT OUTER JOIN Course ON courseId =_id ORDER BY grade";
				Cursor cothersql = db.rawQuery(othersql, null);
				cothersql.moveToFirst();
				String othersql2 = "Select _id, optionalcp, grade FROM Course WHERE _id = (Select courseId FROM SelectedCourses WHERE optionalId = (Select _id FROM Optional WHERE transmitcp = 1)) ORDER BY grade";
				Cursor cothersql2 = db.rawQuery(othersql2, null);
				cothersql2.moveToFirst();
				while (diff > 0)
				{
					if (!cothersql.isAfterLast())
					{
						additionaloroptionalcp = cothersql.getDouble(1);
						additional = true;
						if (!cothersql2.isAfterLast() && cothersql.getDouble(2) <= cothersql2.getDouble(2))
						{
							additionaloroptionalcp = cothersql2.getDouble(1);
							additional = false;
						}
					} else if (!cothersql2.isAfterLast())
					{
						additionaloroptionalcp = cothersql2.getDouble(1);
						additional = false;
					}
					if (diff - additionaloroptionalcp >= 0)
					{
						if (additional)
						{
							db.delete("CPOverhang", "courseId = " + cothersql.getLong(0), null);
							cothersql.moveToNext();
						} else
						{
							values.put("optionalcp", cothersql2.getDouble(1) - additionaloroptionalcp);
							db.update("Course", values, "_id = " + cothersql2.getLong(0), null);
							cothersql2.moveToNext();
						}
						diff -= additionaloroptionalcp;
					} else
					{
						diff = additionaloroptionalcp - diff;
						if (additional)
						{
							values.put("additionalGradedCP", diff);
							db.update("CPOverhang", values, "courseId = " + cother.getLong(0), null);
						} else
						{
							values.put("optionalcp", diff);
							db.update("Course", values, "_id = " + cothersql2.getLong(0), null);
						}
						values.clear();
						diff = 0;
					}
				}
			}

		}
		if (coptionalInfo.getInt(coptionalInfo.getColumnIndex("transmitcp")) != 1)
			coptionalInfo.moveToNext(); 

		coverhang.close();
		coverhang = db.rawQuery(overhang, null);
		coverhang.moveToFirst();

		coverhang2.close();
		coverhang2 = db.rawQuery(overhang2, null);
		coverhang2.moveToFirst();

		ContentValues transmitValues = new ContentValues();
		transmitValues.put("acquiredcp", coverhang2.getDouble(0) + coptionalAcquiredSum.getDouble(1));// +
																										// coverhang4.getDouble(0)
																										// );//+
																										// acquiredcp
																										// +
																										// coptionalInfo.getDouble(0));
		transmitValues.put("chosencp", coverhang.getDouble(0) + coptionalChosenSum.getDouble(1));// +
																									// coverhang3.getDouble(0)
																									// );//
																									// +
																									// chosencp
																									// +
																									// coptionalInfo.getDouble(1));
		db.update("Optional", transmitValues, "transmitcp = 1", null);

		coverhang.close();
		coverhang2.close();
		coverhang3.close();
		coverhang4.close();
		coptionalAcquiredSum.close();
		coptionalChosenSum.close();
		coptionalInfo.close();
	}

	/*
	 * void setAcquiredCPAndChosenCPOptional(ChildEntry entry, long courseId) {
	 * double acquiredcp = 0; double chosencp = 0; double cp; ContentValues
	 * values = new ContentValues(); ContentValues values2 = new
	 * ContentValues(); long substituteId = courseId; double substitutecp = 0;
	 * 
	 * //alle kurse, die der freien wahl angehören und in cpoverhang gespeichert
	 * sind. atm wird das nicht gebraucht String condition; String consql =
	 * "Select courseId FROM SelectedCourses WHERE optionalId = (Select _id FROM Optional WHERE transmitcp = 1)"
	 * ; Cursor conc = db.rawQuery(consql, null); if(conc.moveToFirst()) {
	 * condition = "(_id != " + conc.getLong(0); conc.moveToNext(); while
	 * (!conc.isAfterLast()) { condition = condition + " AND _id != " +
	 * conc.getLong(0); conc.moveToNext(); } condition = condition + ")"; } else
	 * condition = "_id != 0";
	 * 
	 * String acquiredsql =
	 * "Select SUM(additionalGradedCP) FROM CPOverhang LEFT OUTER JOIN Course ON courseId = _id WHERE status = "
	 * + Status.STATUS_PASSED + " AND " + condition; Cursor acquiredc =
	 * db.rawQuery(acquiredsql, null); String chosensql =
	 * "Select SUM(additionalGradedCP) FROM CPOverhang LEFT OUTER JOIN Course ON courseId = _id WHERE "
	 * + condition; Cursor chosenc = db.rawQuery(chosensql, null);
	 * 
	 * 
	 * String sql4 =
	 * "Select acquiredcp, chosencp, _id, transmitcp, cp FROM Optional WHERE _id = "
	 * + ((Optional) entry).id +
	 * " OR transmitcp = 1 ORDER BY CASE WHEN transmitcp is null then 0 else 1 end, transmitcp"
	 * ;
	 * 
	 * Cursor c4 = db.rawQuery(sql4, null); c4.moveToFirst();
	 * 
	 * String sql = "Select courseId FROM SelectedCourses WHERE optionalId = " +
	 * ((Optional) entry).id; Cursor c = db.rawQuery(sql, null);
	 * 
	 * if (c.moveToFirst()) { condition = "_id = " + c.getLong(0);
	 * c.moveToNext(); while (!c.isAfterLast()) { condition = condition +
	 * " OR _id = " + c.getLong(0); c.moveToNext(); } } else condition =
	 * "_id != 0";
	 * 
	 * 
	 * String sql2 = "SELECT SUM(cp) FROM Course WHERE  status=" +
	 * Status.STATUS_PASSED + " AND (" + condition + ")"; Cursor c2 =
	 * db.rawQuery(sql2, null); c2.moveToFirst();
	 * 
	 * String sql3 = "SELECT SUM(cp) FROM Course WHERE " + condition; Cursor c3
	 * = db.rawQuery(sql3, null); c3.moveToFirst();
	 * 
	 * cp = Math.abs(c2.getDouble(0) - c4.getDouble(0));
	 * 
	 * if(c2.getDouble(0) <= c4.getDouble(4) && c4.getInt(3) != 1) {
	 * values.put("acquiredcp", c2.getDouble(0));
	 * 
	 * values2.put("optionalcp", cp); db.update("Course", values2, "_id = " +
	 * courseId, null); db.delete("CPOverhang", "courseId = " + courseId, null);
	 * //todo: hier wohl and der falschen stelle! if(c3.getDouble(0) >
	 * c4.getDouble(4) && c4.getInt(3) != 1) { sql =
	 * "Select courseId, cp, optionalcp FROM SelectedCourses LEFT OUTER JOIN Course ON courseId = _id WHERE optionalId = "
	 * + ((Optional) entry).id + " AND courseId != " + courseId +
	 * " AND status != " + Status.STATUS_PASSED; c.close(); c = db.rawQuery(sql,
	 * null); if(c.moveToFirst()) { values2.clear(); values2.put("courseId",
	 * c.getLong(0)); values2.put("cp", c.getDouble(1));
	 * values2.put("additionalGradedCP", c3.getDouble(0) - c4.getDouble(4));
	 * db.insert("CPOverhang", null, values2); values2.clear();
	 * values2.put("optionalcp", cp - (c3.getDouble(0) - c4.getDouble(4)));
	 * db.update("Course", values2, "_id = " + c.getLong(0), null); substituteId
	 * = c.getLong(0); substitutecp = c.getDouble(1); } } } else if(c4.getInt(3)
	 * == 1) { if(acquiredc.moveToFirst()) values.put("acquiredcp",
	 * c2.getDouble(0) + acquiredc.getDouble(0)); else values.put("acquiredcp",
	 * c2.getDouble(0)); values2.put("optionalcp", cp); db.update("Course",
	 * values2, "_id = " + courseId, null); } else { values.put("acquiredcp",
	 * c4.getDouble(4)); acquiredcp = c2.getDouble(0) - c4.getDouble(4);
	 * 
	 * values2.put("optionalcp", cp - (acquiredcp)); db.update("Course",
	 * values2, "_id = " + courseId, null);
	 * 
	 * c2.close(); sql2 = "Select courseId FROM CPOverhang WHERE courseId = " +
	 * courseId; c2 = db.rawQuery(sql2, null); if(!c2.moveToFirst()) { chosencp
	 * = c3.getDouble(0) - c4.getDouble(4); values2.clear(); values2.put("cp",
	 * cp); values2.put("additionalGradedCP", acquiredcp);
	 * values2.put("courseId", courseId); db.insert("CPOverhang", null,
	 * values2); } }
	 * 
	 * 
	 * if(c3.getDouble(0) <= c4.getDouble(4) && c4.getInt(3) != 1)
	 * values.put("chosencp", c3.getDouble(0)); else if(c4.getInt(3) == 1) {
	 * if(chosenc.moveToFirst()) values.put("chosencp", c3.getDouble(0) +
	 * chosenc.getDouble(0)); else values.put("chosencp", c3.getDouble(0)); }
	 * else { values.put("chosencp", c4.getDouble(4));
	 * 
	 * c2.close(); sql2 = "Select courseId FROM CPOverhang WHERE courseId = " +
	 * substituteId; c2 = db.rawQuery(sql2, null); if(!c2.moveToFirst()) {
	 * chosencp = c3.getDouble(0) - c4.getDouble(4); values2.clear();
	 * values2.put("cp", substitutecp); values2.put("additionalGradedCP",
	 * chosencp); values2.put("courseId", substituteId); db.insert("CPOverhang",
	 * null, values2); }
	 * 
	 * } db.update("Optional", values, "_id=" + ((Optional) entry).id, null);
	 * 
	 * if(c4.moveToNext()) { c2.close(); values.clear();
	 * values.put("acquiredcp", c4.getDouble(0) + acquiredcp);
	 * values.put("chosencp", c4.getDouble(1) + chosencp);
	 * 
	 * db.update("Optional", values, "_id = "+c4.getLong(2), null); }
	 * 
	 * c2.close(); sql2 = "Select * FROM Requirements WHERE optionalId = " +
	 * ((Optional) entry).id; c2 = db.rawQuery(sql2, null); int flag = 1;
	 * if(c2.moveToFirst()) { ContentValues reqValues = new ContentValues();
	 * 
	 * if(!c2.isAfterLast()) { if(!c2.isNull(1))
	 * reqValues.put("requiredCourseId", c2.getLong(1)); if(!c2.isNull(2))
	 * reqValues.put("requiredCriterionId", c2.getLong(2)); if(!c2.isNull(3))
	 * reqValues.put("criterionId", c2.getLong(3)); Log.d("id = ", ""+
	 * courseId); reqValues.put("courseId", courseId); reqValues.put("passed",
	 * c2.getInt(5));
	 * 
	 * flag = c2.getInt(5) * flag; c2.moveToNext(); }
	 * 
	 * c2.close(); sql2 = "Select courseId FROM Requirements WHERE courseId = "
	 * + courseId; c2 = db.rawQuery(sql2, null); if(!c2.moveToFirst()) {
	 * db.insert("Requirements", null, reqValues);
	 * 
	 * if(flag == 0) { values.clear(); values.put("status",
	 * Status.STATUS_REQUIREMENTS_MISSING); db.update("Course", values, "_id = "
	 * + courseId, null); } } }
	 * 
	 * c.close(); c2.close(); c3.close(); c4.close();
	 * 
	 * }
	 */
	/**
	 * Setzt einen Kurs auf den Status "angemeldet"
	 * 
	 * @param id
	 *            ID des Kurses
	 * @param semester
	 *            Semesterzahl
	 */
	public void setCourseSignedUp(long id, int semester)
	{
		ContentValues values = new ContentValues();
		values.put("semester", semester);
		values.put("status", Status.STATUS_SIGNED_UP);
		db.update("Course", values, "_id=" + id, null);

		Toast.makeText(context, "Für den Kurs angemeldet", Toast.LENGTH_SHORT).show();
		notifyDatabaseListeners();

	}

	/**
	 * Führt Änderungen an einem Kurs durch und passt die Datenbank an diese
	 * Änderungen an.
	 * 
	 * @param id
	 *            ID des Kurses
	 * @param semester
	 *            Semesterzahl
	 * @param grade
	 *            Note
	 * @param optional
	 *            Optiona, zu dem der Kurs ggf. gehört
	 * @param cp
	 *            CP des Kurses
	 * @param status
	 *            Status des Kurses
	 */
	public void setCourseEdited(long id, int semester, double grade, ChildEntry optional, double cp, int status)
	{
		ContentValues req = new ContentValues();
		ContentValues values = new ContentValues();
		values.put("semester", semester);
		values.put("grade", grade);
		//values.put("status", status);
		//values.put("optionalcp", cp);
		//values.put("cp", cp);

		if (optional != null)
		{
			//todo: cp anders? richtig setzen
			if (optional instanceof Optional)
			{
				values.put("weight", ((Optional) optional).weight);

				
				String sql = "SELECT optionalId FROM SelectedCourses WHERE  courseId=" + id;
				Cursor c = db.rawQuery(sql, null);
				
				/*if (!c.moveToFirst())
					addSelectedCourse(optional.id, id, 0);
				else if(((Optional)optional).id != c.getLong(0))
				{*/
					removeCourseFromOptionalOrChoosable(id);
					/*ContentValues values2 = new ContentValues();
					values2.put("optionalId", ((Optional) optional).id);
					values2.put("courseId", id);
					if (db.insert("SelectedCourses", null, values2) == 0)
						Log.e("nichts", "gefunden");
					//values.put("optionalcp", cp);
					adjustCPForOptionalOrChoosable(((Optional) optional).id, id, cp, 0, 0);
					*/
					values.put("cp", cp);
					db.update("Course", values, "_id=" + id, null);
					addSelectedCourse(optional.id, id, cp);
				/*}
				else*/
					//adjustCPForOptionalOrChoosable(((Optional) optional).id, id, cp, 0, 0);
					
				c.close();
				values.put("status", status);
				if (status == Status.STATUS_PASSED)
				{
					req.put("passed", 1);
					if (db.update("Requirements", req, "requiredCourseId = " + id, null) == 0)
						Log.e("nichts", "gefunden");
				} else
				{
					req.put("passed", 0);
					if (db.update("Requirements", req, "requiredCourseId = " + id, null) == 0)
						Log.e("nichts", "gefunden");
				}
				db.update("Course", values, "_id=" + id, null);
				
				setAcquiredCPAndChosenCPOptional(optional, id);
				values.clear();
				values.put("weight", ((Optional) optional).weight);
				values.put("neccp", ((Optional) optional).necCp);
				values.put("graded", ((Optional) optional).graded);
				values.put("status", status);
				db.update("Course", values, "_id=" + id, null);
			} else if (optional instanceof Choosable)
			{
				db.update("Course", values, "_id=" + id, null);

				String sql = "SELECT _id FROM Choosable WHERE selectedCourseId=" + id;
				Cursor c = db.rawQuery(sql, null);

				// hier evtl weitermachen, verhalten von neuen choosables
				// nachfragen
				if (!c.moveToFirst())
				{
					removeCourseFromOptionalOrChoosable(id);
					ContentValues values2 = new ContentValues();
					values2.put("selectedCourseId", id);
					if (db.update("Choosable", values2, " _id = " + optional.id, null) == 0)
						Log.e("nichts", "gefunden");
					values.clear();
					values.put("status", status);
					values.put("cp", cp);
					db.update("Course", values, "_id=" + id, null);
				}
				c.close();
			}
		}
		else
		{
			values.put("status", status);
			values.put("cp", cp);
			if (status == Status.STATUS_PASSED)
			{
				req.put("passed", 1);
				if (db.update("Requirements", req, "requiredCourseId = " + id, null) == 0)
					Log.e("nichts", "gefunden");
			} else
			{
				req.put("passed", 0);
				if (db.update("Requirements", req, "requiredCourseId = " + id, null) == 0)
					Log.e("nichts", "gefunden");
			}
			
			db.update("Course", values, "_id=" + id, null);
		}

		checkForCourseRequirements(id);

		checkForNecCP();

		notifyDatabaseListeners();

		Toast.makeText(context, "Kurs wurde geändert", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Führt Änderungen an einem Kriterium durch und passt die Datenbank daran
	 * an.
	 * 
	 * @param id
	 *            ID des Kriteriums
	 * @param semester
	 *            Semesterzahl
	 * @param grade
	 *            Note
	 * @param cp
	 *            CP des Kriteriums
	 * @param status
	 *            Status des Kriteriums
	 */
	public void setCriterionEdited(long id, int semester, double grade, double cp, int status)
	{
		ContentValues values = new ContentValues();

		values.put("semester", semester);

		values.put("grade", grade);
		values.put("status", status);

		values.put("cp", cp);

		if (db.update("Criterion", values, "_id=" + id, null) == 0)
			Log.e("nichts", "gefunden");

		checkForCriterionRequirements(id);

		checkForNecCP();

		notifyDatabaseListeners();

		Toast.makeText(context, "Kriterium wurde geändert", Toast.LENGTH_SHORT).show();

	}

	/**
	 * Trägt eine Choosable-Kurswahl in ie Datenbank ein
	 * 
	 * @param choosableId
	 *            ID des Choosables
	 * @param courseId
	 *            ID des gewählten Kurses
	 */
	public void setChoosableCourseChoice(long choosableId, long courseId)
	{
		ContentValues values = new ContentValues();
		values.put("selectedCourseId", courseId);

		db.update("Choosable", values, "_id=" + choosableId, null);

		notifyDatabaseListeners();
	}

	public String getOptionalCourseSuggestion(long optionalId)
	{
		String sql = "SELECT coursename, coursevak FROM OptionalRecommendation WHERE coursename IS NOT NULL AND optionalId = " + optionalId;
		Cursor c = db.rawQuery(sql, null);

		String courses = null;

		if (c.moveToFirst())
		{
			if (c.getPosition() == 0)
				courses = "  -" + c.getString(0) + " (" + c.getString(1) + ")";
			else
				courses += "\n  -" + c.getString(0) + " (" + c.getString(1) + ")";
		}
		c.close();

		return courses;

	}

	public String getChoosableCourseSuggestion(long choosableId)
	{
		String sql = "SELECT courseId FROM Recommendation WHERE courseId IS NOT NULL AND choosableId = " + choosableId;
		Cursor c = db.rawQuery(sql, null);

		String course = null;

		if (c.moveToFirst())
		{
			Course tmp = getCourse(c.getLong(0));
			course = "  -" + tmp.name + " (" + tmp.vak + ")";
		}
		c.close();

		return course;

	}

	/**
	 * @deprecated
	 * 
	 * @param selectedCourseId
	 * @param vak
	 */
	@Deprecated
	public void setChoosableCourseChoice(long selectedCourseId, String vak)
	{

		String sql = "Select _id FROM Course WHERE vak = '" + vak + "'";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();

		ContentValues values = new ContentValues();
		values.put("selectedCourseId", selectedCourseId);

		db.update("Choosable", values, "_id=" + c.getLong(0), null);
		c.close();
		notifyDatabaseListeners();
	}

	/**
	 * @deprecated
	 * 
	 * @param courseId
	 * @return
	 */
	@Deprecated
	public Category getCategoryOfCourse(long courseId)
	{
		String sql = "Select categoryId FROM Course WHERE _id = " + courseId;
		Cursor c = db.rawQuery(sql, null);
		Category cat = null;
		c.moveToFirst();
		if (c.getLong(0) != 0)
			cat = getCategory(c.getLong(0));
		c.close();
		return cat;
	}

	/**
	 * @deprecated
	 * 
	 * @param id
	 * @return
	 */
	@Deprecated
	public boolean getPassedFlag(long id)
	{
		String sql = "SELECT passed FROM Requirements WHERE couseId =" + id;
		Cursor c = db.rawQuery(sql, null);

		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			if (c.getInt(0) == 0)
				return false;
			c.moveToNext();
		}
		c.close();
		return true;
	}

	/*
	 * Regulation Buffer
	 */

	/**
	 * Gibt alle Informationen des RegulationBuffers zurück.
	 * 
	 * @return Cursor auf den Informationen des RegulationBuffers
	 */
	public Cursor getRegulationBuffer()
	{
		String sql = "SELECT * FROM RegulationBuffer";
		return db.rawQuery(sql, null);
	}

	/**
	 * Fügt einen Prüfungsordnungs-Eintrag in den RegulationBuffer hinzu.
	 * 
	 * @param name
	 *            Name der Prüfungsordnung
	 * @param date
	 *            Datum
	 * @param originalName
	 *            Dateiname
	 * @param pdfName
	 *            Name der dazugehörigen PDF
	 */
	public void insertInRegulationBuffer(String name, String date, String originalName, String pdfName)
	{
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("date", date);
		values.put("originalName", originalName);
		values.put("pdfName", pdfName);
		db.insert("RegulationBuffer", null, values);
	}

	/**
	 * Leert den RegulaionBuffer
	 */
	public void emptyRegulationBuffer()
	{
		db.delete("RegulationBuffer", null, null);
	}

	/**
	 * Führt eine Suche im RegulationBuffer durch
	 * 
	 * @param selectionArg
	 *            Filterung
	 * @return Cursor auf dem Ergebnis der Suche
	 */
	public Cursor searchInRegulationBuffer(String selectionArg)
	{
		return db.rawQuery("SELECT * FROM RegulationBuffer WHERE name LIKE \"%" + selectionArg + "%\" ORDER BY Upper(name)  COLLATE LOCALIZED", null);
	}

	/**
	 * Sucht anhand einer ID im RegulationBuffer
	 * 
	 * @param id
	 *            ID der Prüfungsordnung
	 * @return Cursor auf dem Ergebnis der Suche
	 */
	public Cursor searchIdInRegulationBuffer(String id)
	{
		Cursor c = db.rawQuery("SELECT * FROM RegulationBuffer WHERE _id =" + id, null);

		return c;
	}

	/*
	 * delete-Methoden
	 */

	/**
	 * Löscht einen Kurs aus dem Course-Table
	 * 
	 * @param id
	 *            ID des Kurses
	 */
	public void deleteCourse(long id)
	{
		db.delete("Course", "_id=" + id, null);
		checkForCourseRequirements(id);
		checkForNecCP();
	}

	/**
	 * Löscht ein Kriterium aus dem Criterion-Table
	 * 
	 * @param id
	 *            ID des Kriteriums
	 */
	public void deleteCriterion(long id)
	{
		db.delete("Criterion", "_id=" + id, null);
		checkForCriterionRequirements(id);
		checkForNecCP();
	}

	/**
	 * Löscht einen Kurs aus einem Optional oder Choosable. Im Choosable wird
	 * nur der selectedCourseId Wert auf null gesetzt, bei Optional wird der
	 * Kurs aus der Datenbank gelöscht
	 * 
	 * @param id
	 *            ID des Kurses
	 */
	public void deleteCourseFromOptionalOrChoosable(long courseId)
	{
		boolean continuewithoptional = false;

		Course course = getCourse(courseId);
		if (course.requiredCourse == 2)
		{
			String sql = "Select courseId FROM SelectedCourses WHERE courseId = " + course.id;
			Cursor c = db.rawQuery(sql, null);
			if (c.moveToFirst())
				continuewithoptional = true;
			else
			{
				ContentValues values = new ContentValues();
				values.putNull("selectedCourseId");
				db.update("Choosable", values, "selectedCourseId = " + course.id, null);
				ContentValues values2 = new ContentValues();
				values2.put("status", Status.STATUS_NOT_PASSED);
				values2.putNull("grade");
				db.update("Course", values2, "_id = " + course.id, null);
				checkForCourseRequirements(course.id);
			}
			c.close();
		}
		if (course.requiredCourse == 0 || continuewithoptional)
		{
			deleteCourseFromOptional(course);
			if (course.requiredCourse == 0)
				deleteCourse(course.id);
		}

		notifyDatabaseListeners();
	}

	/**
	 * Löscht einen Kurs aus einem Optional oder Choosable. Im Choosable wird
	 * nur der selectedCourseId Wert auf null gesetzt, bei Optional wird der
	 * Kurs nur aus SelectedCourses ausgetragen
	 * 
	 * @param id
	 */
	public void removeCourseFromOptionalOrChoosable(long id)
	{
		boolean continuewithoptional = false;
		// auch bei delete machen

		Course course = getCourse(id);
		if (course.requiredCourse == 2)
		{
			String sql = "Select courseId FROM SelectedCourses WHERE courseId = " + course.id;
			Cursor c = db.rawQuery(sql, null);
			if (c.moveToFirst())
				continuewithoptional = true;
			else
			{
				ContentValues values = new ContentValues();
				values.putNull("selectedCourseId");
				db.update("Choosable", values, "selectedCourseId = " + course.id, null);
			}
			c.close();
		}
		if (course.requiredCourse == 0 || continuewithoptional)
		{
			deleteCourseFromOptional(course);
		}

		notifyDatabaseListeners();
	}

	public void deleteCourseFromOptional(Course course)
	{
		ContentValues values = new ContentValues();
		ContentValues values2 = new ContentValues();

		String optional = "Select cp, transmitcp, _id, acquiredcp, chosencp FROM Optional WHERE _id = (Select optionalId FROM SelectedCourses WHERE courseId = "
				+ course.id + ")";
		Cursor coptional = db.rawQuery(optional, null);
		coptional.moveToFirst();

		String transmit = "Select cp, _id, acquiredcp, chosencp FROM Optional WHERE transmitcp = 1";
		Cursor ctransmit = db.rawQuery(transmit, null);
		ctransmit.moveToFirst();

		String optionalChosenSum = "Select SUM(cp) FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE optionalId = " + coptional.getLong(2);
		Cursor coptionalChosenSum = db.rawQuery(optionalChosenSum, null);
		coptionalChosenSum.moveToFirst();
  
		String overhangcourse = "Select additionalGradedCP, status FROM CPOverhang LEFT OUTER JOIN Course ON courseId = _id WHERE courseId = " + course.id;
		Cursor coverhangcourse = db.rawQuery(overhangcourse, null);

		if (coptional.getInt(1) == 1 || coptional.getDouble(0) >= coptionalChosenSum.getDouble(0))
		{
			if (course.status == Status.STATUS_PASSED)
				values.put("acquiredcp", coptional.getDouble(3) - course.cp);
			values.put("chosencp", coptional.getDouble(4) - course.cp);
			db.update("Optional", values, "_id = " + coptional.getLong(2), null);
		} else
		{
			double overhangchosen = 0;
			double overhangacquired = 0;
			double overhangchosen2 = 0;
			double overhangacquired2 = 0;

			if (coverhangcourse.moveToFirst())
			{
				db.delete("CPOverhang", "courseId = " + course.id, null);
				course.cp -= coverhangcourse.getDouble(0);
				values.put("chosencp", coptional.getDouble(4) - course.cp);	
				values2.put("chosencp", ctransmit.getDouble(3) - coverhangcourse.getDouble(0));
				if (coverhangcourse.getInt(1) == Status.STATUS_PASSED)
				{
					values.put("acquiredcp", coptional.getDouble(3) - course.cp);
					values2.put("acquiredcp", ctransmit.getDouble(2) - coverhangcourse.getDouble(0));
				}
			} else
			{
				ContentValues values3 = new ContentValues();
				double cpfound = coptionalChosenSum.getDouble(0);
				double dummy = cpfound - coptional.getDouble(0);
				double overhang = 0;
				String sql = "Select CP.courseId, CP.additionalGradedCP, C.optionalcp, C.cp, C.status FROM CPOverhang AS CP LEFT OUTER JOIN Course AS C ON courseId = _id ORDER BY grade";
				Cursor c = db.rawQuery(sql, null);
				c.moveToFirst();

				while (dummy > 0 && !c.isAfterLast())
				{
					if (dummy >= c.getDouble(1))
					{
						overhang = c.getDouble(1) + c.getDouble(2);
						
						if(dummy > coptional.getDouble(0))
						{
							overhangchosen  = course.cp;
							if (c.getInt(4) == Status.STATUS_PASSED)
								overhangacquired = course.cp;
							//course.cp = overhangchosen;
							values2.put("optionalcp", coptional.getDouble(0));//overhang);
							values3.put("additionalGradedCP", c.getDouble(3) - coptional.getDouble(0));
							db.update("CPOverhang", values3, "courseId = " + c.getLong(0), null);
						}
						else
						{
							overhangchosen += c.getDouble(1);
							if (c.getInt(4) == Status.STATUS_PASSED)
								overhangacquired += c.getDouble(1);
							values2.put("optionalcp", c.getDouble(3));//overhang);
							db.delete("CPOverhang", "courseId = " + c.getLong(0), null);
						}
						dummy -= c.getDouble(1);
					} 
					else
					{
						overhang = c.getDouble(1) - dummy;
						//course.cp -= (c.getDouble(1) - dummy);
						overhangchosen += overhang;
						if (c.getInt(4) == Status.STATUS_PASSED)
							overhangacquired += overhang;
						values3.put("additionalGradedCP", c.getDouble(1) - overhang);
						//overhang += c.getDouble(2);
						values2.put("optionalcp", c.getDouble(3)-(c.getDouble(1) - overhang));
						db.update("CPOverhang", values3, "courseId = " + c.getLong(0), null);
						dummy = 0;
						values3.clear();
					}

					db.update("Course", values2, "_id = " + c.getLong(0), null);
					values2.clear();

					c.moveToNext();
				}
				
				if(course.status == Status.STATUS_PASSED)
				{
					values.put("acquiredcp", coptional.getDouble(3) - (course.cp -  overhangacquired));//course.cp);
					Log.d("transmit1", ""+ ctransmit.getDouble(2));
					values2.put("acquiredcp", ctransmit.getDouble(2) - overhangacquired);
				}
				values.put("chosencp", coptional.getDouble(4) - (course.cp -  overhangchosen));
				Log.d("transmit2", ""+ ctransmit.getDouble(3));
				values2.put("chosencp", ctransmit.getDouble(3) - overhangchosen);
			}
			
			db.update("Optional", values, "_id = " + coptional.getLong(2), null);
			if(values2.size() != 0)
				db.update("Optional", values2, "transmitcp = 1", null);
		}
		db.delete("SelectedCourses", "courseId = " + course.id, null);
		db.delete("Requirements", "courseId = " + course.id, null);
	}

	/*
	 * public void deleteCourseFromOptional(Course course) { ContentValues
	 * values = new ContentValues(); double allCp = 0; String condition = "";
	 * String conditionpassed = "";
	 * 
	 * String sqloverhang = "Select * FROM Optional WHERE transmitcp = 1";
	 * Cursor coverhang = db.rawQuery(sqloverhang, null);
	 * coverhang.moveToFirst();
	 * 
	 * double overhangcp = coverhang.getDouble(5); double overhangcppassed =
	 * coverhang.getDouble(4); coverhang.moveToFirst();
	 * 
	 * String sql =
	 * "Select acquiredcp, chosencp, _id, transmitcp FROM Optional WHERE _id = (Select optionalId FROM SelectedCourses WHERE courseId = "
	 * + course.id + ")"; Cursor c = db.rawQuery(sql, null); c.moveToFirst();
	 * 
	 * //alle kurse eines optionals ohne das zu löschende String allCoursesCp =
	 * "Select S.courseId, C.cp, C.status FROM SelectedCourses AS S " +
	 * " LEFT OUTER JOIN Course AS C ON C._id = S.courseId WHERE C._id !=" +
	 * course.id + " AND S.optionalId = " + c.getLong(2); Cursor
	 * allCoursesCpCursor = db.rawQuery(allCoursesCp, null);
	 * allCoursesCpCursor.moveToFirst();
	 * 
	 * condition = "courseId = "; condition = "courseId = ";
	 * while(!allCoursesCpCursor.isAfterLast()) { allCp +=
	 * allCoursesCpCursor.getDouble(1); condition = condition +
	 * allCoursesCpCursor.getLong(0) + " OR courseId = ";
	 * if(allCoursesCpCursor.getInt(2) == Status.STATUS_PASSED) conditionpassed
	 * = conditionpassed + allCoursesCpCursor.getLong(0) + " OR courseId = ";
	 * allCoursesCpCursor.moveToNext(); } condition = condition + "-1";
	 * conditionpassed = conditionpassed + "-1";
	 * 
	 * String sql2 = "Select * FROM CPOverhang WHERE courseId = " + course.id;
	 * Cursor c2 = db.rawQuery(sql2, null); if(c2.moveToFirst()) { //course.cp
	 * -= c2.getDouble(2); if(course.status == Status.STATUS_PASSED) {
	 * overhangcppassed -= c2.getDouble(2); values.put("acquiredcp",
	 * overhangcppassed); } overhangcp -= c2.getDouble(2);
	 * values.put("chosencp", overhangcp); db.update("Optional", values,
	 * "transmitcp = 1", null); values.clear(); }
	 * 
	 * if(allCp <= (c.getDouble(1) - (course.cp - course.optionalcp))) {
	 * allCoursesCp = "Select SUM(additionalGradedCP) FROM CPOverhang WHERE " +
	 * condition; allCoursesCpCursor.close(); allCoursesCpCursor =
	 * db.rawQuery(allCoursesCp, null); if(allCoursesCpCursor.moveToFirst()) {
	 * overhangcp -= allCoursesCpCursor.getDouble(0); values.put("chosencp",
	 * overhangcp); }
	 * 
	 * allCoursesCp = "Select SUM(additionalGradedCP) FROM CPOverhang WHERE " +
	 * conditionpassed; allCoursesCpCursor.close(); allCoursesCpCursor =
	 * db.rawQuery(allCoursesCp, null); if(allCoursesCpCursor.moveToFirst()) {
	 * overhangcppassed -= allCoursesCpCursor.getDouble(0);
	 * values.put("acquiredcp", overhangcppassed); }
	 * 
	 * db.update("Optional", values, "transmitcp = 1", null);
	 * 
	 * allCoursesCp =
	 * "Select cp, _id FROM Course LEFT OUTER JOIN SelectedCourses ON _id = courseId WHERE "
	 * + condition; allCoursesCpCursor.close(); allCoursesCpCursor =
	 * db.rawQuery(allCoursesCp, null); allCoursesCpCursor.moveToFirst();
	 * while(!allCoursesCpCursor.isAfterLast()) { values.clear();
	 * values.put("optionalcp", allCoursesCpCursor.getDouble(0));
	 * db.update("Course", values, "_id = " + allCoursesCpCursor.getLong(1),
	 * null); allCoursesCpCursor.moveToNext(); }
	 * 
	 * db.delete("CPOverhang", condition, null); values.clear(); }
	 * 
	 * 
	 * if (course.status == Status.STATUS_PASSED) values.put("acquiredcp",
	 * c.getDouble(0) - (course.cp - course.optionalcp));
	 * 
	 * values.put("chosencp", c.getDouble(1) - (course.cp - course.optionalcp));
	 * 
	 * db.update("Optional", values, "_id = " + c.getLong(2), null);
	 * 
	 * db.delete("SelectedCourses", "courseId = " + course.id, null);
	 * db.delete("Requirements", "courseId = " + course.id, null);
	 * db.delete("CPOverhang", "courseId = " + course.id, null);
	 * 
	 * c.close(); }
	 */

	/**
	 * Löscht alles, was zu einer Prüfungsordnung gehört.
	 */
	public void deleteExaminationRegulationData()
	{
		db.delete("CourseOfStudies", null, null);
		db.delete("Category", null, null);
		db.delete("Course", null, null);
		db.delete("Requirements", null, null);
		db.delete("Optional", null, null);
		db.delete("SelectedCourses", null, null);
		db.delete("Criterion", null, null);
		db.delete("Choosable", null, null);
		db.delete("CourseChoices", null, null);
		db.delete("CurriculumNames", null, null);
		db.delete("Semester", null, null);
		db.delete("Recommendation", null, null);
		db.delete("OptionalRecommendation", null, null);
		db.delete("CPOverhang", null, null);
		db.delete("LongCourseCp", null, null);
		notifyDatabaseListeners();
	}

	/**
	 * Löscht alle Tabellen der Musterstudienpläne
	 */
	public void deleteRecommendedPlan()
	{
		db.delete("Semester", null, null);
		db.delete("Recommendation", null, null);
		db.delete("OptionalRecommendation", null, null);
		notifyDatabaseListeners();
	}

	/**
	 * Löscht die gesamte Datenbank
	 */
	public void deleteAll()
	{

		db.delete("CourseOfStudies", null, null);
		db.delete("Category", null, null);
		db.delete("Course", null, null);
		db.delete("Requirements", null, null);
		db.delete("Optional", null, null);
		db.delete("SelectedCourses", null, null);
		db.delete("Criterion", null, null);
		db.delete("Choosable", null, null);
		db.delete("CourseChoices", null, null);
		db.delete("UniversityCalendar", null, null);
		db.delete("UniversityCalendarCourseOfStudies", null, null);
		db.delete("RegulationBuffer", null, null);
		db.delete("CurriculumNames", null, null);
		db.delete("Semester", null, null);
		db.delete("Recommendation", null, null);
		db.delete("OptionalRecommendation", null, null);
		db.delete("CPOverhang", null, null);
		db.delete("LongCourseCp", null, null);
		notifyDatabaseListeners();
	}

	public void deleteUniversityCalendar()
	{
		db.delete("UniversityCalendar", null, null);
		db.delete("UniversityCalendarCourseOfStudies", null, null);
	}

	/*
	 * Database-Methoden
	 */

	public class DBHelper extends SQLiteOpenHelper
	{

		public DBHelper(Context context)
		{
			super(context, DATABASE_NAME, null, TABLE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(CREATE_COURSE_OF_STUDIES_TABLE);
			db.execSQL(CREATE_OPTIONAL_TABLE);
			db.execSQL(CREATE_CATEGORY_TABLE);
			db.execSQL(CREATE_CHOOSABLE_TABLE);
			db.execSQL(CREATE_COURSE_TABLE);
			db.execSQL(CREATE_COURSECHOICES_TABLE);
			db.execSQL(CREATE_CRITERION_TABLE);
			db.execSQL(CREATE_REQUIREMENTS_TABLE);
			db.execSQL(CREATE_SELECTEDCOURSES_TABLE);
			db.execSQL(CREATE_UNIVERSITY_CALENDAR);
			db.execSQL(CREATE_REGULATION_BUFFER);
			db.execSQL(CREATE_UNIVERSITY_CALENDAR_COURSEOFSTUDIES);
			db.execSQL(CREATE_CURRICULUM_NAMES_TABLE);
			db.execSQL(CREATE_SEMESTER_TABLE);
			db.execSQL(CREATE_RECOMMENDATION_TABLE);
			db.execSQL(CREATE_OPTIONALRECOMMENDATION_TABLE);
			db.execSQL(CREATE_CPOVERHANG_TABLE);
			db.execSQL(CREATE_LONGCOURSECP_TABLE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{

			if (newVersion > oldVersion)
			{
				Log.v("Database Upgrade", "Database version higher than old."); 
				context.deleteDatabase(DATABASE_NAME);
				PreferenceHelper.resetAll(context);
			}
		}

	}

	public void copyDataBase() throws IOException
	{

		final String DB_PATH = "/mnt/sdcard/datenbak/";
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0)
		{
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	static ArrayList<DatabaseListener> listeners;

	public void addDataBaseListener(DatabaseListener listener)
	{
		if (listeners == null)
			listeners = new ArrayList<DatabaseListener>();
		listeners.add(listener);
	}

	public void notifyDatabaseListeners()
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			if (listeners.get(i) != null)
				listeners.get(i).update();
		}
	}

	/*
	 * Andere
	 */

	/**
	 * @deprecated hier muss getCourseFromUni deleted werden
	 * @param uccId
	 * @return
	 */
	@Deprecated
	public boolean isChosenChoosableCourse(long uccId)
	{
		String sql = "Select _id FROM Choosable WHERE selectedCourseId = " + getCourseFromUniversityCalendar(uccId);
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
			return true;
		return false;
	}

	public void insertRandomNames()
	{

		db.execSQL("DELETE FROM UniversityCalendar");

		String s = "QWERTZUIOPASDFGHJKLYXCVBNM";

		Random r = new Random();

		ContentValues values = new ContentValues();

		for (int i = 0; i < 200; i++)
		{

			values.clear();

			values.put("name", s.substring(r.nextInt(s.length())));

			db.insert("UniversityCalendar", null, values);

		}

	}

	/**
	 * Prüft die Anforderungen eines Kurses
	 * 
	 * @param id
	 *            ID des Kurses
	 */
	public void checkForCourseRequirements(long id)
	{
		String sql = "SELECT R.courseId, R.criterionId, C.status FROM Requirements AS R LEFT OUTER JOIN Course AS C ON R.courseId = C._id WHERE R.requiredCourseId ="
				+ id + " AND (C.status = " + Status.STATUS_REQUIREMENTS_MISSING + " OR C.status = " + Status.STATUS_NOT_PASSED + ")";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			long id2 = -1;
			if (!c.isNull(0))
				id2 = c.getLong(0);
			long id3 = -1;
			if (!c.isNull(1))
				id3 = c.getLong(1);
			if (id2 == 0 && id3 == 0)
			{
				c.moveToNext();
				continue;
			}
			String sql2 = "SELECT passed FROM Requirements WHERE (courseId =" + id2 + " OR criterionId =" + id3 + ") AND passed = 0";
			Cursor c2 = db.rawQuery(sql2, null);
			if (c2.moveToFirst())
			{
				ContentValues stat = new ContentValues();
				stat.put("status", Status.STATUS_REQUIREMENTS_MISSING);
				if (!c.isNull(0))
					db.update("Course", stat, "_id=" + c.getInt(0), null);
				if (!c.isNull(1))
					db.update("Criterion", stat, "_id=" + c.getInt(1), null);
			} else
			{
				ContentValues stat = new ContentValues();
				if (c.getInt(2) == Status.STATUS_REQUIREMENTS_MISSING)
				{
					stat.put("status", Status.STATUS_NOT_PASSED);
					if (!c.isNull(0))
						db.update("Course", stat, "_id=" + c.getInt(0), null);
					if (!c.isNull(1))
						db.update("Criterion", stat, "_id=" + c.getInt(1), null);
				}
			}

			c2.close();
			c.moveToNext();
		}
		c.close();
	}

	/**
	 * Prüft die Anforderungen eines Kriteriums
	 * 
	 * @param id
	 *            ID des Kriteriums
	 */
	public void checkForCriterionRequirements(long id)
	{
		boolean flag = true;

		String sql = "SELECT R.courseId, R.criterionId, C.status FROM Requirements AS R LEFT OUTER JOIN Criterion AS C ON R.courseId = C._id WHERE R.requiredCourseId ="
				+ id + " AND C.status = " + Status.STATUS_REQUIREMENTS_MISSING;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			long id2 = -1;
			if (!c.isNull(0))
				id2 = c.getLong(0);
			long id3 = -1;
			if (!c.isNull(1))
				id3 = c.getLong(1);
			if (id2 == 0 && id3 == 0)
			{
				c.moveToNext();
				continue;
			}
			flag = true;
			String sql2 = "SELECT passed FROM Requirements WHERE courseId = " + id2 + " OR criterionId =" + id3;
			Cursor c2 = db.rawQuery(sql2, null);
			c2.moveToFirst();
			while (!c2.isAfterLast())
			{
				if (c2.getInt(0) == 0)
				{
					flag = false;
					break;
				}
				c2.moveToNext();
			}
			if (flag)
			{
				ContentValues stat = new ContentValues();
				stat.put("status", Status.STATUS_NOT_PASSED);
				if (!c.isNull(0))
					db.update("Course", stat, "_id=" + c.getInt(0), null);
				if (!c.isNull(1))
					db.update("Criterion", stat, "_id=" + c.getInt(1), null);
			}
			c2.close();
			c.moveToNext();
		}
		c.close();
	}

	/**
	 * Prüft bei allen kursen und Kriterien, ob die erforderlichen CP bereits
	 * erreicht wurden.
	 */
	public void checkForNecCP()
	{
		int passed = 0;
		long dummyId = 0;
		ContentValues values = new ContentValues();
		values.put("status", Status.STATUS_REQUIREMENTS_MISSING);
		db.update("Course", values, "neccp > " + getCurrentCP() + " AND status != " + Status.STATUS_REQUIREMENTS_MISSING + " AND status != "
				+ Status.STATUS_PASSED, null);
		db.update("Criterion", values, "neccp > " + getCurrentCP() + " AND status != " + Status.STATUS_REQUIREMENTS_MISSING + " AND status != "
				+ Status.STATUS_PASSED, null);

		String sql = "Select C._id, COALESCE(R.passed, 2) FROM Course AS C LEFT OUTER JOIN Requirements AS R ON C._id = R.courseId  " + "WHERE C.neccp <= "
				+ getCurrentCP() + " AND C.status = " + Status.STATUS_REQUIREMENTS_MISSING;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		ContentValues values2 = new ContentValues();
		values2.put("status", Status.STATUS_NOT_PASSED);
		while (!c.isAfterLast())
		{
			if (dummyId != c.getLong(0) && passed > 0)
			{
				db.update("Course", values2, "_id = " + dummyId, null);
				dummyId = c.getLong(0);
				passed = c.getInt(1);
			} else if (dummyId != c.getLong(0))
			{
				dummyId = c.getLong(0);
				passed = c.getInt(1);
			} else
				passed = passed * c.getInt(1);

			c.moveToNext();
		}
		if (passed > 0)
			db.update("Course", values2, "_id = " + dummyId, null);
		passed = 0;
		dummyId = 0;
		c.close();

		sql = "Select C._id, COALESCE(R.passed, 2) FROM Criterion AS C LEFT OUTER JOIN Requirements AS R ON C._id = R.criterionId  " + "WHERE C.neccp <= "
				+ getCurrentCP() + " AND C.status = " + Status.STATUS_REQUIREMENTS_MISSING + " ORDER BY C._id";
		Cursor c2 = db.rawQuery(sql, null);
		c2.moveToFirst();
		values2.put("status", Status.STATUS_NOT_PASSED);

		while (!c2.isAfterLast())
		{
			if (dummyId != c2.getLong(0) && passed > 0)
			{
				db.update("Criterion", values2, "_id = " + dummyId, null);
				dummyId = c2.getLong(0);
				passed = c2.getInt(1);
			} else if (dummyId != c2.getLong(0))
			{
				dummyId = c2.getLong(0);
				passed = c2.getInt(1);
			} else
				passed = passed * c2.getInt(1);

			c2.moveToNext();
		}
		if (passed > 0)
			db.update("Criterion", values2, "_id = " + dummyId, null);
		c2.close();
		notifyDatabaseListeners();
	}

	/**
	 * Prüft, ob ein Kurs irgendwo eine Anforderung ist
	 * 
	 * @param courseId
	 *            ID des Kurses
	 * @return Ist der Kurs eine Anforderung?
	 */
	public boolean isRequired(long courseId)
	{
		String sql = "SELECT requiredCourse FROM Course WHERE _id = " + courseId + " AND requiredCourse = 1";
		Cursor c = db.rawQuery(sql, null);

		// c.close(); muss noch ausgeführt werden
		if (c.moveToFirst())
		{
			c.close();
			return true;
		} else
		{
			c.close();
			return false;
		}
	}

	/*
	 * Id ist eine UniversityCalendar ID
	 */
	/**
	 * Prüft, ob ein Kurs im UniversityCalendar irgendwo eine Anforderung ist.
	 * 
	 * @param universityCalendarcourseId
	 *            ID des Kurses im UC
	 * @return Ist der Kurs eine Anforderung?
	 */
	public int isRequiredCourse(long universityCalendarcourseId)
	{
		String sql = "Select C.requiredCourse FROM Course AS C WHERE C.vak = (SELECT U.vak FROM UniversityCalendar AS U WHERE _id = "
				+ universityCalendarcourseId + ") AND C.requiredCourse > 0";
		Cursor c = db.rawQuery(sql, null);
		if (c.getCount() != 0)
		{
			c.moveToFirst();
			int retval = c.getInt(0);
			c.close();
			return retval;
		}
		c.close();
		return 0;
	}

	public boolean isChoosableOrOptionalUniversityCalendarCourse(long uccId)
	{
		Long id = getCourseFromUniversityCalendarWithoutCreation(uccId);
		if (id < 1)
			return false;
		String sql = "Select courseId FROM SelectedCourses WHERE courseId = " + id;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst())
			return true;
		c.close();
		sql = "Select selectedCourseId FROM Choosable WHERE selectedCourseId = " + id;
		c = db.rawQuery(sql, null);
		if (c.moveToFirst())
			return true;
		return false;
	}

}