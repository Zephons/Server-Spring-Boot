package fr.isika.projet4.ServerSpringBoot.constant;

public class AuthorityConstant {
	
	public static final String[] USER_AUTHORITIES = { "analysis" };
	public static final String[] ASSISTANT_AUTHORITIES = { "user:read", "user:update" };
    public static final String[] ADMIN_AUTHORITIES = { "user:read", "user:create", "user:update" };
    public static final String[] SUPER_ADMIN_AUTHORITIES = { "user:read", "user:create", "user:update", "user:delete" };

}
