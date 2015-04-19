package droidx.localizeme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by RaminduS on 4/14/2015.
 */
public class SplashActivity extends Activity {
    LocationManager locationManager;
    LocationListener locationListener;
    HttpAsyncTask task;
    static Location location;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences=getSharedPreferences("info",MODE_PRIVATE);
        locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SplashActivity.location=location;
                //Toast.makeText(getBaseContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                //mMap.addMarker(new MarkerOptions().title("current").position(new LatLng(location.getLatitude(),location.getLongitude())));
                //setLocationUpdatesDisable();
                task=new HttpAsyncTask();
                task.execute("https://api.foursquare.com/v2/venues/explore?client_id=4L3ZXW3XZRAWZHCS30I3J20VKAVCD3DQZCITNE2BL1RECQCD&client_secret=5EPRSZX5TNW50FQVZ2FBQVS5YSZKIC5KQBYOUU0VJOCNRRND&v=20130815&ll="+location.getLatitude()+","+location.getLongitude()+"&limit=20");

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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,2000,500,locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,500,locationListener);
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
            //Toast.makeText(SplashActivity.this,"still running",Toast.LENGTH_LONG).show();
            if(result.equals("")){
                Toast.makeText(SplashActivity.this,"Retrying... Please turn on your internet source "+Toast.LENGTH_SHORT,Toast.LENGTH_SHORT).show();
            }else{
                //Toast.makeText(SplashActivity.this,result,Toast.LENGTH_LONG).show();
                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putFloat("lat", (float) SplashActivity.location.getLatitude());
                editor.putFloat("lon", (float) SplashActivity.location.getLongitude());

                editor.putString("result", result);
                editor.commit();
                startActivity(intent);

                task=null;
                locationManager.removeUpdates(locationListener);
                locationManager=null;
                locationListener=null;
                finish();
            }
        }
    }
}
