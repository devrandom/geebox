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
				dialog.dismiss();
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
	
	public static void Move( Context aContext, final Uri aSourceUri, final Uri aTargetUri, final DialogResult aDialogResult ) {
		final FrameLayout frameView = new FrameLayout(aContext);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(aContext)
		.setTitle("Move")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				aDialogResult.callback() ;
			}
		})
		.setNegativeButton("Cancel", null );
		builder.setView(frameView);

		final AlertDialog alertDialog = builder.create();
		alertDialog.getLayoutInflater().inflate(R.layout.file_dialog_move, frameView);
		TextView sourceTextView = (TextView) frameView.findViewById( R.id.test_move_source) ;
		String sourceName = aSourceUri.getPath();
		sourceTextView.setText( sourceName );
		TextView targetEditText = (TextView) frameView.findViewById( R.id.test_move_target);
		String targetName = aTargetUri.getPath();
		targetEditText.setText( targetName );

		alertDialog.show();
	}
	
	public static void Delete( Context aContext, final Uri aSourceUri, final DialogResult aDialogResult ) {
		final FrameLayout frameView = new FrameLayout(aContext);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(aContext)
		.setTitle("Delete")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				aDialogResult.callback() ;
			}
		})
		.setNegativeButton("Cancel", null );
		builder.setView(frameView);

		final AlertDialog alertDialog = builder.create();
		alertDialog.getLayoutInflater().inflate(R.layout.file_dialog_delete, frameView);
		TextView sourceTextView = (TextView) frameView.findViewById( R.id.test_delete_source) ;
		String sourceName = aSourceUri.getPath().substring(aSourceUri.getPath().lastIndexOf(File.separator)+1) ;
		sourceTextView.setText( sourceName );

		alertDialog.show();
	}
	
}
