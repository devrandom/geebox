package info.guardianproject.geebox;

import info.guardianproject.otr.dataplug.DataplugService;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GDataplugService extends DataplugService {
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

}
