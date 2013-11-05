package info.guardianproject.geebox;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
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
    private MyContextWrapper mWrappedContext;

    public GeeboxProviderTest() {
        super(GeeboxProvider.class, AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mDb = getProvider().getHelperForTest().getWritableDatabase();
        mWrappedContext = new MyContextWrapper(getMockContext());
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

        String aReference = "ref1";
        long peerShareId = Geebox.makePeerShare(mResolver, peerId, shareId, aReference);
        long shareId1 = Geebox.makeShare(mResolver, Uri.parse("a/b/d"));
        long peerShareId1 = Geebox.makePeerShare(mResolver, peerId, shareId1, aReference);
        String peerShareReference = Geebox.getPeerShareReference(mResolver, peerShareId);
        String peerShareReference1 = Geebox.getPeerShareReference(mResolver, peerShareId1);
        assertTrue(peerShareReference != peerShareReference1);

        long peerShareId_same = Geebox.makePeerShare(mResolver, peerId, shareId, aReference);
        assertEquals(peerShareId, peerShareId_same);
        Geebox.setPeerShareReference(mResolver, peerShareId, "ref2");
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

    static class MyContextWrapper extends ContextWrapper {
        MyContextWrapper(Context context) {
            super(context);
        }

        @Override
        public Context getApplicationContext() {
            return getBaseContext();
        }
    }

    public void testQuerySharePath() throws Exception {
        long peerId = Geebox.makePeer(mResolver, "me@here", "a@a");
        long share_ab = Geebox.makeShare(mResolver, Uri.parse("a/b"));
        long share_ac = Geebox.makeShare(mResolver, Uri.parse("a/c"));
        long virtual_m = Geebox.makeVirtual(mResolver, share_ab, "d1", "m", false, peerId, 0);
        long virtual_n = Geebox.makeVirtual(mResolver, share_ab, "d1", "n", false, peerId, 0);
        long virtual_o = Geebox.makeVirtual(mResolver, share_ab, "d2", "o", false, peerId, 0);
        long virtual_x = Geebox.makeVirtual(mResolver, share_ac, "d1", "q", false, peerId, 0);

        CursorLoader loader = Geebox.getShareVirtualsCursorLoader(mWrappedContext, share_ab, "d1");
        Cursor cursor = loader.loadInBackground();
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(virtual_m, cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
        cursor.moveToNext();
        assertEquals(virtual_n, cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
        cursor.close();
    }

}
