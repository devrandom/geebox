package info.guardianproject.geebox;

import android.test.ActivityInstrumentationTestCase2;

/**
 *
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void testActivitySetUp() {
        assertNotNull(getActivity());
    }
}
