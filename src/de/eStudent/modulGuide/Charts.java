package de.eStudent.modulGuide;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.LineGraphView;

import de.eStudent.modulGuide.common.Semester;
import de.eStudent.modulGuide.database.DataHelper;
import de.eStudent.modulGuide.preferences.PreferenceHelper;

/**
 * 
 * Graph Ansichten zum CP und Notenverlauf
 */
public class Charts extends Activity
{
	Context context;
	DataHelper helper;
	ArrayList<Semester> semester;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.charts);
		helper = new DataHelper(context);

		LinearLayout layoutGraph1 = (LinearLayout) findViewById(R.id.chartsLayoutGraph1);
		LinearLayout layoutGraph2 = (LinearLayout) findViewById(R.id.chartsLayoutGraph2);

		String[] label;

		int semesterCount = PreferenceHelper.getSemesterInt(context);

		GraphViewData[] dataCpChart = new GraphViewData[semesterCount];
		label = new String[semesterCount];

		// daten generieren für cp Graph
		for (int semester = 1; semester < semesterCount + 1; semester++)

		{
			dataCpChart[semester - 1] = new GraphViewData(semester, helper.getCPForSemester(semester));
			label[semester - 1] = "" + semester;

		}

		GraphViewSeries exampleSeries = new GraphViewSeries("CP verlauf", Color.RED, dataCpChart);

		GraphView graphView = new LineGraphView(this // context
				, "" // heading
		)
		{
			@Override
			protected String formatLabel(double value, boolean isValueX)
			{
				if (!isValueX)
				{
					return "" + Math.round(value);
				} else
					return super.formatLabel(value, isValueX);
			}
		};
		graphView.addSeries(exampleSeries);
		graphView.setHorizontalLabels(label);

		graphView.setShowLegend(true);
		graphView.setLegendWidth(100);
		graphView.setLegendAlign(LegendAlign.BOTTOM);
		layoutGraph1.addView(graphView);

		ArrayList<GraphViewData> dataGradeChart = new ArrayList<GraphViewData>();
		ArrayList<String> lablesGradeChart = new ArrayList<String>();

		// Daten genereiren für Noten Graph
		for (int semester = 1; semester <= semesterCount; semester++)
		{
			double grade = helper.getGradeForSemester(semester);
			if (grade != 0)
			{
				Log.d("grade", "" + grade);
				dataGradeChart.add(new GraphViewData(semester, grade));
				lablesGradeChart.add("" + semester);

			}

		}

		GraphViewSeries exampleSeries2 = new GraphViewSeries("Noten Entwicklung", Color.GREEN,

		dataGradeChart.toArray(new GraphViewData[] {})

		);

		GraphView graphView2 = new LineGraphView(this // context
				, "" // heading
		)
		{
			@Override
			protected String formatLabel(double value, boolean isValueX)
			{
				if (!isValueX)
				{
					return "" + ((float) Math.round(value * 10)) / 10;
				} else
					return super.formatLabel(value, isValueX);
			}
		};
		graphView2.addSeries(exampleSeries2); // data

		graphView2.setHorizontalLabels(lablesGradeChart.toArray(new String[] {}));

		graphView2.setShowLegend(true);
		graphView2.setLegendWidth(160);
		graphView2.setLegendAlign(LegendAlign.BOTTOM);

		layoutGraph2.addView(graphView2);

	}

}