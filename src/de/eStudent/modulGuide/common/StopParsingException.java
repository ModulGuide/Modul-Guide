package de.eStudent.modulGuide.common;

import org.xml.sax.SAXException;

/**
 * Unterbricht das Parsen.
 */
public class StopParsingException extends SAXException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3677046370735977416L;

	public StopParsingException()
	{
		super("Stop Parsing");
	}
}
