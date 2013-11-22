package sc.luna.ldapcallresolver;

import java.util.Calendar;

public class CallItem {

	public String name;
	public String number;
	public Calendar time;
	public int id;
	public final int ONE_WEEK_SECONDS = 3600*24*7;
	
	public CallItem(int id, String number, String name) {
		this.name = name;
		this.number = number.trim();
		this.time = Calendar.getInstance();
		this.id=id;
	}

	public CallItem(int id, String number, String name, Calendar time) {
		this.name = name;
		this.number = number.trim();
		this.time = time;
		this.id=id;
	}

	public CallItem(int id,String number, String name, long time) {
		this.time = Calendar.getInstance();
		this.time.setTimeInMillis(time);
		this.name = name;
		this.number = number.trim();
		this.id=id;
	}

	public long getTimeInMillis() {
		return this.time.getTimeInMillis();
	}

	public Calendar getTime() {
		return this.time;
	}

	public String getName() {
		return this.name;
	}

	public String getNumber() {
		return this.number;
	}
	
	public int getElapsedSeconds() {
		long curtime = Calendar.getInstance().getTimeInMillis();
		return Math.round((curtime-getTimeInMillis())/1000);
	}
	
	public boolean isOldEntry() {
		int elapsed=getElapsedSeconds();
		return elapsed>ONE_WEEK_SECONDS;
	}
}
