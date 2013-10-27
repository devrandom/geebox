package info.guardianproject.geebox;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import info.guardianproject.otr.dataplug.DataplugService;

public class GDataplugService extends DataplugService {
    public static final String DATAPLUG_URI = "chatsecure:/geebox";
	private static final String REGISTRATION =
			"{ 'descriptor': 	{ 'uri': 'chatsecure:/geebox', 'name': 'ChatSecure Filebox' }, 'meta': { 'publish' : true } }";
    private static final int INVITE_NOTIFICATIONS = 4000;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
	protected String getRegistration() throws JSONException {
		JSONObject json = new JSONObject( REGISTRATION );				
		return json.toString() ;
	}

	@Override
	protected void doActivate(String aAccountId, String aFriendId) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleIncomingRequest(Request aRequest) throws Exception {
		String aUri = aRequest.getUri();
        Log.i(TAG, "URI = " + aUri);
        if (aUri.startsWith(DATAPLUG_URI + "/invite/")) {
            notifyInvite(aRequest);
        } else {
            Log.e(TAG, "Unknown URI");
        }
		// TODO Auto-generated method stub

	}

    private void notifyInvite(Request aRequest) {
        String aUriString = aRequest.getUri();
        Uri aUri = Uri.parse(aUriString);
        String aReference = aUri.getLastPathSegment();
        String aName = aUri.getQueryParameter("name");

        PendingIntent intent = makeInviteIntent(
                aRequest.getAccountId(),
                aRequest.getFriendId(),
                aReference, aName);
        Notification.Builder builder = new Notification.Builder(this).
                setContentTitle("Invite").
                setContentText("from " + aRequest.getFriendId() + " for " + aName).
                setSmallIcon(R.drawable.geebox_icon).
                setTicker(aRequest.getFriendId() + " shared " + aName).
                setContentIntent(intent).setAutoCancel(false);
        Notification notification = builder.build();

        mNotificationManager.notify(INVITE_NOTIFICATIONS, notification);

    }

    private PendingIntent makeInviteIntent(String aAccountId, String aFriendId, String aReference, String aName) {
        Intent intent =
                new Intent(this, InviteAcceptActivity.class)
                        .putExtra(Api.EXTRA_ACCOUNT_ID, aAccountId)
                        .putExtra(Api.EXTRA_FRIEND_ID, aFriendId)
                        .putExtra(Api.EXTRA_REFERENCE, aReference)
                        .putExtra(Api.EXTRA_NAME, aName)
                ;
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }


    @Override
    public int onStartCommand(Intent aIntent, int flags, int startId) {
        try {
            if (!handleIntent( aIntent ))
                return super.onStartCommand(aIntent, flags, startId);
        } catch (Throwable e) {
            error( e.getMessage() ) ;
        }
        return START_NOT_STICKY;
    }

    private boolean handleIntent(Intent intent) {
        if (Api.ACTION_INVITE_PEER.equals(intent.getAction())) {
            String aAccountId = intent.getStringExtra(Api.EXTRA_ACCOUNT_ID);
            String aFriendId = intent.getStringExtra(Api.EXTRA_FRIEND_ID);
            String aName = intent.getStringExtra(Api.EXTRA_NAME);
            String aReference = intent.getStringExtra(Api.EXTRA_REFERENCE);
            Uri aUri = new Uri.Builder()
                    .scheme("chatsecure")
                    .path("/geebox/invite/")
                    .appendPath(aReference)
                    .appendQueryParameter("name", aName).build();
            sendOutgoingRequest( aAccountId, aFriendId, "OFFER", aUri.toString(), null, new RequestCallback() {
                @Override
                public void onResponse(Request aRequest, byte[] aContent, String headersString) {
                    try {
                        handleIncomingResponseInvite(aRequest, aContent) ;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }) ;

            return true;
        }
        return false;
    }

    private void handleIncomingResponseInvite(Request aRequest, byte[] aContent) {
        info("saw invite response");
    }

    @Override
    protected void handleDiscover(Intent aIntent) throws JSONException {
        super.handleDiscover(aIntent);
    }

    public static void startService_invitePeer(Context aContext,
                                               String aAccount,
                                               String aPeer,
                                               String aReference,
                                               String aName) {
        Intent intent = new Intent(aContext, GDataplugService.class);
        intent.setAction(Api.ACTION_INVITE_PEER);
        intent.putExtra(Api.EXTRA_ACCOUNT_ID, aAccount);
        intent.putExtra(Api.EXTRA_FRIEND_ID, aPeer);
        intent.putExtra(Api.EXTRA_REFERENCE, aReference);
        intent.putExtra(Api.EXTRA_NAME, aName);
        aContext.startService(intent);
    }


    private void error(String message) {
        Log.e(TAG, message);
    }

    private void info(String message) {
        Log.i(TAG, message);
    }
}
