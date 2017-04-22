package com.forus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PartFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "ForUs DetailFragment";
    private DatabaseReference MeetingRef;
    private DatabaseReference MembersRef;
    private Query queryRef;

    // Class declear
    Meeting meeting;
    Member member  ;

    String AuthUid        ;
//    String AuthEmail      ;
//    String AuthDisplayName;
//    String AuthNickName   ;

    String MeetingKey;

    EditText etMeetName;
    EditText etMeetDate;
    EditText etMeetTime;
    EditText etMeetDesc;
    EditText etMeetPass;

    public PartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PartFragment newInstance(String param1, String param2) {
        PartFragment fragment = new PartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            MeetingKey = getArguments().getString(ARG_PARAM1);  // MeetingKey
            //mParam2 = getArguments().getString(ARG_PARAM2);  // Meeting
            Log.d(TAG,"MeetingKey["+MeetingKey+"]  ");
        }
        AuthUid = ((MainActivity)getActivity()).getAuthUid();
        Log.d(TAG,"[onCreate] AuthUid["+AuthUid+"]  ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_part, container, false);

        // Refresh Meeting from DB
        MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");
        MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");

        etMeetName    = (EditText) v.findViewById(R.id.etMeetName   );
        etMeetDate    = (EditText) v.findViewById(R.id.etMeetDate   );
        etMeetTime    = (EditText) v.findViewById(R.id.etMeetTime   );
        etMeetDesc    = (EditText) v.findViewById(R.id.etMeetDesc   );
        etMeetPass    = (EditText) v.findViewById(R.id.etMeetPass   );

        Button btnParticipation = (Button) v.findViewById(R.id.btnParticipation);
        Button btnCancel       = (Button) v.findViewById(R.id.btnCancel  );
        // 모임 참여하기
        btnParticipation.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                String MeetPass = etMeetPass.getText().toString();
                String MtPass = meeting.getMtPass().toString();
                Log.d(TAG,"Pass compare MeetPass["+MeetPass+"], MtPass["+MtPass+"]");

                if ( MeetPass.equals(MtPass) ) {

                    // 1. NickName
                    if (member.getNickName().toString().equals("") ) {
                        MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("NickName").setValue( member.getDisplayName().toString() ); // NickName 이 없으면 DisplayName 으로 한다.
                    } else {
                        MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("NickName").setValue( member.getNickName().toString() );   // NickName
                    }

                    // 2. PartYN
                    MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("PartYN").setValue( "Y" );

                    // 2. 참여수락 일자
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String today = sdf.format(new Date());
                    MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("ParticipationDate").setValue( today );

                    Toast.makeText(getActivity(), "모임에 참여하였습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "패스워드가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnCancel.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"btnCancel");
            }
        });

        // 모임정보 가져오기
        queryRef = MeetingRef.child(MeetingKey);    // key
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                meeting = snapshot.getValue(Meeting.class);
                etMeetName.setText(meeting.getMtName());
                etMeetDate.setText(meeting.getMtFrdt());
                etMeetTime.setText(meeting.getMtFrtm());
                etMeetDesc.setText(meeting.getMtDesc());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        // 개인정보 가져오기
        queryRef = MembersRef.child(AuthUid);    // key
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                member = snapshot.getValue(Member.class);
//                AuthEmail       = member.getEmail      ();
//                AuthDisplayName = member.getDisplayName();
//                AuthNickName    = member.getNickName   ();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });



        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}