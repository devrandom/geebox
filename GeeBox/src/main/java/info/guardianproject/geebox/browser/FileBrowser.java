/**
 * 
 */
package info.guardianproject.geebox.browser;

import info.guardianproject.geebox.Geebox;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.ipaulpro.afilechooser.FileChooserActivity;

/**
 * Copyright (C) 2013 Lior Saar. All rights reserved.
 * 
 * @author liorsaar
 * 
 */
public class FileBrowser extends FileChooserActivity {

	public static void startActivityForResult(Activity aActivity, int aRequestCode) {
		Intent intent = new Intent(aActivity, FileBrowser.class);
		intent.setAction( ACTION_FILE_BROWSER );
		aActivity.startActivityForResult(intent, aRequestCode);
	}
	
	@Override
	public File createVirtual( Cursor aCursor ) {
		String dir = aCursor.getString( aCursor.getColumnIndexOrThrow( Geebox.Shares.COLUMN_NAME_DIRECTORY ) );
		String ROOT = "/" ; // FIXME we need a root
		return new File( ROOT + dir ) ;
	}
	
	@Override
	public Loader<Cursor> getVirtualsCursorLoader(String aPath ) {
      // TODO filter by directory
      // TODO extract this to something that is passed in
      return new CursorLoader(this,
              Geebox.Virtuals.CONTENT_URI,
              null, null, null, null);
	}
	
}
