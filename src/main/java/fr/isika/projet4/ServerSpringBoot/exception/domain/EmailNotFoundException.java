package fr.isika.projet4.ServerSpringBoot.exception.domain;

public class EmailNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2893963494732279111L;
	
	public EmailNotFoundException(String message) {
		super(message);
	}
	
}
