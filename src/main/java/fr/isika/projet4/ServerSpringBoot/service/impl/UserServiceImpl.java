package fr.isika.projet4.ServerSpringBoot.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fr.isika.projet4.ServerSpringBoot.constant.FileConstant;
import fr.isika.projet4.ServerSpringBoot.constant.UserServiceImplConstant;
import fr.isika.projet4.ServerSpringBoot.enumeration.Role;
import fr.isika.projet4.ServerSpringBoot.exception.model.EmailExistsException;
import fr.isika.projet4.ServerSpringBoot.exception.model.EmailNotFoundException;
import fr.isika.projet4.ServerSpringBoot.exception.model.NotAnImageFileException;
import fr.isika.projet4.ServerSpringBoot.exception.model.UserNotFoundException;
import fr.isika.projet4.ServerSpringBoot.exception.model.UsernameExistsException;
import fr.isika.projet4.ServerSpringBoot.model.Preference;
import fr.isika.projet4.ServerSpringBoot.model.User;
import fr.isika.projet4.ServerSpringBoot.model.UserPrincipal;
import fr.isika.projet4.ServerSpringBoot.repository.UserRepository;
import fr.isika.projet4.ServerSpringBoot.service.EmailService;
import fr.isika.projet4.ServerSpringBoot.service.LoginAttemptService;
import fr.isika.projet4.ServerSpringBoot.service.UserService;

