package info.guardianproject.geebox;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import info.guardianproject.otr.dataplug.DataplugService;

public class GDataplugService extends DataplugService {
    public static final String DATAPLUG_URI = "chatsecure:/geebox";
	private static final String REGISTRATION =
			"{ 'descriptor': 	{ 'uri': 'chatsecure:/geebox', 'name': 'ChatSecure Filebox' }, 'meta': { 'publish' : true } }";

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
		// TODO Auto-generated method stub

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
            String aUri;
            try {
                aUri = DATAPLUG_URI + "/invite/" + aReference + "?name=" + URLEncoder.encode(aName, "UTF8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            sendOutgoingRequest( aAccountId, aFriendId, "OFFER", aUri, null, new RequestCallback() {
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
