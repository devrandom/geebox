package info.guardianproject.geebox;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author devrandom
 */
public final class Geebox {
    public static final String AUTHORITY = "info.guardianproject.geebox";
    public static final String SCHEME = "content://";

    public static final class Peers implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/peers");

        public static final String COLUMN_NAME_ADDRESS = "address";
        /** A reference into the local queue to where the peer updated their state */
        public static final String COLUMN_NAME_QUEUE_REFERENCE = "queue_reference";
        /** A reference into the remote queue to where we have updated our state */
        public static final String COLUMN_NAME_PEER_QUEUE_REFERENCE = "peer_queue_reference";

        private Peers() {}
        public static final String TABLE_NAME = "peers";
    }

    public static final class Shares implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/shares");

        public static final String COLUMN_NAME_DIRECTORY = "directory";
        public static final String COLUMN_NAME_REFERENCE = "reference";
        public static final String COLUMN_NAME_STATUS = "status";

        private Shares() {}
        public static final String TABLE_NAME = "shares";
    }

    public static final class PeerShares implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/peer_shares");

        public static final String COLUMN_NAME_PEER = "peer_id";
        public static final String COLUMN_NAME_SHARE = "share_id";
        /** share reference on peer */
        public static final String COLUMN_NAME_REFERENCE = "reference";

        private PeerShares() {}
        public static final String TABLE_NAME = "peer_shares";
    }

    public static final class Queue implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/queue");

        public static final String COLUMN_NAME_SEQUENCE = "sequence";
        /** Opaque reference for remote */
        public static final String COLUMN_NAME_REFERENCE = "reference";
        /** share / directory / name is the full path */
        public static final String COLUMN_NAME_SHARE = "share_id";
        public static final String COLUMN_NAME_DIRECTORY = "directory";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_OPERATION = "operation";
        public static final String TABLE_NAME = "queue";
    }

    public static final class Virtuals implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/virtuals");

        /** share / directory / name is the full path */
        public static final String COLUMN_NAME_SHARE = "share_id";
        public static final String COLUMN_NAME_DIRECTORY = "directory";
        public static final String COLUMN_NAME_NAME = "name";

        /** The peer where this is hosted */
        public static final String COLUMN_NAME_PEER = "peer_id";

        public static final String COLUMN_NAME_IS_DIR = "is_dir";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";

        private Virtuals() {}
        public static final String TABLE_NAME = "virtuals";
    }

    public static long makeShare(ContentResolver aContentResolver, Uri aUri) {
        String uri = aUri.toString();
        long shareId = getShare(aContentResolver, aUri);
        if (shareId >= 0) return shareId;
        ContentValues values = new ContentValues();
        values.put(Shares.COLUMN_NAME_DIRECTORY, uri);
        Uri shareUri = aContentResolver.insert(Shares.CONTENT_URI, values);
        return Long.parseLong(shareUri.getLastPathSegment());
    }

    public static long getShare(ContentResolver aContentResolver, Uri aUri) {
        Cursor cursor =
                aContentResolver.query(Shares.CONTENT_URI,
                        null,
                        Shares.COLUMN_NAME_DIRECTORY + "= ?",
                        new String[] {aUri.toString()},
                        null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(Shares._ID));
            }
        } finally {
            cursor.close();
        }
        return -1;
    }

    public static long makePeerShare(ContentResolver aContentResolver, long peerId, long shareId) {
        Cursor cursor =
                aContentResolver.query(PeerShares.CONTENT_URI,
                        null,
                        PeerShares.COLUMN_NAME_PEER + "= ? AND " +
                                PeerShares.COLUMN_NAME_SHARE + " = ?"
                        ,
                        new String[] {String.valueOf(peerId), String.valueOf(shareId)},
                        null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(PeerShares._ID));
            }
        } finally {
            cursor.close();
        }
        ContentValues values;
        values = new ContentValues();
        values.put(PeerShares.COLUMN_NAME_PEER, peerId);
        values.put(PeerShares.COLUMN_NAME_SHARE, shareId);
        Uri peerShareUri = aContentResolver.insert(PeerShares.CONTENT_URI, values);
        return Long.parseLong(peerShareUri.getLastPathSegment());
    }

    public static long makePeer(ContentResolver aContentResolver, String contact) {
        Cursor cursor =
                aContentResolver.query(Peers.CONTENT_URI,
                        null,
                        Peers.COLUMN_NAME_ADDRESS + "= ?",
                        new String[] {contact},
                        null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(Peers._ID));
            }
        } finally {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put(Peers.COLUMN_NAME_ADDRESS, contact);
        Uri peerUri = aContentResolver.insert(Peers.CONTENT_URI, values);
        return Long.parseLong(peerUri.getLastPathSegment());
    }
}
