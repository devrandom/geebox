package info.guardianproject.geebox;

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

        public static final String COLUMN_NAME_PEER = "peer";
        public static final String COLUMN_NAME_SHARE = "share";
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
        public static final String COLUMN_NAME_SHARE = "share";
        public static final String COLUMN_NAME_DIRECTORY = "directory";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_OPERATION = "operation";
        public static final String TABLE_NAME = "queue";
    }

    public static final class Virtuals implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/virtuals");

        /** share / directory / name is the full path */
        public static final String COLUMN_NAME_SHARE = "share";
        public static final String COLUMN_NAME_DIRECTORY = "directory";
        public static final String COLUMN_NAME_NAME = "name";

        /** The peer where this is hosted */
        public static final String COLUMN_NAME_PEER = "peer";

        public static final String COLUMN_NAME_IS_DIR = "is_dir";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";

        private Virtuals() {}
        public static final String TABLE_NAME = "virtuals";
    }
}
