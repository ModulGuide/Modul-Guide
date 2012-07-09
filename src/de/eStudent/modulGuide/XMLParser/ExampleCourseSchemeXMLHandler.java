package de.eStudent.modulGuide.XMLParser;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.eStudent.modulGuide.common.Choosable;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.Optional;
import de.eStudent.modulGuide.common.Semester;

public class ExampleCourseSchemeXMLHandler extends DefaultHandler
{

	public Semester[] list;

	private String att;
	private String optionalName;
	private int currentOptionalPart;
	private boolean semcpTagOpen = false;
	private boolean optionalTagOpen = false;
	private int cpRange;

	@Override
	public void startElement(String uri, String openTag, String qName, Attributes attributes) throws SAXException
	{

		if (openTag.equalsIgnoreCase("main"))
		{
			att = attributes.getValue("semcount");
			if (att != null)
			{
				try
				{
					list = new Semester[Integer.parseInt(att)];

					// inital allen Semestern 30 Cp zuweisen
					for (int i = 1; i <= list.length; i++)
					{
						list[i - 1] = new Semester(i);
						list[i - 1].plannedCp = 30;
					}

				} catch (Exception e)
				{
					throw new SAXException("semcount fehlt");
				}
			} else
				throw new SAXException("semcount fehlt");

		}

		else if (openTag.equalsIgnoreCase("semcp"))
		{
			semcpTagOpen = true;
		} else if (openTag.equalsIgnoreCase("item"))
		{

			if (semcpTagOpen)
			{
				int sem = Integer.parseInt(attributes.getValue("semester"));
				int cp = Integer.parseInt(attributes.getValue("cp"));

				list[sem - 1].plannedCp = cp;
			} else if (optionalTagOpen)
			{

				int cp = Integer.parseInt(attributes.getValue("cp"));
				String courseVak = attributes.getValue("coursevak");
				String coursename = attributes.getValue("coursename");

				att = attributes.getValue("sem");

				String sems[] = att.split(",");

				for (int i = 0; i < sems.length; i++)
				{
					Optional optional = new Optional(optionalName);
					optional.cp = cp;
					optional.coursename = coursename;
					optional.coursevak = courseVak;
					optional.cpRange = cpRange;
					optional.alternative = generateAlternative(sems, i);
					optional.part = currentOptionalPart;

					((ArrayList<Optional>) (list[Integer.parseInt(sems[i]) - 1].childs)).add(optional);
				}

				currentOptionalPart++;
				cpRange += cp;
			}

		}

		else if (openTag.equalsIgnoreCase("course"))
		{
			String name = attributes.getValue("name");

			att = attributes.getValue("sem");

			String sems[] = att.split(",");

			for (int i = 0; i < sems.length; i++)
			{
				Course course = new Course();
				course.name = name;
				course.alternative = generateAlternative(sems, i);

				((ArrayList<Course>) (list[Integer.parseInt(sems[i]) - 1].childs)).add(course);
			}

		}

		else if (openTag.equalsIgnoreCase("criterion"))
		{

			String name = attributes.getValue("name");

			att = attributes.getValue("sem");

			String sems[] = att.split(",");

			for (int i = 0; i < sems.length; i++)
			{
				Criterion criterion = new Criterion(name);
				criterion.alternative = generateAlternative(sems, i);

				((ArrayList<Criterion>) (list[Integer.parseInt(sems[i]) - 1].childs)).add(criterion);
			}
		}

		else if (openTag.equalsIgnoreCase("choosable"))
		{
			String name = attributes.getValue("name");

			att = attributes.getValue("sem");

			String sems[] = att.split(",");

			for (int i = 0; i < sems.length; i++)
			{
				Choosable choosable = new Choosable();
				choosable.name = name;
				choosable.alternative = generateAlternative(sems, i);

				choosable.reccomendedCourseName = attributes.getValue("course");

				((ArrayList<Choosable>) (list[Integer.parseInt(sems[i]) - 1].childs)).add(choosable);
			}
		}

		else if (openTag.equalsIgnoreCase("optional"))
		{
			optionalName = attributes.getValue("name");
			cpRange = 0;
			optionalTagOpen = true;
			currentOptionalPart = 1;
		}

	}

	@Override
	public void endElement(String uri, String endTag, String qName) throws SAXException
	{
		if (endTag.equalsIgnoreCase("semcp"))
		{
			semcpTagOpen = false;
		} else if (endTag.equalsIgnoreCase("optional"))
		{
			optionalTagOpen = false;
		}

	}

	private String generateAlternative(String[] sems, int currentSem)
	{
		if (sems.length == 1)
			return "";
		else
		{
			String tmp[] = new String[sems.length - 1];

			for (int i = 0, x = 0; i < sems.length; i++)
				if (i != currentSem)
					tmp[x++] = sems[i];

			sems = tmp;

		}

		if (sems.length == 0)
		{
			return "-";
		}

		int count = sems.length;
		String result = "";

		for (int i = 1; i <= count; i++)
		{
			if (i == count)
				result += sems[i - 1] + ".";
			else if (i == count - 1)
				result += sems[i - 1] + ". o. ";
			else
				result += sems[i - 1] + ". , ";
		}

		return result;
	}

}
