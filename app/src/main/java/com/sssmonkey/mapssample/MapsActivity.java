package com.sssmonkey.mapssample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {
    /** 地図の初期位置. */
    private static final LatLng DEFAULT_PIN_POSITION = new LatLng(35.691636, 139.701732);
    /** ズームレベル初期値. */
    private static final float DEFAULT_MAP_ZOOM = 11.0f;
    /** 円の半径初期値. */
    private static final Double DEFAULT_CIRCLE_RANGE = 5d * 1000;
    private static final Double CIRCLE_RADIUS_BY_WALK = 5 * 1000d;
    /** 範囲選択サークルの半径（15km）：自転車 */
    private static final Double CIRCLE_RADIUS_BY_BICYCLE = 15 * 1000d;
    /** 範囲選択サークルの半径（50km）：車 */
    private static final Double CIRCLE_RADIUS_BY_CAR = 50 * 1000d;
    private GoogleMap mMap;
    private Marker mMarker = null;
    private Circle mCircle = null;
    private Double mRange = CIRCLE_RADIUS_BY_CAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMarker.remove();
                mCircle.remove();
                mMarker = mMap.addMarker(new MarkerOptions().position(cameraPosition.target));
                mCircle = mMap.addCircle(createCircleOptions(cameraPosition.target, mRange));
            }
        });

        mMarker = mMap.addMarker(new MarkerOptions().position(DEFAULT_PIN_POSITION));
        mCircle = mMap.addCircle(createCircleOptions(DEFAULT_PIN_POSITION, DEFAULT_CIRCLE_RANGE));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_PIN_POSITION, DEFAULT_MAP_ZOOM));

        // 現在地情報取得ボタンの配置
        requestPermissions(PERMISSIONS_FOR_GET_LOCATION);
    }

    private static final String[] PERMISSIONS_FOR_GET_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void requestPermissionsSuccess() {
        Log.d(getPackageName(), "requestPermissionsSuccess: called child");
        try {
            mMap.setMyLocationEnabled(true);
            // 現在地情報取得
            getCurrentLocation();
        }catch(SecurityException e){
            // 使用上問題ないため握りつぶす
            Log.w(getPackageName(), e.getMessage());
        }
    }

    private void getCurrentLocation() throws SecurityException{
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = createCurrentLocationListener(this, locationManager);

        // NetworkもGPSもONの場合はハイブリッドで取得
        // LocationListenerは同じインスタンスなので片方取得できたら両方止まる
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000,
                    0,
                    locationListener
            );
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    locationListener
            );
        }
    }

    private static LocationListener createCurrentLocationListener(final Context context, final LocationManager locationManager){
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String msg = "Lat=" + location.getLatitude()
                        + "\nLng=" + location.getLongitude()
                        + "\nAccuracy=" + location.getAccuracy();
                Log.d("Network", msg);
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.JAPAN);
                    List<Address> list_address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
                    String adr = "";
                    StringBuffer adrbuf = new StringBuffer();
                    if (!list_address.isEmpty()) {
                        Address address = list_address.get(0);
                        StringBuffer strbuf = new StringBuffer();
                        //adressをStringへ
                        String buf;
                        for (int i = 0; (buf = address.getAddressLine(i)) != null; i++) {
                            strbuf.append("address.getAddressLine(" + i + "):" + buf + "\n");
                        }

                        adr = strbuf.toString();
                        adrbuf.append("[");
                        adrbuf.append(address.getAdminArea());
                        adrbuf.append(":" + address.getLocality());
                        adrbuf.append(":" + address.getSubLocality());
                        adrbuf.append("]");
                    }
                    Log.d("Address", adr);
                    Log.d("Address2", adrbuf.toString());
                    Toast.makeText(context, msg + adr + adrbuf.toString(), Toast.LENGTH_LONG).show();

                    locationManager.removeUpdates(this);
                }catch (IOException ie){
                    Log.d(context.getPackageName(), ie.getMessage());
                }catch (SecurityException se){
                    Log.d(context.getPackageName(), se.getMessage());
                    // TODO エラーハンドリング
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(context.getPackageName(), String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                // NOP
            }

            @Override
            public void onProviderDisabled(String provider) {
                // NOP
            }
        };
    }


    @Override
    protected void requestPermissionsFailed(String[] permissions, int[] grantResults){
        Log.d(getPackageName(), "requestPermissionsFailed: called child");
        // エラーハンドリング
        if(canShowAgainPermissions(this, permissions)){
            new AlertDialog.Builder(this)
                    .setTitle("パーミッション取得エラー")
                    .setMessage("位置情報の取得に失敗しました。")
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("パーミッション取得エラー")
                    .setMessage("位置情報の取得に失敗しました。\nご利用端末の設定＞アプリ＞権限から、位置情報をONにすることで現在地からの距離検索が可能です。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openSettings();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private static CircleOptions createCircleOptions(LatLng position, Double range){
        CircleOptions co = new CircleOptions();
        co.center(position);
        co.radius(range);
        co.fillColor(Color.parseColor("#3300FFCC")); // Circle内部の色
        co.strokeColor(Color.parseColor("#FF0000FF")); // Circle枠の色
        co.strokeWidth(2); // Circle枠の太さ
        return co;
    }
    /** 範囲選択サークルの半径（5km）：徒歩 */

    public void changeWalk(View v){
        changeDistanceRange(CIRCLE_RADIUS_BY_WALK, 11.5f);
    }
    public void changeBicycle(View v){
        changeDistanceRange(CIRCLE_RADIUS_BY_BICYCLE, 10.3f);
    }
    public void changeCar(View v){
        changeDistanceRange(CIRCLE_RADIUS_BY_CAR, 8.5f);
    }
    private void changeDistanceRange(Double range, float zoom){
        mRange = range;
        mCircle.remove();
        mCircle = mMap.addCircle(createCircleOptions(mMap.getCameraPosition().target, range));
        float zoomLevel = mMap.getCameraPosition().zoom;
        int diff = (int)Math.abs(zoomLevel-zoom);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), diff * 500, null);
    }
    public void doSearch(View v){
        double lat = mMap.getCameraPosition().target.latitude;
        double lon = mMap.getCameraPosition().target.longitude;
        Toast.makeText(getApplicationContext(), "検索実行[" + String.valueOf(lat) + "," + String.valueOf(lon) + "]から" + mRange / 1000 + "km圏内", Toast.LENGTH_LONG).show();
    }
}
