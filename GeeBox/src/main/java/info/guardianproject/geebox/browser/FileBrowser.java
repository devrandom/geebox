/**
 * 
 */
package info.guardianproject.geebox.browser;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.VFS;
import com.ipaulpro.afilechooser.VFile;
import com.ipaulpro.afilechooser.VirtualsFactory;

import java.util.List;

import info.guardianproject.geebox.GeeVFS;
import info.guardianproject.geebox.Geebox;

/**
 * Copyright (C) 2013 Lior Saar. All rights reserved.
 * 
 * @author liorsaar
 * 
 */
public class FileBrowser extends FileChooserActivity implements VirtualsFactory {
    protected static final int REQUEST_CODE_FOLDER_BROWSER = 6661;
    protected static final int REQUEST_CODE_FILE_BROWSER = 6662;
    protected static final int REQUEST_CODE_FOLDER_BROWSER_MOVE = 6663;

    // FIXME make VirtualsFactory an inner class
	public static void startActivityForResult(Activity aActivity, int aRequestCode, String aBaseApth ) {
		Intent intent = new Intent(aActivity, FileBrowser.class);
		intent.setAction( ACTION_FILE_BROWSER );
        intent.putExtra( EXTRA_BASE_PATH, aBaseApth);
		aActivity.startActivityForResult(intent, aRequestCode);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mBasePath = Geebox.Config.getBasePath(); // FIXME how should this be done ?
        mAppRootPath = Geebox.Config.getBasePath(); // FIXME how should this be done ?
        super.onCreate(savedInstanceState);
    }

    @Override
	public VFile createVirtual( Cursor aCursor ) {
        return Geebox.virtualToFile(aCursor);
	}

    @Override
    public List<VFile> createVirtualList(Cursor aCursor) {
        final boolean FAKE = true ;
        if( FAKE ) aCursor = Geebox.createFakeVirtualCursor(getContentResolver(), mVirtualsPath ) ;

        return Geebox.virtualToFileList(aCursor);
    }

    private String mVirtualsPath ;

    @Override
	public Loader<Cursor> getVirtualsCursorLoader(String aPath ) {
        mVirtualsPath = aPath ; // debug only - used in createFakeVirtualCursor
      // TODO filter by directory
      // TODO extract this to something that is passed in
      return new CursorLoader(this,
              Geebox.Virtuals.CONTENT_URI,
              null, null, null, null);
	}

    @Override
    public VirtualsFactory getVirtualsFactory() {
        return this;
    }

    @Override
    public VFS getVFS() {
        return new GeeVFS();
    }
}
