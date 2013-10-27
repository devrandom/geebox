/**
 * 
 */
package info.guardianproject.geebox.browser;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import info.guardianproject.geebox.R;

/**
 * Copyright (C) 2013 Lior Saar. All rights reserved.
 * 
 * @author liorsaar
 * 
 */
public class MainBrowser extends FileBrowser {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_asset:
                FileBrowser.startActivityForResult(this, REQUEST_CODE_FILE_BROWSER, Environment.getExternalStorageDirectory().getAbsolutePath() );
                break;
            case R.id.action_new_folder:
                Toast.makeText(this, "New Folder", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share_folder:
                Toast.makeText(this, "Share Folder", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FileBrowser.REQUEST_CODE_FOLDER_BROWSER:
                break ;

            case FileBrowser.REQUEST_CODE_FILE_BROWSER:
                if( ! isOK( resultCode, data )) {
                    return ;
                }
                doFilePicked(data.getData());
                break ;

            default:
                throw new RuntimeException("Unknown requestCode: " + requestCode );
        }
    }

    protected boolean isOK( int resultCode, Intent data ) {
        // TODO error handling
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "resultCode != OK : " + resultCode, Toast.LENGTH_LONG ).show() ;
            return false ;
        }
        if (data == null) {
            Toast.makeText(this, "data NULL", Toast.LENGTH_LONG ).show() ;
            return false ;
        }
        return true ;
    }

    private void doFilePicked(Uri data) {
        Toast.makeText(this, "doFilePicked: " + data , Toast.LENGTH_LONG ).show() ;
    }



}
