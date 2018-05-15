package com.example.android.run;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static MapsFragment instance = null;
    private PolygonOptions polygonOpt;

    //show text content
    private String text = "";
    private static int uid;
    private static String token;
    private static boolean valid;

    public Handler updateHandler ;
    public Runnable updateRunnable ;
    static int flag = 0;
    static int num = 0;
    static boolean show = false;

    public static MapsFragment getInstance() {
//        if( instance == null ) {
//            synchronized (MapsFragment.class) {
//                if (instance == null) {
//                    instance = new MapsFragment();
//                }
//            }
//        }
        synchronized (MapsFragment.class) {
                instance = new MapsFragment();
        }
        return instance;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    Location lastLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);


        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        //read uid and token
        readPrefs();
        if(!isNetworkAvailable()){
            Alert("Please check your internet connection, then try again.");
        }
        //update location
        if(flag == 0){
            updateHandler = new Handler();
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
                    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        Alert("Please check your GPS.");
                    }
                    else if(lastLocation!=null){
                        MyTaskPut updatePut = new MyTaskPut();
                        updatePut.execute(getResources().getString(R.string.apiURL)+"/member/update"
                                ,"uid=" + String.valueOf(uid) + "&operator_uid=" + String.valueOf(uid) + "&token=" + token + "&position_n=" + String.valueOf(lastLocation.getLatitude())
                                        + "&position_e=" + String.valueOf(lastLocation.getLongitude()));

                        //get result from function "onPostExecute" in class "myTaskPut"
                        try {
                            String readDataFromHttp = updatePut.get();
                            //Parse JSON info
                            parseJson(readDataFromHttp,"location");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.i("update",valid + String.valueOf(lastLocation.getLatitude())+"  "+lastLocation.getLongitude());

                        //if invalid, show alert
                        if(!valid && !show){
                            num ++;
                            if(num >= 4){   //一次五秒，數四次
                                show = true;
                                new AlertDialog.Builder(getContext())
                                        .setCancelable(false)   //按到旁邊也不會消失
                                        .setMessage("你超過邊界囉!")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                show = false;
                                            }
                                        }).show();
                                num = 0;
                            }
                        }
                    }
                    updateHandler.postDelayed(this, 5000);
                }
            };
            updateHandler.postDelayed(updateRunnable, 0);
            flag++;
        }

        return rootView;
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        updateHandler.removeCallbacks(updateRunnable);
//    }

    public void Refresh(){
//        // Create new fragment and transaction
//        Fragment newFragment = new MapsFragment();
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//
//        // Replace whatever is in the fragment_container view with this fragment,
//        // and add the transaction to the back stack
//        transaction.replace(R.id.frag_map, newFragment)
//                .addToBackStack(null)
//                .commit();
        initial(googleMap);
    }

    //========================內存=========================
    private void readPrefs(){
        SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
        System.out.println(uid);
        System.out.println(token);
    }

    //======================建立地圖==============================
    @Override
    public void onMapReady(GoogleMap mgoogleMap) {
        googleMap = mgoogleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24.794574, 120.992936), 17));
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                System.out.println("not request");
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        if(lastLocation!=null){
                            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            //move map camera
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                            System.out.println("update");
                        }
                        return false;
                    }
                });
                initial(googleMap);
            } else {
                //Request Location Permission
                System.out.println("request");
                checkLocationPermission();
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        if(lastLocation!=null){
                            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            //move map camera
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                            System.out.println("update");
                        }
                        return false;
                    }
                });
                initial(googleMap);
            }
        }
        else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if(lastLocation!=null){
                        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        //move map camera
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                        System.out.println("update");
                    }
                    return false;
                }
            });
            initial(googleMap);
        }
    }
    private void initial(GoogleMap mMap){
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Alert("Please check your GPS.");
        }

        //clear old setting of map
        mMap.clear();
        setBoundary(mMap);
        addMissionMarker(mMap);
        setScore();
        System.out.println("init");
        if(lastLocation!=null){
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            //move map camera
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24.794574, 120.992936), 17));

