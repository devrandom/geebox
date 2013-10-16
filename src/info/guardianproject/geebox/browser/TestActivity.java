package info.guardianproject.geebox.browser;

import java.io.File;

import info.guardianproject.geebox.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {

	private static final int REQUEST_CODE_FOLDER_BROWSER = 6661;
	private static final int REQUEST_CODE_FILE_BROWSER = 6662;
	private static final int REQUEST_CODE_FOLDER_BROWSER_MOVE = 6663;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		setListeners(this);
	}

	private void setListeners(final Activity aActivity) {
		aActivity.findViewById(R.id.test_pick_file).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickFilePicker(aActivity);
			}
		});
		aActivity.findViewById(R.id.test_pick_folder).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickFolderPicker(aActivity);
			}
		});
		aActivity.findViewById(R.id.test_rename).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickRename(aActivity, mPickedUri );
			}
		});
		aActivity.findViewById(R.id.test_delete).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickDelete(aActivity, mPickedUri);
			}
		});
		aActivity.findViewById(R.id.test_move).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickMove(aActivity);
			}
		});
	}

	protected void onClickFilePicker(Activity aActivity) {
		FileBrowser.startActivityForResult(aActivity, REQUEST_CODE_FILE_BROWSER);
	}

	protected void onClickFolderPicker(Activity aActivity) {
		FolderBrowser.startActivity(aActivity, REQUEST_CODE_FOLDER_BROWSER);
	}
	
	protected void onClickRename(final Activity aActivity, final Uri aSourceUri ) {
		FileDialog.Rename(aActivity, aSourceUri, new FileDialog.DialogResult() {
			@Override
			public void callback(Object... args) {
				try {
					FileSystem.rename( aSourceUri, (String)args[0] ) ;
				} catch( Throwable t ) {
					Toast.makeText(aActivity, "Rename failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	protected void onClickDelete(final Activity aActivity, final Uri aSourceUri) {
		FileDialog.Delete(aActivity, aSourceUri, new FileDialog.DialogResult() {
			@Override
			public void callback(Object... args) {
				try {
					FileSystem.delete( aSourceUri ) ;
				} catch( Throwable t ) {
					Toast.makeText(aActivity, "Delete failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	protected void onClickMove(Activity aActivity) {
		FolderBrowser.startActivity(aActivity, REQUEST_CODE_FOLDER_BROWSER_MOVE);
	}
	
	
	protected void onActivityResultMove(final Activity aActivity, final Uri aSourceUri, final Uri aTargetUri) {
		FileDialog.Move(aActivity, aSourceUri, aTargetUri, new FileDialog.DialogResult() {
			@Override
			public void callback( Object ... args ) {
				try {
					FileSystem.move( aSourceUri, aTargetUri ) ;
				} catch( Throwable t ) {
					Toast.makeText(aActivity, "Rename failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_FOLDER_BROWSER:
			if( ! isOK( resultCode, data )) {
				return ;
			}
			doFolderPicked( data.getData() );
			break ;
			
		case REQUEST_CODE_FILE_BROWSER:
			if( ! isOK( resultCode, data )) {
				return ;
			}
			doFilePicked( data.getData() );
			break ;
			
		case REQUEST_CODE_FOLDER_BROWSER_MOVE:
			if( ! isOK( resultCode, data )) {
				return ;
			}
			onActivityResultMove( this, mPickedUri, data.getData() );
			break ;
			
			default:
				throw new RuntimeException("Unknown requestCode: " + requestCode );
		}
	}

	protected boolean isOK( int resultCode, Intent data ) {
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

	Uri mPickedUri ;
	private void doFolderPicked(Uri aUri) {
		mPickedUri = aUri ;
		setPicked( aUri ) ;
	}
	
	private void doFilePicked(Uri aUri) {
		mPickedUri = aUri ;
		setPicked( aUri ) ;
	}
	
	private void setPicked(Uri aUri) {
		if( aUri == null ) {
			((TextView)findViewById(R.id.test_source_name)).setText( "" );
			((Button)findViewById(R.id.test_rename)).setEnabled(false);
			((Button)findViewById(R.id.test_delete)).setEnabled(false);
			((Button)findViewById(R.id.test_move)).setEnabled(false);
		} else {
			((TextView)findViewById(R.id.test_source_name)).setText( aUri.toString() );
			((Button)findViewById(R.id.test_rename)).setEnabled(true);
			((Button)findViewById(R.id.test_delete)).setEnabled(true);
			((Button)findViewById(R.id.test_move)).setEnabled(true);
		}
	}
}
