package com.ipaulpro.afilechooser;

import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

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
    }


    public void testVirtualList() {
        // share
        long shareId = Geebox.makeShare(mResolver, Uri.parse("/a/b/c")) ;
        long peerId = Geebox.makePeer(mResolver, "me@here", "peer@there");
        // make virtual
        long virtualId = Geebox.makeVirtual(mResolver, shareId, "dir", "name", false, peerId, 0);
        // set
        Cursor cursor = mResolver.query(Geebox.Virtuals.CONTENT_URI, null, null, null, null);
        List<VFile> list = Geebox.virtualToFileList(cursor);
        assertEquals(1, list.size());
        File file = list.get(0);
        assertEquals("/a/b/c/dir/name", file.getPath());
    }

    public void testFakeVirtualList() {
        int dirCount = 2 ;
        int fileCount = 3 ;
        Cursor cursor = Geebox.createFakeVirtualCursor(mResolver, "a/b/c", dirCount, fileCount);
        List<VFile> list = Geebox.virtualToFileList(cursor);
        assertEquals(dirCount*fileCount, list.size());
        File file = list.get(0);
        assertEquals("/a/b/c/dir0/filename0", file.getPath());
    }
}
