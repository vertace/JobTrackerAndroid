package com.tt.jobtracker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import com.tt.helpers.DatabaseHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tt.data.MapShopSortViewModel;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by BS-308 on 4/21/2015.
 */
public class MapForMultipleShop extends FragmentActivity {
    private GoogleMap mMap;
    LatLng latLng,currentlang;
    String addressText,currenttext;
    LocationManager mlocManager;
    LocationListener mlocListener;
    List<android.location.Address> addresses=null;

    Geocoder geocoder ;
    String latlngShop[];
    String mapShopsort[];
    Button btndirection;
    int temp=0;
    boolean isGPSEnabled=false;
    boolean isNetworkEnabled=false;
    int globalVar,globalShopOrder;
    int count,i,modulo;
    Polyline poly;
    ArrayList<MapShopSortViewModel> mapShopSortList;
    MapShopSortViewModel mapshopSingle;
    public ProgressDialog m_ProgressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        btndirection=(Button)findViewById(R.id.btn_Navigation);

        btndirection.setVisibility(View.GONE);

        SupportMapFragment fm = (SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map);
        mMap=fm.getMap();
       // mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        geocoder = new Geocoder(this);
        latlngShop=new String[Shared.TaskList.size()];



        m_ProgressDialog = ProgressDialog
                .show(MapForMultipleShop.this,
                        "Please wait...",
                        "Updating the route...",
                        true);

