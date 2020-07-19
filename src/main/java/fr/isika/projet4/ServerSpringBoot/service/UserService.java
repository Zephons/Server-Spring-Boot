package fr.isika.projet4.ServerSpringBoot.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import fr.isika.projet4.ServerSpringBoot.domain.User;
import fr.isika.projet4.ServerSpringBoot.exception.domain.EmailExistsException;
import fr.isika.projet4.ServerSpringBoot.exception.domain.EmailNotFoundException;
import fr.isika.projet4.ServerSpringBoot.exception.domain.NotAnImageFileException;
import fr.isika.projet4.ServerSpringBoot.exception.domain.UserNotFoundException;
import fr.isika.projet4.ServerSpringBoot.exception.domain.UsernameExistsException;

public interface UserService {
	
	User register(String firstName, String lastName, String userName, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException;
	
	List<User> getUsers();
	
	User findUserByUserName(String userName);
	
	User findUserByEmail(String email);
	
	User addNewUser(String firstName, String lastName, String userName, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException, MessagingException;
	
	User updateUser(String currentUserName, String newFirstName, String newLastName, String newUserName, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;

	void deleteUser(String username) throws IOException, UserNotFoundException;
	
	void resetPassWord(String email) throws EmailNotFoundException, MessagingException;
	
	User updateProfileImage(String userName, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	
	User updatePreference(String userName, String keyword, String language, int callTime) throws UserNotFoundException;
	
}
