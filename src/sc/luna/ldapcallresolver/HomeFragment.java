package sc.luna.ldapcallresolver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HomeFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	static TextView tMsg;
	static Button bPrefs;
	static Button bActivate;

	public HomeFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		Resources res=getResources();
		bActivate = (Button) rootView.findViewById(R.id.activateButton);
		tMsg = (TextView) rootView.findViewById(R.id.textstatus);
		bPrefs = (Button) rootView.findViewById(R.id.prefButton);
		
		bPrefs.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent i=new Intent(getActivity().getBaseContext(), LdapPreferences.class);
				startActivity(i);
			}
		});
		
				
		if(pref.getBoolean("pref_active", false)) {
			bActivate.setText(res.getString(R.string.homeinactivebutton));
			tMsg.setText(res.getString(R.string.statusactive));			
		} else {
			bActivate.setText(res.getString(R.string.homeactivebutton));
			tMsg.setText(res.getString(R.string.statusinactive));		
		}
		
		bActivate.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				boolean currstatus = pref.getBoolean("pref_active", false);
				pref.edit().putBoolean("pref_active", !currstatus).commit();
				getActivity().recreate();
			}
		});
		
		
		return rootView;
	}
}
