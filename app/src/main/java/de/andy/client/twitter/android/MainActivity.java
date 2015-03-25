package de.andy.client.twitter.android;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Window;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        MainFragment mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                                   .add(R.id.main_activity_frame_layout, mainFragment).commit();
    }
}