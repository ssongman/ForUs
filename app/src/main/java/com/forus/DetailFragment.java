package com.forus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";   // arMeetingKey
    private static final String ARG_PARAM2 = "param2";   // arMeeting

    // TODO: Rename and change types of parameters
    private String MeetingKey;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "ForUs DetailFragment";
    private DatabaseReference MeetingRef;
    private Query queryRef;

    public ArrayList<String> arMtMembers;
    public ArrayList<String> arMtMemberKeys;
    public ArrayAdapter<String> arAdapter;
    public ListView lvMtMembers;

    // Class
    MtMember mtMember  ;
    String AuthUid     ;

    EditText etMeetName;
    EditText etMeetDate;
    EditText etMeetTime;
    EditText etMeetDesc;


    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
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
            MeetingKey = getArguments().getString(ARG_PARAM1);  // arMeetingKey
            mParam2 = getArguments().getString(ARG_PARAM2);  // arMeeting
            Log.d(TAG,"mParam1["+MeetingKey+"]  mParam2["+mParam2+"]");
        }
        AuthUid = ((MainActivity)getActivity()).getAuthUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_detail, container, false);


        // Refresh Meeting from DB
        MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");

        etMeetName    = (EditText) v.findViewById(R.id.etMeetName   );
        etMeetDate    = (EditText) v.findViewById(R.id.etMeetDate   );
        etMeetTime    = (EditText) v.findViewById(R.id.etMeetTime   );
        etMeetDesc    = (EditText) v.findViewById(R.id.etMeetDesc   );

        Button btnLocaStart = (Button) v.findViewById(R.id.btnLocaStart);
        Button btnLocaEnd   = (Button) v.findViewById(R.id.btnLocaEnd  );
        Button btnWatchMap  = (Button) v.findViewById(R.id.btnWatchMap );
        Button btnMeetExit  = (Button) v.findViewById(R.id.btnMeetExit );
        Button btnNotify    = (Button) v.findViewById(R.id.btnNotify   );

        btnLocaStart.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"btnLocaStart");
                Intent intent = new Intent(getActivity(), GPSService.class);
                intent.putExtra("MeetingKey", MeetingKey);
                intent.putExtra("AuthUid", AuthUid);
                getActivity().startService(intent);
            }
        });
        btnLocaEnd.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"btnLocaEnd");
                Intent intent = new Intent(getActivity(), GPSService.class);
                boolean result = getActivity().stopService(intent);
                Log.d(TAG, "stop result : " + result);
            }
        });
        btnWatchMap.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                // Log.d(TAG,"btnWatchMap");
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("MeetingKey", MeetingKey);
                startActivity(intent);

            }
        });
        // 모임 나가기
        btnMeetExit.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "모임나기기버튼");

                new AlertDialog.Builder(getActivity())
                    .setTitle("질문")
                    .setMessage("모임에서 나가시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MeetingRef.child(MeetingKey).child("MtMembers").child(AuthUid).child("PartYN").setValue( "N" );
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
            }
        });
        btnNotify.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                // Log.d(TAG,"btnNotify");
                // Notify
                // update Notify_yn
                MeetingRef.child(MeetingKey).child("MtNotify_yn").setValue("Y");
            }
        });

        // 모임정보 가져오기
        queryRef = MeetingRef.child(MeetingKey);    // key
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                Meeting meeting = snapshot.getValue(Meeting.class);
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

        // 참여자 목록 가져오기
        arMtMembers = new ArrayList<String>();
        arMtMemberKeys = new ArrayList<String>();
        //arMtMembers.add("김유신");
        //arMtMembers.add("이순신");
        arAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arMtMembers);
//      arAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, arMtMembers);
        lvMtMembers = (ListView) v.findViewById(R.id.lvMtMembers);
        lvMtMembers.setAdapter(arAdapter);
        //lvMtMembers.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        queryRef = MeetingRef.child(MeetingKey).child("MtMembers").orderByChild("PartYN").equalTo("Y");
        //queryRef = MeetingRef.limitToFirst(10);    // 일단 10개만 읽는다.
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                arMtMembers.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    mtMember = postSnapshot.getValue(MtMember.class);
                    arMtMembers.add(  mtMember.getNickName().toString()   );
                    arMtMemberKeys.add(postSnapshot.getKey());
                }
                arAdapter.notifyDataSetChanged();
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

    private void AlertMessage(String string)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("알림");
        builder.setMessage(string);
        builder.setPositiveButton("확인", null);
        builder.show();
    }
}
