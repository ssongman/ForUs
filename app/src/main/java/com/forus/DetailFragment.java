package com.forus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;
import com.squareup.picasso.Picasso;

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
    private DatabaseReference MemberMeetRef;

    private Query queryRef;

    private ArrayList<MtMember> arMtMembers;
    private ArrayList<String> arMtMemberKeys;
//    private ArrayAdapter<String> arAdapter;
    private MtMemberAdapter arAdapter;
    private ListView lvMtMembers;

    // Class
    private Meeting meeting;
    private Member my_member  ;
    private MtMember mtMember  ;
    private String AuthUid     ;

    private TextView tvMeetName;
    private TextView tvMeetLeader;
    private TextView tvMeetCrdt;
    private TextView tvMeetDesc;
    private TextView tvMeetMember;


    private Button btnLocaStart;
    private Button btnLocaEnd  ;
    private Button btnWatchMap ;
    private Button btnMeetExit ;
    private Button btnNotify   ;

    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;


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
//            mParam2 = getArguments().getString(ARG_PARAM2);  // arMeeting
//            Log.d(TAG,"mParam1["+MeetingKey+"]  mParam2["+mParam2+"]");
        }
        AuthUid = ((MainActivity)getActivity()).getAuthUid();
        my_member = ((MainActivity)getActivity()).getMember();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_detail, container, false);


        // Refresh Meeting from DB
        MeetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");

        tvMeetName = (TextView) v.findViewById(R.id.tvMeetName   );
        tvMeetLeader = (TextView) v.findViewById(R.id.tvMeetLeader   );
        tvMeetCrdt = (TextView) v.findViewById(R.id.tvMeetCrdt   );
        tvMeetDesc = (TextView) v.findViewById(R.id.tvMeetDesc   );
        tvMeetMember = (TextView) v.findViewById(R.id.tvMeetMember   );

        btnLocaStart = (Button) v.findViewById(R.id.btnLocaStart);
        btnLocaEnd   = (Button) v.findViewById(R.id.btnLocaEnd  );
        btnWatchMap  = (Button) v.findViewById(R.id.btnWatchMap );
        btnMeetExit  = (Button) v.findViewById(R.id.btnMeetExit );
        btnNotify    = (Button) v.findViewById(R.id.btnNotify   );

        btnLocaStart.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"btnLocaStart");
                if (boolean_permission) {
                    Intent intent = new Intent(getActivity(), GPSService.class);
                    intent.putExtra("MeetingKey", MeetingKey);
                    intent.putExtra("AuthUid", AuthUid);
                    getActivity().startService(intent);
                } else {
                    Toast.makeText(getActivity(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                }
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
                            MemberMeetRef = FirebaseDatabase.getInstance().getReference().child("Members").child(AuthUid).child("MemberMeet");
                            MemberMeetRef.child(MeetingKey).removeValue();
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
            }
        });
        // 카카오공유
        btnNotify.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                // Log.d(TAG,"btnNotify");

                shareKaKao();
                // update Notify_yn
                MeetingRef.child(MeetingKey).child("MtNotify_yn").setValue("Y");
            }
        });

        // 모임정보 가져오기
        queryRef = MeetingRef.child(MeetingKey);    // key
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {    // addValueEventListener,  addListenerForSingleValueEvent
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                meeting = snapshot.getValue(Meeting.class);
                if (meeting.getMtName()   != null ) tvMeetName.setText(meeting.getMtName());
                if (meeting.getMtLeader() != null ) tvMeetLeader.setText("모임장 : " + meeting.getMtLeader());
                if (meeting.getMtCrdt()   != null ) tvMeetCrdt.setText(meeting.getMtCrdt());
                if (meeting.getMtDesc()   != null ) tvMeetDesc.setText(meeting.getMtDesc());
                tvMeetMember.setText("모임멤버 : " + meeting.getMtMembersCnt() + "명");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        // 참여자 목록 가져오기
        arMtMembers = new ArrayList<MtMember>();
        arMtMemberKeys = new ArrayList<String>();

        arAdapter = new MtMemberAdapter(getActivity(), R.layout.fragment_detail_mtmember, arMtMembers);
        lvMtMembers = (ListView) v.findViewById(R.id.lvMtMembers);
        lvMtMembers.setAdapter(arAdapter);

        queryRef = MeetingRef.child(MeetingKey).child("MtMembers").orderByChild("PartYN").equalTo("Y");
        //queryRef = MeetingRef.limitToFirst(10);    // 일단 10개만 읽는다.
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {    // addValueEventListener,  addListenerForSingleValueEvent
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                MeetingRef.child(MeetingKey).child("MtMembersCnt").setValue( snapshot.getChildrenCount() );  // 참여인원
                arMtMembers.clear();
                arMtMemberKeys.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    mtMember = postSnapshot.getValue(MtMember.class);
                    arMtMembers.add(mtMember);
                    arMtMemberKeys.add(postSnapshot.getKey());
                }
                arAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        // GPS permission
        fn_permission();
//        runtime_permissions();

        return v;
    }

