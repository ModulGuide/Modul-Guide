package de.eStudent.modulGuide.XMLParser;

import java.util.LinkedList;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.eStudent.modulGuide.common.UniversityCalendarCourse;

/**
 * @author Chris
 */
public class UniversityCalendarXMLHandler extends DefaultHandler
{

	/**
	 * @uml.property name="tags"
	 * @uml.associationEnd multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	Stack tags = new Stack();

	/**
	 * @uml.property name="currentCourse"
	 * @uml.associationEnd
	 */
	UniversityCalendarCourse currentCourse;
	/**
	 * @uml.property name="courseOfStudies"
	 */
	public String courseOfStudies;
	/**
	 * @uml.property name="listOfCourses"
	 */
	public LinkedList<UniversityCalendarCourse> listOfCourses = new LinkedList<UniversityCalendarCourse>();

	/**
	 * @uml.property name="coursesTagOpen"
	 */
	boolean coursesTagOpen = false;
	/**
	 * @uml.property name="courseTagOpen"
	 */
	boolean courseTagOpen = false;

	/**
	 * @uml.property name="staffTagOpen"
	 */
	boolean staffTagOpen = false;
	/**
	 * @uml.property name="dateTagOpen"
	 */
	boolean dateTagOpen = false;

	final static Integer subjectTag = 0;
	final static Integer titleTag = 1;
	final static Integer coursesTag = 2;
	final static Integer courseTag = 3;

	final static Integer vakTag = 4;
	final static Integer courseTitleTag = 5;
	final static Integer ectsTag = 6;
	final static Integer descriptionTag = 7;
	final static Integer staffTag = 8;
	final static Integer memberTag = 9;
	final static Integer datesTag = 10;
	final static Integer dateTag = 11;
	final static Integer prefixTag = 12;
	final static Integer textTag = 13;

	/**
	 * Called when tag starts ( ex:- <name>AndroidPeople</name> -- <name> )
	 */
	@SuppressWarnings("unchecked")
	@Override
	/*
	 * public void startElement(String uri, String openTag, String qName,
	 * Attributes attributes) throws SAXException {
	 * 
	 * if (openTag.equalsIgnoreCase("subject")) { if (tags.isEmpty())
	 * tags.add(subjectTag); else throw new SAXException("fail"); } else if
	 * (openTag.equalsIgnoreCase("title") && !coursesTagOpen) { if
	 * (tags.peek().equals(subjectTag)) tags.add(titleTag); else throw new
	 * SAXException("fail"); } else if (openTag.equalsIgnoreCase("courses")) {
	 * if (tags.peek().equals(subjectTag)) { tags.add(coursesTag);
	 * coursesTagOpen = true; } else throw new SAXException("fail"); } else if
	 * (openTag.equalsIgnoreCase("course")) { if
	 * (tags.peek().equals(coursesTag)) { currentCourse = new
	 * UniversityCalendarCourse(); currentCourse.date = "";
	 * listOfCourses.add(currentCourse); tags.add(courseTag); courseTagOpen =
	 * true; } else throw new SAXException("fail"); } else if (courseTagOpen) {
	 * if (openTag.equalsIgnoreCase("vak")) tags.add(vakTag); else if
	 * (openTag.equalsIgnoreCase("title")) tags.add(courseTitleTag); else if
	 * (openTag.equalsIgnoreCase("ects")) tags.add(ectsTag); else if
	 * (openTag.equalsIgnoreCase("description")) tags.add(descriptionTag);
	 * 
	 * else if (openTag.equalsIgnoreCase("staff")) { tags.add(staffTag);
	 * staffTagOpen = true; } else if (openTag.equalsIgnoreCase("dates"))
	 * tags.add(datesTag); else if (staffTagOpen) { if
	 * (openTag.equalsIgnoreCase("member")) tags.add(memberTag); else =======
	 */
	public void startElement(String uri, String openTag, String qName, Attributes attributes) throws SAXException
	{

		if (openTag.equalsIgnoreCase("subject"))
		{
			if (tags.isEmpty())
				tags.add(subjectTag);
			else
				throw new SAXException("fail");
		} else if (openTag.equalsIgnoreCase("title") && !coursesTagOpen)
		{
			if (tags.peek().equals(subjectTag))
				tags.add(titleTag);
			else
				throw new SAXException("fail");
		} else if (openTag.equalsIgnoreCase("courses"))
		{
			if (tags.peek().equals(subjectTag))
			{
				tags.add(coursesTag);
				coursesTagOpen = true;
			} else
				throw new SAXException("fail");
		} else if (openTag.equalsIgnoreCase("course"))
		{
			if (tags.peek().equals(coursesTag))
			{
				currentCourse = new UniversityCalendarCourse();
				currentCourse.date = "";
				currentCourse.graded = 2;
				listOfCourses.add(currentCourse);
				tags.add(courseTag);
				courseTagOpen = true;
			} else
				throw new SAXException("fail");
		} else if (courseTagOpen)
		{
			if (openTag.equalsIgnoreCase("vak"))
				tags.add(vakTag);
			else if (openTag.equalsIgnoreCase("title"))
				tags.add(courseTitleTag);
			else if (openTag.equalsIgnoreCase("ects"))
				tags.add(ectsTag);
			else if (openTag.equalsIgnoreCase("description"))
				tags.add(descriptionTag);

			else if (openTag.equalsIgnoreCase("staff"))
			{
				tags.add(staffTag);
				staffTagOpen = true;
			} else if (openTag.equalsIgnoreCase("dates"))
				tags.add(datesTag);
			else if (staffTagOpen)
			{
				if (openTag.equalsIgnoreCase("member"))
					tags.add(memberTag);
				else
					// >>>>>>> .r100
					throw new SAXException("fail");
			}

			else if (openTag.equalsIgnoreCase("date"))
			{
				if (tags.peek().equals(datesTag))
				{
					tags.add(dateTag);
					dateTagOpen = true;
				} else
					throw new SAXException("fail");

			} else if (dateTagOpen)
			{
				if (openTag.equalsIgnoreCase("prefix"))
					tags.add(prefixTag);
				else if (openTag.equalsIgnoreCase("text"))
					tags.add(textTag);
			}
		}

	}

