package info.guardianproject.geebox;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author devrandom
 */
public final class Geebox {
    public static final String AUTHORITY = "info.guardianproject.geebox";
    public static final String SCHEME = "content://";

    public static final class Peers implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/peers");

        public static final String COLUMN_NAME_ACCOUNT = "account";
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
        /** */
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
        public static final String COLUMN_NAME_PEER_SHARE_REFERENCE = "peer_share_reference";
        /** share directory */
        public static final String COLUMN_NAME_SHARES_DIRECTORY = "shares.directory";
        /** new, active */
        public static final String COLUMN_NAME_STATUS = "status";

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
        public static final String COLUMN_NAME_VIRTUALS_DIRECTORY = "virtuals.directory";
        public static final String COLUMN_NAME_NAME = "name";

        /** The peer where this is hosted */
        public static final String COLUMN_NAME_PEER = "peer_id";

        public static final String COLUMN_NAME_IS_DIR = "is_dir";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";

        /** column name in join with shares */
        public static final String COLUMN_NAME_SHARE_DIRECTORY = "share_directory";
        public static final String COLUMN_NAME_VIRTUAL_DIRECTORY = "virtual_directory";

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

    public static long makeVirtual(ContentResolver aContentResolver,
                                   long aShareId, String aDir, String aName,
                                   boolean aIsDir, long aPeerId, long aSize) {
        long virtualId = getVirtual( aContentResolver, aShareId, aDir, aName );
        if (virtualId >= 0) return virtualId;
        ContentValues values = new ContentValues();
        values.put(Virtuals.COLUMN_NAME_SHARE, aShareId);
        values.put(Virtuals.COLUMN_NAME_DIRECTORY, aDir);
        values.put(Virtuals.COLUMN_NAME_NAME, aName);
        values.put(Virtuals.COLUMN_NAME_IS_DIR, aIsDir);
        values.put(Virtuals.COLUMN_NAME_PEER, aPeerId);
        values.put(Virtuals.COLUMN_NAME_SIZE, aSize);
        Uri virtualUri = aContentResolver.insert(Virtuals.CONTENT_URI, values);
        return Long.parseLong(virtualUri.getLastPathSegment());
    }

    private static long getVirtual(ContentResolver aContentResolver, long aShareId, String aDir, String aName) {
        Cursor cursor =
                aContentResolver.query(Virtuals.CONTENT_URI,
                        null,
                        Virtuals.COLUMN_NAME_SHARE + "= ? AND " +
                                Virtuals.COLUMN_NAME_VIRTUALS_DIRECTORY + "= ? AND " +
                                Virtuals.COLUMN_NAME_NAME + "= ? ",
                        new String[]{String.valueOf(aShareId), aDir, aName},
                        null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(Virtuals._ID));
            }
        } finally {
            cursor.close();
        }
        return -1;
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

    public static String getPeerShareReference(ContentResolver aResolver, long peerShareId) {
        Cursor cursor =
                aResolver.query(ContentUris.withAppendedId(PeerShares.CONTENT_URI, peerShareId),
                        null,
                        null,
                        null,
                        null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(PeerShares.COLUMN_NAME_PEER_SHARE_REFERENCE));
            }
        } finally {
            cursor.close();
        }
        return null;
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

    public static long makePeer(ContentResolver aContentResolver, String aAccount, String aContact) {
        Cursor cursor =
                aContentResolver.query(Peers.CONTENT_URI,
                        null,
                        Peers.COLUMN_NAME_ACCOUNT + "= ? AND " + Peers.COLUMN_NAME_ADDRESS + "= ?",
                        new String[] {aAccount, aContact},
                        null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(Peers._ID));
            }
        } finally {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put(Peers.COLUMN_NAME_ACCOUNT, aAccount);
        values.put(Peers.COLUMN_NAME_ADDRESS, aContact);
        Uri peerUri = aContentResolver.insert(Peers.CONTENT_URI, values);
        return Long.parseLong(peerUri.getLastPathSegment());
    }

    public static class VirtualFile extends File {
        boolean mDirectory;

        public VirtualFile(String path, boolean isDirectory) {
            super(path);
            mDirectory = isDirectory;
        }

        @Override
        public boolean isDirectory() {
            return mDirectory;
        }
    }

    public static File virtualToFile(Cursor aCursor) {
        String share = aCursor.getString(aCursor.getColumnIndexOrThrow(Virtuals.COLUMN_NAME_SHARE_DIRECTORY));
        String dir = aCursor.getString( aCursor.getColumnIndexOrThrow( Virtuals.COLUMN_NAME_VIRTUAL_DIRECTORY ) );
        String name = aCursor.getString( aCursor.getColumnIndexOrThrow( Virtuals.COLUMN_NAME_NAME ) );
        boolean isDirectory =
                1 == aCursor.getInt( aCursor.getColumnIndexOrThrow( Virtuals.COLUMN_NAME_IS_DIR ) );

        String ROOT = "/" ; // FIXME we need a root
        return new VirtualFile( ROOT + share + "/" + dir + "/" + name, isDirectory ) ;
    }

    public static List<File> virtualToFileList(Cursor aCursor) {
        List<File> list = new ArrayList<File>();
        aCursor.moveToPosition(-1);
        while( aCursor.moveToNext() ) {
            File file = virtualToFile(aCursor) ;
            list.add(file);
        }
        return list ;
    }

    public static Cursor createFakeVirtualCursor(ContentResolver mResolver, String aShare, int aDirCount, int aFileCount) {
        // share
        long shareId = makeShare(mResolver, Uri.parse( aShare )) ;
        long peerId = makePeer(mResolver, "me@here", "peer@there" );
        // make virtuals
        for( int iDir = 0 ; iDir < aDirCount ; iDir++) {
            for( int iFile = 0 ; iFile < aFileCount ; iFile++ ) {
                String dirName = "dir" + iDir ;
                String fileName = "filename" + iFile ;
                long virtualId = makeVirtual(mResolver, shareId, dirName, fileName, false, peerId, 0);
            }
        }
        // set
        Cursor cursor = mResolver.query(Geebox.Virtuals.CONTENT_URI, null, null, null, null);
        return cursor;
    }

    public static Cursor createFakeVirtualCursor(ContentResolver mResolver, String aShare ) {
        // share
        long shareId = makeShare(mResolver, Uri.parse( aShare )) ;
        long peerId = makePeer(mResolver, "me@here", "peer@there" );
        // make virtuals
        for( int iFile = 2 ; iFile < 4 ; iFile++ ) {
            String fileName = "file" + iFile ;
            long virtualId = makeVirtual(mResolver, shareId, aShare, fileName, false, peerId, 0);
        }
        // set
        Cursor cursor = mResolver.query(Geebox.Virtuals.CONTENT_URI, null, null, null, null);
        return cursor;
    }

    public static final class Config {
        public static String getBasePath() {
            String path = Environment.getExternalStorageDirectory() + File.separator + "GeeBox" + File.separator;
            File file = new File( path ) ;
            if( ! file.exists() ) {
                if( ! file.mkdirs() ) {
                    throw new RuntimeException( "ERROR: creating " + path );
                }
            }
            return path;
        }
    }

}