//    private void btnLocal_enable(boolean abutton_enable) {
//        if (abutton_enable) {
//            btnLocaStart.setEnabled(true);
//            btnLocaEnd.setEnabled(true);
//        } else {
//            btnLocaStart.setEnabled(false);
//            btnLocaEnd.setEnabled(false);
//        }
//    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION))) {
                //
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSIONS);
            }
        } else {
            boolean_permission = true;
        }
    }

    public void shareKaKao() {
        try {
            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(getActivity()) ;
            final KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            kakaoBuilder.addText("ForUs: " + meeting.getMtName().toString());
            String url = "https://lh4.googleusercontent.com/-6AAwrT0qFEM/AAAAAAAAAAI/AAAAAAAAAMQ/LG3Sb6MbquE/s96-c/photo.jpg";
            kakaoBuilder.addImage(url, 400, 400);

            kakaoBuilder
                    .addAppButton("앱으로 입장", new AppActionBuilder()
                    .addActionInfo(AppActionInfoBuilder
                            .createAndroidActionInfoBuilder()
                            .setExecuteParam("MeetingKey=" + MeetingKey)
                            .setMarketParam("referrer=kakaotalklink")
                            .build())
                    .addActionInfo(AppActionInfoBuilder
                            .createiOSActionInfoBuilder()
                            .setExecuteParam("MeetingKey=1111")
                            .build())
                    .build()
            );
//            kakaoBuilder
//                    .addAppLink("자세히 보기",  new AppActionBuilder()
//                    .addActionInfo(AppActionInfoBuilder
//                        .createAndroidActionInfoBuilder()
//                        .setExecuteParam("MeetingKey=1234")
//                        .setMarketParam("referrer=kakaotalklink")
//                        .build())
//                    .addActionInfo(AppActionInfoBuilder
//                        .createiOSActionInfoBuilder()
//                        .setExecuteParam("MeetingKey=1234")
//                        .build())
//                    .build());

            kakaoLink.sendMessage(kakaoBuilder, getActivity());
            /*
            이미지 추가 시 가로,세로 사이즈는 최소 81px 이상으로 해주어야 하며, 500kb 이하의 이미지가 가능.
            addAppButton() 으로 추가 한 버튼을 클릭할 경우, 사용자의 스마트폰 단말기에서
            내가 카카오 개발자센터에서 등록한 패키지로 설치유무를 확인합니다.
                    미 설치시 등록되어있는 마켓URL로 이동하게 되고..
            설치되어 있는 경우 등록 된 앱을 실행하여 <intent-filter>를 등록한 Activity를 띄워줍니다.
            */


        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                } else {
                    Toast.makeText(getActivity(), "Please allow the permission", Toast.LENGTH_LONG).show();
                }
            }
        }
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


    class MtMemberAdapter extends BaseAdapter {
        Context maincon;
        LayoutInflater Inflater;
        ArrayList<MtMember> arSrc;
        int layout;

        public MtMemberAdapter(Context context, int alayout, ArrayList<MtMember> aarSrc) {
            maincon = context;
            Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            arSrc = aarSrc;
            layout = alayout;
        }

        public int getCount() {
            return arSrc.size();
        }

        public MtMember getItem(int position) {
            return arSrc.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            if (convertView == null) {
                convertView = Inflater.inflate(layout, parent, false);
            }

            ImageView ivProfile = (ImageView) convertView.findViewById(R.id.ivProfile);
            TextView tvNickName = (TextView) convertView.findViewById(R.id.tvNickName);
            TextView tvParticipationDate = (TextView) convertView.findViewById(R.id.tvParticipationDate);
            TextView tvLeader = (TextView) convertView.findViewById(R.id.tvLeader);

            try {
                String AuthPhotoURL, NickName, ParticipationDate;
                NickName = arSrc.get(position).getNickName().toString();
                ParticipationDate = arSrc.get(position).getParticipationDate().toString();
                Log.d(TAG, "position["+position+"],  NickName[" + NickName + "],  ParticipationDate["+ParticipationDate+"]");
                if (arSrc.get(position).getAuthPhotoURL() != null ) {
                    AuthPhotoURL = arSrc.get(position).getAuthPhotoURL().toString();

                    Picasso.with(getActivity())
                            .load(AuthPhotoURL)
                            //.error(R.drawable.error)
                            //.placeholder(R.drawable.placeholder)
                            .resize(100, 100)
                            .centerCrop()
                            .into(ivProfile);
                    Log.d(TAG, "AuthPhotoURL: " + AuthPhotoURL);
                } else {
//                    ivProfile.setImageResource("@mipmap/ic_launcher");
                    ivProfile.setImageResource(android.R.drawable.ic_menu_more);
                }

                tvNickName.setText(NickName);
                tvParticipationDate.setText("참여일시: " + ParticipationDate);
                String mtLeader = meeting.getMtLeader().toString();
                if (NickName.equals( mtLeader ) ) {
                    tvLeader.setVisibility(View.VISIBLE);     // 모임장
                } else {
                    tvLeader.setVisibility(View.INVISIBLE);
                }

            } catch(Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage() );
            }



            return convertView;
        }
    }



}