	/**
	 * Called when tag closing ( ex:- <name>AndroidPeople</name> -- </name> )
	 */
	@Override
	public void endElement(String uri, String endTag, String qName) throws SAXException
	{

		if (endTag.equalsIgnoreCase("subject"))
		{
			if (!tags.pop().equals(subjectTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("title") && !coursesTagOpen)
		{
			if (!tags.pop().equals(titleTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("courses"))
		{
			coursesTagOpen = false;
			if (!tags.pop().equals(coursesTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("course"))
		{
			courseTagOpen = false;
			if (!tags.pop().equals(courseTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("vak"))
		{
			if (!tags.pop().equals(vakTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("title"))
		{
			if (!tags.pop().equals(courseTitleTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("ects"))
		{
			if (!tags.pop().equals(ectsTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("description"))
		{
			if (!tags.pop().equals(descriptionTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("staff"))
		{
			staffTagOpen = false;
			if (!tags.pop().equals(staffTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("member"))
		{
			if (!tags.pop().equals(memberTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("dates"))
		{
			if (!tags.pop().equals(datesTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("date"))
		{
			dateTagOpen = false;
			if (!tags.pop().equals(dateTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("prefix"))
		{
			if (!tags.pop().equals(prefixTag))
				throw new SAXException("fail");
		} else if (endTag.equalsIgnoreCase("text"))
		{
			if (!tags.pop().equals(textTag))
				throw new SAXException("fail");
		}

	}

	/**
	 * Called to get tag characters ( ex:- <name>AndroidPeople</name> -- to get
	 * AndroidPeople Character )
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{

		if (tags.peek().equals(courseTitleTag))
		{
			currentCourse.name = new String(ch, start, length);
			currentCourse.name = currentCourse.name.trim();
		} else if (tags.peek().equals(vakTag))
			currentCourse.vak = new String(ch, start, length);
		else if (tags.peek().equals(ectsTag))
			try
			{
				currentCourse.cp = Integer.parseInt(new String(ch, start, length));
			} catch (Exception e)
			{
				currentCourse.cp = 0;
			}

		else if (tags.peek().equals(descriptionTag))
			currentCourse.description = new String(ch, start, length);
		else if (tags.peek().equals(memberTag))
			currentCourse.staff = new String(ch, start, length);
		else if (tags.peek().equals(textTag))
			currentCourse.date = currentCourse.date + new String(ch, start, length);
		else if (tags.peek().equals(titleTag))
			courseOfStudies = new String(ch, start, length);

	}

}
