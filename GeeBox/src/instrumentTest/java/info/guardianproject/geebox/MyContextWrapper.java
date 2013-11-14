package info.guardianproject.geebox;

import android.content.Context;
import android.content.ContextWrapper;

/**
* @author devrandom
*/
class MyContextWrapper extends ContextWrapper {
    MyContextWrapper(Context context) {
        super(context);
    }

    @Override
    public Context getApplicationContext() {
        return getBaseContext();
    }
}
