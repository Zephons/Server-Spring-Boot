package fr.isika.projet4.ServerSpringBoot.constant;

import java.util.concurrent.TimeUnit;

public class SecurityConstant {
	
	public static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7); // 7 days expressed in milliseconds
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String TOKEN_HEADER = "Jwt-Token";
	public static final String TOKEN_CANNOT_BE_VERIFIED = "Token can not be verified";
	public static final String GET_THE_TWEETH_SENSE_LLC = "The Tweeth Sense, LLC";
	public static final String GET_THE_TWEETH_SENSE_ADMINISTRATION = "The Tweeth Sense Administration";
	public static final String AUTHORITIES = "authorities";
	public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page.";
	public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page.";
	public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
	//public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/resetpassword/**", "/user/image/**"};
	public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/image/**", "/user/test"};
}
