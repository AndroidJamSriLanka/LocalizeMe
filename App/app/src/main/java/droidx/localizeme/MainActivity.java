package droidx.localizeme;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    HttpAsyncTask task;
    static String result="";

    LocationManager locationManager;
    LocationListener locationListener;
    static LatLng location;
    SharedPreferences sharedPreferences;
    String mode="ex";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences=getSharedPreferences("info",MODE_PRIVATE);
        result=sharedPreferences.getString("result", "");
        location=new LatLng(sharedPreferences.getFloat("lat",0),sharedPreferences.getFloat("lon",0));
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        setUpMapIfNeeded();
        task=new HttpAsyncTask();
        setLocationUpdatesEnable();
        CameraPosition cameraPosition=new CameraPosition.Builder()
                .target(new LatLng(sharedPreferences.getFloat("lat",0),sharedPreferences.getFloat("lon",0)))
                .zoom(12)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        setMarkers();
    }

    public void reInitTask(){
        task=new HttpAsyncTask();
    }

    private void setMarkers() {
/*

*/

        if(mode.equals("ex")){
            try {
                JSONObject json =new JSONObject(result);
                JSONArray items =json.getJSONObject("response").getJSONArray("groups").getJSONObject(0).getJSONArray("items");
                int i=0;
                while(true){
                    mMap.addMarker(new MarkerOptions()
                            .title(items.getJSONObject(i).getJSONObject("venue").getString("name"))
                            .position(new LatLng(items.getJSONObject(i).getJSONObject("venue").getJSONObject("location").getDouble("lat"),
                                    items.getJSONObject(i).getJSONObject("venue").getJSONObject("location").getDouble("lng"))))
                    ;

                    i++;
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this,"Loaded All data",Toast.LENGTH_LONG).show();
            }
        }else if(mode.equals("search")){
            try {
                JSONObject json =new JSONObject(result);
                JSONArray locations =json.getJSONObject("response").getJSONArray("venues");
                int i=0;
                while(true){
                    mMap.addMarker(new MarkerOptions()
                            .title(locations.getJSONObject(i).getString("name"))
                            .position(new LatLng(locations.getJSONObject(i).getJSONObject("location").getDouble("lat"), locations.getJSONObject(i).getJSONObject("location").getDouble("lng"))))
                    ;

                    i++;
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this,"Loaded All data",Toast.LENGTH_LONG).show();
            }
        }

    }

    public void reInitMap(){
        mMap.clear();
        setMarkers();
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void setLocationUpdatesEnable(){
        mMap.setMyLocationEnabled(true);
        locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                MainActivity.location=new LatLng(location.getLatitude(),location.getLongitude());
                //Toast.makeText(getBaseContext(),location.getLatitude()+" "+location.getLongitude(),Toast.LENGTH_SHORT).show();
                //mMap.addMarker(new MarkerOptions().title("current").position(new LatLng(location.getLatitude(),location.getLongitude())));
                //setLocationUpdatesDisable();
                CameraPosition cameraPosition=new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(),location.getLongitude()))
                        .zoom(12)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,500,locationListener);
    }
    public void setLocationUpdatesDisable(){
        locationManager.removeUpdates(locationListener);
        locationManager=null;
        locationListener=null;
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude+"&limit=20");
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=specials&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=trending&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=food&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=drinks&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=coffee&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=shops&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 8:
                mTitle = getString(R.string.title_section8);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=arts&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 9:
                mTitle = getString(R.string.title_section9);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=outdoors&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
            case 10:
                mTitle = getString(R.string.title_section10);
                reInitTask();
                mode="ex";
                task.execute("https://api.foursquare.com/v2/venues/explore?section=sights&client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+MainActivity.location.latitude+","+MainActivity.location.longitude);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }




    public boolean isConnectedToInternet(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //etResponse.setText(result);
            //Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();
            MainActivity.result=result;
            reInitMap();
        }
    }
}

class Venue{
    private String id, name, contact,address,shortCatName;
    private int distance;
    private float rating;
    private  LatLng latLng;

    public String getId(){return id;}
    public String getName(){return name;}
    public int getDistance(){return distance;}
    public LatLng getLatLng(){return latLng;}
    public String getContact(){return contact;}
    public String getAddress(){return address;}
    public String getShortCatName(){return shortCatName;}
    public float getRating(){return rating;}

    public Venue setId(String id){this.id=id; return this;}
    public Venue setName(String name){this.name=name; return this;}
    public Venue setDistance(int distance){this.distance=distance; return this;}
    public Venue setLatLng(LatLng latLng){this.latLng=latLng; return this;}
    public Venue setContact(String contact){this.contact=contact; return this;}
    public Venue setAddress(String address){this.address=address; return this;}
    public Venue setShortCatName(String shortCatName){this.shortCatName=shortCatName; return this;}
    public Venue setRating(float rating){this.rating=rating; return this;}

}
/*
new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
        .execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
        }
*/

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}