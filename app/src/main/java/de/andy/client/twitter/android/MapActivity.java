package de.andy.client.twitter.android;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends Activity {

    private GoogleMap googleMap;
    private double latitude, longitude;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle bundle = getIntent().getExtras();
        latitude = bundle.getDouble(TweetFragment.INTENT_LATITUDE);
        longitude = bundle.getDouble(TweetFragment.INTENT_LONGITUDE);
        user = bundle.getString(TweetFragment.INTENT_USER);
        LatLng position = new LatLng(latitude, longitude);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_view)).getMap();

        if (googleMap != null) {
            Marker marker = googleMap.addMarker(new MarkerOptions().position(position)
                                                                   .title(user));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 5));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
