package info.guardianproject.geebox;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.common.collect.Maps;

import java.security.SecureRandom;
import java.util.Map;

import static info.guardianproject.geebox.Geebox.PeerShares;
import static info.guardianproject.geebox.Geebox.Peers;
import static info.guardianproject.geebox.Geebox.Queue;
import static info.guardianproject.geebox.Geebox.Shares;
import static info.guardianproject.geebox.Geebox.Virtuals;

/**
 * @author devrandom
 */
public class GeeboxProvider extends ContentProvider {
    private static final String TAG = "GeeBox.Provider";
    private static final int DATABASE_VERSION = 104;
    private static final String DATABASE_NAME = "geebox.db";

    private DatabaseHelper mHelper;
    private SecureRandom mRandom;

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            // calls the super constructor, requesting the default cursor factory.
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Peers.TABLE_NAME + " ("
                    + Peers._ID + " INTEGER PRIMARY KEY"
                    + "," + Peers.COLUMN_NAME_ACCOUNT + " TEXT NOT NULL"
                    + "," + Peers.COLUMN_NAME_ADDRESS + " TEXT NOT NULL"
                    + "," + Peers.COLUMN_NAME_QUEUE_REFERENCE + " TEXT"
                    + "," + Peers.COLUMN_NAME_PEER_QUEUE_REFERENCE + " TEXT"
                    + ");");

            db.execSQL("CREATE TABLE " + Shares.TABLE_NAME + " ("
                    + Shares._ID + " INTEGER PRIMARY KEY"
                    + "," + Shares.COLUMN_NAME_DIRECTORY + " TEXT NOT NULL"
                    + "," + Shares.COLUMN_NAME_REFERENCE + " TEXT NOT NULL"
                    + "," + Shares.COLUMN_NAME_STATUS + " TEXT NOT NULL"
                    + ");");

            db.execSQL("CREATE TABLE " + PeerShares.TABLE_NAME + " ("
                    + PeerShares._ID + " INTEGER PRIMARY KEY"
                    + "," + PeerShares.COLUMN_NAME_PEER + " INTEGER NOT NULL"
                    + "," + PeerShares.COLUMN_NAME_SHARE + " INTEGER NOT NULL"
                    + "," + PeerShares.COLUMN_NAME_REFERENCE + " TEXT NOT NULL"
                    + ", UNIQUE(" + PeerShares.COLUMN_NAME_SHARE + ", "
                    + PeerShares.COLUMN_NAME_PEER + ")"
                    + ");");


            db.execSQL("CREATE TABLE " + Queue.TABLE_NAME + " ("
                    + Queue._ID + " INTEGER PRIMARY KEY"
                    + "," + Queue.COLUMN_NAME_SEQUENCE + " INTEGER NOT NULL"
                    + "," + Queue.COLUMN_NAME_REFERENCE + " TEXT NOT NULL"
                    + "," + Queue.COLUMN_NAME_SHARE + " INTEGER NOT NULL"
                    + "," + Queue.COLUMN_NAME_DIRECTORY + " TEXT NOT NULL"
                    + "," + Queue.COLUMN_NAME_NAME + " TEXT NOT NULL"
                    + "," + Queue.COLUMN_NAME_OPERATION + " TEXT NOT NULL"
                    + ", UNIQUE (" + Queue.COLUMN_NAME_SEQUENCE + ")"
                    + ");");

            db.execSQL("CREATE TABLE " + Virtuals.TABLE_NAME + " ("
                    + Virtuals._ID + " INTEGER PRIMARY KEY"
                    + "," + Virtuals.COLUMN_NAME_SHARE + " INTEGER NOT NULL"
                    + "," + Virtuals.COLUMN_NAME_DIRECTORY + " TEXT NOT NULL"
                    + "," + Virtuals.COLUMN_NAME_NAME + " TEXT NOT NULL"
                    + "," + Virtuals.COLUMN_NAME_PEER + " INTEGER NOT NULL"
                    + "," + Virtuals.COLUMN_NAME_IS_DIR + " BOOLEAN NOT NULL"
                    + "," + Virtuals.COLUMN_NAME_SIZE + " INTEGER NOT NULL"
                    + "," + Virtuals.COLUMN_NAME_CREATED_AT + " TEXT NOT NULL DEFAULT (DATETIME('now'))"
                    + "," + Virtuals.COLUMN_NAME_UPDATED_AT + " TEXT NOT NULL DEFAULT (DATETIME('now'))"
                    + ", UNIQUE("
                    + Virtuals.COLUMN_NAME_SHARE + ","
                    + Virtuals.COLUMN_NAME_DIRECTORY + ","
                    + Virtuals.COLUMN_NAME_NAME
                    + ")"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + Peers.TABLE_NAME);
            onCreate(db);
        }
    }

    private static final UriMatcher sUriMatcher;

    private static final int PEERS = 1;
    private static final int SHARES = 2;
    private static final int PEER_SHARES = 3;
    private static final int QUEUE = 4;
    private static final int VIRTUALS = 5;
    private static final int PEERS_ID = 11;
    private static final int SHARES_ID = 102;
    private static final int PEER_SHARES_PEER_SHARE = 103;
    private static final int QUEUE_ID = 104;
    private static final int VIRTUALS_ID = 105;

    private static Map<Integer, String> sTableMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sTableMap = Maps.newHashMap();

        addMatcher("peers", Peers.TABLE_NAME, PEERS);
        addMatcher("peers/#", Peers.TABLE_NAME, PEERS_ID);
        addMatcher("shares", Shares.TABLE_NAME, SHARES);
        addMatcher("shares/#", Shares.TABLE_NAME, SHARES_ID);
        addMatcher("peer_shares", PeerShares.TABLE_NAME, PEER_SHARES);
        addMatcher("peer_shares/#/#", PeerShares.TABLE_NAME, PEER_SHARES_PEER_SHARE);
        addMatcher("queue", Queue.TABLE_NAME, QUEUE);
        addMatcher("queue/#", Queue.TABLE_NAME, QUEUE_ID);
        addMatcher("virtuals", Virtuals.TABLE_NAME, VIRTUALS);
        addMatcher("virtuals/#", Virtuals.TABLE_NAME, VIRTUALS_ID);
    }

    private static void addMatcher(String name, String tableName, int matchId) {
        sUriMatcher.addURI(Geebox.AUTHORITY, name, matchId);
        sTableMap.put(matchId, tableName);
    }

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        mRandom = new SecureRandom(); // TODO(miron) fix Android SecureRandom breakage
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int match = sUriMatcher.match(uri);
        if (match < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);
        qb.setTables(sTableMap.get(match));
        switch (match) {
            case PEERS:
                break;
            case SHARES:
                break;
            case PEER_SHARES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "share_id, peer_id";
                }
                qb.setTables("peer_shares JOIN peers ON (peer_id = peers._id) JOIN shares ON (share_id = shares._id)");
                break;
            case QUEUE:
                break;
            case VIRTUALS:
                qb.setTables("virtuals JOIN shares ON (share_id = shares._id)");
                projection = new String[] {
                        "virtuals._id as _id",
                        "shares.directory as share_directory",
                        "virtuals.directory as virtual_directory",
                        "name",
                        "is_dir",
                        "size",
                        "created_at",
                        "updated_at"
                };
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = "_id";
        } else {
            orderBy = sortOrder;
        }

        // Opens the database object in "read" mode, since no writes need to be done.
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor c = qb.query(
                db,            // The database to query
                projection,    // The columns to return from the query
                where,     // The columns for the where clause
                whereArgs, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                orderBy        // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        if (match < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);
        String table = sTableMap.get(match);

        switch (match) {
            case PEERS:
                break;
            case SHARES:
                values.put(Shares.COLUMN_NAME_REFERENCE, makeReference());
                if (!values.containsKey(Shares.COLUMN_NAME_STATUS))
                    values.put(Shares.COLUMN_NAME_STATUS, "new");
                break;
            case PEER_SHARES:
                values.put(Shares.COLUMN_NAME_REFERENCE, makeReference());
                break;
            case QUEUE:
                break;
            case VIRTUALS:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI for insert " + uri);
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long rowId = db.insert(table, null, values);

        if (rowId < 0)
            throw new SQLException("Failed to insert into " + table);
        Uri itemUri = ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    private String makeReference() {
        byte[] bytes = new byte[16];
        mRandom.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    public int delete(Uri uri, String where, String[] args) {
        int match = sUriMatcher.match(uri);
        if (match < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);
        if (!TextUtils.isEmpty(where))
            throw new IllegalArgumentException("No where is supported for updates");
        String table = sTableMap.get(match);
        long id = Long.parseLong(uri.getLastPathSegment());
        where = "_id = " + id;

        switch (match) {
            case PEERS_ID:
                break;
            case SHARES_ID:
                break;
            case VIRTUALS_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI for update " + uri);
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count = db.delete(table, where, args);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] args) {
        int match = sUriMatcher.match(uri);
        if (match < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);
        if (!TextUtils.isEmpty(where))
            throw new IllegalArgumentException("No where is supported for updates");
        String table = sTableMap.get(match);
        long id = Long.parseLong(uri.getLastPathSegment());
        where = "_id = " + id;

        switch (match) {
            case PEERS_ID:
                break;
            case SHARES_ID:
                break;
            case VIRTUALS_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI for update " + uri);
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count = db.update(table, values, where, args);

        return count;
    }

    DatabaseHelper getHelperForTest() {
        return mHelper;
    }
}
