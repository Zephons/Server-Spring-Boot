package fr.isika.projet4.ServerSpringBoot.exception.model;

public class EmailExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4270302999276813336L;

	public EmailExistsException(String message) {
		super(message);
	}
	
}
