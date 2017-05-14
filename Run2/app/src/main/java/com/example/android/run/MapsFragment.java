package com.example.android.run;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MapsFragment extends Fragment
{
    //show text content
    private String text = "";

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        //access TabActivity and put text content into text
        TabActivity mTabActivity = (TabActivity) activity;
        text = mTabActivity.getMapsText();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                setBoundary(googleMap);

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);


                // For dropping a marker at a point on the Map
                LatLng place = new LatLng(24.7947, 120.9932);

                // Create marker
                MarkerOptions marker = new MarkerOptions().position(place).title("Mission 1");
                // Changing marker icon
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mission_marker));
                // add marker
                googleMap.addMarker(marker);
                //googleMap.addMarker(new MarkerOptions().position(place).title("Mission"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(place).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return rootView;
    }

    void setBoundary(GoogleMap mMap){
        PolygonOptions polygonOpt = new PolygonOptions();

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

    /*@Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }*/

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
}