//            System.out.println("init");
        }
    }
    private void setBoundary(GoogleMap mMap){
        polygonOpt = new PolygonOptions();

        //get map boundary from server
        MyTaskGet httpGetBoundary = new MyTaskGet();
        httpGetBoundary.execute(getResources().getString(R.string.apiURL)+"/download/map/boundary.kml");
        try {
            String readDataFromHttp = httpGetBoundary.get();

            //parse .kml
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(readDataFromHttp));
            int eventType = xpp.getEventType();

            String tagName = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                } else if(eventType == XmlPullParser.START_TAG) {
                    tagName = xpp.getName();
                } else if(eventType == XmlPullParser.END_TAG) {
                } else if(eventType == XmlPullParser.TEXT) {
                    if(tagName.equals("coordinates")){
                        String[] pointList = xpp.getText().split("\n");
                        for(String p : pointList){
                            String[] point = p.split(",");
                            //If point has values in it
                            //.trim() is to delete the whitespaces in front
                            if(point.length!=1){
                                polygonOpt.add(new LatLng(Double.parseDouble(point[1]),Double.parseDouble(point[0].trim())));
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }

            //Set lines color
            polygonOpt.strokeColor(Color.BLACK);
            //add to the map
            mMap.addPolygon(polygonOpt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<HashMap<String,String>> missionList;
    ArrayList<HashMap<String,String>> reportList;
    int serverTimeHour,serverTimeMin;
    void addMissionMarker(GoogleMap mMap){
        String readDataFromHttp;
        MyTaskGet httpGetMission = new MyTaskGet();
        httpGetMission.execute(getResources().getString(R.string.apiURL)+"/mission/read?operator_uid="+String.valueOf(uid)+"&token="+token);
        //get result from function "onPostExecute" in class "myTaskGet"
        try {
            readDataFromHttp = httpGetMission.get();
            //Parse JSON info
            parseJson(readDataFromHttp,"mission");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //get report(to get the mission state)
        MyTaskGet httpGetReport = new MyTaskGet();
        httpGetReport.execute(getResources().getString(R.string.apiURL)+"/report/read?operator_uid="+String.valueOf(uid)+"&token="+token+"&uid="+String.valueOf(uid));
        try {
            readDataFromHttp = httpGetReport.get();
            parseJson(readDataFromHttp,"report");
        } catch (Exception e) {
            e.printStackTrace();
        }
        missionState();
        missionFilter();

        //add marker for each mission
        if(!missionList.isEmpty()){
            for(HashMap<String,String> m : missionList){
                double location_n = Double.parseDouble(m.get("location_n"));
                double location_e = Double.parseDouble(m.get("location_e"));
                if(location_e!=0 && location_n!=0){
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location_n,location_e))
                            .title(m.get("title")));
                    //unhandle remove marker
                }
            }
        }
    }

    private int score;
    void setScore (){
        TextView scoreView = (TextView)getActivity().findViewById(R.id.score);
        //get score
        MyTaskGet httpGetScore = new MyTaskGet();
        httpGetScore.execute(getResources().getString(R.string.apiURL)+"/member/read?operator_uid="+String.valueOf(uid)+"&token="+token+"&uid="+String.valueOf(uid));
        try {
            String readDataFromHttp = httpGetScore.get();
            parseJson(readDataFromHttp,"score");
        } catch (Exception e) {
            e.printStackTrace();
        }
        scoreView.setText(String.valueOf(score));

    }
    //Parse json received from server
    void parseJson (String info, String instru){
        if(instru.equals("mission")){
            missionList = new ArrayList<>();
            try {
                JSONObject payload = new JSONObject(new JSONObject(info).getString("payload"));
                JSONArray objects = payload.getJSONArray("objects");
                //Get mission number
                for(int i=0;i<objects.length();i++){
                    JSONObject subObject;
                    subObject = objects.getJSONObject(i);
                    HashMap<String,String> mission = new HashMap<>();
                    //put mid into hashmap
                    mission.put("mid",subObject.getString("mid"));
                    //put title into hashmap
                    mission.put("title",subObject.getString("title"));

                    //parse time, take hour&min only
                    //and put time_end into hashmap
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Calendar cal = Calendar.getInstance();
                    try {
                        Date date = dateFormat.parse(subObject.getString("time_end"));
                        cal.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mission.put("time_end",String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+":"+String.valueOf(cal.get(Calendar.MINUTE)));
                    //put location_e(經) into hashmap
                    mission.put("location_e",subObject.getString("location_e"));
                    //put location_n(緯) into hashmap
                    mission.put("location_n",subObject.getString("location_n"));

                    missionList.add(mission);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(instru.equals("report")){
            reportList = new ArrayList<>();
            try {
                JSONObject jObject = new JSONObject(info);

                //parse and get server time
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Calendar cal = Calendar.getInstance();
                try {
                    Date date = dateFormat.parse(jObject.getString("server_time"));
                    cal.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                serverTimeHour = cal.get(Calendar.HOUR_OF_DAY);
                serverTimeMin = cal.get(Calendar.MINUTE);
//                serverTimeHour = 0;
//                serverTimeMin = 0;

                JSONObject payload = new JSONObject(jObject.getString("payload"));
                JSONArray objects = payload.getJSONArray("objects");
                for(int i=0;i<objects.length();i++){
                    JSONObject subObject;
                    subObject = objects.getJSONObject(i);
                    HashMap<String,String> report = new HashMap<>();
                    report.put("mid",subObject.getString("mid"));
                    report.put("status",subObject.getString("status"));
                    reportList.add(report);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(instru.equals("score")){
            try {
                JSONObject payload = new JSONObject(new JSONObject(info).getString("payload"));
                JSONArray objects = payload.getJSONArray("objects");
                score = objects.getJSONObject(0).getInt("score");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                JSONObject payload = new JSONObject(new JSONObject(info).getString("payload"));
                valid = payload.getBoolean("valid_area");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Add mission state
    void missionState(){
        for(int i=0;i<reportList.size();i++){
            for(int j=0;j<missionList.size();j++){
                //find mid in each report
                //and add status to the corresponding mission in missionlist
                if(reportList.get(i).get("mid").equals(missionList.get(j).get("mid"))){
                    String status = reportList.get(i).get("status");   //0:being judged 1:success 2:fail
                    missionList.get(j).put("status",status);
                    break;
                }
            }
        }
        //add ("status",-1) to the missions that doesn't appear in reportlist
        for(int i=0;i<missionList.size();i++){
            if(!missionList.get(i).containsKey("status")) {
                missionList.get(i).put("status","-1");
            }
        }
    }
    //Filter out completed and expired mission
    void missionFilter(){
        ArrayList<HashMap<String,String>> oldMissionList = new ArrayList<>(missionList);
        missionList.clear();
        for(HashMap<String,String> m : oldMissionList){
            String missionTime = m.get("time_end");
            String[] part = missionTime.split(":");
            int hour = Integer.valueOf(part[0]);
            int min = Integer.valueOf(part[1]);
            if(hour>serverTimeHour){
                if(!m.get("status").equals("1")){
                    missionList.add(m);
                }
            }else if(hour==serverTimeHour){
                if(min>serverTimeMin){
                    if(!m.get("status").equals("1")){
                        missionList.add(m);
                    }
                }
            }
        }
    }

    //======================要權限=======================
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //ActivityCompat.requestPermissions(getActivity(),
            //        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            //        MY_PERMISSIONS_REQUEST_LOCATION );

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                System.out.println("show dialog");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity().getApplicationContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }

    }

    //======================建立google api,FusedLocationApi===========================
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

            googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("granted");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        System.out.println(location.getLatitude()+"   "+location.getLongitude());
    }


    //===================HTTP==========================
    //HTTPGet
    static class MyTaskGet extends AsyncTask<String,Void,String> {
        URL url = null;
        HttpURLConnection connection = null;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        public String doInBackground(String...arg0) {
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String urlStr = arg0[0];

            try
            {
                // create the HttpURLConnection
                url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();

                // 使用甚麼方法做連線
                connection.setRequestMethod("GET");

                // 是否添加參數(ex : json...等)
                //connection.setDoOutput(true);

                // 設定TimeOut時間
                connection.setReadTimeout(15*1000);
                connection.connect();

                // 伺服器回來的參數
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                connection.disconnect();
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

    //HTTPPUT
    //Parameter in string array : [0] : api, [1] : parameter sended to db
    class MyTaskPut extends AsyncTask<String,Void,String>{

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            URL url;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String urlStr = arg0[0];
            String para = arg0[1];

            try {
                url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();

                //連線方式
                urlConnection.setRequestMethod("PUT");

                //設置輸出入流串
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //POST方法不能緩存數據,需手動設置使用緩存的值為false
                urlConnection.setUseCaches(false);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                writer.write(para);

                //flush the data in buffer to server and close the writer
                writer.flush();
                writer.close();

                //read response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();
                String line ;

                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();

            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

    //show an alert dialog
    void Alert(String mes){
        new AlertDialog.Builder(getActivity())
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
