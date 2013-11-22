/*
 * 
 * @author Jose Carlos Luna Duran
 * 
 * Wraps unboundid LDAP library functionality just to resolve numbers and get names
 * 
 */

package sc.luna.ldapcallresolver;

import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.StrictMode;
import android.util.Log;

import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

public class LdapSearcher {
	private String ldapHost = null;
	private int ldapPort;
	private boolean ldapSSL;
	private String ldapDN;
	private String ldapBaseDN;
	private String ldapPassword;
	private LDAPConnection connection;

	public LdapSearcher(LdapSearcherOptions opt) throws LdapSearcherException {
		this.setUrl(opt.ldapUrl);
		this.setBaseDN(opt.ldapBaseDN);
		this.setDN(opt.ldapUserDN);
		this.setPassword(opt.ldapPassword);		
	}

	public CallItem find(String num) throws LdapSearcherException {
		if (ldapHost==null) {
			throw new LdapSearcherException("No LDAP defined", LdapSearcherException.LDAP_INIT);
		}
	    Log.d("LDAPN", "Connecting to ldap...");
		connectLDAP();
		Log.d("LDAPN", "Number to search is " + num);
		CallItem personOut = getSearchResult(num);
		Log.d("LDAPR", personOut.getName());
		return personOut;
	}

	// Setters
	public void setDN(String dN) {
		ldapDN = dN;
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public void setBaseDN(String baseDN) {
		ldapBaseDN = baseDN;
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public void setPassword(String pass) {
		ldapPassword = pass;
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public void setUrl(String url) throws LdapSearcherException {
		parseLdapUrl(url);
	}

	//
	// Parse an ldap url in its components
	//
	public void parseLdapUrl(String url) throws LdapSearcherException {
		Pattern ldapUrl = Pattern
				.compile("(ldap|ldaps)://([a-zA-Z.-]+)(:\\d+)?/?");
		Matcher m = ldapUrl.matcher(url.trim());
		if (connection != null) {
			connection.close();
			connection = null;
		}
		if (m.matches()) {
			Log.d("LDAP", "Parsing ldap url");
			String protocol = m.group(1);
			if ("ldap".equals(protocol)) {
				ldapPort = 389;
				ldapSSL = false;
			} else {
				ldapPort = 636;
				ldapSSL = true;
			}
			ldapHost = m.group(2);
			String port = m.group(3);
			if (port != null) {
				ldapPort = Integer.parseInt(port.substring(1));
			}
		} else {
			throw new LdapSearcherException("Could not parse LDAP server url",
					LdapSearcherException.LDAP_URL_PARSING);
		}

	}

	private void connectLDAP() throws LdapSearcherException {
		SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
		LDAPConnectionOptions options = new LDAPConnectionOptions();
		// Just for debugging
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.permitNetwork().build());
		options.setAutoReconnect(true);
		options.setConnectTimeoutMillis(200000);
		options.setFollowReferrals(false);
		options.setMaxMessageSize(0);

		// Try to connect
		try {
			if (ldapSSL == true) {
				connection = new LDAPConnection(
						sslUtil.createSSLSocketFactory(), options);
			} else {
				connection = new LDAPConnection(options);
			}
			Log.d("LDAPN", "Now connecting...");
			connection.connect(ldapHost, ldapPort);

		} catch (GeneralSecurityException e1) {
			throw new LdapSearcherException("Error initializing SSL",
					LdapSearcherException.LDAP_INIT);
		} catch (LDAPException e1) {
			throw new LdapSearcherException(String.format(
					"Error connection to LDAP server (%s:%d)", this.ldapHost,
					this.ldapPort), LdapSearcherException.LDAP_CONNECTION);
		}

		// Authenticate if needed
		try {
			if (ldapPassword != null) {
				Log.d("LDAPN", "Now binding...");
				connection.bind(ldapDN, ldapPassword);
			}
		} catch (LDAPException e) {
			connection.close();
			throw new LdapSearcherException(e.getMessage(),
					LdapSearcherException.LDAP_PASSWORD);
		}
		Log.d("LDAPN", "Binding done...");
	}

	// What LDAP attributes I'm interested in
	public String[] getLdapAttributes() {
		String[] attributeNames = new String[] { "givenName", "sn" };
		return attributeNames;
	}

	// Get name from phone number (either mobile or fixed)
	public synchronized CallItem getSearchResult(String phone)
			throws LdapSearcherException {
		String searchFilter = "(|(mobile=" + phone + ")(telephoneNumber="
				+ phone + "))";
		StringBuffer outPerson = new StringBuffer();
		SearchResult searchResult;
		CallItem outCallItem = null;

		if (ldapHost == null) {
			throw new LdapSearcherException("Ldap info not initialized",
					LdapSearcherException.LDAP_INIT);
		}
		try {
			Log.d("LDAPN", "Now searching...");
			searchResult = connection.search(ldapBaseDN, SearchScope.SUB,
					searchFilter, getLdapAttributes());
		} catch (LDAPSearchException e) {
			throw new LdapSearcherException(e.getMessage(),
					LdapSearcherException.LDAP_EXCEPTION);
		}

		Log.i("LDAP", searchResult.getEntryCount() + " entries returned.");
		for (SearchResultEntry e : searchResult.getSearchEntries()) {
			String personName = e.getAttribute("givenName").getValue() + " "
					+ e.getAttribute("sn").getValue();
			outPerson.append(personName + "\n");
		}
		outCallItem = new CallItem(-1, phone, outPerson.toString());
		Log.d("LDAPN", "Search finished...");
		return outCallItem;
	}

	// Exception class
	public class LdapSearcherException extends Exception {
		private static final long serialVersionUID = 8847244761384632956L;
		private int errorCode = -1;
		public final static int ERROR_UNKNOWN = -1;
		public final static int LDAP_URL_PARSING = 1;
		public final static int LDAP_PASSWORD = 2;
		public final static int LDAP_CONNECTION = 3;
		public final static int LDAP_EXCEPTION = 4;
		public final static int LDAP_NOT_FOUND = 5;
		public final static int LDAP_INIT = 6;

		public LdapSearcherException(String message, int code) {
			super(message);
			this.errorCode = code;
		}

		public int getErrorCode() {
			return this.errorCode;
		}
	}

}
