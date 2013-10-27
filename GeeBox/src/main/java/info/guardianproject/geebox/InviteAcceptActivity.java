package info.guardianproject.geebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author devrandom
 */
public class InviteAcceptActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String aFriend = getIntent().getStringExtra(Api.EXTRA_FRIEND_ID);
        String aName = getIntent().getStringExtra(Api.EXTRA_NAME);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.dialogIcon)
                .setTitle("Invited to Share")
                .setMessage("Accept share by " + aFriend + " of " + aName + "?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        dialog.show();
    }
}
