package com.forus;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ForUs MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference MembersRef;
    private Query queryRef;
    private boolean firstTime = true;
    private String MeetingKeyFromKakao;
    private boolean SharedFromKakaoYN = false;
    private boolean isFirebaseCalledOnce = false;

    // Class declear
    Member member  ;

    public String AuthUid;
//    public String AuthNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // MeetingKeyFromKakao = "";
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if(uri != null) {
                MeetingKeyFromKakao = uri.getQueryParameter("MeetingKey");
                Log.d(TAG, "MeetingKeyFromKakao["+MeetingKeyFromKakao+"]");

                if (MeetingKeyFromKakao != null && !MeetingKeyFromKakao.equals("") ) {
                    // kakao 에서 호출되었으므로 바로 참여창으로 넘기자.
                    SharedFromKakaoYN = true;
                }
            }
        }

        // Auth
        mAuth = FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    // AuthState 가 여러번 수행되어서 이렇게 변수하나 사용하여 한번만 수행되도록 수정한다.
                    if (!isFirebaseCalledOnce) {
                        isFirebaseCalledOnce = true;
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // User is signed in
                            Log.d(TAG, "it is authenticate [SIgn IN]");
                            Log.d(TAG, "onAuthStateChanged getUid:" + user.getUid());
                            Log.d(TAG, "onAuthStateChanged getDisplayName:" + user.getDisplayName());
                            Log.d(TAG, "onAuthStateChanged getEmail:" + user.getEmail());
                            Log.d(TAG, "onAuthStateChanged getProviders:" + user.getProviders());

                            AuthUid = user.getUid();

                            // 멤버정보 가져오기 from DB - NickName
                            MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");
                            queryRef = MembersRef.child(AuthUid);    // key
                            queryRef.addValueEventListener(new ValueEventListener() {   // 여기서는 singleEvent 사용하지 않고 addValueEventListener 사용한다. 왜냐고? 멤버정보가 변경될때마다가 member class를 갱신해주기 위해
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    //Getting the data from snapshot
                                    member = snapshot.getValue(Member.class);   // 개인정보를 member class 에 담는다.
                                    // member.getUid()
                                    // member.getEmail()
                                    // member.getDisplayName()
                                    // member.getNickName()
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                                }
                            });

                            if (SharedFromKakaoYN) {
                                // kakao 앱에서 호출되었으므로 바로 참여창으로 넘기자.
                                Fragment fr = new PartFragment();
                                Bundle args = new Bundle();
                                args.putString("param1", MeetingKeyFromKakao);
                                fr.setArguments(args);
                                getSupportFragmentManager().beginTransaction().
                                        add(R.id.fragment_container, fr, "Part").commit();

                                firstTime = false;
                            } else {
                                if (firstTime) {   // 앱실행후 처음일때만 수행함.  왜냐고? onAuthStateChanged callback 함수의 특성상 권한이 변경될때마다 실행되기 때문.
                                    // 처음으로 접속시 AuthFragment 로
                                    getSupportFragmentManager().beginTransaction().
                                            add(R.id.fragment_container, new ListFragment(), "List").commit();
                                    firstTime = false;
                                }
                            }


                        } else {
                            // User is signed out
                            Log.d(TAG, "it is not authenticate...");
                            //                    ivPhotoURL.setImageResource(android.R.drawable.ic_menu_more);

                            if (firstTime) {   // 처음일때만 수행함.
                                // 처음으로 접속시 AuthFragment 로
                                getSupportFragmentManager().beginTransaction().
                                        add(R.id.fragment_container, new AuthFragment(), "Auth").commit();
                                firstTime = false;
                            }
                        }
                    }
                }
            }
        );

//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    // User is signed in
//                    Log.d(TAG, "it is authenticate [SIgn IN]");
//                    Log.d(TAG, "onAuthStateChanged getUid:" + user.getUid());
//                    Log.d(TAG, "onAuthStateChanged getDisplayName:" + user.getDisplayName());
//                    Log.d(TAG, "onAuthStateChanged getEmail:" + user.getEmail());
//                    Log.d(TAG, "onAuthStateChanged getProviders:" + user.getProviders());
//
//                    AuthUid = user.getUid();
//
//                    // 멤버정보 가져오기 from DB - NickName
//                    MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");
//                    queryRef = MembersRef.child(AuthUid);    // key
//                    queryRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot snapshot) {
//                            //Getting the data from snapshot
//                            member = snapshot.getValue(Member.class);   // 개인정보를 member class 에 담는다.
//                            // member.getUid()
//                            // member.getEmail()
//                            // member.getDisplayName()
//                            // member.getNickName()
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Log.w(TAG, "Failed to read value.", databaseError.toException());
//                        }
//                    });
//
//                    if (firstTime) {   // 처음일때만 수행함.
//                        // 처음으로 접속시 AuthFragment 로
//                        getSupportFragmentManager().beginTransaction().
//                                add(R.id.fragment_container, new ListFragment()).commit();
//                        firstTime = false;
//                    }
//
//                } else {
//                    // User is signed out
//                    Log.d(TAG, "it is not authenticate...");
////                    ivPhotoURL.setImageResource(android.R.drawable.ic_menu_more);
//
//                    // 처음으로 접속시 AuthFragment 로
//                    getSupportFragmentManager().beginTransaction().
//                            add(R.id.fragment_container, new AuthFragment()).commit();
//                }
//            }
//        };

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fr = null;
        String aTag = "";

        switch(item.getItemId()) {
            case R.id.mnAuth:
//                Toast.makeText(this, "mnAuth", Toast.LENGTH_SHORT).show();
                fr = new AuthFragment() ;
                aTag = "Auth";
                break;
            case R.id.mnProfile:
//                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                fr = new ProfileFragment() ;
                aTag = "Profile";
//                Bundle args = new Bundle();
//                args.putString("param1", AuthUid);
//                args.putString("param2", "");
//                fr.setArguments(args);
                break;
            case R.id.mnList:
//                Toast.makeText(this, "mnList", Toast.LENGTH_SHORT).show();
                fr = new ListFragment() ;
                aTag = "List";
                break;
            case R.id.mnCreate:
//                Toast.makeText(this, "mnCreate", Toast.LENGTH_SHORT).show();
                fr = new CreateFragment() ;
                aTag = "Create";
                break;
            case R.id.mnParticipation:
//                Toast.makeText(this, "mnParticipation", Toast.LENGTH_SHORT).show();
                fr = new PartFragment() ;
                aTag = "Part";
                break;
        }

//        getSupportFragmentManager().beginTransaction().
//                replace(R.id.fragment_container, fr).commit();

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        String currTag = currentFragment.getTag();
        if (currTag == null || !currTag.equals(aTag.toString())) {      // 자기 화면을 다시 call 하는 일을 없애기 위해서...
            // Create new fragment and transaction
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fr, aTag);
            transaction.addToBackStack(null);                  // and add the transaction to the back stack
            transaction.commit();
        }


        return false;
    }


    public String getAuthUid()
    {
        return AuthUid;
    }
    public String getAuthNickName()
    {
        return member.getNickName().toString();
    }
    public Member getMember()
    {
        return member;
    }

    public void setAuthUid(String authUid)
    {
        AuthUid = authUid;
    }
    public void setAuthNickName(String authNickName)
    {
        member.setNickName(authNickName);
    }
    public void setMember(Member amember)
    {
        member = amember;
    }

    private void AlertMessage(String string)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage(string);
        builder.setPositiveButton("확인", null);
        builder.show();
    }

}
