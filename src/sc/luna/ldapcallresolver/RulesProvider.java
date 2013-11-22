package sc.luna.ldapcallresolver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class RulesProvider extends ContentProvider {
	private RulesDatabaseHelper mRulesDB;
	private static final String AUTHORITY = "sc.luna.ldapcallresolver.rules.contentprovider";
	public static final String BASE_PATH = "rules";
	public static final String CONTENT_URI_PREFIX = "content://" + AUTHORITY + "/";
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_PREFIX + BASE_PATH);
	private static final String CONTENT_TYPE = "vnd.sc.luna.ldapcallresolver.rules";
	private static final String CONTENT_ITEM_TYPE = "rule";
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int RULES = 1;
	private static final int RULE = 2;
	private static final int RULE_UP = 3;
	private static final int RULE_DOWN = 4;

	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, RULES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RULE);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/up", RULE_UP);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/down", RULE_DOWN);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = mRulesDB.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case RULES:
			mRulesDB.deleteAll(db);
			break;
		case RULE:
			String id = uri.getLastPathSegment();
			mRulesDB.deleteRuleById(db, Integer.parseInt(id));
			break;
		default:
			throw new IllegalArgumentException("Unknown delete URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        if(match==RULES) {
        	return CONTENT_TYPE +"/"+ CONTENT_ITEM_TYPE + "s";
        }
		return CONTENT_TYPE +"/"+ CONTENT_ITEM_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = mRulesDB.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case RULES:
			
			Rule ruleToInsert = new Rule(-1,-1,
					values.getAsString(RulesDatabaseHelper.COL_REGEXPIN),
					values.getAsString(RulesDatabaseHelper.COL_REGEXPOUT),
					values.getAsBoolean(RulesDatabaseHelper.COL_STOP));
			id=mRulesDB.storeRule(db, ruleToInsert);		
			break;
		default:
			throw new IllegalArgumentException("Unknown insert URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public boolean onCreate() {
		mRulesDB = new RulesDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		checkColumns(projection);

		queryBuilder.setTables(RulesDatabaseHelper.TABLE);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case RULES:
			break;
		case RULE:
			queryBuilder.appendWhere(RulesDatabaseHelper.COL_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown query URI: " + uri);
		}

		SQLiteDatabase db = mRulesDB.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);

		// Notify listeners...
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = mRulesDB.getWritableDatabase();
		int rowsUpdated = 0;
		String id;
		List<String> segments;
		switch (uriType) {
		case RULE:
			id = uri.getLastPathSegment();
			//Do not let outsiders to touch COL_PRIO!
			if(values.containsKey(RulesDatabaseHelper.COL_PRIO)) {
				values.remove(RulesDatabaseHelper.COL_PRIO);
			}
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(RulesDatabaseHelper.TABLE, values,
						RulesDatabaseHelper.COL_ID + "=" + id, null);
			} else {
				rowsUpdated = db.update(RulesDatabaseHelper.TABLE, values,
						RulesDatabaseHelper.COL_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		case RULE_UP:
			segments = uri.getPathSegments();
			id = segments.get(segments.size()-2);
			mRulesDB.moveRuleUpById(db, Integer.parseInt(id));
			rowsUpdated=2;
			break;
		case RULE_DOWN:
			segments = uri.getPathSegments();
			id = segments.get(segments.size()-2);
			mRulesDB.moveRuleDownById(db, Integer.parseInt(id));
			rowsUpdated=2;
			break;			
		default:
			throw new IllegalArgumentException("Unknown update URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	// Check if the projection requested is valid
	private void checkColumns(String[] projection) {
		String[] available = { RulesDatabaseHelper.COL_ID,
				RulesDatabaseHelper.COL_PRIO, RulesDatabaseHelper.COL_REGEXPIN,
				RulesDatabaseHelper.COL_REGEXPOUT, RulesDatabaseHelper.COL_STOP };

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
