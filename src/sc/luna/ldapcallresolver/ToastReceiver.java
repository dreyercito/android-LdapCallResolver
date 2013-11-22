package sc.luna.ldapcallresolver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastReceiver extends BroadcastReceiver {
	public static final String PARAM_NAME = "name";
	public static final String SERVICE = "sc.luna.ldapcallresolver.intent.toast";

	@Override
	public void onReceive(Context context, Intent intent) {
		String name = intent.getStringExtra(ToastReceiver.PARAM_NAME);
	    LayoutInflater inflater = LayoutInflater.from(context);
	        
		View layout = inflater.inflate(R.layout.toast, null);
		TextView message = (TextView) layout.findViewById(R.id.toastText);
		message.setText(name);
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, 0, 150);
		toast.setView(layout);
		toast.show();		
	}
}