        try {
            mapShopSortList=new ArrayList<MapShopSortViewModel>();
            for(int k=0;k<Shared.TaskList.size();k++)
            {

                latlngShop[k]=new String();

                addresses= geocoder.getFromLocationName(Shared.TaskList.get(k).ShopAddress, 5);

              //  Map<String, String> treeMap = new TreeMap<String, String>(addresses);
            if(addresses!=null && addresses.size()>0)
                     {
                      for (int i = 0; i < addresses.size(); i++)
                    {

                   android.location.Address address = (android.location.Address) addresses.get(i);

                     // Creating an instance of GeoPoint, to display in Google Map
                        latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        latlngShop[k] = String.valueOf(address.getLatitude()) + "," + String.valueOf(address.getLongitude());
                        addressText = String.format("%s, %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                                address.getCountryName());


        //	Marker ciu = mMap.addMarker(new MarkerOptions().position(latLng).title("Address"));
        //     ciu.setTitle(addressText);
        // Locate the first location

    }

    Marker ciu = mMap.addMarker(new MarkerOptions().position(latLng).title("Address"));
    ciu.setTitle(addressText);

    mMap.setMyLocationEnabled(true);
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
}

             /*   Double result= distance(12.9265533, 80.10782879999999,12.9908401,80.21827569999999);
                double value=result;
               // mMap.setOnMyLocationChangeListener(myLocationChangeListener);*/



            }

        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            SstAlert.Show(MapForMultipleShop.this, "Error",
                    "Some Problem occured");
        }

        ArrayList<MapShopSortViewModel> test=mapShopSortList;

        if(addresses!=null && addresses.size()>0) {
            mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            mlocListener = new MyMapLocationListener();


            isGPSEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled) {
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, mlocListener);
            } else {
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, mlocListener);
            }
            //  mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 3000, 0, mlocListener);
            //  ((MyLocationListener) mlocListener).DrawRoute();
            //final LatLng CIU = new LatLng(35.21843892856462, 33.41662287712097);
            //	Marker ciu = mMap.addMarker(new MarkerOptions() .position(CIU).title("My Office"));
        }
        else{
            Toast.makeText(getApplicationContext(), "Network is slow wait...", Toast.LENGTH_SHORT).show();
        }

    }

  /*  private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            Marker ciu  = mMap.addMarker(new MarkerOptions().position(loc));
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
        }
    };*/


   private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



    public void btnNavigationClick(View v)
    {
        if(Shared.html_instructions!=null)
        {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    MapForMultipleShop.this);
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Directions");
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapForMultipleShop.this,android.R.layout.simple_list_item_1);

            arrayAdapter.addAll(Shared.html_instructions);
            builderSingle.setAdapter(arrayAdapter, null);
            builderSingle.show();
        }
        else
        {
            SstAlert.Show(MapForMultipleShop.this, "Error",
                    "Please wait");

        }

    }

    private String GetShA() {
        PackageInfo info;
        String hash_key="";
        try {

            info = getPackageManager().getPackageInfo(
                    "info.tekguc.umut.googlemapsmapsandroidv2", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                hash_key = new String(Base64.encode(md.digest(), 0));
            }

        } catch (PackageManager.NameNotFoundException e1) {
        } catch (NoSuchAlgorithmException e) {
        } catch (Exception e) {
        }
        return hash_key;
    }
    @Override
         public boolean onCreateOptionsMenu(Menu menu) {
           // Inflate the menu; this adds items to the action bar if it is present.
           getMenuInflater().inflate(R.menu.mapmenu, menu);
           return true;
       }
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mnuBack:
                ShopShortList();
                // startActivity(getIntent());

               // intent = new Intent(this, TaskList.class);
              //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //getApplicationContext().startActivity(intent);

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
   private void  ShopShortList()
    {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        if(globalShopOrder==0) {
            dbHelper.deleteMapShop();
            for (MapShopSortViewModel saveShop : Shared.MapSortByShop) {
                dbHelper.insertMapSortByShopName(saveShop);
            }
            globalShopOrder++;
        }
        if( mapShopsort!=null)
        {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    MapForMultipleShop.this);
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Shop Name Order");
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapForMultipleShop.this,android.R.layout.simple_list_item_1);
           ArrayList<MapShopSortViewModel> OrderShopList=dbHelper.getAllShopByOrder();
            for( int l=0;l<OrderShopList.size();l++)
            {


                i=l+1;
                arrayAdapter.add(i+". "+OrderShopList.get(l).ShopName);
                i++;
            }
          //  arrayAdapter.addAll(Shared.end_address);
            builderSingle.setAdapter(arrayAdapter, null);
            builderSingle.show();
        }
        else
        {
            SstAlert.Show(MapForMultipleShop.this, "Error",
                    "Please wait");

        }

    }
    /**private Handler mHandler = new Handler();
    private Runnable onRequestLocation = new Runnable() {
        @Override
        public void run() {
            // Ask for a location
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
            // Run this again in an hour
            mHandler.postDelayed(onRequestLocation, DateUtils.HOUR_IN_MILLIS);
        }
    };*/



    public class MyMapLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location loc)
        {
            mlocManager.removeUpdates(mlocListener);
            loc.getLatitude();
            loc.getLongitude();

            String Text = "My current location is: " +
                    "Latitud = " + loc.getLatitude() +
                    "Longitud = " + loc.getLongitude();
            currentlang = new LatLng(loc.getLatitude(), loc.getLongitude());
            Marker cione = mMap.addMarker(new MarkerOptions().position(currentlang).title("Address"));
            cione.setTitle("you");
            if(temp==0)
            {
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentlang, 15));
                temp++;
            }
            mapShopsort=new String[Shared.TaskList.size()];
            mapShopSortList=new ArrayList<MapShopSortViewModel>();
            for(int k=0;k<Shared.TaskList.size();k++)
            {
                String Latlanvalue=latlngShop[k];
                mapShopsort[k]=new String();
                if(Latlanvalue!=null && !Latlanvalue.isEmpty())
                {

                    String[] LatLangsplit = Latlanvalue.split(",");
                    mapshopSingle=new MapShopSortViewModel();
                    String Lat=LatLangsplit[0];
                    String Lan=LatLangsplit[1];
                    double Shoplat=Double.parseDouble(Lat);
                    double Shoplan=Double.parseDouble(Lan);
                    mapshopSingle.Lat =Shoplat;
                    mapshopSingle.Lon=Shoplan;
                    mapshopSingle.ShopName=Shared.TaskList.get(k).ShopName;
                    mapshopSingle.TaskID=Shared.TaskList.get(k).ID;
                    mapShopsort[k]=Shared.TaskList.get(k).ShopName;
                    mapshopSingle.distance= distance(loc.getLatitude(),loc.getLongitude(),Shoplat,Shoplan);
                    mapShopSortList.add(mapshopSingle);
                }
            }
            Shared.MapSortByShop= mapShopSortList;
            globalShopOrder=0;


                   // mlocManager.removeUpdates(mlocListener);
            DrawRoute();
            btndirection.setVisibility(View.VISIBLE);

            //Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }
        public  void DrawRoute()
        {
            if(Shared.TaskList.size()<=8)
            {
                String url = getDirectionsUrl(currentlang, latLng);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
            else {
                 count = Shared.TaskList.size() / 8;
                  modulo=Shared.TaskList.size()%8;
                for (i = 0; modulo==0?i < count:i <=count; i++)
                {
                    String url = getDirectionsUrl(currentlang, latLng);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }
            }

        }
        public String getDirectionsUrl(LatLng origin,LatLng dest)
        {

            // Origin of route
            String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
            String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
            // Destination of route



            // Sensor enabled
            String sensor = "sensor=false";
            String mode = "mode=driving";
            String waypoints="waypoints=";
            if(Shared.TaskList.size()<=8)
            {
                for (int k = 0; k <Shared.TaskList.size(); k++) {

                    waypoints = waypoints + latlngShop[k] + "|";
                }
            }
            else {
                if (count == i) {
                    for (int k = globalVar; k < globalVar + modulo; k++) {

                        waypoints = waypoints + latlngShop[k] + "|";

                    }
                    globalVar = 0;
                    m_ProgressDialog.dismiss();
                } else {
                    for (int k = globalVar; k < globalVar + 8; k++) {

                        waypoints = waypoints + latlngShop[k] + "|";

                    }
                    globalVar = globalVar + 8;
                }
            }
            waypoints = waypoints.substring(0, waypoints.length()-1);

            //     waypoints=waypoints+"Velacherry,Chennai|Tnagar,Chennai|Tamabam,Chennai";
            // waypoints=waypoints+"44a,sasinagar main raod,velachery,chennai-42|131,velachery byepass road,velachery,chennai-42";

            // Building the parameters to the web service
            String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode+"&"+waypoints;//str_origin+"&"+str_dest+"&"+waypoints+"&"+sensor;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

            return url;
        }
        public String downloadUrl(String strUrl) throws IOException{
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while( ( line = br.readLine()) != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception While Get", e.toString());
            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
        public class DownloadTask extends AsyncTask<String, Void, String>{

            // Downloading data in non-ui thread
            protected String doInBackground(String... url) {

                // For storing data from web service
                String data = "";

                try{
                    // Fetching the data from web service
                    data = downloadUrl(url[0]);
                }catch(Exception e){
                    Log.d("Background Task",e.toString());
                }
                return data;
            }

            // Executes in UI thread, after the execution of
            // doInBackground()
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                ParserTask parserTask = new ParserTask();
                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);

            }
        }




    }

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {


            /* if(poly!=null)
              {
                  poly.remove();
              }*/
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }


            // Drawing polyline in the Google Map for the i-th route
            poly=mMap.addPolyline(lineOptions);
            m_ProgressDialog.dismiss();

        }
    }

}
