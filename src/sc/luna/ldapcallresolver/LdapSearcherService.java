package sc.luna.ldapcallresolver;

import sc.luna.ldapcallresolver.LdapSearcher.LdapSearcherException;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class LdapSearcherService extends IntentService {
	public static final String PARAM_NUMBER = "number";
	static LdapSearcher ldap;
	static CacheLdapDatabaseHelper sSQLInstance = null;

	public LdapSearcherService() {
		super("LdapSearcherService");
		Log.d("CallService", "Building service...");

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("CallService", "Starting service...");
		LdapSearcherOptions opt = null;
		String callingNumber = intent.getStringExtra(PARAM_NUMBER);
		String processedNumber;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		// If not active... get the hell outta here
		if (!prefs.getBoolean("pref_active", false)) {
			return;
		}

		opt = new LdapSearcherOptions(prefs);

		try {
			ldap = new LdapSearcher(opt);
		} catch (LdapSearcherException e1) {
			Log.d("Service", "Error creating searcher:" + e1.getMessage());
			return;
		}

		if (sSQLInstance == null) {
			sSQLInstance = new CacheLdapDatabaseHelper(getApplicationContext());
		}

		// Use preferences for checking contact
		if (prefs.getBoolean("pref_skip_contacts", false)) {
			if (contactExists(getApplicationContext(), callingNumber)) {
				return;
			}
		}

		CallItem theCall=null;

		// Cache usage
		if (prefs.getBoolean("pref_use_cache", true)) {
			theCall = sSQLInstance.getEntry(callingNumber);
			if (theCall == null || theCall.isOldEntry()) {
				theCall = null;
				Log.d("CallService", "Number not in cache");
			} else {
				Log.d("CallService", "Number in cache "+theCall.getName());
			}
		} 
		
		//Not in cache , old, or cache disabled
		if (theCall == null) {
			try {
				// Apply rules
				processedNumber = processNumber(callingNumber);
				// If none match, check preferences
				if (processedNumber == null) {
					if (!prefs.getBoolean("pref_resolve_if_no_match", true)) {
						return;
					}
					processedNumber = callingNumber;
				}
				theCall = ldap.find(processedNumber);
				if (theCall != null) {
					Log.d("Service","Storing in cache");
					sSQLInstance.storeNumber(callingNumber, theCall.getName());
				}
				Log.d("CallService", "Number is for " + theCall.getName());
			} catch (LdapSearcherException e) {
				Log.d("Service","Ldap exception "+e.getMessage());
			}
		}

		if (theCall != null && !theCall.getName().equals("")) {
			Log.d("Toast", "Informing toast receiver");
			Intent msgIntent = new Intent(ToastReceiver.SERVICE);
			msgIntent.putExtra(ToastReceiver.PARAM_NAME, theCall.getName());
			sendBroadcast(msgIntent);
		}
	}

	private String processNumber(String callingNumber) {
		String outNumber = null;
		Uri uri = RulesProvider.CONTENT_URI;
		Rule rule;
		Cursor cursor = getContentResolver().query(uri,
				RulesDatabaseHelper.FULL_PROJECTION, null, null,
				RulesDatabaseHelper.COL_PRIO + " asc");

		if (cursor != null && cursor.getCount() > 0) {
			String transformNumber = callingNumber;
			Boolean matchesFound = false;
			while (cursor.moveToNext()) {
				int stopInt = cursor.getInt(cursor
						.getColumnIndexOrThrow(RulesDatabaseHelper.COL_STOP));
				rule = new Rule(
						cursor.getInt(cursor
								.getColumnIndexOrThrow(RulesDatabaseHelper.COL_ID)),
						cursor.getInt(cursor
								.getColumnIndexOrThrow(RulesDatabaseHelper.COL_PRIO)),
						cursor.getString(cursor
								.getColumnIndexOrThrow(RulesDatabaseHelper.COL_REGEXPIN)),
						cursor.getString(cursor
								.getColumnIndexOrThrow(RulesDatabaseHelper.COL_REGEXPOUT)),
						stopInt == 1);

				Log.d("Service", String.format(
						"Rule: ID: %d NameIn %s Prio %d", rule.id,
						rule.regexpIn, rule.prio));

				if (rule.matches(transformNumber)) {
					matchesFound = true;
					transformNumber = rule.process(transformNumber);
					// Stop is used only if matches
					if (rule.stop) {
						Log.d("Service", String.format(
								"Number %s transformed to %s[stop]",
								callingNumber, transformNumber));
						return transformNumber;
					}
				}
				
			}
			if(matchesFound) {
				outNumber=transformNumber;
			}
		}
		Log.d("Service", String.format("Number %s transformed to %s",
				callingNumber, outNumber));
		return outNumber;
	}

	public boolean contactExists(Context context, String number) {
		// / number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,
				PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri,
				mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				Log.d("Service", "Number exists in contact list");
				return true;
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		return false;
	}
}
