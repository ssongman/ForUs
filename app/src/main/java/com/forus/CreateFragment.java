package com.forus;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ForUs CreateFragment";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private DatabaseReference meetingRef;
    private DatabaseReference meetingRefKey;
    private String temp_key;

    private EditText etMtName;
    private EditText etMtPass;
    private EditText etMtFrdt;
    private EditText etMtFrtm;
    private EditText etMtDesc;

    public String AuthUid;
    public String AuthNickName;
    

    public CreateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        AuthUid = ((MainActivity)getActivity()).getAuthUid();
        AuthNickName = ((MainActivity)getActivity()).getAuthNickName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create, container, false);


        meetingRef = FirebaseDatabase.getInstance().getReference().child("Meeting");

        etMtName = (EditText) v.findViewById(R.id.etMtName);
        etMtPass = (EditText) v.findViewById(R.id.etMtPass);
        etMtFrdt = (EditText) v.findViewById(R.id.etMtFrdt);
        etMtFrtm = (EditText) v.findViewById(R.id.etMtFrtm);
        etMtDesc = (EditText) v.findViewById(R.id.etMtDesc);
        
        Button btnCreate = (Button) v.findViewById(R.id.btnCreate);
        Button btnCancel = (Button) v.findViewById(R.id.btnCancel);
        btnCreate.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {

                Log.d(TAG,"Before DB insert ");

                // 모임정보 등록
                Map<String, Object> map = new HashMap<String, Object>();
                temp_key = meetingRef.push().getKey();
                meetingRef.updateChildren(map);

                Map<String, Object> map2 = new HashMap<String, Object>();
                meetingRefKey = meetingRef.child(temp_key);
                map2.put("MtName",etMtName.getText().toString());
                map2.put("MtPass",etMtPass.getText().toString());
                map2.put("MtFrdt",etMtFrdt.getText().toString());
                map2.put("MtFrtm",etMtFrtm.getText().toString());
                map2.put("MtDesc",etMtDesc.getText().toString());
                meetingRefKey.updateChildren(map2);

                // 참여멤버, 모임장은 자동참여
                meetingRefKey.child("MtMembers").child(AuthUid).child("NickName").setValue( AuthNickName );   // 1. NickName
                meetingRefKey.child("MtMembers").child(AuthUid).child("PartYN").setValue( "Y" );              // 2. PartYN
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String today = sdf.format(new Date());
                meetingRefKey.child("MtMembers").child(AuthUid).child("ParticipationDate").setValue( today ); // 3. 참여수락 일자


                Log.d(TAG,"After DB insert ");
                getActivity().getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, new ListFragment()).commit();
            }
        });
        btnCancel.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"Before Test1 ");
                getActivity().getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, new ListFragment()).commit();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        //AlertMessage("Alert test");

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
