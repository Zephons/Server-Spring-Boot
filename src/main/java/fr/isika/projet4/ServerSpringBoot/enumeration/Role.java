package fr.isika.projet4.ServerSpringBoot.enumeration;

import fr.isika.projet4.ServerSpringBoot.constant.AuthorityConstant;

public enum Role {
	
	ROLE_USER(AuthorityConstant.USER_AUTHORITIES),
	ROLE_ASSISTANT(AuthorityConstant.ASSISTANT_AUTHORITIES),
	ROLE_ADMIN(AuthorityConstant.ADMIN_AUTHORITIES),
	ROLE_SUPER_ADMIN(AuthorityConstant.SUPER_ADMIN_AUTHORITIES);
	
	private String[] authorities;
	
	Role(String... authorities) {
		this.authorities = authorities;
	}
	
	public String[] getAuthorities() {
		return authorities;
	}

}
