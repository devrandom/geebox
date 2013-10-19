/**
 * 
 */
package info.guardianproject.geebox.browser;

import java.io.File;
import java.io.IOException;

import android.net.Uri;

public class FileSystem {
	
	public static void rename( Uri aSourceUri, String aTarget ) throws IOException {
		if( aSourceUri == null ) {
			throw new IOException( "NULL source" );
		}
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
		if( aSourceUri == null ) {
			throw new IOException( "NULL source" );
		}
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
		deleteRecursive(sourceFile);
	}
	
	private static void deleteRecursive(File aFileOrDirectory) throws IOException {
	    if (aFileOrDirectory.isDirectory()) {
	    		for (File child : aFileOrDirectory.listFiles()) {
	    			deleteRecursive(child);
	    		}
	    }

	    boolean success = aFileOrDirectory.delete();
		if( ! success ) {
			throw new IOException( "Delete failed: " + aFileOrDirectory );
		}
	}
	
	public static void move( Uri aSourceUri, Uri aTargetUri ) throws IOException {
		if( aSourceUri == null ) {
			throw new IOException( "NULL source" );
		}
		if( aTargetUri == null ) {
			throw new IOException( "NULL target" );
		}
		File sourceFile = new File(aSourceUri.getPath());
		// source does not exist
		if( ! sourceFile.exists() ) {
			throw new IOException( "Source not found: " + aSourceUri.getPath() );
		}
		File targetFolder = new File(aTargetUri.getPath());
		// target not a folder
		if( ! targetFolder.isDirectory() ) {
			throw new IOException( "Target not a folder: " + aTargetUri );
		}
		// TODO merge those 2 cases
		if( sourceFile.isFile() ) {
			String sourceFileName = sourceFile.getPath().substring(sourceFile.getPath().lastIndexOf(File.separator));
			String targetName = targetFolder.getPath() + sourceFileName ;
			File targetFile = new File(targetName);
			// target exists
			if( targetFile.exists() ) {
				throw new IOException( "Target exists: " + targetFile );
			}
			
			// do it
			boolean success = sourceFile.renameTo( targetFile );
			if( ! success ) {
				throw new IOException( "Move failed: " + aSourceUri + " " + aTargetUri );
			}
			return ;
		}
		if( sourceFile.isDirectory() ) {
			if( aTargetUri.getPath().startsWith(aSourceUri.getPath()) ) {
				throw new IOException( "Source contained in target: " + aSourceUri + " " + aTargetUri );
			}
			
			String sourceFileName = sourceFile.getPath().substring(sourceFile.getPath().lastIndexOf(File.separator));
			String targetName = targetFolder.getPath() + sourceFileName ;
			File targetFile = new File(targetName);
			// target exists
			if( targetFile.exists() ) {
				throw new IOException( "Target exists: " + targetFile );
			}
			
			// do it
			boolean success = sourceFile.renameTo( targetFile );
			if( ! success ) {
				throw new IOException( "Move failed: " + aSourceUri + " " + aTargetUri );
			}
			return ;
			
		}
		return ;
	}
	
}
