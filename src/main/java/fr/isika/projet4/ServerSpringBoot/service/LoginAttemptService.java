package fr.isika.projet4.ServerSpringBoot.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {

	private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
	private static final int ATTEMPT_INCREMENT = 1;
	private LoadingCache<String, Integer> loginAttemptCache; // userName type (String) & number of attempts type (Integer)

	public LoginAttemptService() {
		loginAttemptCache = CacheBuilder.newBuilder()
				.expireAfterWrite(15, TimeUnit.MINUTES)
				.maximumSize(100)
				.build(new CacheLoader<String, Integer>(){
					public Integer load(String key) {
						return 0;
					}
				});
	}

	public void evictUserFromLoginAttemptCache(String userName) {
		loginAttemptCache.invalidate(userName);
	}

	public void addUserToLoginAttemptCache(String userName) {
		int attempts = 0;
		try {
			attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(userName);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		loginAttemptCache.put(userName, attempts);
	}

	public boolean hasExceededMaxAttempts(String userName) {
		try {
			return loginAttemptCache.get(userName) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}
}
