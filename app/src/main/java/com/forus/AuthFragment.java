package com.forus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AuthFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    SignInButton btnSignIn;
    Button  btnSignOut;
    Button  btnProfile;
    TextView tvStatus;
    TextView tvDisplayName;
    TextView tvEmail;
    TextView tvUid;

    private static GoogleApiClient mGoogleApiClient;
    private static final String TAG = "ForUs AuthFragment";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private DatabaseReference MembersRef;
    private Query queryRef;
    private String AuthUid;
    private String AuthDisplayName;
    private String AuthEmail;
    private String AuthPhotoURL;

    private ImageView ivPhotoURL;
    private boolean firstSignIn=false;

    public AuthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auth, container, false);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        btnSignIn = (SignInButton) v.findViewById(R.id.btnSignIn);
        btnSignOut = (Button) v.findViewById(R.id.btnSignOut);
        btnProfile = (Button) v.findViewById(R.id.btnProfile);
        tvStatus = (TextView) v.findViewById(R.id.tvStatus);
        tvDisplayName = (TextView) v.findViewById(R.id.tvDisplayName);
        tvEmail = (TextView) v.findViewById(R.id.tvEmail);
        tvUid = (TextView) v.findViewById(R.id.tvUid);
        ivPhotoURL = (ImageView) v.findViewById(R.id.ivPhotoURL) ;

        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnProfile.setOnClickListener(this);

        // Auth
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        Log.d(TAG, "it is authenticate [SIgn IN]");
                        Log.d(TAG, "onAuthStateChanged getUid:" + user.getUid());
                        Log.d(TAG, "onAuthStateChanged getDisplayName:" + user.getDisplayName());
                        Log.d(TAG, "onAuthStateChanged getEmail:" + user.getEmail());
                        Log.d(TAG, "onAuthStateChanged getProviders:" + user.getProviders());
                        Log.d(TAG, "onAuthStateChanged getPhotoUrl:" + user.getPhotoUrl().toString());

                        AuthUid = user.getUid();
                        AuthDisplayName = user.getDisplayName();
                        AuthEmail = user.getEmail();
                        AuthPhotoURL = user.getPhotoUrl().toString();

                        tvStatus.setText("Status: Sign In");
                        tvDisplayName.setText("DisplayName: " + AuthDisplayName);
                        tvEmail.setText("Email: " + AuthEmail);
                        tvUid.setText("Uid: " + AuthUid    );

                        // User's Photo
                        Picasso.with(getActivity())
                                .load( AuthPhotoURL )
                                //.error(R.drawable.error)
                                //.placeholder(R.drawable.placeholder)
                                .resize(100, 100)
                                .centerCrop()
                                .into(ivPhotoURL);

                        // Sign UP 이후 첫 설정값
                        MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");
                        queryRef = MembersRef.child(AuthUid);    // key
                        queryRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // TODO: handle the case where the data already exists
                                }
                                else {
                                    // TODO: handle the case where the data does not yet exist
                                    if (firstSignIn) {
                                        Log.d(TAG, "Sign IN 이후 첫 설정, UID 조회후 없을때 설정함. AuthUid[" + AuthUid + "] ");
                                        MembersRef.child(AuthUid).child("DisplayName").setValue(AuthDisplayName); // 1. DisplayName
                                        MembersRef.child(AuthUid).child("Email").setValue(AuthEmail);             // 2. Email
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        String today = sdf.format(new Date());
                                        MembersRef.child(AuthUid).child("CreateDate").setValue(today);            // 3. CreateDate
                                        MembersRef.child(AuthUid).child("AuthPhotoURL").setValue(AuthPhotoURL);   // 4. PhotoUrl
                                        MembersRef.child(AuthUid).child("NickName").setValue(AuthDisplayName);    // 5. NIckName 이 없으면 DisplayName 으로 지정해 준다.
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "Failed to read value.", databaseError.toException());
                            }
                        });

                        updateUI(true);
                    } else {
                        // User is signed out
                        Log.d(TAG, "it is not authenticate...");

                        tvStatus.setText("Status:");
                        tvDisplayName.setText("DisplayName: ");
                        tvEmail.setText("Email: ");
                        tvUid.setText("Uid: ");

                        ivPhotoURL.setImageResource(android.R.drawable.ic_menu_more);
                        updateUI(false);
                    }
                }
            }
        );


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSignIn:
                signIn();
                break;
            case R.id.btnSignOut:
                signOut();
                break;
            case R.id.btnProfile:

                Fragment fr = new ProfileFragment();
                Bundle args = new Bundle();
                args.putString("param1", AuthUid);
                args.putString("param2", "");
                fr.setArguments(args);

                // Create new fragment and transaction
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fr);
                transaction.addToBackStack(null);                  // and add the transaction to the back stack
                transaction.commit();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // return from GoogleSignInApi.getSignInIntent
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult result.isSuccess : " + result.isSuccess());
            Log.d(TAG, "resultCode["+resultCode+"]");
            if (result.isSuccess()) {
                Log.d(TAG, "firstSignIn = true 설정 ");
                firstSignIn = true;
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                handleSignInResult(account);
                Log.d(TAG, "Result Success");
                updateUI(true);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(false);
                // [END_EXCLUDE]
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                //tvStatus.setText("Sign Out");
                updateUI(false);
//                // 멤버 삭제  -- 삭제하지 말자.
//                MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");
//                MembersRef.child(AuthUid).removeValue();
            }
        });
    }

    private void updateUI(boolean AuthFlag) {
        if (AuthFlag) {
            btnSignIn.setEnabled(false);
            btnSignOut.setEnabled(true);
            btnProfile.setEnabled(true);
        } else {
            btnSignIn.setEnabled(true);
            btnSignOut.setEnabled(false);
            btnProfile.setEnabled(false);
        }
    }

    private void handleSignInResult(GoogleSignInAccount acct) {
        Log.d(TAG, "handleSignInResult acct.getId:" + acct.getId());
        Log.d(TAG, "handleSignInResult acct.getDisplayName:" + acct.getDisplayName());
        Log.d(TAG, "handleSignInResult acct.getEmail:" + acct.getEmail());

        // 사용자 재인증하기 리스너 설정
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "handleSignInResult signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "handleSignInResult signInWithCredential", task.getException());
//                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}
