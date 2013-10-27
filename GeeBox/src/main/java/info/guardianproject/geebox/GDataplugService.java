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

import java.io.UnsupportedEncodingException;

import info.guardianproject.otr.dataplug.DataplugService;

public class GDataplugService extends DataplugService {
    public static final String DATAPLUG_URI = "chatsecure:/geebox";
	private static final String REGISTRATION =
			"{ 'descriptor': 	{ 'uri': 'chatsecure:/geebox', 'name': 'ChatSecure Filebox' }, 'meta': { 'publish' : true } }";
    private static final int INVITE_NOTIFICATIONS = 4000;
    private static final int INVITE_ACCEPT_NOTIFICATIONS = 4001;
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
	}

    private void notifyInvite(Request aRequest) {
        String aUriString = aRequest.getUri();
        Uri aUri = Uri.parse(aUriString);
        String aReference = aUri.getLastPathSegment();
        String aName = aUri.getQueryParameter("name");

        PendingIntent intent = makeInvitePendingIntent(
                aRequest.getAccountId(),
                aRequest.getFriendId(),
                aReference, aName,
                aRequest.getId());
        Notification.Builder builder = new Notification.Builder(this).
                setContentTitle("Invite").
                setContentText("from " + aRequest.getFriendId() + " for " + aName).
                setSmallIcon(R.drawable.geebox_icon).
                setTicker(aRequest.getFriendId() + " shared " + aName).
                setContentIntent(intent).setAutoCancel(true);
        Notification notification = builder.build();

        mNotificationManager.notify(INVITE_NOTIFICATIONS, notification);

    }

    private PendingIntent makeInvitePendingIntent(String aAccountId, String aFriendId, String aReference, String aName, String aRequestId) {
        Intent intent = InviteAcceptActivity.makeInviteIntent(this, aAccountId, aFriendId, aReference, aName, aRequestId);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    private PendingIntent makeInviteAcceptedPendingIntent(String aAccountId, String aFriendId, String aName) {
        Intent intent = InviteAcceptActivity.makeInviteAcceptedIntent(this, aAccountId, aFriendId, aName);
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

    private boolean handleIntent(Intent intent) throws JSONException, UnsupportedEncodingException {
        if (Api.ACTION_INVITE_PEER.equals(intent.getAction())) {
            String aAccountId = intent.getStringExtra(Api.EXTRA_ACCOUNT_ID);
            String aFriendId = intent.getStringExtra(Api.EXTRA_FRIEND_ID);
            String aName = intent.getStringExtra(Api.EXTRA_NAME);
            String aReference = intent.getStringExtra(Api.EXTRA_REFERENCE);
            final long aPeerShareId = intent.getLongExtra(Api.EXTRA_PEER_SHARE_ID, -1);
            Uri aUri = new Uri.Builder()
                    .scheme("chatsecure")
                    .path("/geebox/invite/")
                    .appendPath(aReference)
                    .appendQueryParameter("name", aName).build();
            final PendingIntent pendingIntent = makeInviteAcceptedPendingIntent(
                    aAccountId,
                    aFriendId,
                    aName);
            Notification.Builder builder = new Notification.Builder(this).
                    setContentTitle("Invite Accepted").
                    setContentText("by " + aFriendId + " for " + aName).
                    setSmallIcon(R.drawable.geebox_icon).
                    setTicker(aFriendId + " accepted " + aName).
                    setContentIntent(pendingIntent).setAutoCancel(true);
            final Notification notification = builder.build();

            sendOutgoingRequest( aAccountId, aFriendId, "OFFER", aUri.toString(), null, new RequestCallback() {
                @Override
                public void onResponse(Request aRequest, byte[] aContent, String headersString) {
                    try {
                        handleIncomingResponseInvite(aPeerShareId, notification, aContent) ;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }) ;

            return true;
        }

        if (Api.ACTION_RESPOND_INVITE_PEER.equals(intent.getAction())) {
            String aRequestId = intent.getStringExtra(Api.EXTRA_REQUEST_ID);
            String aReference = intent.getStringExtra(Api.EXTRA_REFERENCE);
            JSONObject object = new JSONObject();
            object.put("reference", aReference);
            sendOutgoingResponse(aRequestId, dumpJSONContent(object), null);

            return true;
        }

        return false;
    }

    private void handleIncomingResponseInvite(
            long peerShareId,
            Notification notification,
            byte[] aContent) throws JSONException {
        JSONObject json = parseJSONContent(aContent);
        String reference = json.getString("reference");
        info("saw invite response for " + peerShareId + " remote=" + reference);
        Geebox.setPeerShareReference(getContentResolver(), peerShareId, reference);
        mNotificationManager.notify(INVITE_ACCEPT_NOTIFICATIONS, notification);
    }

    @Override
    protected void handleDiscover(Intent aIntent) throws JSONException {
        super.handleDiscover(aIntent);
    }

    public static void startService_invitePeer(Context aContext,
                                               String aAccount,
                                               String aPeer,
                                               String aReference,
                                               String aName,
                                               long aPeerShareId
                                               ) {
        Intent intent = new Intent(aContext, GDataplugService.class);
        intent.setAction(Api.ACTION_INVITE_PEER);
        intent.putExtra(Api.EXTRA_ACCOUNT_ID, aAccount);
        intent.putExtra(Api.EXTRA_FRIEND_ID, aPeer);
        intent.putExtra(Api.EXTRA_REFERENCE, aReference);
        intent.putExtra(Api.EXTRA_NAME, aName);
        intent.putExtra(Api.EXTRA_PEER_SHARE_ID, aPeerShareId);
        aContext.startService(intent);
    }

    public static void startService_respondInvitePeer(Context aContext,
                                               String aRequestId,
                                               String aReference) {
        Intent intent = new Intent(aContext, GDataplugService.class);
        intent.setAction(Api.ACTION_RESPOND_INVITE_PEER);
        intent.putExtra(Api.EXTRA_REQUEST_ID, aRequestId);
        intent.putExtra(Api.EXTRA_REFERENCE, aReference);
        aContext.startService(intent);
    }

    private void error(String message) {
        Log.e(TAG, message);
    }

    private void info(String message) {
        Log.i(TAG, message);
    }

    private byte[] dumpJSONContent(JSONObject object) throws UnsupportedEncodingException {
        return object.toString().getBytes(CHARSET);
    }

    private JSONObject parseJSONContent(byte[] aContent) throws JSONException {
        try {
            return new JSONObject(new String(aContent, CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
