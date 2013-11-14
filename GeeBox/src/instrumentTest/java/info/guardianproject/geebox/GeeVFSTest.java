package info.guardianproject.geebox;

import android.database.DataSetObserver;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.test.ProviderTestCase2;

import com.google.common.collect.Lists;
import com.ipaulpro.afilechooser.VFile;

import java.io.File;
import java.util.List;

import static info.guardianproject.geebox.Geebox.AUTHORITY;

/**
 * @author devrandom
 */
public class GeeVFSTest extends ProviderTestCase2<GeeboxProvider> {
    private GeeVFS mVFS;
    private MyContextWrapper mWrappedContext;

    public GeeVFSTest() {
        super(GeeboxProvider.class, AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWrappedContext = new MyContextWrapper(getMockContext());
        mVFS = new GeeVFS("/here/root");
        mVFS.init("/d");
        mVFS.setObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
            }

            @Override
            public void onInvalidated() {
            }
        });
    }

    public void testEmpty() {
        assertTrue(mVFS.getVFiles().isEmpty());
    }

    public void testPhysicals() {
        List<File> physicals = Lists.newArrayList();
        physicals.add(new File("/here/root/d/1"));
        physicals.add(new File("/here/root/d/2"));
        mVFS.getFileLoaderCallback().onLoadFinished(null, physicals);

        Geebox.makeShare(mWrappedContext.getContentResolver(), Uri.parse("/d/2"));
        //FIXME make sure shares have absolute dirs everywhere
        CursorLoader loader = Geebox.getSharesCursorLoader(mWrappedContext, "/d");

        mVFS.getSharesLoaderCallback().onLoadFinished(null, loader.loadInBackground());

        List<VFile> files = mVFS.getVFiles();
        assertEquals(2, files.size());
        VFile file0 = files.get(0);
        assertEquals("/d/1", file0.getPath());
        assertFalse(((Geebox.GeeVFile) file0).isShare());
        VFile file1 = files.get(1);
        assertEquals("/d/2", file1.getPath());
        assertTrue(((Geebox.GeeVFile) file1).isShare());
    }
}
