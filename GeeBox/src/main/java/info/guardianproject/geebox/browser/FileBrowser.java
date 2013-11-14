/**
 * 
 */
package info.guardianproject.geebox.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.VFS;

import java.io.File;

import info.guardianproject.geebox.GeeVFS;
import info.guardianproject.geebox.Geebox;

/**
 * Copyright (C) 2013 Lior Saar. All rights reserved.
 * 
 * @author liorsaar
 * 
 */
public class FileBrowser extends FileChooserActivity {
    protected static final int REQUEST_CODE_FOLDER_BROWSER = 6661;
    protected static final int REQUEST_CODE_FILE_BROWSER = 6662;
    protected static final int REQUEST_CODE_FOLDER_BROWSER_MOVE = 6663;
    private String mStoreRootPath;

	public static void startActivityForResult(Activity aActivity, int aRequestCode, String aBasePath ) {
		Intent intent = new Intent(aActivity, FileBrowser.class);
		intent.setAction( ACTION_FILE_BROWSER );
        intent.putExtra( EXTRA_BASE_PATH, aBasePath);
		aActivity.startActivityForResult(intent, aRequestCode);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if( getIntent().getStringExtra(EXTRA_BASE_PATH) != null ) {
            mBasePath = getIntent().getStringExtra(EXTRA_BASE_PATH);
        } else {
            mBasePath = File.separator; // root
        }
        mStoreRootPath = Geebox.Config.getStoreRootPath();
        super.onCreate(savedInstanceState);
    }

    @Override
    public VFS getVFS() {
        return new GeeVFS(mStoreRootPath);
    }
}
