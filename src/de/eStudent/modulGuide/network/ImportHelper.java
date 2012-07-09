package de.eStudent.modulGuide.network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import de.eStudent.modulGuide.XMLParser.UniversityCalendarXMLHandler;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.ExaminationRegulationList.getRegulationPDFromServer;
import de.eStudent.modulGuide.preferences.ExaminationRegulationList.getRegulationXMLFromServer;
import de.eStudent.modulGuide.preferences.ExampleCourseSchemeList.getCourseSchemeFromServer;
import de.eStudent.modulGuide.preferences.PreferenceHelper;


/**
 * Methoden zum Herunterladen von Daten von unserem Server
 *
 */
public class ImportHelper
{

	/** Der Pfad des Servers */
	// public final String SERVERPATH = "http://ceggert.ce.ohost.de/";

	public final static String SERVERPATH = "http://chrisrathjen.de/eStudent/";

	/** Der Ordner auf dem Server, in dem die Prüfungsordnungen gespeichert sind */
	public final static String REGULATIONSPATH = SERVERPATH + "Pruefungsordnungen/";

	public final static String EXAMPLE_COURSE_SCHEME_PATH = SERVERPATH + "Musterstudienplaene/";

	/**
	 * Der Ordner auf dem Server, in dem das Vorlesungsverzeichnis gespeichert
	 * ist,
	 */
	public final static String LECTURESPATH = SERVERPATH + "Vorlesungsverzeichnis/";

	/** Der Ordner auf dem Server, in dem die PDFs angezeigt werden. */
	public final static String REGULATIONS_PDF = SERVERPATH + "PDF/";
	
	private static final int timeoutConnection = 3000; 
	private static final int timeoutRead = 5000;
	private static final int timeoutSocket = 5000; 


