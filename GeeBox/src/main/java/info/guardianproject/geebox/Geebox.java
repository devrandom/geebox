package info.guardianproject.geebox;

import android.provider.BaseColumns;

/**
 * Created by android on 10/14/13.
 */
public final class Geebox {
    public static final String AUTHORITY = "info.guardianproject.geebox";

    public static final class Peers implements BaseColumns{
        public static final String COLUMN_NAME_ADDRESS = "address";

        private Peers() {}
        public static final String TABLE_NAME = "peers";
    }
}
