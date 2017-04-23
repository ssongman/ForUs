package com.forus;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private GoogleMap mMap;

    private static final String TAG = "ForUs MapsActivity";
    private DatabaseReference MeetingRef;
    private Query queryRef;
    private String MeetingKey;
    private MtMember mtMember;
    private float[] marker_color = new float[] {
            BitmapDescriptorFactory.HUE_AZURE  ,
            BitmapDescriptorFactory.HUE_BLUE   ,
            BitmapDescriptorFactory.HUE_CYAN   ,
            BitmapDescriptorFactory.HUE_GREEN  ,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ORANGE ,
            BitmapDescriptorFactory.HUE_RED    ,
            BitmapDescriptorFactory.HUE_ROSE   ,
            BitmapDescriptorFactory.HUE_VIOLET ,
            BitmapDescriptorFactory.HUE_YELLOW
    };

    private SlidingDrawer slidingDrawer;
    private Context context;
    private Button btnClickMe, btnHandle;
    private TextView tvText1;
    private CheckBox cbWakeLock;
    private PowerManager mPm;
    private PowerManager.WakeLock mWakeLock;
    private boolean mbAlwaysBright = false;

    private ArrayList<MtMember> arMtMembers;
    private ArrayList<String> arMtMemberKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        MeetingKey = intent.getStringExtra("MeetingKey");
        if (MeetingKey != null ){
            //mEdit.setText(text);
        }

        context = this.getApplicationContext();
        btnHandle = (Button) findViewById(R.id.btnHandle);
        btnClickMe = (Button) findViewById(R.id.btnClickMe);
        tvText1 = (TextView) findViewById(R.id.tvText1);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        cbWakeLock = (CheckBox) findViewById(R.id.cbWakeLock);
        mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeAlways");


        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                btnHandle.setText("-");
                tvText1.setText("Aleady dragged...");
            }
        });

        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                btnHandle.setText("+");
                tvText1.setText("For more info drag the button..");
            }
        });

        btnClickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "The button has been clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // 화면꺼짐방지
        cbWakeLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.getId() == R.id.cbWakeLock) {
                    if (b) {
                        // isChecked
                        mbAlwaysBright = true;
                        mWakeLock.acquire();
                    } else {
                        mbAlwaysBright = false;
                        if (mWakeLock.isHeld()) {
                            mWakeLock.release();
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면꺼짐방지
        if (mbAlwaysBright) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 화면꺼짐방지
        if (mbAlwaysBright) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public void mOnClick(View v) {
        int i = 0;
        for (Marker marker : mMarkerArray) {
            marker.showInfoWindow();
            if (i == 1) {
                marker.setVisible(true);
                marker.setPosition(new LatLng(37.518550, 126.940200));
            }
            i++;
            //marker.remove(); <-- works too!
        }
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
        // Set a preference for minimum and maximum zoom.
//        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(18.0f);

        // 위치정보 가져오기 from DB
        MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");
        queryRef = MeetingRef.child(MeetingKey).child("MtMembers").orderByChild("PartYN").equalTo("Y");
        //queryRef = MeetingRef.limitToFirst(10);    // 일단 10개만 읽는다.
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Marker marker;
                double mlatitude  = 0.0;
                double mlongitude = 0.0;
                double mlatitude_new  = 0.0;
                double mlongitude_new = 0.0;
                // 위경도 평균치 구하기 위한 변수
                ArrayList<Double> mlatitude_list =  new ArrayList<Double>();
                ArrayList<Double> mlongitude_list =  new ArrayList<Double>();
                ArrayList<LatLng> latlng_list =  new ArrayList<LatLng>();
                LatLng latlng;

                String mAuthUid;
                int mcnt=0;

                mMarkerArray.clear();
                mMap.clear();
                LatLngBounds.Builder b = new LatLngBounds.Builder();

                arMtMembers = new ArrayList<MtMember>();
                arMtMemberKeys = new ArrayList<String>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    mtMember = postSnapshot.getValue(MtMember.class);
                    arMtMembers.add(mtMember);
                    arMtMemberKeys.add(postSnapshot.getKey());

                    mAuthUid = postSnapshot.getKey();
                    mlatitude  = mtMember.getLatitude();   // 위도
                    mlongitude = mtMember.getLongitude();  // 경도
                    Log.d(TAG, "Uid["+mAuthUid+"], 닉네임["+mtMember.getNickName().toString()+"],  위도["+mlatitude+"],  경도["+mlongitude+"]");

                    if ( mlatitude == 0 ) {
                        // 위도,경도정보가 없으면 지도에 marker 하지 않음
                    } else {
                        // 위경도 평균치 구하기 위한 변수
                        mlatitude_list.add(mlatitude);
                        mlongitude_list.add(mlongitude);
                        latlng = new LatLng(mlatitude, mlongitude);
                        latlng_list.add(latlng);   // 위경도를 보관하자. 비록 지금은 활용도가 없지만 나중에 사용하자.

                        marker = mMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title(mtMember.getNickName().toString())
                            .snippet(mtMember.getNickName().toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(marker_color[mcnt%10])));
                        mMarkerArray.add(marker);
                        Log.d(TAG, "Marker added 닉네임["+mtMember.getNickName().toString()+"]");
                        mcnt++;

                        b.include( marker.getPosition() );
                    }
                }
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.getUiSettings().setZoomControlsEnabled(true);

                if ( mcnt > 0 ) {
                    // build the LatLngBounds object
                    LatLngBounds bounds = b.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));  // padding : 200
                }

//                // 위경도의 평균치를 구해서 화면을 이동.
//                mlatitude_new = average(mlatitude_list);
//                mlongitude_new = average(mlongitude_list);
//                LatLng location_markers_aver = new LatLng(mlatitude_new, mlongitude_new);
//                mMap.moveCamera(CameraUpdateFactory.newLatLng( location_markers_aver ));
////                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( location_markers_aver, 15));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });


        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(building63, 13));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(building63));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.519576, 126.940245), 16));

//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        googleMap.addMarker(new MarkerOptions()
//                .position(latLng)
//                .title("My Spot")
//                .snippet("This is my spot!")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        googleMap.getUiSettings().setCompassEnabled(true);
//        googleMap.getUiSettings().setZoomControlsEnabled(true);
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

//        // 두지점 거리구하기
//        float[] results = new float[1];
//        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
    }

    public double average(ArrayList<Double> array) {
        double sum = 0;
        for(int i=0; i < array.size(); i++)
            sum = sum + array.get(i) ;

        return sum / array.size();
    }
}
