package sc.luna.ldapcallresolver;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class CacheLdapDatabaseHelper extends SQLiteOpenHelper {

	static Calendar cal = Calendar.getInstance();

	public CacheLdapDatabaseHelper(Context context) {
		super(context, "calls", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE calls  ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "number TEXT, name TEXT, time LONG)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void storeNumber(String number, String name) {
		SQLiteDatabase db = getWritableDatabase();
		storeNumber(db, number, name, cal.getTimeInMillis());
	}

	private void storeNumber(SQLiteDatabase db, String number, String name,
			long timeInMillis) {
		CallItem itemResult = null;
		// Check if entry already exists
        itemResult = getEntry(number);
        // If it is not empty, update entry
        if(itemResult!=null) {
        	SQLiteStatement stmt = db
    				.compileStatement("UPDATE calls set time=?,name=? where id=?");
    		stmt.bindLong(1, timeInMillis);
    		stmt.bindString(2, name);
    		stmt.bindLong(3,itemResult.id);
    		stmt.execute();
        	return;        	
        }
		// Else insert it
		SQLiteStatement stmt = db
				.compileStatement("INSERT INTO calls VALUES (null,?,?,?)");
		stmt.bindString(1, number);
		stmt.bindString(2, name);
		stmt.bindLong(3, timeInMillis);
		stmt.execute();
	}

	public CallItem getEntry(String number) {
		CallItem itemResult = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT id,number,name,time FROM "
				+ "calls where number=? ORDER BY time DESC ",
				new String[] { number });
		if (cursor.moveToNext()) {
			itemResult = new CallItem(cursor.getInt(0), cursor.getString(1),
					cursor.getString(2), cursor.getLong(3));
		}
		return itemResult;
	}
}
