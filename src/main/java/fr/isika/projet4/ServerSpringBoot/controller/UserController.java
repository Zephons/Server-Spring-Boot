package fr.isika.projet4.ServerSpringBoot.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.isika.projet4.ServerSpringBoot.constant.FileConstant;
import fr.isika.projet4.ServerSpringBoot.constant.SecurityConstant;
import fr.isika.projet4.ServerSpringBoot.exception.ExceptionHandling;
import fr.isika.projet4.ServerSpringBoot.exception.model.EmailExistsException;
import fr.isika.projet4.ServerSpringBoot.exception.model.EmailNotFoundException;
import fr.isika.projet4.ServerSpringBoot.exception.model.NotAnImageFileException;
import fr.isika.projet4.ServerSpringBoot.exception.model.UserNotFoundException;
import fr.isika.projet4.ServerSpringBoot.exception.model.UsernameExistsException;
import fr.isika.projet4.ServerSpringBoot.model.HttpResponse;
import fr.isika.projet4.ServerSpringBoot.model.User;
import fr.isika.projet4.ServerSpringBoot.model.UserPrincipal;
import fr.isika.projet4.ServerSpringBoot.service.UserService;
import fr.isika.projet4.ServerSpringBoot.utility.JwtUtility;

@RestController
@RequestMapping(value = {"/", "/user"})
public class UserController extends ExceptionHandling {
	
	public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully.";
	public static final String EMAIL_SENT = "An email with a new password was sent to: ";
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JwtUtility jwtUtility;
	
	@Autowired
	public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtility jwtUtility) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtUtility = jwtUtility;
	}

	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		authenticate(user.getUserName(), user.getPassWord()); 
		User loginUser = userService.findUserByUserName(user.getUserName()); 
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeaders = getJwtHeader(userPrincipal);
		return new ResponseEntity<User>(loginUser, jwtHeaders, HttpStatus.OK);
	}

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
		User newUser = userService.register(
				user.getFirstName(), 
				user.getLastName(),
				user.getUserName(),
				user.getEmail());
		return new ResponseEntity<User>(newUser, HttpStatus.OK);
	}
	
	@PostMapping("/add")
	@PreAuthorize("hasAnyAuthority('user:create')")
	public ResponseEntity<User> addNewUser(
			@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName,
			@RequestParam("userName") String userName,
			@RequestParam("email") String email,
			@RequestParam("role") String role,
			@RequestParam("isActive") String isActive,
			@RequestParam("isNotLocked") String isNotLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage
			) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException, MessagingException {
		User newUser = userService.addNewUser(firstName, lastName, userName, email, role, 
				Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<User>(newUser, HttpStatus.OK); 
	}
	
	@PostMapping("/update")
	@PreAuthorize("hasAnyAuthority('user:update')")
	public ResponseEntity<User> updateUser (
			@RequestParam("currentUserName") String currentUserName,
			@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName,
			@RequestParam("userName") String userName,
			@RequestParam("email") String email,
			@RequestParam("role") String role,
			@RequestParam("isActive") String isActive,
			@RequestParam("isNotLocked") String isNotLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage
			) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		User updatedUser = userService.updateUser(currentUserName, firstName, lastName, userName, email, role, 
				Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<User>(updatedUser, HttpStatus.OK); 
	}
	
	@GetMapping("/find/{userName}")
	@PreAuthorize("hasAnyAuthority('user:read')")
	public ResponseEntity<User> getUser(@PathVariable("userName") String userName) {
		User user = userService.findUserByUserName(userName);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@GetMapping("/list")
	@PreAuthorize("hasAnyAuthority('user:read')")
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.getUsers();
		return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}
	
	@GetMapping("/reset-password/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {
		userService.resetPassWord(email);
		return response(HttpStatus.OK, EMAIL_SENT + email);
	}
	
	@DeleteMapping("/delete/{userName}")
	@PreAuthorize("hasAnyAuthority('user:delete')")
	public ResponseEntity<HttpResponse> deleteUser(@PathVariable("userName") String userName) throws IOException, UserNotFoundException {
		userService.deleteUser(userName);
		return response(HttpStatus.OK, USER_DELETED_SUCCESSFULLY);
	};
	
	@PostMapping("/update-profile-image")
	public ResponseEntity<User> updateProfileImage (
			@RequestParam("userName") String userName,
			@RequestParam("profileImage") MultipartFile profileImage
			) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		User updatedUser = userService.updateProfileImage(userName, profileImage);
		return new ResponseEntity<User>(updatedUser, HttpStatus.OK); 
	}
	
	@PostMapping("/preference/update")
	public ResponseEntity<User> updatePreference (
			@RequestParam("userName") String userName,
			@RequestParam("keyword") String keyword,
			@RequestParam("language") String language,
			@RequestParam("callTime") int callTime
			) throws UserNotFoundException {
		User updatedUser = userService.updatePreference(userName, keyword, language, callTime);
		return new ResponseEntity<User>(updatedUser, HttpStatus.OK);
	}
	
	@GetMapping(path = "/image/{userName}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getProfileImage(@PathVariable("userName") String userName, @PathVariable("fileName") String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER + userName + FileConstant.FORWARD_SLASH + fileName));
	}
	
	@GetMapping(path = "/image/profile/{userName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage(@PathVariable("userName") String userName) throws IOException {
		URL url = new URL (FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + userName + FileConstant.VARIATION_HUMAN);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (InputStream inputStream = url.openStream()) {
			int bytesRead;
			byte[] chunk = new byte[1024];
			while ((bytesRead = inputStream.read(chunk)) > 0) {
				byteArrayOutputStream.write(chunk, 0, bytesRead);
			}
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
		return new ResponseEntity<HttpResponse>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
                message), httpStatus); // 2nd argument should be httpStatus or httpStatus.OK ?
	}

	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(SecurityConstant.TOKEN_HEADER, jwtUtility.generateJwtToken(userPrincipal));
		return headers;
	}

	private void authenticate(String userName, String passWord) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, passWord));
	}
	
}
