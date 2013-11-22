package sc.luna.ldapcallresolver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

//
// This class handles the calls and starts the IntentService for resolving the
// name.
//
public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context mContext, Intent intent) {
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		if (state == null)
			return;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext.getApplicationContext());

		// If not active... get the hell outta here
		if (!prefs.getBoolean("pref_active", false)) {
			return;
		}

		if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			// Get the phone
			Bundle bundle = intent.getExtras();
			String callingNumber = bundle.getString("incoming_number");
			Log.d("CallReceiver", "Got call " + callingNumber);
			// Call the resolver/displayer service
			Intent msgIntent = new Intent(mContext, LdapSearcherService.class);
			msgIntent.putExtra(LdapSearcherService.PARAM_NUMBER, callingNumber);
			mContext.startService(msgIntent);
		}
	}

}
