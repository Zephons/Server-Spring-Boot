package fr.isika.projet4.ServerSpringBoot.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import fr.isika.projet4.ServerSpringBoot.domain.UserPrincipal;
import fr.isika.projet4.ServerSpringBoot.service.LoginAttemptService;

@Component
public class AuthenticationSuccessListener {
	
	private LoginAttemptService loginAttemptService;

	@Autowired
	public AuthenticationSuccessListener(LoginAttemptService loginAttemptService) {
		this.loginAttemptService = loginAttemptService;
	}
	
	@EventListener
	public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
		Object principal = event.getAuthentication().getPrincipal();
		if (principal instanceof UserPrincipal) {
			UserPrincipal userPrincipal = (UserPrincipal) principal;
			loginAttemptService.evictUserFromLoginAttemptCache(userPrincipal.getUsername());
		}
	}

}
