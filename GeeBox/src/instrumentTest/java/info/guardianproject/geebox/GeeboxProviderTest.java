package info.guardianproject.geebox;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

/**
 * Created by android on 10/14/13.
 */
public class GeeboxProviderTest extends ProviderTestCase2<GeeboxProvider> {
    private SQLiteDatabase mDb;
    private MockContentResolver mResolver;

    public GeeboxProviderTest() {
        super(GeeboxProvider.class, Geebox.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mDb = getProvider().getHelperForTest().getWritableDatabase();
    }

    public void testCreate() throws Exception {
        mDb.insertOrThrow(Geebox.Peers.TABLE_NAME, Geebox.Peers.COLUMN_NAME_ADDRESS, new ContentValues());
    }
}
