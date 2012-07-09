package de.eStudent.modulGuide;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import de.eStudent.modulGuide.common.Category;
import de.eStudent.modulGuide.common.ChildEntry;
import de.eStudent.modulGuide.common.Choosable;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.Optional;
import de.eStudent.modulGuide.common.Other;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * Klasse für die Ansicht der Tabelle für den Musterstudeinplan
 * 
 */
public class StudyCoursePlanTable extends FragmentActivity {
	// Der Context
	private Context context;

	// Der Webview in dem die Tabelle angezeigt wird
	private WebView webView;

	// Die aktuelle Seite des ViewPagers
	private int currentPage = 1;

	// Der Viewpagers
	private ViewSwitcher viewSwitcher;

	// Fehlermeldung
	private TextView otherMsg;

	// Referenz zum thread, um diesen vorzeitig beenden zu können
	private AsyncTask<Object, Object, Integer> task;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.study_course_plan_table);

		context = this;

		webView = (WebView) findViewById(R.id.webView);
		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		otherMsg = (TextView) findViewById(R.id.otherText);

		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setBuiltInZoomControls(true);

		task = new DataTask().execute();

	}

	private class DataTask extends AsyncTask<Object, Object, Integer> {
		private StringBuilder builder;
		private DataHelper dataHelper;

		// can use UI thread here

		public DataTask() {
			builder = new StringBuilder();
			dataHelper = new DataHelper(context);
		}

		@Override
		protected void onPreExecute() {
			showOtherView();
			otherMsg.setText("Loading ...");
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Integer result) {
			dataHelper.close();

			switch (result) {

			// Kein Musterstudienplan ausgewählt
			case 0:
				showOtherView();
				otherMsg.setText("Kein Musterstudienplan ausgewählt.");
				break;

			// Zeige Tabelle an
			case 1:
				showTableView();
				Log.d("html", builder.toString());
				webView.loadDataWithBaseURL(null, builder.toString(), "text/html", "UTF-8", null);
				break;
			}

			task = null;

		}

		@Override
		protected void onCancelled(Integer result) {
			dataHelper.close();
			super.onCancelled(result);
		}

		// automatically done on worker thread (separate from UI thread)
		@SuppressWarnings("unchecked")
		@Override
		protected Integer doInBackground(Object... params) {
			// überprüfe, ob überhaupt ein Musterstudeinplan ausgewählt ist
			if (PreferenceHelper.getExampleCourseSchemeName(context).length() == 0)
				return 0;

			// --- geneierierung der HTML Tabelle ---

			builder.append(HTML_TAG_OPEN);
			builder.append(HMTL_HEADER);
			builder.append(HTML_BODY_TAG_OPEN);
			builder.append(BIGTABLE_OPEN);

			ArrayList<Category> table = dataHelper.getExampleCourseSchemeTable();

			// Tabellenheader generieren
			builder.append(THEAD_OPEN);
			builder.append(TR_OPEN);
			builder.append(TH_OPEN).append("Sem").append(TH_CLOSE);

			for (int i = 0; i < table.size(); i++) {
				builder.append(TH_OPEN);

				builder.append(HEADERTABLE_OPEN);
				builder.append(TD_OPEN).append(table.get(i).name).append(TD_CLOSE);
				builder.append(TD_OPEN).append("CP").append(TD_CLOSE);

				builder.append(TABLE_CLOSE);

				builder.append(TH_CLOSE);
			}
			builder.append(TH_OPEN).append("CP").append(TH_CLOSE);
			builder.append(TR_CLOSE);
			builder.append(THEAD_CLOSE);

			// Tabellenrumpf
			builder.append(TBODY_OPEN);

			int semCount = table.get(0).semester.size();
			int categoryCount = table.size();

			// durch alle Semester iterieren
			for (int currentSem = 1; currentSem <= semCount; currentSem++) {
				// Eine Semester Zeile
				builder.append(TR_OPEN);

				// Semester Zelle
				builder.append(TD_OPEN).append(currentSem).append(TD_CLOSE);

				ArrayList<ChildEntry> list;

				// Durch alle Categorien des aktuellen Semester iterieren
				for (int i = 0; i < categoryCount; i++) {
					list = ((ArrayList<ChildEntry>) table.get(i).semester.get(currentSem - 1).childs);

					builder.append(TD_OPEN);

					if (list.isEmpty())
						builder.append(EMPTY_CELL);
					else {
						// generiere inner Table
						builder.append(TABLE_OPEN);
						ChildEntry entry;

						// durch Child Entrys iterieren
						for (int x = 0; x < list.size(); x++) {
							entry = list.get(x);

							builder.append(TR_OPEN);

							builder.append(TD_OPEN);

							if (entry.getStatus() == de.eStudent.modulGuide.common.Status.STATUS_PASSED)
								builder.append("<font color=green>");
							else if (entry.getStatus() == de.eStudent.modulGuide.common.Status.STATUS_SIGNED_UP)
								builder.append("<font color=#FFB90F>");

							if (entry instanceof Course) {
								if (((Course) entry).firstSemestercp != 0)
									builder.append(entry.name + " Teil 1");
								else
									builder.append(entry.name);
							} else if (entry instanceof Choosable) {
								if (((Choosable) entry).firstSemestercp != 0)
									builder.append(entry.name + " Teil 1");
								else
									builder.append(entry.name);
							} else if (entry instanceof Optional) {
								if (((Optional) entry).firstSemestercp != 0)
									builder.append(entry.name + " Teil 1");
								else
									builder.append(entry.name);

								if (((Optional) entry).alternative.length() != 0)
									builder.append("<br>alt.:" + entry.alternative + " Sem.");
							} else
								builder.append(entry.name);

							if (entry.getStatus() == de.eStudent.modulGuide.common.Status.STATUS_PASSED)
								builder.append(" \u2714 </font>");
							else if (entry.getStatus() == de.eStudent.modulGuide.common.Status.STATUS_SIGNED_UP)
								builder.append(" \u2714 </font>");

							builder.append(TD_CLOSE);

							builder.append(TD_OPEN);

							if (entry instanceof Course) {

								if (((Course) entry).firstSemestercp != 0)
									builder.append(Statics.convertCP(((Course) entry).firstSemestercp));
								else
									builder.append(Statics.convertCP(((Course) entry).cp));

							} else if (entry instanceof Criterion) {
								builder.append(Statics.convertCP(((Criterion) entry).cp));
							} else if (entry instanceof Optional) {
								
								if (((Optional) entry).firstSemestercp != 0)
									builder.append(Statics.convertCP(((Optional) entry).firstSemestercp));
								else
									builder.append(Statics.convertCP(((Optional) entry).cp));
								
							} else if (entry instanceof Choosable) {
							
								
								if(((Choosable) entry).firstSemestercp != 0)
									builder.append(Statics.convertCP(((Choosable) entry).firstSemestercp));
								else
									builder.append(Statics.convertCP(((Choosable) entry).cp));
								
							} else if (entry instanceof Other) {
								builder.append(Statics.convertCP(((Other) entry).cp));
							}

							builder.append(TD_CLOSE);
							builder.append(TR_CLOSE);

							builder.append("\n");

						}

						builder.append(TABLE_CLOSE);
					}

					builder.append(TD_CLOSE);

				}

				// CP Zelle
				builder.append(TD_OPEN).append(table.get(0).semester.get(currentSem - 1).plannedCp).append(TD_CLOSE);

				builder.append(TR_CLOSE);
			}

			builder.append(TBODY_CLOSE);

			builder.append(TABLE_CLOSE);
			builder.append(HTML_BODY_TAG_CLOSE);
			builder.append(HTML_TAG_CLOSE);

			return 1;

		}

	}

	private final static String HTML_TAG_OPEN = "<html>";
	private final static String HTML_TAG_CLOSE = "</html>";
	private final static String HTML_BODY_TAG_OPEN = "<body>";
	private final static String HTML_BODY_TAG_CLOSE = "</body>";

	private final static String BIGTABLE_OPEN = "<table class= \"bigtable\">";
	private final static String HEADERTABLE_OPEN = "<table class= \"headertable\">";

	private final static String TABLE_OPEN = "<table class=\"table\">";
	private final static String TABLE_CLOSE = "</table>";

	private final static String THEAD_OPEN = "<thead>";
	private final static String THEAD_CLOSE = "</thead>";
	private final static String TR_OPEN = "<tr>";
	private final static String TR_CLOSE = "</tr>";
	private final static String TH_OPEN = "<th>";
	private final static String TH_CLOSE = "</th>";
	private final static String TBODY_OPEN = "<tbody>";
	private final static String TBODY_CLOSE = "</tbody>";
	private final static String TD_OPEN = "<td>";
	private final static String TD_OPEN_ALIGN_RIGHT = "<td align=\"right\">";
	private final static String TD_CLOSE = "</td>";
	private final static String EMPTY_CELL = "&nbsp";

	private final static String HMTL_HEADER = "<head>\r\n" + "<style type=\"text/css\"> \r\n" + "<!-- \r\n" + " table.bigtable{\r\n" + "\r\n"
			+ "    border-spacing: 0px;\r\n" + "    border-collapse: collapse;\r\n" + "	font-size:100%;\r\n" + "}\r\n" + "table.bigtable th {\r\n"
			+ "    text-align: center;\r\n" + "    font-weight: bold;\r\n" + "    padding: 2px;\r\n" + "    border: 3px solid #FFFFFF;\r\n"
			+ "    background: #4a70aa;\r\n" + "    color: #FFFFFF;\r\n" + "}\r\n" + "table.bigtable td {\r\n" + "    text-align: center;\r\n"
			+ "    padding: 2px;\r\n" + "    border: 3px solid #FFFFFF;\r\n" + "    background: #e3f0f7;\r\n" + "}\r\n" + "\r\n" + "\r\n" + "table.table {\r\n"
			+ " width:100%;\r\n" + "\r\n" + " border: 0px solid #FFFFFF;\r\n" + " 	font-size:100%;\r\n" + "\r\n" + "}\r\n" + "\r\n" + "table.table td {\r\n"
			+ "    text-align:left;\r\n" + "    border: 0px solid #FFFFFF;\r\n" + "\r\n" + "}\r\n" +

			"table.table td+td {\r\n" + "    text-align:right;\r\n" + "    border: 0px solid #FFFFFF;\r\n" + "\r\n" + "}\r\n" +

			"table.headertable {\r\n" + "\r\n" + " width:100%;\r\n" + " height:100%;\r\n" + " border: 0px solid #FFFFFF;\r\n" + " 	font-size:100%;\r\n"
			+ "    padding:0px;\r\n" +

			"\r\n" + "}\r\n" + "\r\n" +

			"table.headertable td {\r\n" + " height:100%;\r\n" + "    text-align:left;\r\n" + "    border: 0px solid #FFFFFF;\r\n"
			+ "    background: #4a70aa;\r\n" + "    color: #FFFFFF;\r\n" + "    font-weight: bold;\r\n" +

			"\r\n" + "}\r\n" +

			"table.headertable td+td {\r\n" + " height:100%;\r\n" + "    text-align:right;\r\n" + "    vertical-align:bottom;\r\n" + "    font-size: 80%;\r\n" +

			"    border: 0px solid #FFFFFF;\r\n" + "\r\n" + "}\r\n" +

			"html, body, table \r\n" + "{\r\n" + "height: 100%;\r\n" + "} " +

			"--> \r\n" + "	\r\n" + "\r\n" + "	</style>\r\n" + "</head>";

	/**
	 * Zeigt die Tabellenansicht
	 */
	private void showTableView() {
		if (currentPage != 1) {
			viewSwitcher.showPrevious();
			currentPage = 1;
		}
	}

	/**
	 * Zeit die Alternativ Ansciht mit einer Nachricht
	 */
	private void showOtherView() {
		if (currentPage != 2) {
			viewSwitcher.showNext();
			currentPage = 2;
		}
	}

	@Override
	protected void onDestroy() {
		if (task != null)
			task.cancel(true);
		super.onDestroy();
	}

}
