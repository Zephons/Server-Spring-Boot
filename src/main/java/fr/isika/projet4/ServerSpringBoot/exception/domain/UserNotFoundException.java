package fr.isika.projet4.ServerSpringBoot.exception.domain;

public class UserNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4735312932957954977L;

	public UserNotFoundException(String message) {
		super(message);
	}
	
}
