package de.eStudent.modulGuide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Statics;
import de.eStudent.modulGuide.database.DataHelper;


/**
 * Dialog zum erstellen eines Dialoges
 * 
 */
public class CreateCustomCourseDialog extends Activity
{

	//boolean ob es einen Fehler im Namen Feld gibt
	private boolean nameE = true;
	
	//boolean ob es einen Fehler im CP Feld gibt
	private boolean cpE = true;
	
	//Der ok Button
	private Button ok;
	
	//Fehlermeldung
	private TextView error;
	
	//Namen Feld
	private EditText name;
	
	//vak Feld
	private EditText vak;
	
	//cp Feld
	private EditText cp;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_custom_course_dialog);

		((TextView) findViewById(R.id.title)).setText("Kurs erstellen");
		ok = (Button) findViewById(R.id.ok);
		error = (TextView) findViewById(R.id.error);
		name = (EditText) findViewById(R.id.name);
		vak = (EditText) findViewById(R.id.vak);
		cp = (EditText) findViewById(R.id.cp);

		Intent intent = getIntent();

		Bundle extras = intent.getExtras();

		final long optionalId = extras.getLong(Statics.ID);

		name.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// nothing to do
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// nothing to do
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				if (s.length() == 0)
					nameE = true;
				else
					nameE = false;

				if (nameE || cpE)
					ok.setEnabled(false);
				else
					ok.setEnabled(true);
			}
		});

		cp.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// nothing to do
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// nothing to do
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				if (s.length() == 0)
				{
					error.setVisibility(View.INVISIBLE);
					cpE = true;
				} else
				{
					double cpValue;
					try
					{
						cpValue = Double.parseDouble(cp.getText().toString());
					} catch (Exception e)
					{
						cpValue = -1;
					}

					if (cpValue < 0)
					{
						error.setText("Cp Wert fehlerhaft.");
						error.setVisibility(View.VISIBLE);
						cpE = true;
					} else
					{
						error.setVisibility(View.INVISIBLE);
						cpE = false;
					}

				}

				if (nameE || cpE)
					ok.setEnabled(false);
				else
					ok.setEnabled(true);

			}
		});

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				finish();

			}
		});

		ok.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Course c = new Course();
				c.name = name.getText().toString();
				c.vak = name.getText().toString();
				c.cp = Double.parseDouble(cp.getText().toString());
				c.graded = 2;
				DataHelper helper = new DataHelper(getBaseContext());
				helper.addCustomCourseToOptional(optionalId, c);
				helper.close();
				finish();

			}
		});

	}

}
