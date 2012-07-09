package de.eStudent.modulGuide.XMLParser;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.eStudent.modulGuide.common.Category;
import de.eStudent.modulGuide.common.Choosable;
import de.eStudent.modulGuide.common.Course;
import de.eStudent.modulGuide.common.Criterion;
import de.eStudent.modulGuide.common.MainInfo;
import de.eStudent.modulGuide.common.Optional;

public class ExaminationRegulationXMLHandler extends DefaultHandler
{

	public ArrayList<Category> listofCategory = new ArrayList<Category>();

	public MainInfo mainInfo = new MainInfo();
	boolean mainTagOpen = false;
	boolean categoryTagOpen = false;
	boolean choosableTagOpen = false;
	String currentTag = "";
	Category currentCategory;
	Choosable currentChoosable;

	/**
	 * Called when tag starts ( ex:- <name>AndroidPeople</name> -- <name> )
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void startElement(String uri, String openTag, String qName, Attributes attributes) throws SAXException
	{
		currentTag = openTag;

		if (openTag.equalsIgnoreCase("main") && !mainTagOpen)
		{
			mainTagOpen = true;

			String att = attributes.getValue("subject");
			if (att != null)
				mainInfo.subject = att;
			else
				throw new SAXException("fehlende Main infos1");

			att = attributes.getValue("regulationDate");
			if (att != null)
				mainInfo.regulationDate = att;
			else
				throw new SAXException("fehlende Main infos2");

			att = attributes.getValue("facultyNr");
			if (att != null)
				mainInfo.facultyNr = Integer.parseInt(att);
			else
				throw new SAXException("fehlende Main infos3");
			
			
			
			att = attributes.getValue("degree");
			mainInfo.degree = att;

			att = attributes.getValue("cp");
			if (att != null)
				mainInfo.requiredCp = Integer.parseInt(att);
			else
				throw new SAXException("fehlende Main infos4");
			
			
			
			att = attributes.getValue("degree");
			if (att != null)
				mainInfo.degree = att;
			else
				throw new SAXException("fehlende Main infos4");

		} else if (mainTagOpen && openTag.equalsIgnoreCase("category"))
		{
			categoryTagOpen = true;
			currentCategory = new Category(attributes.getValue("name"));
			listofCategory.add(currentCategory);
		} else if (categoryTagOpen)
		{
			if (openTag.equalsIgnoreCase("course"))
			{
				parseCourse(openTag, attributes);
			} else if (openTag.equalsIgnoreCase("optional"))
			{
				Optional o = new Optional(attributes.getValue("name"));
				String att = attributes.getValue("cp");
				if (att != null)
					o.cp = Double.parseDouble(att);
				else
					o.cp = 0;

				att = attributes.getValue("vak");
				if (att != null)
					o.vak = parseVak(att);
				else
					throw new SAXException("vak fehlt");

				att = attributes.getValue("note");
				if (att != null)
					o.note = att;

				att = attributes.getValue("weight");
				if (att != null)
					o.weight = Double.parseDouble(att);
				else
					o.weight = 1.0;

				att = attributes.getValue("requirements");
				if (att != null)
				{
					o.requirements = att.split(",");
					for (int i = 0; i < o.requirements.length; i++)
						o.requirements[i] = o.requirements[i].trim();
				}

				att = attributes.getValue("neccp");
				if (att != null)
					o.necCp = Integer.parseInt(att);

				att = attributes.getValue("graded");
				if (att != null)
				{
					if (att.equals("false"))
						o.graded = 0;
					else
						o.graded = 1;
				} else
					o.graded = 2;

				att = attributes.getValue("transmitcp");
				if (att != null)
					o.transmitcp = Boolean.valueOf(att).booleanValue();

				att = attributes.getValue("duration");
				if (att != null)
					o.duration = Integer.parseInt(att);
				else
					o.duration = 1;
				o.splitcp = parseSplitCp(attributes, o.duration, o.cp);

				((ArrayList<Optional>) currentCategory.childs).add(o);
			} else if (openTag.equalsIgnoreCase("choosable"))
			{
				choosableTagOpen = true;
				currentChoosable = new Choosable();
				currentChoosable.name = attributes.getValue("name");

				String att = attributes.getValue("cp");
				if (att != null)
					currentChoosable.cp = Double.parseDouble(att);

				att = attributes.getValue("requirements");
				if (att != null)
				{
					currentChoosable.requirements = att.split(",");
					for (int i = 0; i < currentChoosable.requirements.length; i++)
						currentChoosable.requirements[i] = currentChoosable.requirements[i].trim();
				}

				att = attributes.getValue("neccp");
				if (att != null)
					currentChoosable.necCP = Integer.parseInt(att);

				currentChoosable.note = attributes.getValue("note");

				att = attributes.getValue("duration");
				if (att != null)
					currentChoosable.duration = Integer.parseInt(att);
				else
					currentChoosable.duration = 1;

				att = attributes.getValue("graded");
				if (att != null)
				{
					if (att.equals("false"))
						currentChoosable.graded = 0;
					else
						currentChoosable.graded = 1;
				} else
					currentChoosable.graded = 2;
				att = attributes.getValue("weight");
				if (att != null)
					currentChoosable.weight = Double.parseDouble(att);
				else
					currentChoosable.weight = 1.0;

				currentChoosable.splitcp = parseSplitCp(attributes, currentChoosable.duration, currentChoosable.cp);

				((ArrayList<Choosable>) currentCategory.childs).add(currentChoosable);

			} else if (openTag.equalsIgnoreCase("criterion"))
			{
				Criterion c = new Criterion(attributes.getValue("name"));
				c.note = attributes.getValue("note");

				String att = attributes.getValue("requirements");
				if (att != null)
				{
					c.requirements = att.split(",");
					for (int i = 0; i < c.requirements.length; i++)
						c.requirements[i] = c.requirements[i].trim();
				}

				att = attributes.getValue("cp");
				if (att != null)
					c.cp = Double.parseDouble(att);

				att = attributes.getValue("neccp");
				if (att != null)
					c.neccp = Integer.parseInt(att);

				att = attributes.getValue("graded");
				if (att != null)
				{
					if (att.equals("false"))
						c.graded = 0;
					else
						c.graded = 1;
				} else
					c.graded = 2;

				att = attributes.getValue("weight");
				if (att != null)
					c.weight = Double.parseDouble(att);
				else
					c.weight = 1.0;

				// more attributes?
				((ArrayList<Criterion>) currentCategory.childs).add(c);
			} else
				throw new SAXException("Wrong openTag " + openTag);

		} else
			throw new SAXException("Wrong openTag " + openTag);

	}

	private String parseVak(String vak)
	{
		if (vak.startsWith("vak") || vak.startsWith("("))
		{
			return vak.replaceAll("vak", "C.vak");
		} else
		{
			// normale vak
			String result = "C.vak ";
			if (vak.startsWith("!"))
				return result += "NOT LIKE '" + vak.substring(1) + "'";

			return result += "LIKE '" + vak + "'";

		}
	}

	@SuppressWarnings("unchecked")
	Course parseCourse(String openTag, Attributes attributes) throws SAXException
	{

		Course c = new Course(attributes.getValue("name"));

		if (choosableTagOpen)
		{
			c.cp = currentChoosable.cp;
			c.note = currentChoosable.note;
			c.necCP = currentChoosable.necCP;
			c.requirements = currentChoosable.requirements;
			c.duration = currentChoosable.duration;
			c.splitcp = currentChoosable.splitcp;
			c.graded = currentChoosable.graded;
			c.weight = currentChoosable.weight;
		}
		String att;
		if (!choosableTagOpen)
		{
			att = attributes.getValue("cp");
			if (att != null)
				c.cp = Double.parseDouble(att);
			else
				c.cp = 0;
		}

		att = attributes.getValue("vak");
		if (att != null)
			c.vak = att;
		else
			throw new SAXException("vak fehlt");

		att = attributes.getValue("requirements");
		if (att != null)
		{
			c.requirements = att.split(",");
			for (int i = 0; i < c.requirements.length; i++)
				c.requirements[i] = c.requirements[i].trim();
		}

		att = attributes.getValue("neccp");
		if (att != null)
			c.necCP = Integer.parseInt(att);

		c.note = attributes.getValue("note");

		att = attributes.getValue("duration");
		if (att != null)
			c.duration = Integer.parseInt(att);
		else
			c.duration = 1;

		att = attributes.getValue("graded");
		if (att != null)
		{
			if (att.equals("false"))
				c.graded = 0;
			else
				c.graded = 1;
		} else
			c.graded = 2;
		att = attributes.getValue("weight");
		if (att != null)
			c.weight = Double.parseDouble(att);
		else
			c.weight = 1.0;

		c.splitcp = parseSplitCp(attributes, c.duration, c.cp);

		if (choosableTagOpen)
		{
			c.requiredCourse = 2;
			currentChoosable.subjects.add(c);
			currentChoosable.cp = c.cp;
		} else
		{
			c.requiredCourse = 1;
			((ArrayList<Course>) currentCategory.childs).add(c);
		}

		return c;
	}

	private double[] parseSplitCp(Attributes attributes, int duration, double allCp) throws SAXException
	{

		double[] splitcp = null;

		if (duration != 1)
		{
			String att = attributes.getValue("splitcp");

			if (att == null)
			{
				int cp = (int) (allCp / duration);

				splitcp = new double[duration];
				for (int i = 0; i < duration; i++)  
					splitcp[i] = cp;

				splitcp[0] += allCp % cp;

			} else
			{
				String cp[] = att.split(",");
				if (duration != cp.length)
					throw new SAXException("splitcp is wrong");

				splitcp = new double[cp.length];

				double totalCp = 0;
				for (int i = 0; i < cp.length; i++)
				{
					splitcp[i] = Double.parseDouble(cp[i]);
					totalCp += splitcp[i];
				}

				if (allCp != totalCp)
					throw new SAXException("splitcp is wrong");

			}
		}

		return splitcp;
	}

	/**
	 * Called when tag closing ( ex:- <name>AndroidPeople</name> -- </name> )
	 */
	@Override
	public void endElement(String uri, String endTag, String qName) throws SAXException
	{

		if (endTag.equalsIgnoreCase("main"))
			mainTagOpen = false;
		else if (endTag.equalsIgnoreCase("category"))
			categoryTagOpen = false;
		else if (endTag.equalsIgnoreCase("choosable"))
			choosableTagOpen = false;

	}

	/**
	 * Called to get tag characters ( ex:- <name>AndroidPeople</name> -- to get
	 * AndroidPeople Character )
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{

		// if (currentElement) {
		// currentValue = new String(ch, start, length);

	}

}
