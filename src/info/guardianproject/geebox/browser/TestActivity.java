package info.guardianproject.geebox.browser;

import info.guardianproject.geebox.R;
import android.app.Activity;
import android.app.AlertDialog;
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
				onClickDelete(aActivity);
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
	
	protected void onClickRename(Activity aActivity, final Uri aSource ) {
		final FrameLayout frameView = new FrameLayout(aActivity);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(aActivity)
		.setTitle("Rename")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText targetEditText = (EditText) frameView.findViewById( R.id.test_rename_target);
				String target = targetEditText.getText().toString();
				doRename( aSource, target ) ;
			}
		})
		.setNegativeButton("Cancel", null );
		builder.setView(frameView);

		final AlertDialog alertDialog = builder.create();
		alertDialog.getLayoutInflater().inflate(R.layout.test_rename, frameView);
		TextView sourceTextView = (TextView) frameView.findViewById( R.id.test_rename_source) ;
		sourceTextView.setText( aSource.getPath() );
		alertDialog.show();
	}
	
	private void doRename( Uri aSource, String aTarget ) {
		try {
			FileSystem.rename( aSource, aTarget ) ;
		} catch( Throwable t ) {
			Toast.makeText(this, "Rename failed: " + t.getMessage(), Toast.LENGTH_LONG);
		}
	}

	protected void onClickDelete(Activity aActivity) {
	}
	protected void onClickMove(Activity aActivity) {
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
