package info.guardianproject.geebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * @author devrandom
 */
public class InviteAcceptActivity extends Activity {
    private static final String TAG = "Geebox.InviteAcceptActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String aFriend = intent.getStringExtra(Api.EXTRA_FRIEND_ID);
        String aName = intent.getStringExtra(Api.EXTRA_NAME);
        if (Api.ACTION_INVITE_PEER.equals(intent.getAction()))
            showInvitedDialog(aFriend, aName);
        else if (Api.ACTION_RESPOND_INVITE_PEER.equals(intent.getAction())) {
            //TODO(miron) - jump to share
            finish();
        }
    }

    private void showInvitedDialog(String aFriend, String aName) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.dialogIcon)
                .setTitle("Invited to Share")
                .setMessage("Accept share by " + aFriend + " of " + aName + "?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doInvited();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .create();
        dialog.show();
    }

    private void doInvited() {
        String aAccount = getIntent().getStringExtra(Api.EXTRA_ACCOUNT_ID);
        String aFriend = getIntent().getStringExtra(Api.EXTRA_FRIEND_ID);
        String aName = getIntent().getStringExtra(Api.EXTRA_NAME);
        String aReference = getIntent().getStringExtra(Api.EXTRA_REFERENCE);
        String aRequestId = getIntent().getStringExtra(Api.EXTRA_REQUEST_ID);

        long peerId = Geebox.makePeer(getContentResolver(), aAccount, aFriend);
        // TODO(miron) let user place share
        String aDirectory = "/";
        long shareId = Geebox.makeShare(getContentResolver(), Uri.parse(aDirectory + aName));
        long peerShareId = Geebox.makePeerShare(getContentResolver(), peerId, shareId, aReference);
        String aLocalReference = Geebox.getPeerShareReference(getContentResolver(), peerShareId);
        Log.i(TAG, "Created peerShare " + peerShareId);
        GDataplugService.startService_respondInvitePeer(this, aRequestId, aLocalReference);
        // TODO(miron) show share
        finish();
    }

    public static Intent makeInviteIntent(Context aContext, String aAccountId, String aFriendId, String aReference, String aName, String aRequestId) {
        return new Intent(aContext, InviteAcceptActivity.class)
                .putExtra(Api.EXTRA_ACCOUNT_ID, aAccountId)
                .putExtra(Api.EXTRA_FRIEND_ID, aFriendId)
                .putExtra(Api.EXTRA_REFERENCE, aReference)
                .putExtra(Api.EXTRA_NAME, aName)
                .putExtra(Api.EXTRA_REQUEST_ID, aRequestId)
                .setAction(Api.ACTION_INVITE_PEER);
    }

    public static Intent makeInviteAcceptedIntent(Context aContext, String aAccountId, String aFriendId, String aName) {
        return new Intent(aContext, InviteAcceptActivity.class)
                .putExtra(Api.EXTRA_ACCOUNT_ID, aAccountId)
                .putExtra(Api.EXTRA_FRIEND_ID, aFriendId)
                .putExtra(Api.EXTRA_NAME, aName)
                .setAction(Api.ACTION_RESPOND_INVITE_PEER);
    }
}
