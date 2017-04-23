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
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";   // AuthUid
    private static final String ARG_PARAM2 = "param2";   //

    private static final String TAG = "ForUs ProfileFragment";
    private DatabaseReference MembersRef;
    private Query queryRef;

    private EditText etEmail      ;
    private EditText etDisplayName;
    private EditText etNickName   ;
    private ImageView ivUserPhoto;

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
//        if (getArguments() != null) {
//            AuthUid = getArguments().getString(ARG_PARAM1);  // AuthUid
//            //mParam2 = getArguments().getString(ARG_PARAM2);  // arMeeting
//            Log.d(TAG, "mParam1[" + AuthUid + "]  ");
//        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            AuthUid = user.getUid();
        } else {
            // No user is signed in
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
        ivUserPhoto = (ImageView) v.findViewById((R.id.ivUserPhoto));
        Button btnSave = (Button) v.findViewById(R.id.btnSave);

        btnSave.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                NickName = etNickName.getText().toString();
                Log.d(TAG, "[btnSave setOnClickListener] Uid : " + AuthUid + "  NickName ["+NickName+"]");
                // 멤버정보 DB - NickName
                MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");
                MembersRef.child(AuthUid).child("NickName").setValue(NickName);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Auth1
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            AuthUid = user.getUid();
            etEmail      .setText(user.getEmail());
            etDisplayName.setText(user.getDisplayName());

            Member member = ((MainActivity)getActivity()).getMember();
            etNickName   .setText(member.getNickName());

            // User's Photo
            Picasso.with(getActivity())
                    .load( user.getPhotoUrl().toString() )
                    //.error(R.drawable.error)
                    //.placeholder(R.drawable.placeholder)
                    .resize(100, 100)
                    .centerCrop()
                    .into(ivUserPhoto);
        } else {
            // No user is signed in
            Log.d(TAG, "onAuthStateChanged signed_out");
//            ivUserPhoto.setImageResource(android.R.drawable.ic_menu_more);
            ivUserPhoto.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        return v;
    }

}
