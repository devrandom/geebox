/**
 * 
 */
package info.guardianproject.geebox.browser;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.ipaulpro.afilechooser.FileChooserActivity;

import java.io.File;
import java.util.List;

import info.guardianproject.geebox.Geebox;

/**
 * Copyright (C) 2013 Lior Saar. All rights reserved.
 * 
 * @author liorsaar
 * 
 */
public class FileBrowser extends FileChooserActivity implements FileChooserActivity.VirtualsFactory {
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
	public File createVirtual( Cursor aCursor ) {
        return Geebox.virtualToFile(aCursor);
	}

    @Override
    public List<File> createVirtualList(Cursor aCursor) {
        final boolean FAKE = true ;
        if( FAKE ) aCursor = Geebox.createFakeVirtualCursor(getContentResolver(), "/x/y/", 2, 3) ;

        return Geebox.virtualToFileList(aCursor);
    }

    @Override
	public Loader<Cursor> getVirtualsCursorLoader(String aPath ) {
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

}
