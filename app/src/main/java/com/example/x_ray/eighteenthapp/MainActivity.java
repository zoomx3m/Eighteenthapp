package com.example.x_ray.eighteenthapp;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Random;

public class MainActivity extends AbstractMainActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String DATA_FRAGMENT_TAG = "data fragment";

    private GoogleMap mMap;
    private Marker mLondonMarker, mMyMarker;
    private Polyline mPolyline;
    private List<Marker> listOfMarkers;
    private int numberOfMarkers;
    private Location currentLocation;
    private Random random;
    private Circle circle;
    private Polygon polygon;
    private Polyline polyline;
    private boolean needsFocus = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isGoogleMapsAvailable()) {
            setContentView(R.layout.activity_main);
            commitDataFragment();
            Log.v(TAG, Keys.getCertificateSHA1Fingerprint(MainActivity.this));

            ((MapFragment) getFragmentManager().findFragmentById(R.id.google_map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap _googleMap) {
                    mMap = _googleMap;
                    initMap();
                    restoreState();
                }
            });
        } else {
            setContentView(R.layout.activity_abstract_map);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        clearMarkers();
        listOfMarkers.clear();
        return true;
    }

    private void clearMarkers() {
        mMap.clear();
        numberOfMarkers = 0;
    }


    private void initMap() {
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
//        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
//        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        MapListeners listeners = new MapListeners();
        mMap.setOnMapLongClickListener(listeners);
        mMap.setOnMapClickListener(listeners);
        mMap.setOnMarkerDragListener(listeners);
        random = new Random(32);
        DataFragment dataFragment = getDataFragment();
        listOfMarkers = dataFragment.getListOfMarkers();

    }

    private void focusOnCurrentLocation() {
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
    }

    private Marker createMarker(LatLng latLng) {
        float color = (float) random.nextInt(360);
        String lat = String.format("lat: %.2f", latLng.latitude);
        String lng = String.format("lng: %.2f", latLng.longitude);
        numberOfMarkers++;
        return mMap.addMarker(new MarkerOptions().title(lat + "/" + lng)
                .snippet("Marker # " + numberOfMarkers)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .position(latLng));
    }

    private void drawLine() {

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(5)
                .color(Color.RED)
                .geodesic(true);
        for (int i = 0; i < listOfMarkers.size(); i++) {
            polylineOptions.add(listOfMarkers.get(i).getPosition());
        }
        polyline = mMap.addPolyline(polylineOptions);
    }

    private void drawCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .strokeWidth(5)
                .strokeColor(Color.GRAY)
                .fillColor(0x4500ffee)
                .radius(200);
        circle = mMap.addCircle(circleOptions);
    }

    private void drawPolygon() {
        PolygonOptions rectOptions = new PolygonOptions();
        for (int i = 0; i < listOfMarkers.size(); i++) {
            rectOptions.add(listOfMarkers.get(i).getPosition());
        }
        rectOptions
                .strokeColor(Color.BLACK)
                .strokeWidth(5)
                .fillColor(0x4522ffcc);
        polygon = mMap.addPolygon(rectOptions);
    }

    private void addMarkerWithPos(Marker marker, int pos) {
        listOfMarkers.set(pos, createMarker(marker.getPosition()));
        listOfMarkers.remove(marker);
    }

    private void addMarkerForRestore(LatLng latLng) {
        createMarker(latLng);
    }

    private class MapListeners implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener,
            GoogleMap.OnMapClickListener {

        @Override
        public void onMapClick(LatLng latLng) {
            if (numberOfMarkers == 0) {
                listOfMarkers.add(createMarker(latLng));
                drawCircle(latLng);
            } else if (numberOfMarkers == 1) {
                circle.remove();
                listOfMarkers.add(createMarker(latLng));
            } else if (numberOfMarkers > 1 && numberOfMarkers < 4) {
                if (polygon != null)
                    polygon.remove();
                listOfMarkers.add(createMarker(latLng));
                drawPolygon();
            } else if (numberOfMarkers >= 4) {
                if (polygon != null)
                    polygon.remove();
                if (polyline != null)
                    polyline.remove();
                listOfMarkers.add(createMarker(latLng));
                drawLine();
            }

        }

        @Override
        public void onMapLongClick(LatLng latLng) {

        }

        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {

            if (numberOfMarkers > 2 && numberOfMarkers <= 4) {
                if (polygon != null)
                    polygon.remove();
                drawPolygon();
            } else if (numberOfMarkers > 4) {
                if (polyline != null)
                    polyline.remove();
                drawLine();
            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            currentLocation = location;
            if (needsFocus) {
                focusOnCurrentLocation();
                needsFocus = false;
            }
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

    private void commitDataFragment() {
        if (getDataFragment() == null) {
            DataFragment headlessFragment = new DataFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(headlessFragment, DATA_FRAGMENT_TAG)
                    .commit();
        }
    }

    public DataFragment getDataFragment() {
        return (DataFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAGMENT_TAG);
    }


    private void restoreState() {
        if (listOfMarkers.size() == 1) {
            addMarkerForRestore(listOfMarkers.get(0).getPosition());
            drawCircle(listOfMarkers.get(0).getPosition());
        }
        if (listOfMarkers.size() == 2) {
            addMarkerForRestore(listOfMarkers.get(0).getPosition());
            addMarkerForRestore(listOfMarkers.get(1).getPosition());
        }
        if (listOfMarkers.size() > 2 && listOfMarkers.size() < 5) {
            for (int i = 0; i < listOfMarkers.size(); i++) {
                addMarkerWithPos(listOfMarkers.get(i), i);
            }
            drawPolygon();
        }
        if (listOfMarkers.size() >= 5) {
            for (int i = 0; i < listOfMarkers.size(); i++) {
                addMarkerWithPos(listOfMarkers.get(i), i);
            }
            drawLine();
        }

    }

}