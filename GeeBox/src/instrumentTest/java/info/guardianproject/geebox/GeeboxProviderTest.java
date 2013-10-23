package info.guardianproject.geebox;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import static info.guardianproject.geebox.Geebox.AUTHORITY;
import static info.guardianproject.geebox.Geebox.Peers;
import static info.guardianproject.geebox.Geebox.Shares;

/**
 * @author devrandom
 */
public class GeeboxProviderTest extends ProviderTestCase2<GeeboxProvider> {
    private SQLiteDatabase mDb;
    private MockContentResolver mResolver;

    public GeeboxProviderTest() {
        super(GeeboxProvider.class, AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mDb = getProvider().getHelperForTest().getWritableDatabase();
    }

    public void testCreate() throws Exception {
        long peerId = Geebox.makePeer(mResolver, "me@here", "a@a");

        Cursor cursor = mResolver.query(Peers.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals("a@a", cursor.getString(cursor.getColumnIndexOrThrow(Peers.COLUMN_NAME_ADDRESS)));
        cursor.close();

        long shareId = Geebox.makeShare(mResolver, Uri.parse("a/b/c"));
        cursor = mResolver.query(Shares.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals("a/b/c", cursor.getString(cursor.getColumnIndexOrThrow(Shares.COLUMN_NAME_DIRECTORY)));
        cursor.close();

        long peerShareId = Geebox.makePeerShare(mResolver, peerId, shareId);
        long shareId1 = Geebox.makeShare(mResolver, Uri.parse("a/b/d"));
        long peerShareId1 = Geebox.makePeerShare(mResolver, peerId, shareId1);
        String peerShareReference = Geebox.getPeerShareReference(mResolver, peerShareId);
        String peerShareReference1 = Geebox.getPeerShareReference(mResolver, peerShareId1);
        assertTrue(peerShareReference != peerShareReference1);
    }

    public void testMutatePeer() throws Exception {
        Cursor cursor = mResolver.query(Peers.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        ContentValues values = new ContentValues();

        long id = Geebox.makePeer(mResolver, "me@here", "a@a");

        values.put(Peers.COLUMN_NAME_QUEUE_REFERENCE, "abc");
        mResolver.update(ContentUris.withAppendedId(Peers.CONTENT_URI, id), values, null, null);
        cursor = mResolver.query(Peers.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        assertEquals("abc", cursor.getString(cursor.getColumnIndexOrThrow(Peers.COLUMN_NAME_QUEUE_REFERENCE)));
        cursor.close();

        mResolver.delete(ContentUris.withAppendedId(Peers.CONTENT_URI, id), null, null);
        cursor = mResolver.query(Peers.CONTENT_URI, null, null, null, null);
        assertFalse(cursor.moveToFirst());
        cursor.close();
    }
}