	/**
	 * Gibt einen Stream als String zurück
	 * 
	 * @param is
	 *            Der Stream
	 * @return Streaminhalt als String
	 */
	private String convertStreamToString(InputStream is)
	{

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		} catch (IOException e)
		{
			e.printStackTrace();// TODO: FEHLER
		} finally
		{
			try
			{
				is.close();
			} catch (IOException e)
			{
				e.printStackTrace();// TODO:FEHLER
			}
		}
		return sb.toString();
	}

	


	/**
	 * Holt sich die Dateinamen aller Vorlesungsdateien
	 * 
	 * @return Liste von Strings
	 * @throws Exception
	 *             Fehler bei der Serveranfrage
	 */
	public void getLections(Context context) throws URISyntaxException, ClientProtocolException, IOException, JSONException
	{
		
		HttpParams httpParameters = new BasicHttpParams(); 
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection); 
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpGet request = new HttpGet();
		InputStream instream = null;
		DataHelper helper = new DataHelper(context);
		
		try
		{

			// action=lectures ist das Kommando, um ein Array der Dateinamen der
			// Vorlesungen
			request.setURI(new URI(SERVERPATH + "getFiles.php?action=lectures"));

			HttpResponse response = client.execute(request);

			instream = response.getEntity().getContent();
			String result = convertStreamToString(instream);

			
			// Gesamtes JSON-Objekt, enthält "names" auch noch
			JSONObject json = new JSONObject(result);

			JSONArray valArray = json.getJSONArray("names");

			helper.db.beginTransaction();
			helper.emptyUniversityCalendarSubjects();

			String name = null;
			JSONObject another_json_object;
			String fileName = null;

			for (int i = 0; i < valArray.length(); i++)
			{
				another_json_object = valArray.getJSONObject(i);

				fileName = another_json_object.getString("XMLName");
				name = another_json_object.getString("Name");

				name = name.replaceAll("%C4", "Ä");
				name = name.replaceAll("%E4", "ä");
				name = name.replaceAll("%D6", "Ö");
				name = name.replaceAll("%F6", "ö");
				name = name.replaceAll("%DC", "Ü");
				name = name.replaceAll("%FC", "ü");
				name = name.replaceAll("%DF", "ß");

				helper.addUniversityCalendarSubject(name, fileName);
			}

			helper.db.setTransactionSuccessful();
			// PreferenceHelper.saveUniversityCalendarSubjectsTimeStamp(context);
		}

		finally
		{
			if (helper.db.inTransaction())
				helper.db.endTransaction();
			if (instream != null)
				instream.close();
			helper.close();
		}
	}

	/**
	 * Holt sich die Dateinamen und das Datum aller Prüfungsordnungen
	 * 
	 * @return Liste von String-Arrays {Dateiname, Datum}
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws JSONException
	 * @throws Exception
	 */
	public void getRegulations(Context context) throws URISyntaxException, ClientProtocolException, IOException, JSONException
	{

		HttpParams httpParameters = new BasicHttpParams(); 
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection); 
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpGet request = new HttpGet();
		InputStream instream = null;
		DataHelper helper = new DataHelper(context);

		try
		{
			// action=lectures ist das Kommando, um ein Array der Dateinamen der
			// Vorlesungen
			request.setURI(new URI(SERVERPATH + "getFiles.php?action=regulations"));

			HttpResponse response = client.execute(request);
			instream = response.getEntity().getContent();
			String result = convertStreamToString(instream);

			result = result.replaceAll("%C4", "Ä");
			result = result.replaceAll("%E4", "ä");
			result = result.replaceAll("%D6", "Ö");
			result = result.replaceAll("%F6", "ö");
			result = result.replaceAll("%DC", "Ü");
			result = result.replaceAll("%FC", "ü");
			result = result.replaceAll("%DF", "ß");

			// Gesamtes JSON-Objekt, enthält "names" auch noch
			JSONObject json = new JSONObject(result);

			JSONArray valArray = json.getJSONArray("names");

			helper.db.beginTransaction();
			helper.emptyRegulationBuffer();

			String originalname;
			String name;
			String date;
			String pdfname;
			JSONObject another_json_object;

			for (int i = 0; i < valArray.length(); i++)
			{
				// Werte auslesen und in die Datenbank eintragen
				another_json_object = valArray.getJSONObject(i);

				originalname = another_json_object.getString("XML");
				name = another_json_object.getString("Name");
				date = another_json_object.getString("Date");
				pdfname = another_json_object.getString("PDF");

				helper.insertInRegulationBuffer(name, date, originalname, pdfname);
			}

			helper.db.setTransactionSuccessful();
		}

		finally
		{
			if (helper.db.inTransaction())
				helper.db.endTransaction();
			if (instream != null)
				instream.close();
			helper.close();
		}

	}

	/**
	 * Holt sich die Dateinamen und das Datum aller Prüfungsordnungen
	 * 
	 * @return Liste von String-Arrays {Dateiname, Datum}
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws JSONException
	 * @throws Exception
	 */
	public void getCurriculum(Context context) throws URISyntaxException, ClientProtocolException, IOException, JSONException
	{

		HttpParams httpParameters = new BasicHttpParams(); 
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection); 
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpGet request = new HttpGet();
		InputStream instream = null;
		DataHelper helper = new DataHelper(context);

		try
		{
			// action=lectures ist das Kommando, um ein Array der Dateinamen der
			// Vorlesungen
			request.setURI(new URI(SERVERPATH + "getFiles.php?action=curriculum&regulationName=" + PreferenceHelper.getRegulationName(context)));

			HttpResponse response = client.execute(request);
			instream = response.getEntity().getContent();
			String result = convertStreamToString(instream);

			result = result.replaceAll("%C4", "Ä");
			result = result.replaceAll("%E4", "ä");
			result = result.replaceAll("%D6", "Ö");
			result = result.replaceAll("%F6", "ö");
			result = result.replaceAll("%DC", "Ü");
			result = result.replaceAll("%FC", "ü");
			result = result.replaceAll("%DF", "ß");

			// Gesamtes JSON-Objekt, enthält "names" auch noch
			JSONObject json = new JSONObject(result);

			JSONArray valArray = json.getJSONArray("names");

			helper.db.beginTransaction();
			helper.emptyCurriculumNames();

			String originalname;
			String name;
			String description;
			JSONObject another_json_object;

			for (int i = 0; i < valArray.length(); i++)
			{
				// Werte auslesen und in die Datenbank eintragen
				another_json_object = valArray.getJSONObject(i);

				originalname = another_json_object.getString("XMLName");
				name = another_json_object.getString("Name");
				description = another_json_object.getString("Description");

				helper.insertCiriculumName(originalname, name, description);
			}

			helper.db.setTransactionSuccessful();
		}

		finally
		{
			helper.db.endTransaction();
			if (instream != null)
				instream.close();
			helper.close();
		}

	}

	/**
	 * Speichert eine Vorlesungs-XML des Servers
	 * 
	 * @param name
	 *            Dateiname der XML
	 * @param subjectId
	 *            ID des Faches
	 * @param listener
	 *            Listener
	 * @param context
	 *            Kontext
	 * @throws Exception
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void saveLectureXML(String name, long subjectId, NetworkListener listener, Context context) throws Exception, ParserConfigurationException,
			SAXException
	{
		URL u;
		InputStream is = null;
		DataHelper dataHelper = new DataHelper(context);

		dataHelper.deleteUniversityCalendarForASubject(subjectId);

		// viel Perfomanter
		StringBuilder response = new StringBuilder();
		try
		{
			u = new URL(LECTURESPATH + "" + name);
			URLConnection urlConnection = u.openConnection();
			urlConnection.setConnectTimeout(timeoutConnection);
			urlConnection.setReadTimeout(timeoutRead);
			
			urlConnection.connect();
			int file_size = urlConnection.getContentLength();

			
			
			
			is = u.openStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));

			long readData = 0;
			String data = "";
			while ((data = reader.readLine()) != null)
			{
				response.append(data);
				readData += data.getBytes().length;
				if (listener != null)
					listener.update((int) (((double) readData / file_size) * 100));
				if (readData % 10000 == 0)
					Log.e("t", "tt");

			}

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			UniversityCalendarXMLHandler myXMLHandler = new UniversityCalendarXMLHandler();
			xr.setContentHandler(myXMLHandler);
			xr.parse(new InputSource(new ByteArrayInputStream(response.toString().getBytes("utf-8"))));

			dataHelper.addListOfCourses(myXMLHandler.listOfCourses, subjectId);

		} finally
		{
			dataHelper.close();
			if (is != null)
				is.close();

		}

	}

	/**
	 * Lädt die übergebene Prüfungsordnungs-XML-Datei herunter und gibt sie in
	 * String-Form zurück.
	 * 
	 * @param filename
	 *            Dateiname
	 * @return String der XML-Datei
	 * @throws IOException
	 */
	public String getRegulationXML(String filename, getRegulationXMLFromServer listener) throws IOException
	{

		/* Diese Variante erlaubt UTF-8, ist aber nicht ganz so schön */

		URL u;
		InputStream is = null;

		// viel Perfomanter
		StringBuilder response = new StringBuilder();

		filename = filename.replaceAll(" ", "%20");

		try
		{

			u = new URL(REGULATIONSPATH + "" + filename);
			URLConnection urlConnection = u.openConnection();
			urlConnection.setConnectTimeout(timeoutConnection);
			urlConnection.setReadTimeout(timeoutRead);
			urlConnection.connect();
			int file_size = urlConnection.getContentLength();

			is = u.openStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
			long readData = 0;
			String data = "";
			while ((data = reader.readLine()) != null)
			{
				response.append(data);
				readData += data.getBytes().length;
				listener.publishProgress((int) (((double) readData / file_size) * 100));

			}

		} finally
		{
			if (is != null)
				is.close();

		}

		return response.toString();
	}

	/**
	 * Läd den übergebenen Studienverlaufsplan vom Server runter und gibt in als
	 * String zurück
	 * 
	 * @param filename
	 *            Dateiname
	 * @return String der XML-Datei
	 * @throws IOException
	 */
	public String getExampleCourseScheme(String filename, getCourseSchemeFromServer listener) throws IOException
	{

		/* Diese Variante erlaubt UTF-8, ist aber nicht ganz so schön */

		URL u;
		InputStream is = null;

		// viel Perfomanter
		StringBuilder response = new StringBuilder();

		filename = filename.replaceAll(" ", "%20");

		try
		{

			u = new URL(EXAMPLE_COURSE_SCHEME_PATH + "" + filename);
			URLConnection urlConnection = u.openConnection();
			urlConnection.setConnectTimeout(timeoutConnection);
			urlConnection.setReadTimeout(timeoutRead);
			urlConnection.connect();
			int file_size = urlConnection.getContentLength();

			is = u.openStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
			long readData = 0;
			String data = "";
			while ((data = reader.readLine()) != null)
			{
				response.append(data);
				readData += data.getBytes().length;
				listener.publishProgress((int) (((double) readData / file_size) * 100));

			}

		} finally
		{
			if (is != null)
				is.close();
		}

		return response.toString();
	}

	/**
	 * Findet die Adresse, auf dem sich eine PDF befindet.
	 * 
	 * @param filename
	 *            Der Dateiname
	 * @param listener
	 * @return
	 * @throws IOException
	 */
	public Uri getRegulationPDF(String filename, getRegulationPDFromServer listener) throws IOException
	{
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

		File file = new File(dir, filename);

		URL url = new URL(REGULATIONS_PDF + "" + filename);
		FileOutputStream f = new FileOutputStream(file);

		try
		{
			int downloaded = 0;

			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setConnectTimeout(timeoutConnection);
			c.setReadTimeout(timeoutRead);
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			int fileSize = c.getContentLength();
			c.connect();
			InputStream in = c.getInputStream();
			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0)
			{
				f.write(buffer, 0, len1);
				downloaded += len1;
				listener.publishProgress((int) (((double) downloaded / fileSize) * 100));
			}

		} finally
		{
			if (f != null)
				f.close();
		}

		return Uri.fromFile(file);

	}

}
