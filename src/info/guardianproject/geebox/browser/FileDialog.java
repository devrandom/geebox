/**
 * 
 */
package info.guardianproject.geebox.browser;

import info.guardianproject.geebox.R;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class FileDialog {
	
	public interface DialogResult {
		void callback( Object... args ) ;
	}
	
	public static void Rename( Context aContext, final Uri aSourceUri, final DialogResult aDialogResult ) {
		final FrameLayout frameView = new FrameLayout(aContext);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(aContext)
		.setTitle("Rename")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText targetEditText = (EditText) frameView.findViewById( R.id.test_rename_target);
				String target = targetEditText.getText().toString();
				aDialogResult.callback( target ) ;
			}
		})
		.setNegativeButton("Cancel", null );
		builder.setView(frameView);

		final AlertDialog alertDialog = builder.create();
		alertDialog.getLayoutInflater().inflate(R.layout.test_rename, frameView);
		TextView sourceTextView = (TextView) frameView.findViewById( R.id.test_rename_source) ;
		String sourceName = aSourceUri.getPath().substring(aSourceUri.getPath().lastIndexOf(File.separator)+1) ;
		sourceTextView.setText( sourceName );
		EditText targetEditText = (EditText) frameView.findViewById( R.id.test_rename_target);
		targetEditText.setText( sourceName );

		alertDialog.show();
	}
}
