package de.eStudent.modulGuide.preferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eStudent.modulGuide.R;
import de.eStudent.modulGuide.StudiumhelferActivity;

/**
 * Ansicht für die Lizenz
 * 
 */
public class License extends SherlockActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.license);

		// Layout der Action Bar setzen
		final ActionBar actionBar = getSupportActionBar();
		BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.bg_striped_img));
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
		background.setDither(true);
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayHomeAsUpEnabled(true);

		displayLicense();

	}

	/**
	 * Lädt die Lizenz aus dem raw Ordner uns zeigt sie an
	 */
	private void displayLicense()
	{
		TextView text = (TextView) findViewById(R.id.text);

		InputStream inputStream = getResources().openRawResource(R.raw.license);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try
		{
			i = inputStream.read();
			while (i != -1)
			{
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		text.setText(byteArrayOutputStream.toString());
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
