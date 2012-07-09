package de.eStudent.modulGuide.common;

import de.eStudent.modulGuide.R;

/**
 * Enthält Variablen für Statusabfragen für eine einfachere Benutzung.
 */
public final class Status
{

	/** Nicht bestanden */
	public final static int STATUS_NOT_PASSED = 4;

	/** Bestanden */
	public final static int STATUS_PASSED = 1;

	/** Die Anforderungen wurden noch nicht erfüllt */
	public final static int STATUS_REQUIREMENTS_MISSING = 2;

	/** Angemeldet */
	public final static int STATUS_SIGNED_UP = 3;

	/**
	 * Sucht das passende Icon für jeden Status
	 * 
	 * @param status
	 *            Der Status, zu dem das Icon gewählt wurde
	 * @return ID des Icons
	 */
	public final static int getIcon(int status)
	{
		switch (status)
		{
		case Status.STATUS_NOT_PASSED:
			return R.drawable.blue_icon;
		case Status.STATUS_PASSED:
			return R.drawable.green_icon;

		case Status.STATUS_REQUIREMENTS_MISSING:
			return R.drawable.red_icon;

		case Status.STATUS_SIGNED_UP:
			return R.drawable.yellow_icon;

		default:
			return R.drawable.blue_icon;
		}

	}

}
