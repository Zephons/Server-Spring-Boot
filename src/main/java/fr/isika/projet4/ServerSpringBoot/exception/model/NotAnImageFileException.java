package fr.isika.projet4.ServerSpringBoot.exception.model;

public class NotAnImageFileException extends Exception {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -3781147958342497268L;

	public NotAnImageFileException(String message) {
        super(message);
    }
    
}
