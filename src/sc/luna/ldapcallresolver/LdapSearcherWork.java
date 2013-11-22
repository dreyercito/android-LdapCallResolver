package sc.luna.ldapcallresolver;

import sc.luna.ldapcallresolver.LdapSearcher.LdapSearcherException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LdapSearcherWork implements Runnable {
		private String numberToSearch;
		private Handler msgHandler;
		public static final int LDAP_MSG_OK=1;
		public static final int LDAP_MSG_ERROR=2;
		public static final int LDAP_MSG_ERROR_PARSING=3;
		private LdapSearcherOptions ldapOpt;
		

		public LdapSearcherWork(String num, Handler resultsHandler, LdapSearcherOptions opt) {
			super();
			numberToSearch = num;
			msgHandler=resultsHandler;
			ldapOpt=opt;
			Log.d("WORKER", "Number is " + num);
		}

		public void run() {
			CallItem myResult = null;
			try {
				LdapSearcher ldap = new LdapSearcher(ldapOpt);
				myResult = ldap.find(numberToSearch);
				Message message = new Message();
				message.what = LDAP_MSG_OK;
				message.obj = (Object) myResult;
				msgHandler.sendMessage(message);
			} catch (LdapSearcherException e) {
				Message message = new Message();
				if(e.getErrorCode()==LdapSearcherException.LDAP_URL_PARSING) {
					message.what = LDAP_MSG_ERROR_PARSING;
				} else {
					message.what = LDAP_MSG_ERROR;
				}
				message.obj = (Object) e.getMessage();
				msgHandler.sendMessage(message);
			}
		}
	}
