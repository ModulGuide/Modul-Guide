package de.eStudent.modulGuide.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.StudiumhelferActivity;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.database.DeleteDatabaseTask;


/**
 * Klasse für die Einstellungen
 *
 */
public class Preferences extends SherlockPreferenceActivity
{

	//Der Context
	private Context context;
	
	//Der DataHelper
	private DataHelper dataHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		
		//Layout der Actionbar setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		context = this;

		addPreferencesFromResource(R.layout.preferences);

		dataHelper = new DataHelper(context);


		//Datenbank resetten
		Preference resetDatabasePreference = findPreference("resetDatabase");
		resetDatabasePreference.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Datenbank wirklich löschen?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						new DeleteDatabaseTask(context).execute();
						PreferenceHelper.setUniversityCalendarComplete(context, false);
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}

		});

		//Prüfungsordnung auswählen
		Preference LoadAndSaveExaminationRegulationPreference = findPreference("loadAndSaveExaminationRegulation");
		LoadAndSaveExaminationRegulationPreference.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				Intent intent = new Intent(getBaseContext(), ExaminationRegulationList.class);
				startActivity(intent);

				return true;
			}

		});

		
		//Über uns Dialog
		Preference about = findPreference("about");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				Intent intent = new Intent(getBaseContext(), About.class);
				startActivity(intent);

				return true;
			}

		});

		
		//Musterstudienplan auswählen
		Preference exampleCourseScheme = findPreference("exampleCourseScheme");
		exampleCourseScheme.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				if (PreferenceHelper.getRegulationName(context).equals(""))
				{
					Toast.makeText(context, "Zuerst Prüfungsordnung auswählen", Toast.LENGTH_LONG).show();
				} else
				{
					Intent intent = new Intent(getBaseContext(), ExampleCourseSchemeList.class);
					startActivity(intent);
				}
				return true;

			}

		});

		
		//FAQ
		Preference faq = findPreference("faq");
		faq.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				String url = "https://sites.google.com/site/modulguidefaq/";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return true;
			}

		});

		
		//Versions
		Preference versions = findPreference("versions");
		versions.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				getVersionsDialog(context).show();
				return true;
			}

		});

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (dataHelper != null)
		{
			dataHelper.close();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		//Startansicht öffnen
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Erzeugt und gibt den Dialog für die Änderungshistorie zurück
	 * @param context
	 * @return Der Dialog
	 */
	public static Dialog getVersionsDialog(Context context)
	{
		Dialog dialog = new Dialog(context);

		dialog.setContentView(R.layout.versions);
		dialog.setTitle("Änderungshistorie");

		TextView text = (TextView) dialog.findViewById(R.id.historyOfChanges);
		text.setText(Html.fromHtml(versions));

		return dialog;
	}

	
	//Die Änderungshistorie
	private static final String versions = "<b>2.0</b> <br>\n" + "&nbsp&nbsp&nbsp-Um Musterstudienplan erweitert<br>\n"
			+ "&nbsp&nbsp&nbsp-Prüfungsordnung um etliche Tags erweitert<br>\n" + "&nbsp&nbsp&nbsp-About Dialog hinzugefügt<br>\n"
			+ "&nbsp&nbsp&nbsp-Licence Dialog hinzugefügt<br>\n" + "&nbsp&nbsp&nbsp-Änderungshistorie hinzugefügt<br>\n"
			+ "&nbsp&nbsp&nbsp-FAQ hinzugefügt<br>\n" + "&nbsp&nbsp&nbsp-Bugfixes<br><br>\n" + "<b>1.0</b> <br>\n" + "&nbsp&nbsp&nbsp-1. Version<br>";

}
