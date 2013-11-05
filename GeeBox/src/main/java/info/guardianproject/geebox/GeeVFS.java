package info.guardianproject.geebox;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.ipaulpro.afilechooser.FileLoader;
import com.ipaulpro.afilechooser.VFS;
import com.ipaulpro.afilechooser.VFile;
import com.ipaulpro.afilechooser.VirtualsFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author devrandom
 */
public class GeeVFS implements VFS {
    private LoaderManager mLoaderManager;
    private int mStartLoaderId;
    private DataSetObserver mObserver;
    private VirtualsFactory mVirtualsFactory;
    private List<VFile> mVirtuals;
    private List<VFile> mPhysicals;
    private String mPath;
    private FileLoaderCallback mFileLoaderCallback;
    private CursorLoaderCallback mCursorLoaderCallback;
    private Context mContext;

    public LoaderManager getLoaderManager() {
        return mLoaderManager;
    }

    @Override
    public void setObserver(DataSetObserver aObserver) {
        this.mObserver = aObserver;
    }

    @Override
    public void onActivityCreated(Context aContext, LoaderManager loaderManager, int startLoaderId, String aPath) {
        mContext = aContext;
        mLoaderManager = loaderManager;
        mStartLoaderId = startLoaderId;
        mPath = aPath;
        mFileLoaderCallback = new FileLoaderCallback();
        getLoaderManager().initLoader(startLoaderId, null, mFileLoaderCallback);
        mCursorLoaderCallback = new CursorLoaderCallback();
        getLoaderManager().initLoader(startLoaderId + 1, null, mCursorLoaderCallback);

    }

    @Override
    public void setVirtualsFactory(VirtualsFactory virtualsFactory) {
        mVirtualsFactory = virtualsFactory;
    }

    @Override
    public List<VFile> getVFiles() {
        ArrayList<VFile> vFiles = new ArrayList<VFile>();
        if( mPhysicals != null )
            vFiles.addAll(mPhysicals);
        if( mVirtuals != null )
            vFiles.addAll(mVirtuals);
        // TODO more sorts
        Collections.sort(vFiles, new Comparator<File>() {
            @Override
            public int compare(File data1, File data2) {
                return data1.compareTo(data2);
            }
        });
        return vFiles;
    }

    class FileLoaderCallback implements
            LoaderManager.LoaderCallbacks<List<File>> {
        @Override
        public Loader<List<File>> onCreateLoader(int id, Bundle args) {
            return new FileLoader(mContext, mPath, false); //TODO isFolder
        }

        @Override
        public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
            mPhysicals = new ArrayList<VFile>(data.size());
            for (File f : data) {
                // TODO directory flag
                mPhysicals.add(new VFile(f.getPath()));
            }
            mObserver.onChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<File>> loader) {
            mObserver.onInvalidated();
        }
    }

    class CursorLoaderCallback implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return mVirtualsFactory.getVirtualsCursorLoader( mPath ) ;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mVirtuals = mVirtualsFactory.createVirtualList(data) ;
            mObserver.onChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mObserver.onInvalidated();
        }
    }

}
