package sc.luna.ldapcallresolver;

import android.content.SharedPreferences;

public class LdapSearcherOptions {
	public String ldapUrl = null;
	public String ldapBaseDN = null;
	public String ldapUserDN = null;
	public String ldapPassword = null;

	//Parameters to be able to launch searches
	LdapSearcherOptions(String ldapUrl, String ldapBaseDN,
			String ldapUserDN, String ldapPassword) {
		this.ldapUrl = ldapUrl;
		this.ldapBaseDN = ldapBaseDN;
		this.ldapUserDN = ldapUserDN;
		this.ldapPassword = ldapPassword;
	}
	public LdapSearcherOptions(SharedPreferences pref) {
		this.ldapUrl=pref.getString("pref_ldapurl", "");
		this.ldapBaseDN=pref.getString("pref_ldap_basedn", "");
		this.ldapUserDN = pref.getString("pref_ldap_userdn", "");
		this.ldapPassword = pref.getString("pref_ldap_password", "");
	}

}

