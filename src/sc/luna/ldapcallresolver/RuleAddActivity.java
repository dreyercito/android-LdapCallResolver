package sc.luna.ldapcallresolver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressLint("ShowToast")
public class RuleAddActivity extends Activity {

	private Button bAdd;
	private EditText eRuleIn;
	private EditText eRuleOut;
	private CheckBox cStop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rule_add);
		bAdd = (Button) findViewById(R.id.ruleAddButton);
		bAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				addRule(view);
			}
		});
		eRuleIn = (EditText) findViewById(R.id.editRegexpIn);
		eRuleOut = (EditText) findViewById(R.id.editRegexpOut);
		cStop = (CheckBox) findViewById(R.id.checkboxStop);
	}

	public void addRule(View view) {
		// First verify that the rule is ok
		String ruleIn = eRuleIn.getText().toString();
		String ruleOut = eRuleOut.getText().toString();
		Resources r =  view.getContext().getResources();
		@SuppressWarnings(value = { "unused" })
		Pattern regexpIn;
		
		if(ruleIn.equals("")) {
			showError(view, r.getString(R.string.rule_add_error_empty_regexp_in));	
			return;
		}

		if(ruleOut.equals("")) {
			showError(view, r.getString(R.string.rule_add_error_empty_regexp_out));	
			return;
		}
		
		// Is the regexp in rule valid?
		try {
			regexpIn =  Pattern.compile(ruleIn, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			showError(view,r.getString(R.string.rule_add_error_regexp_in));
			return;
		}
		
		//Simple check for avoid common errors: 
		//If there is a capture group, check that is used!
		if(Pattern.compile("\\(").matcher(ruleIn).find()) {
			if(!Pattern.compile("\\$\\{\\d").matcher(ruleOut).find()) {
				showError(view, r.getString(R.string.rule_add_error_capture_regexp_out));	
				return;	
			}
		}		
		// Add the new rule to the content provider
		ContentValues vals = new ContentValues();
		vals.put(RulesDatabaseHelper.COL_REGEXPIN, eRuleIn.getText().toString());
		vals.put(RulesDatabaseHelper.COL_REGEXPOUT, eRuleOut.getText()
				.toString());
		vals.put(RulesDatabaseHelper.COL_STOP, cStop.isChecked());
		getContentResolver().insert(RulesProvider.CONTENT_URI, vals);
		debugLogValues();
		finish();
	}
	
	public void showError(View view,String error) {
		Toast.makeText(view.getContext(), error, Toast.LENGTH_SHORT).show();
	}

	public void debugLogValues() {
		Uri uri = RulesProvider.CONTENT_URI;
		Cursor cursor = getContentResolver().query(uri,
				RulesDatabaseHelper.FULL_PROJECTION, null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				Log.d("DEBUGVAL",
						String.format(
								"ID: %d RegexpIn: %s RegexpOut: %s Prio %d",
								cursor.getInt(cursor
										.getColumnIndexOrThrow(RulesDatabaseHelper.COL_ID)),
								cursor.getString(cursor
										.getColumnIndexOrThrow(RulesDatabaseHelper.COL_REGEXPIN)),
								cursor.getString(cursor
										.getColumnIndexOrThrow(RulesDatabaseHelper.COL_REGEXPOUT)),
								cursor.getInt(cursor
										.getColumnIndexOrThrow(RulesDatabaseHelper.COL_PRIO))));
			}

		}

	}

}
