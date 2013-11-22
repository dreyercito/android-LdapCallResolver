package sc.luna.ldapcallresolver;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class CustomRuleAdapter extends ArrayAdapter<Rule> {

	private final List<Rule> list;
	private final Context context;
	private final LayoutInflater mInflater;

	public CustomRuleAdapter(Context context, List<Rule> list) {
		super(context, R.layout.rule_item, list);
		this.context = context;
		this.list = list;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	static class ViewHolder {
		protected TextView tRuleIn;
		protected TextView tRuleOut;
		protected TextView tStop;
		protected Button bUp;
		protected Button bDown;
		protected Button bDel;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.rule_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.tRuleIn = (TextView) view.findViewById(R.id.itemin);
			viewHolder.tRuleOut = (TextView) view.findViewById(R.id.itemout);
			viewHolder.tStop = (TextView) view.findViewById(R.id.itemstop);
			viewHolder.bUp = (Button) view.findViewById(R.id.itemupbutton);
			viewHolder.bDown = (Button) view.findViewById(R.id.itemdownbutton);
			viewHolder.bDel = (Button) view.findViewById(R.id.itemdelbutton);
			view.setTag(viewHolder);
		} else {
		    view = convertView;
		}
		if(position==0) {
	     ((ViewHolder) view.getTag()).bUp.setVisibility(View.INVISIBLE);
 		} else {
	    	 ((ViewHolder) view.getTag()).bUp.setVisibility(View.VISIBLE);

 		}
		
		if(position==(list.size()-1)) {
		     ((ViewHolder) view.getTag()).bDown.setVisibility(View.INVISIBLE);
	    } else {
	    	 ((ViewHolder) view.getTag()).bDown.setVisibility(View.VISIBLE);
	    }
		
		((ViewHolder) view.getTag()).tRuleIn.setText("IN:"
				+ list.get(position).regexpIn);
		((ViewHolder) view.getTag()).tRuleOut.setText("OUT:"
				+ list.get(position).regexpOut);

		if (list.get(position).stop) {
			((ViewHolder) view.getTag()).tStop.setText("stop");
		} else {
			((ViewHolder) view.getTag()).tStop.setText("");
		}

		final int rule_id = list.get(position).id;
		final int currposition = position;

		boolean stop = list.get(position).stop;
		if (stop) {
			((ViewHolder) view.getTag()).tStop.setText("STOP");
		}
		((ViewHolder) view.getTag()).bDel
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						do_del_rule(rule_id);
						list.remove(currposition);
						notifyDataSetChanged();
					}
				});
		
		((ViewHolder) view.getTag()).bUp
		.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(currposition==0) {
					return;
				}
				do_up_rule(rule_id);
				Collections.swap(list, currposition, currposition-1);
				notifyDataSetChanged();
			}
		});
		((ViewHolder) view.getTag()).bDown
		.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(currposition==list.size()-1) {
					return;
				}
				do_down_rule(rule_id);
				Collections.swap(list, currposition, currposition+1);
				notifyDataSetChanged();
			}
		});

		return view;
	}

	public void do_del_rule(int rule_id) {
		Log.d("Adapter", "Deleting rule " + Integer.toString(rule_id));
		context.getContentResolver().delete(
				Uri.parse(RulesProvider.CONTENT_URI_PREFIX
						+ RulesProvider.BASE_PATH + "/"
						+ Integer.toString(rule_id)), null, null);
	}

	public void do_up_rule(int rule_id) {
		ContentValues vals = new ContentValues();
		Log.d("Adapter", "Moving up rule " + Integer.toString(rule_id));
		context.getContentResolver().update(Uri.parse(RulesProvider.CONTENT_URI_PREFIX+"rules/"+Integer.toString(rule_id)+"/up"), vals, null, null);
	}

	public void do_down_rule(int rule_id) {
		ContentValues vals = new ContentValues();
		Log.d("Adapter", "Moving down rule " + Integer.toString(rule_id));
		context.getContentResolver().update(Uri.parse(RulesProvider.CONTENT_URI_PREFIX+"rules/"+Integer.toString(rule_id)+"/down"), vals, null, null);
	}

}
