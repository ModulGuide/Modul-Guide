package de.eStudent.modulGuide.network;


/**
 * Interface um beim Downloaden einen Progress auf der UI visualisieren zu können
 *
 */
public interface NetworkListener
{

	/**
	 * Fortschritt setzen
	 * @param value Der aktuelle Fortschritt
	 */
	public void update(int value);
}
