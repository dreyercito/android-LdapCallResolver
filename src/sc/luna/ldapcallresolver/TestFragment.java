package sc.luna.ldapcallresolver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TestFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static TextView msgView;
	public static TextView phoneView;

	public static ProgressDialog mProgressDialog;
	public static Button bTest;
	public static Button bAgenda;

	// Handler for the thread that tests LDAP connection
	static Handler resultsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LdapSearcherWork.LDAP_MSG_OK:
				msgView.setText("Ok: " + ((CallItem) msg.obj).getName());
				break;
			case LdapSearcherWork.LDAP_MSG_ERROR:

				msgView.setText("Error: " + (String) msg.obj);
				break;
			case LdapSearcherWork.LDAP_MSG_ERROR_PARSING:
				msgView.setText("Error: "
						+ msgView.getContext().getResources()
								.getString(R.string.error_pref));
				break;
			}
			super.handleMessage(msg);
			mProgressDialog.dismiss();
		}
	};

	public TestFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_test, container,
				false);
		msgView = (TextView) rootView.findViewById(R.id.results);
		phoneView = (TextView) rootView.findViewById(R.id.phoneIn);
		// Configure buttons
		bTest = (Button) rootView.findViewById(R.id.buttontest);
		bTest.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				doTest(view);
			}
		});
		// Contact picker button
		bAgenda = (Button) rootView.findViewById(R.id.buttonagenda);
		bAgenda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
				startActivityForResult(intent, 1);
			}
		});

		// Create spinner
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		return rootView;
	}

	// Get the phone number
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();
			if (uri != null) {
				Cursor c = null;
				try {
					c = this.getActivity()
							.getContentResolver()
							.query(uri,
									new String[] {
											ContactsContract.CommonDataKinds.Phone.NUMBER,
											ContactsContract.CommonDataKinds.Phone.TYPE },
									null, null, null);

					if (c != null && c.moveToFirst()) {
						String number = c.getString(0);
						number = number.replaceAll("\\s", "");
						phoneView.setText(number);
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		}
	}

	// Function that gets the number and tries to resolve it using ldap
	public void doTest(View view) {
		Log.d("TEST", "Button test");
		String thePhone = phoneView.getText().toString().trim();
		mProgressDialog.show();
		LdapSearcherOptions ldapOpt;

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		ldapOpt = new LdapSearcherOptions(pref);

		LdapSearcherWork searcher = new LdapSearcherWork(thePhone,
				TestFragment.resultsHandler, ldapOpt);
		new Thread(searcher).start();
	}

}
