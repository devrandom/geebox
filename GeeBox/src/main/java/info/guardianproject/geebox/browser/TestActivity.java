package info.guardianproject.geebox.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import info.guardianproject.geebox.Geebox;
import info.guardianproject.geebox.R;

import static info.guardianproject.geebox.Geebox.Peers;
import static info.guardianproject.geebox.Geebox.makePeer;
import static info.guardianproject.geebox.Geebox.makePeerShare;
import static info.guardianproject.geebox.Geebox.makeShare;

public class TestActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_CODE_FOLDER_BROWSER = 6661;
	private static final int REQUEST_CODE_FILE_BROWSER = 6662;
	private static final int REQUEST_CODE_FOLDER_BROWSER_MOVE = 6663;
    public static final int REQUEST_CODE_INVITE_CONTACT = 6664;
    private SimpleCursorAdapter mAdapter;

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
                onClickRename(aActivity, mPickedUri);
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
        aActivity.findViewById(R.id.test_share).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickShare(aActivity);
            }
        });
        aActivity.findViewById(R.id.test_share).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickShare1(aActivity, mPickedUri);
                return true;
            }
        });
	}

    private void onClickShare(Activity aActivity) {
        Dialog aDialog;
        AlertDialog.Builder bDialog = new AlertDialog.Builder(this);
        ListView shareList = new ListView(this);
        mAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{Peers.COLUMN_NAME_ADDRESS},
                new int[]{android.R.id.text1},
                0
        );
        shareList.setAdapter(mAdapter);
        bDialog.setView(shareList);
        aDialog = bDialog.create();
        aDialog.show();
        getLoaderManager().restartLoader(0, null, this);
    }

    private void onClickShare1(final Activity aActivity, final Uri aSourceUri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("vnd.android.cursor.dir/imps-contacts");
        startActivityForResult(intent, REQUEST_CODE_INVITE_CONTACT);
    }


    protected void onActivityResultInvite(Activity aActivity, Uri aDirectory, String aAccount, String aPeer) {
        long peerId = makePeer(getContentResolver(), aAccount, aPeer);
        long shareId = makeShare(getContentResolver(), aDirectory);
        long peerShareId = makePeerShare(getContentResolver(), peerId, shareId);
        Toast.makeText(aActivity, "stuff " + peerShareId, Toast.LENGTH_LONG).show();
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
				setPicked(null);
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
				setPicked(null);
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
				setPicked(null);
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

            case REQUEST_CODE_INVITE_CONTACT:
                if( ! isOK( resultCode, data )) {
                    return ;
                }
                onActivityResultInvite( this, mPickedUri, data.getStringExtra("account"), data.getStringExtra("peer") );
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
		setPicked( aUri ) ;
	}
	
	private void doFilePicked(Uri aUri) {
		setPicked( aUri ) ;
	}
	
	private void setPicked(Uri aUri) {
		mPickedUri = aUri ;
		if( aUri == null ) {
			((TextView)findViewById(R.id.test_source_name)).setText( "" );
			((Button)findViewById(R.id.test_rename)).setEnabled(false);
			((Button)findViewById(R.id.test_delete)).setEnabled(false);
			((Button)findViewById(R.id.test_move)).setEnabled(false);
            ((Button)findViewById(R.id.test_share)).setEnabled(false);
		} else {
			((TextView)findViewById(R.id.test_source_name)).setText( aUri.toString() );
			((Button)findViewById(R.id.test_rename)).setEnabled(true);
			((Button)findViewById(R.id.test_delete)).setEnabled(true);
			((Button)findViewById(R.id.test_move)).setEnabled(true);
            ((Button)findViewById(R.id.test_share)).setEnabled(true);
		}
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mPickedUri == null)
            return null;
        return new CursorLoader(this, Geebox.PeerShares.CONTENT_URI, null,
                Geebox.Shares.COLUMN_NAME_DIRECTORY + "= ?",
                new String[] {mPickedUri.toString()}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
