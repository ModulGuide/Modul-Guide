package de.eStudent.modulGuide.common;

/**
 * Enthält Daten über den aktuellen Studiengang bzw. den Status 
 * der aktuell gewählten Prüfungsordnung.
 */
public class MainInfo
{
	/** Der gewählte Studiengang */
	public String subject;
	
	/** Die ID des Studienganges */
	public long id;
	
	/** Die Fachbereichsnummer */
	public int facultyNr;
	
	/** Das Datum der Prüfungsordnung */
	public String regulationDate;
	
	/** Das aktuelle Semester */
	public int semester;
	
	/** Die erreichten CP */
	public int cp;
	
	/** Die benötigten CP, um das Studium abzuschließen. */
	public int requiredCp;
	
	/**Akademischer Grad	 */
	public String degree;
	

	/** Leerer Konstruktor */
	public MainInfo()
	{
	}

	/**
	 * Konstruktor zur Erstellung des MainInfo-Objekts.
	 * @param id ID des Studiengangs
	 * @param subject NAme des Studiengangs
	 * @param facultyNr Fachbereichsnummer des Studiengangs
	 * @param regulationDate Datum der Prüfungsordnung
	 * @param cp Erreichte CP des Studiengangs
	 * @param requiredCp Benötigte CP, um das Studium zu erreichen
	 * @param semester Das Semester, in dem man sich aktuell befindet 
	 */
	public MainInfo(long id, String subject, int facultyNr, String regulationDate, int cp, int requiredCp, int semester, String degree)
	{
		this.id = id;
		this.subject = subject;
		this.facultyNr = facultyNr;
		this.regulationDate = regulationDate;
		this.cp = cp;
		this.requiredCp = requiredCp;
		this.semester = semester;
		this.degree = degree;

	}

}