@Service
@Transactional
@Qualifier("UserDetailsService_Customed")
public class UserServiceImpl implements UserService, UserDetailsService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private UserRepository userRepository;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private LoginAttemptService loginAttemptService;
	private EmailService emailService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, LoginAttemptService loginAttemptService, EmailService emailService) {
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.loginAttemptService = loginAttemptService;
		this.emailService = emailService;
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		User user = userRepository.findUserByUserName(userName);
		if (user == null) {
			LOGGER.error(UserServiceImplConstant.NO_USER_FOUND_BY_USERNAME + userName); //changed username to email
			throw new UsernameNotFoundException(UserServiceImplConstant.NO_USER_FOUND_BY_USERNAME + userName); //changed username to email
		} else {
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepository.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info(UserServiceImplConstant.USER_FOUND_BY_USERNAME + userName); //changed username to email
			return userPrincipal;
		}
	}

	private void validateLoginAttempt(User user) {
		if (user.isNotLocked()) {
			if (loginAttemptService.hasExceededMaxAttempts(user.getUserName())) { 
				user.setNotLocked(false);
			} else {
				user.setNotLocked(true);
			}
		} else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUserName());
		}
	}

	@Override
	public User register(String firstName, String lastName, String userName, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
		validateNewUserNameAndEmail(StringUtils.EMPTY, userName, email);
		User user = new User();
		user.setUserId(generateUserId());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUserName(userName);
		String passWord = generatePassword();
		user.setPassWord(encodePassword(passWord));
		user.setEmail(email);
		user.setProfileImageUrl(getTemporaryProfileImageUrl(userName));
		user.setJoinDate(new Date());
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setActive(true);
		user.setNotLocked(true);
		userRepository.save(user);
		LOGGER.info("New user password: " + passWord);
		emailService.sendNewPassWordEmail(firstName, passWord, email);
		return user;
	}
	
	@Override
	public User addNewUser(String firstName, String lastName, String userName, String email, String role,
			boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException, MessagingException {
		validateNewUserNameAndEmail(StringUtils.EMPTY, userName, email);
		User user = new User();
		user.setUserId(generateUserId());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setJoinDate(new Date());
		user.setUserName(userName);
		user.setEmail(email);
		String passWord = generatePassword();
		user.setPassWord(encodePassword(passWord));
		user.setActive(isActive);
		user.setNotLocked(isNotLocked);
		user.setRole(getRoleEnumName(role).name());
		user.setAuthorities(getRoleEnumName(role).getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(userName));
		userRepository.save(user);
		saveProfileImage(user, profileImage);
		LOGGER.info("New user password: " + passWord);
		emailService.sendNewPassWordEmail(firstName, passWord, email);
		return user;
	}
	
	@Override
	public User updateUser(String currentUserName, String newFirstName, String newLastName, String newUserName,
			String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		User currentUser = validateNewUserNameAndEmail(currentUserName, newUserName, newEmail);
		currentUser.setFirstName(newFirstName);
		currentUser.setLastName(newLastName);
		currentUser.setUserName(newUserName);
		currentUser.setEmail(newEmail);
		currentUser.setActive(isActive);
		currentUser.setNotLocked(isNotLocked);
		currentUser.setRole(getRoleEnumName(role).name());
		currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
		userRepository.save(currentUser);
		saveProfileImage(currentUser, profileImage);
		return currentUser;
	}

	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}
	
	private Role getRoleEnumName(String role) {
		return Role.valueOf(role.toUpperCase());
	}
	
	private String setProfileImageURL(String userName) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.USER_IMAGE_PATH + userName + FileConstant.FORWARD_SLASH + userName + FileConstant.DOT + FileConstant.JPG_EXTENSION).toUriString();
	}
	
	private String getTemporaryProfileImageUrl(String userName) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.DEFAULT_USER_IMAGE_PATH + userName).toUriString();
	}
	
	private String encodePassword(String password) {
		return bCryptPasswordEncoder.encode(password);
	}
	
	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}
	
	@Override
	public User updatePreference(String userName, String keyword, String language, int callTime) throws UserNotFoundException {
		User user = findUserByUserName(userName);
		if (user == null) {
			throw new UserNotFoundException(UserServiceImplConstant.NO_USER_FOUND_BY_USERNAME);
		}
		Preference preference = new Preference(keyword, language, callTime);
		user.setPreference(preference);
		userRepository.save(user);
		return user;
	}
	
	@Override
	public User updateProfileImage(String userName, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		User user = validateNewUserNameAndEmail(userName, null, null);
		saveProfileImage(user, profileImage);
		return user;
	}
	
	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
		if (profileImage != null) {
			if(!Arrays.asList(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
                throw new NotAnImageFileException(profileImage.getOriginalFilename() + FileConstant.NOT_AN_IMAGE_FILE);
            }
			Path userFolder = Paths.get(FileConstant.USER_FOLDER + user.getUserName()).toAbsolutePath().normalize();
			if (!Files.exists(userFolder)) {
				Files.createDirectories(userFolder);
				LOGGER.info(FileConstant.DIRECTORY_CREATED + userFolder);
			}
			Files.deleteIfExists(Paths.get(userFolder + user.getUserName() + FileConstant.DOT + FileConstant.JPG_EXTENSION));
			Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUserName() + FileConstant.DOT + FileConstant.JPG_EXTENSION), StandardCopyOption.REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageURL(user.getUserName()));
			userRepository.save(user);
			LOGGER.info(FileConstant.FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename() + ", renamed to " + user.getUserName() + FileConstant.DOT + FileConstant.JPG_EXTENSION);
		}	
	}

	private User validateNewUserNameAndEmail(String currentUserName, String newUserName, String newEmail) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
		User newUserByUserName = findUserByUserName(newUserName);
		User newUserByEmail = findUserByEmail(newEmail);
		if (StringUtils.isNotBlank(currentUserName)) {
			User currentUser = findUserByUserName(currentUserName);
			if (currentUser == null) {
				throw new UserNotFoundException(UserServiceImplConstant.NO_USER_FOUND_BY_USERNAME + currentUserName);
			}
			
			if (newUserByUserName != null && !currentUser.getId().equals(newUserByUserName.getId())) { // This needs reconsideration. 47
				throw new UsernameExistsException(UserServiceImplConstant.USERNAME_ALREADY_EXISTS);
			}
			
			if (newUserByEmail != null && !currentUser.getId().equals(newUserByEmail.getId())) { // This needs reconsideration. 47
				throw new EmailExistsException(UserServiceImplConstant.EMAIL_ALREADY_EXISTS); 
			}
			return currentUser;
		} else {
			if (newUserByUserName != null) {
				throw new UsernameExistsException(UserServiceImplConstant.USERNAME_ALREADY_EXISTS);
			}
			if (newUserByEmail != null) {
				throw new EmailExistsException(UserServiceImplConstant.EMAIL_ALREADY_EXISTS);
			}
			return null;
		}
	}

	@Override
	public void deleteUser(String userName) throws IOException, UserNotFoundException {
		User user = findUserByUserName(userName);
		if (user == null) {
			throw new UserNotFoundException(UserServiceImplConstant.NO_USER_FOUND_BY_USERNAME);
		}
        Path userFolder = Paths.get(FileConstant.USER_FOLDER + user.getUserName()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
		userRepository.deleteById(user.getId());
	}

	@Override
	public void resetPassWord(String email) throws EmailNotFoundException, MessagingException {
		User user = findUserByEmail(email);
		if (user == null) {
			throw new EmailNotFoundException(UserServiceImplConstant.NO_USER_FOUND_BY_EMAIL + email);
		}
		String passWord = generatePassword();
		user.setPassWord(encodePassword(passWord));
		userRepository.save(user);
		LOGGER.info("New user password: " + passWord);
		emailService.sendNewPassWordEmail(user.getFirstName(), passWord, user.getEmail());
	}

	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public User findUserByUserName(String userName) {
		return userRepository.findUserByUserName(userName);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}

}
