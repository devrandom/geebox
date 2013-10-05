package info.guardianproject.geebox.browser;

import info.guardianproject.geebox.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
	}

	protected void onClickFilePicker(Activity aActivity) {
		FileBrowser.startActivity(aActivity, REQUEST_CODE_FILE_BROWSER);
	}

	protected void onClickFolderPicker(Activity aActivity) {
		FolderBrowser.startActivity(aActivity, REQUEST_CODE_FOLDER_BROWSER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_FOLDER_BROWSER:
			if (resultCode != RESULT_OK) {
				return; // da fail
			}
			if (data == null) {
				return; // da fail
			}
			doFolderPicked( data.getData() );
			break ;
			
		case REQUEST_CODE_FILE_BROWSER:
			if (resultCode != RESULT_OK) {
				return; // da fail
			}
			if (data == null) {
				return; // da fail
			}
			doFilePicked( data.getData() );
			break ;
			
			default:
				throw new RuntimeException("Unknown requestCode: " + requestCode );
		}
	}

	private void doFolderPicked(Uri aUri) {
		((TextView)findViewById(R.id.test_source_name)).setText( aUri.toString() );
	}
	
	private void doFilePicked(Uri aUri) {
		((TextView)findViewById(R.id.test_source_name)).setText( aUri.toString() );
	}

}
