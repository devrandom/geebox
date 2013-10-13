/**
 * 
 */
package info.guardianproject.geebox.browser;

import android.app.Activity;
import android.content.Intent;

import com.ipaulpro.afilechooser.FileChooserActivity;

/**
 * Copyright (C) 2013 Lior Saar. All rights reserved.
 * 
 * @author liorsaar
 * 
 */
public class FileBrowser extends FileChooserActivity {

	public static void startActivityForResult(Activity aActivity, int aRequestCode) {
		Intent intent = new Intent(aActivity, FolderBrowser.class);
		intent.setAction( ACTION_FILE_BROWSER );
		aActivity.startActivityForResult(intent, aRequestCode);
	}

}
