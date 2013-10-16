/**
 * 
 */
package info.guardianproject.geebox.browser;

import java.io.File;
import java.io.IOException;

import android.net.Uri;

public class FileSystem {
	
	public static void rename( Uri aSourceUri, String aTarget ) throws IOException {
		File sourceFile = new File(aSourceUri.getPath());
		// source does not exist
		if( ! sourceFile.exists() ) {
			throw new IOException( "Source not found: " + aSourceUri.getPath() );
		}
		// source not a file
		if( ! sourceFile.isFile() ) {
			throw new IOException( "Source not a file: " + aSourceUri.getPath() );
		}
		// target empty
		if( aTarget == null  ||  aTarget.trim().length() <= 0 ) {
			throw new IOException( "Empty target name" );
		}
		// illegal chars
		if( aTarget.indexOf(File.separator) > 0 ) { // TODO input field chars definition in layout
			throw new IOException( "Illegal Characters: " + aTarget );
		}
		// do it
		String targetFullPath = sourceFile.getPath().substring(0,sourceFile.getPath().lastIndexOf(File.separator));
		File targetFile = new File( targetFullPath + File.separator + aTarget);
		if( targetFile.exists() ) {
			throw new IOException( "Target exists: " + aTarget );
		}
		
		boolean success = sourceFile.renameTo(targetFile);
		if( ! success ) {
			throw new IOException( "Rename failed: " + aSourceUri + " " + aTarget );
		}
	}
	
	public static void delete( Uri aSourceUri ) throws IOException {
		File sourceFile = new File(aSourceUri.getPath());
		// source does not exist
		if( ! sourceFile.exists() ) {
			throw new IOException( "Source not found: " + aSourceUri.getPath() );
		}
		// source not a file/directory
		if( ! (sourceFile.isFile() || sourceFile.isDirectory()) ) {
			throw new IOException( "Source not a file: " + aSourceUri.getPath() );
		}
		// do it
		boolean success = sourceFile.delete();
		if( ! success ) {
			throw new IOException( "Delete failed: " + aSourceUri );
		}
	}
	
	public static void move( Uri aSourceUri, Uri aTargetUri ) throws IOException {
		return ;
	}
	
}
