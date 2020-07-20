package fr.isika.projet4.ServerSpringBoot.exception.model;

public class UsernameExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8605054391058178949L;

	public UsernameExistsException(String message) {
		super(message);
	}
	
}
