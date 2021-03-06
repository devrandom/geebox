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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author devrandom
 */
public class GeeVFS implements VFS {
    private final String mRootPath;
    private LoaderManager mLoaderManager;
    private int mStartLoaderId;
    private DataSetObserver mObserver;

    private List<File> mPhysicals;

    private List<VFile> mVirtuals;
    private List<VFile> mLocals;

    private String mPath;
    private FileLoaderCallback mFileLoaderCallback;
    private VirtualsLoaderCallback mVirtualsLoaderCallback;
    private Context mContext;
    private Cursor mSharesCursor;
    private SharesLoaderCallback mSharesLoaderCallback;

    public GeeVFS(String aRootPath) {
        mRootPath = aRootPath;
    }

    public LoaderManager getLoaderManager() {
        return mLoaderManager;
    }

    @Override
    public void setObserver(DataSetObserver aObserver) {
        this.mObserver = aObserver;
    }

    @Override
    public void onActivityCreated(Context aContext, LoaderManager loaderManager, int startLoaderId, String aPath) {
        init(aPath);
        mContext = aContext;
        mLoaderManager = loaderManager;
        mStartLoaderId = startLoaderId;
        getLoaderManager().initLoader(startLoaderId, null, mFileLoaderCallback);
        getLoaderManager().initLoader(startLoaderId + 1, null, mVirtualsLoaderCallback);
        getLoaderManager().initLoader(startLoaderId + 2, null, mSharesLoaderCallback);
    }

    // For testing
    void init(String aPath) {
        mPath = aPath;
        mFileLoaderCallback = new FileLoaderCallback();
        mVirtualsLoaderCallback = new VirtualsLoaderCallback();
        mSharesLoaderCallback = new SharesLoaderCallback();
    }

    @Override
    public List<VFile> getVFiles() {
        ArrayList<VFile> vFiles = new ArrayList<VFile>();
        if( mLocals != null )
            vFiles.addAll(mLocals);
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
            String fullPath = mRootPath + mPath;
            return new FileLoader(mContext, fullPath, false); //TODO isFolder
        }

        @Override
        public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
            mPhysicals = data;
            onDataChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<File>> loader) {
            mObserver.onInvalidated();
        }
    }

    private void onDataChanged() {
        if (mPhysicals != null && mSharesCursor != null) {
            Map<String, Long> sharePaths = new HashMap<String, Long>();
            mSharesCursor.moveToPosition(-1);
            while (mSharesCursor.moveToNext()) {
                String directory =
                        mSharesCursor.getString(mSharesCursor.getColumnIndexOrThrow(Geebox.Shares.COLUMN_NAME_DIRECTORY));
                long id = mSharesCursor.getLong(mSharesCursor.getColumnIndexOrThrow(Geebox.Shares._ID));
                sharePaths.put(directory, id);
            }
            mLocals = new ArrayList<VFile>(mPhysicals.size());
            for (File f : mPhysicals) {
                String virtualPath = f.getPath().substring(mRootPath.length());
                // TODO directory flag
                // TODO share flag
                if (sharePaths.containsKey(virtualPath)) {
                    mLocals.add(new Geebox.GeeVFile(virtualPath, f.isDirectory(), sharePaths.get(virtualPath)));
                } else {
                    mLocals.add(new Geebox.GeeVFile(virtualPath, f.isDirectory()));
                }
            }
            mObserver.onChanged();
        }
    }

    class SharesLoaderCallback implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return Geebox.getSharesCursorLoader(mContext, mPath);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mSharesCursor = data;
            onDataChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mSharesCursor = null;
            mObserver.onInvalidated();
        }
    }

    class VirtualsLoaderCallback implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            long shareId=0; //FIXME 911
            return Geebox.getShareVirtualsCursorLoader(mContext, shareId, mPath );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//            final boolean FAKE = true ;
//            if( FAKE ) data = Geebox.createFakeVirtualCursor(mContext.getContentResolver(), mPath ) ;

            mVirtuals =  Geebox.virtualToFileList(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mObserver.onInvalidated();
        }
    }

//    @Override
//    public VFile createVirtual( Cursor aCursor ) {
//        return Geebox.virtualToFile(aCursor);
//    }
//
//    @Override
//    public List<VFile> createVirtualList(Cursor aCursor) {
//        final boolean FAKE = true ;
//        if( FAKE ) aCursor = Geebox.createFakeVirtualCursor(getContentResolver(), mVirtualsPath ) ;
//
//        return Geebox.virtualToFileList(aCursor);
//    }


    // For testing
    FileLoaderCallback getFileLoaderCallback() {
        return mFileLoaderCallback;
    }

    // For testing
    SharesLoaderCallback getSharesLoaderCallback() {
        return mSharesLoaderCallback;
    }

    // For testing
    VirtualsLoaderCallback getVirtualsLoaderCallback() {
        return mVirtualsLoaderCallback;
    }
}
