package com.forus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GPSService extends Service {
    private final String TAG="ForUs GPSService";
    public LocationManager mLocMan;
    String mProvider;

    int    mCount = 0;
    double mLatitude = 0.0;
    double mLongitude = 0.0;
    double mAltitude = 0.0;

    boolean mQuit;
    String AuthUid     ;
    String MeetingKey  ;

    private DatabaseReference MeetingRef;

    public GPSService() {
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocMan.getBestProvider(new Criteria(), true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "위치공유 서비스 종료", Toast.LENGTH_SHORT).show();
        mQuit=true;

        if ( // Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "현재상태: Location 사용불가");
            return  ;
        }
        mLocMan.removeUpdates(mListener);
        Log.d(TAG, "현재상태: 서비스정지");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        MeetingKey = intent.getStringExtra("MeetingKey");
        AuthUid = intent.getStringExtra("AuthUid");

        if ( // Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, String.format("현재상태: Location 사용불가, Build.VERSION.SDK_INT[%d]", Build.VERSION.SDK_INT) );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 퍼미션 요청
                // ActivityCompat.requestPermissions( this , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number can be used
            }
            //return START_STICKY;
        }

        MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");
        mCount = 0;
        mLocMan.requestLocationUpdates(mProvider, 5000, 10, mListener);  // 5초, 10m 이상 움직임일때 mListener 요청
        Log.d(TAG, "위치공유 서비스 시작" );
        Toast.makeText(this, "위치공유  서비스 시작" , Toast.LENGTH_SHORT).show();

        mQuit = false;
//        NewsThread thread = new NewsThread(this, mHandler);
//        thread.start();
        return START_STICKY;
    }


    LocationListener mListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mCount++;
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mAltitude = location.getAltitude();

            String sloc = String.format("수신회수: %d\n위도: %f\n경도: %f\n고도: %f", mCount, mLatitude, mLongitude, mAltitude );
            Toast.makeText(getApplicationContext(), sloc , Toast.LENGTH_SHORT).show();
            Log.d(TAG, sloc);

            MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("Latitude").setValue( mLatitude ); // 위도
            MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("Longitude").setValue( mLongitude ); // 경도
        }

        public void onProviderDisabled(String provider) {
            Log.d(TAG, "현재상태: 서비스사용불가,  GPS 시작 창 열기");
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG, "현재상태: 서비스사용가능");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            String sStatus = "";
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                    sStatus="범위벗어남";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    sStatus="일시적 불능";
                    break;
                case LocationProvider.AVAILABLE:
                    sStatus="사용가능";
                    break;
            }
            Log.d(TAG, provider + "상태변경 : " + sStatus);
        }
    };

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch(requestCode) {
//            case MY_PERMISSION_REQUEST_FINE_LOCATION:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission granted
//                    permissionIsGranted = true;
//                } else {
//                    // permission denied
//                    permissionIsGranted = false;
//                    Toast.makeText(getApplicationContext(), "This app requires location permission to be granted", Toast.LENGTH_SHORT).show();
////                    mStatus.setText("permission not granted");
//                    Log.d(TAG, "permission not granted");
//                }
//                break;
//            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
//                break;
//        }
//    }

//    class NewsThread extends Thread {
//        GPSService mParent;
//        Handler mHandler;
//        String[] arNews = {
//                "뉴스1",
//                "뉴스2",
//                "뉴스3",
//                "뉴스4",
//                "뉴스5"
//        };
//        public NewsThread(GPSService parent, Handler handler) {
//            mParent = parent;
//            mHandler = handler;
//        }
//        public void run() {
//            for (int idx = 0; mQuit == false ; idx++) {
//                Message msg = new Message();
//                msg.what = 0;
//                msg.obj = arNews[idx % arNews.length];
//                mHandler.sendMessage(msg);
//                try {
//                    Thread.sleep(5000);
//                } catch(Exception e) {
//                    Log.d(TAG, "sleep error");
//                }
//            }
//        }
//    }
//
//    Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            if (msg.what == 0){
//                String news = (String) msg.obj;
//                Toast.makeText(GPSService.this, "AuthUid ["+AuthUid+"],   new [" + news + "]", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
}
