package sc.luna.ldapcallresolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RulesFragment extends ListFragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	static Button bAdd;
	static Button bDelAll;
	static CustomRuleAdapter myRuleAdapter;


	public RulesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_rules, container,
				false);

		bAdd = (Button) rootView.findViewById(R.id.buttonadd);
		bDelAll = (Button) rootView.findViewById(R.id.buttondelall);


		bAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getActivity().getBaseContext(),
						RuleAddActivity.class);
				startActivityForResult(i,1);
			}
		});	
		bDelAll.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				do_del_all();
				myRuleAdapter.clear();
			}
		});
		List<Rule> myrules=getRules();
		myRuleAdapter=new CustomRuleAdapter(getActivity(),myrules);
        setListAdapter(myRuleAdapter);
		return rootView;
	}
	


	private List<Rule> getRules() {
		List<Rule> listRules =  new ArrayList<Rule>();
		Uri uri = RulesProvider.CONTENT_URI;
		Rule rule;
		Cursor cursor = getActivity().getContentResolver().query(uri,
				RulesDatabaseHelper.FULL_PROJECTION, null, null,
				RulesDatabaseHelper.COL_PRIO + " asc");
		if (cursor != null && cursor.getCount() > 0) {
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
				listRules.add(rule);
				Log.d("FragmentLoader", String.format(
						"Rule: ID: %d NameIn %s Prio %d", rule.id,
						rule.regexpIn, rule.prio));
			}
		}
		return listRules;
	}
	
	private void do_del_all(){
		getActivity().getContentResolver().delete(RulesProvider.CONTENT_URI, null, null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		myRuleAdapter.clear();
		myRuleAdapter.addAll(getRules());
	}
}
