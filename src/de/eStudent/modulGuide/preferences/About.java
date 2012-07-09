package de.eStudent.modulGuide.preferences;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.StudiumhelferActivity;

/**
 * Ansicht für "Über uns"
 */
public class About extends SherlockActivity
{

	// Der Kontext
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		context = this;

		// Actionbar layout setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Feedback
		TextView feedback = (TextView) findViewById(R.id.feedback);
		feedback.setMovementMethod(LinkMovementMethod.getInstance());
		feedback.setText(Html
				.fromHtml("<a href=\"https://docs.google.com/spreadsheet/viewform?formkey=dGwxOHkxVUtCS29GNnJGZTUydldDNmc6MQ\">Feedback geben</a> "));

		// Twitter
		TextView twitter = (TextView) findViewById(R.id.twitter);
		twitter.setMovementMethod(LinkMovementMethod.getInstance());
		twitter.setText(Html.fromHtml("<a href=\"https://twitter.com/#!/eStudent_Bremen\">Twitter</a> "));

		// ---Ab hier Biblotheken----

		TextView graphView = (TextView) findViewById(R.id.graphView);
		graphView.setMovementMethod(LinkMovementMethod.getInstance());
		graphView.setText(Html.fromHtml("<a href=\"https://github.com/jjoe64/GraphView\">GraphView</a> "));

		TextView greenDroid = (TextView) findViewById(R.id.greendroid);
		greenDroid.setMovementMethod(LinkMovementMethod.getInstance());
		greenDroid.setText(Html.fromHtml("<a href=\"https://github.com/cyrilmottier/GreenDroid\">GreenDroid</a> "));

		TextView actionbarSherlock = (TextView) findViewById(R.id.actionbarSherlock);
		actionbarSherlock.setMovementMethod(LinkMovementMethod.getInstance());
		actionbarSherlock.setText(Html.fromHtml("<a href=\"http://actionbarsherlock.com/\">ActionBarSherlock</a> "));

		TextView viewpagerTabs = (TextView) findViewById(R.id.viewpagerTabs);
		viewpagerTabs.setMovementMethod(LinkMovementMethod.getInstance());
		viewpagerTabs.setText(Html.fromHtml("<a href=\"https://github.com/astuetz/android-viewpagertabs\">ViewPagerTabs</a> "));

		// ---- Biblotheken ende ----

		// License
		TextView license = (TextView) findViewById(R.id.license);
		license.setMovementMethod(LinkMovementMethod.getInstance());
		Spannable spans = (Spannable) license.getText();
		ClickableSpan clickSpan = new ClickableSpan()
		{

			@Override
			public void onClick(View widget)
			{
				Intent intent = new Intent(getBaseContext(), License.class);
				startActivity(intent);
			}
		};
		spans.setSpan(clickSpan, 0, spans.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Problem melden per E-Mail
		TextView problem = (TextView) findViewById(R.id.problem);
		problem.setMovementMethod(LinkMovementMethod.getInstance());
		Spannable spans2 = (Spannable) problem.getText();
		ClickableSpan clickSpan2 = new ClickableSpan()
		{

			@Override
			public void onClick(View widget)
			{
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

				/* Fill it with Data */
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "modul.guide@gmail.com" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ModulGuide");

				StringBuffer buf = new StringBuffer();
				buf.append("\n\n\n\n\nSysteminformationen:\nVERSION.RELEASE {" + Build.VERSION.RELEASE + "}");
				buf.append("\nVERSION.INCREMENTAL {" + Build.VERSION.INCREMENTAL + "}");
				buf.append("\nVERSION.SDK_INT {" + Build.VERSION.SDK_INT + "}");
				buf.append("\nFINGERPRINT {" + Build.FINGERPRINT + "}");
				buf.append("\nBOARD {" + Build.BOARD + "}");
				buf.append("\nBRAND {" + Build.BRAND + "}");
				buf.append("\nDEVICE {" + Build.DEVICE + "}");
				buf.append("\nMANUFACTURER {" + Build.MANUFACTURER + "}");
				buf.append("\nMODEL {" + Build.MODEL + "}");

				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, buf.toString());

				/* Send it off to the Activity-Chooser */
				context.startActivity(Intent.createChooser(emailIntent, "Email senden..."));
			}
		};
		spans2.setSpan(clickSpan2, 0, spans2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		// Startansicht öffnen
		case android.R.id.home:
			Intent mainActivity = new Intent(getBaseContext(), StudiumhelferActivity.class);
			mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
