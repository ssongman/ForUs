package com.forus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";   // AuthUid
    private static final String ARG_PARAM2 = "param2";   //

    private static final String TAG = "ForUs ProfileFragment";
    private DatabaseReference MembersRef;
    private Query queryRef;

    private EditText etEmail      ;
    private EditText etDisplayName;
    private EditText etNickName   ;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String AuthUid;
    private String NickName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AuthUid = getArguments().getString(ARG_PARAM1);  // AuthUid
            //mParam2 = getArguments().getString(ARG_PARAM2);  // arMeeting
            Log.d(TAG, "mParam1[" + AuthUid + "]  ");
        } else {
            AuthUid = ((MainActivity)getActivity()).getAuthUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        etEmail       = (EditText) v.findViewById(R.id.etEmail      );
        etDisplayName = (EditText) v.findViewById(R.id.etDisplayName);
        etNickName    = (EditText) v.findViewById(R.id.etNickName   );
        Button btnSave = (Button) v.findViewById(R.id.btnSave);

        btnSave.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                NickName = etNickName.getText().toString();
                Log.d(TAG, "[btnSave setOnClickListener] Uid : " + AuthUid + "  NickName ["+NickName+"]");
                MembersRef.child(AuthUid).child("NickName").setValue(NickName);
                //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Refresh Meeting from DB
        MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");

        // Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    AuthUid = user.getUid();
                    Log.d(TAG, "[onAuthStateChanged] Uid : " + AuthUid);

                    // DB listener
                    queryRef = MembersRef.child(AuthUid);    // key
                    queryRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            //Getting the data from snapshot
                            Member member = snapshot.getValue(Member.class);
                            etEmail      .setText(member.getEmail      ());
                            etDisplayName.setText(member.getDisplayName());
                            etNickName   .setText(member.getNickName   ());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "Failed to read value.", databaseError.toException());
                        }
                    });

                    Log.d(TAG, "onAuthStateChanged signed_in:" + user.getUid());
                    Log.d(TAG, "onAuthStateChanged getDisplayName:" + user.getDisplayName());
                    Log.d(TAG, "onAuthStateChanged getEmail:" + user.getEmail());
                    Log.d(TAG, "onAuthStateChanged getId:" + user.getProviders());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged signed_out");
                }
            }
        };


        // DB listener
        queryRef = MembersRef.child(AuthUid);    // key
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                Member member = snapshot.getValue(Member.class);
                etEmail      .setText(member.getEmail      ());
                etDisplayName.setText(member.getDisplayName());
                etNickName   .setText(member.getNickName   ());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });


        return v;
    }

}
