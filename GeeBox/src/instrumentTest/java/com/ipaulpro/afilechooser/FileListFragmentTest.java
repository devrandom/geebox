package com.ipaulpro.afilechooser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.Loader;
import android.test.InstrumentationTestCase;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import junit.framework.Test;

import java.io.File;
import java.util.List;

import info.guardianproject.geebox.Geebox;
import info.guardianproject.geebox.GeeboxProvider;

import static info.guardianproject.geebox.Geebox.AUTHORITY;

/**
 * Created by liorsaar on 10/21/13.
 */
public class FileListFragmentTest extends ProviderTestCase2<GeeboxProvider> {
    private MockContentResolver mResolver;
    private FileListFragment mFragment;

    public FileListFragmentTest() {
        super(GeeboxProvider.class, AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mFragment = new FileListFragment();
        mFragment.setVirtualsFactory(new FileChooserActivity.VirtualsFactory() {
            @Override
            public File createVirtual(Cursor aCursor) {
                return Geebox.virtualToFile(aCursor);
            }

            @Override
            public Loader<Cursor> getVirtualsCursorLoader(String mPath) {
                return null;
            }
        });
    }


    public void testFileList() {
        // share
        long shareId = Geebox.makeShare(mResolver, Uri.parse("a/b/c")) ;
        long peerId = Geebox.makePeer(mResolver, "me@here", "peer@there" );
        // make virtual
        long virtualId = Geebox.makeVirtual(mResolver, shareId, "dir", "name", false, peerId, 0);
        // set
        Cursor cursor = mResolver.query(Geebox.Virtuals.CONTENT_URI, null, null, null, null);
        List<File> list = mFragment.getVirtualList(cursor);
        assertEquals(1, list.size());
        File file = list.get(0);
        assertEquals("/a/b/c/dir/name", file.getPath());
    }
}
