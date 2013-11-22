package sc.luna.ldapcallresolver;

/*
 * @author Jose Carlos Luna Duran
 * $Id$
 * 
 * Database helper for rules (allows to move up and down), delete and update 
 * 
 */
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class RulesDatabaseHelper extends SQLiteOpenHelper {

	public static final String TABLE = "rules";
	public static final String COL_ID = "id";
	public static final String COL_PRIO = "prio";
	public static final String COL_REGEXPIN = "regexpin";
	public static final String COL_REGEXPOUT = "regexpout";
	public static final String COL_STOP = "stop";
	public static final String[] FULL_PROJECTION = { COL_ID, COL_PRIO,
			COL_REGEXPIN, COL_REGEXPOUT, COL_STOP };

	public RulesDatabaseHelper(Context context) {
		super(context, "rules", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + "  (" + COL_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PRIO
				+ " INTEGER," + COL_REGEXPIN + " TEXT, " + COL_REGEXPOUT
				+ " TEXT, " + COL_STOP + " INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public int getNewPrio(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT " + COL_PRIO + " FROM " + TABLE
				+ " ORDER BY " + COL_PRIO + " DESC ", null);
		if (cursor.moveToNext()) {
			int prioResult = cursor.getInt(0);
			return prioResult + 1;
		}
		return 1;
	}

	public long storeRule(SQLiteDatabase db, Rule theRule) {
		int stopInt = 0;
		int prio;
		prio = getNewPrio(db);
		theRule.prio = prio;
		if (theRule.stop == true) {
			stopInt = 1;
		}
		ContentValues values = new ContentValues();
		values.put(COL_PRIO, Integer.toString(prio));
		values.put(COL_REGEXPIN, theRule.regexpIn);
		values.put(COL_REGEXPOUT, theRule.regexpOut);
		values.put(COL_STOP, Integer.toString(stopInt));
		return db.insert(TABLE, null, values);
	}

	public Rule getRuleById(SQLiteDatabase db, int id) {
		Rule outRule = null;
		Cursor cursor = db.rawQuery("SELECT " + COL_ID + "," + COL_PRIO + ","
				+ COL_REGEXPIN + "," + COL_REGEXPOUT + "," + COL_STOP
				+ " FROM " + TABLE + " where " + COL_ID + "=? ORDER BY "
				+ COL_PRIO + " DESC", new String[] { Integer.toString(id) });
		if (cursor.moveToNext()) {
			boolean stopRule = false;
			if (cursor.getInt(4) == 1) {
				stopRule = true;
			}
			
			outRule = new Rule(cursor.getInt(0), cursor.getInt(1),
					cursor.getString(2), cursor.getString(3), stopRule);
			
			Log.d("Rule",String.format("Rule: %d|%d|%s|%s|%d",cursor.getInt(0), cursor.getInt(1),
					cursor.getString(2), cursor.getString(3), cursor.getInt(4) ));
		}
		return outRule;
	}

	public Rule getRuleByPrio(SQLiteDatabase db, int prio) {
		Rule outRule = null;
		Cursor cursor = db.rawQuery("SELECT " + COL_ID + "," + COL_PRIO + ","
				+ COL_REGEXPIN + "," + COL_REGEXPOUT + "," + COL_STOP
				+ " FROM " + TABLE + " where " + COL_PRIO + "=? ORDER BY "
				+ COL_PRIO + " DESC", new String[] { Integer.toString(prio) });
		if (cursor.moveToNext()) {
			boolean stopRule = false;
			if (cursor.getInt(4) == 1) {
				stopRule = true;
			}
			outRule = new Rule(cursor.getInt(0), cursor.getInt(1),
					cursor.getString(2), cursor.getString(3), stopRule);
		}		
		Log.d("RuleByPrio",String.format("Rule: %d|%d|%s|%s|%d",cursor.getInt(0), cursor.getInt(1),
				cursor.getString(2), cursor.getString(3), cursor.getInt(4) ));
		return outRule;
	}

	public void updateRule(SQLiteDatabase db, Rule theRule) {
		int stopInt = 0;

		if (theRule.stop == true) {
			stopInt = 1;
		}
		SQLiteStatement stmt = db.compileStatement("update " + TABLE + " set "
				+ COL_PRIO + "=?," + COL_REGEXPIN + "=?," + COL_REGEXPOUT
				+ "=?," + COL_STOP + "=?" + " where " + COL_ID + "=?");
		stmt.bindLong(1, theRule.prio);
		stmt.bindString(2, theRule.regexpIn);
		stmt.bindString(3, theRule.regexpOut);
		stmt.bindLong(4, stopInt);
		stmt.bindLong(5, theRule.id);
		stmt.execute();
	}

	public Rule[] getRuleChain() {
		ArrayList<Rule> Rules = new ArrayList<Rule>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + COL_ID + "," + COL_PRIO + ","
				+ COL_REGEXPIN + "," + COL_REGEXPOUT + "," + COL_STOP
				+ " FROM " + TABLE + "ORDER BY " + COL_PRIO + " DESC ", null);

		if (cursor.moveToNext()) {
			Rule singleRule;
			boolean stopRule = false;
			if (cursor.getInt(4) == 1) {
				stopRule = true;
			}
			singleRule = new Rule(cursor.getInt(0), cursor.getInt(1),
					cursor.getString(2), cursor.getString(3), stopRule);
			Rules.add(singleRule);
		}

		return (Rule[]) Rules.toArray();
	}

	public void moveRuleUpById(SQLiteDatabase db, int id) {
		Rule theRule = getRuleById(db, id);
		if (theRule==null) {
			return;
		}
		int oldPrio;
		if (theRule.prio == 1) {
			return;
		}
		Rule oldRule = getRuleByPrio(db, theRule.prio - 1);
		oldPrio = oldRule.prio;
		oldRule.prio = theRule.prio;
		theRule.prio = oldPrio;
		updateRule(db, theRule);
		updateRule(db, oldRule);
	}

	public void moveRuleDownById(SQLiteDatabase db, int id) {
		Rule theRule = getRuleById(db, id);
		if (theRule==null) {
			return;
		}
		int newPrio = getNewPrio(db);
		int oldPrio;
		if (newPrio - 1 == theRule.prio) {
			return;
		}
		Rule oldRule = getRuleByPrio(db, newPrio - 1);
		oldPrio = oldRule.prio;
		oldRule.prio = theRule.prio;
		theRule.prio = oldPrio;
		updateRule(db, theRule);
		updateRule(db, oldRule);
	}

	public void deleteRuleById(SQLiteDatabase db, int id) {
		Rule theRule = getRuleById(db, id);
		SQLiteStatement stmt = db.compileStatement("update " + TABLE + " set "
				+ COL_PRIO + "=" + COL_PRIO + "-1 where " + COL_PRIO + ">?");
		stmt.bindLong(1, theRule.prio);
		stmt.execute();

		stmt = db.compileStatement("delete from " + TABLE + " where " + COL_ID
				+ "=?");
		stmt.bindLong(1, theRule.id);
		stmt.execute();
	}
	
	
	public void deleteAll(SQLiteDatabase db) {
		SQLiteStatement stmt = db.compileStatement("delete from " + TABLE);
		stmt.execute();
	}

}
