package com.forus;

import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ForUs MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    // Class declear
    Member member  ;

    public String AuthUid;
//    public String AuthNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().
                add(R.id.fragment_container, new AuthFragment()).commit();




//        mAuth = FirebaseAuth.getInstance();
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    // User is signed in
//                    AuthUid = user.getUid();
//                    Log.d(TAG, "onAuthStateChanged signed_in:" + user.getUid());
//                    Log.d(TAG, "onAuthStateChanged getDisplayName:" + user.getDisplayName());
//                    Log.d(TAG, "onAuthStateChanged getEmail:" + user.getEmail());
//                    Log.d(TAG, "onAuthStateChanged getId:" + user.getProviders());
//
//                    // 개인정보 가져오기 from DB
//                    MembersRef = FirebaseDatabase.getInstance().getReference().child("Members");
//                    queryRef = MembersRef.child(AuthUid);    // key
//                    queryRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot snapshot) {
//                            //Getting the data from snapshot
//                            member = snapshot.getValue(Member.class);   // 개인정보를 member class 에 담는다.
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Log.w(TAG, "Failed to read value.", databaseError.toException());
//                        }
//                    });
//
//                } else {
//                    // User is signed out
//                    Log.d(TAG, "onAuthStateChanged signed_out");
//                    getSupportFragmentManager().beginTransaction().
//                            add(R.id.fragment_container, new AuthFragment()).commit();
//
//                }
//            }
//        };


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fr = null;

        switch(item.getItemId()) {
            case R.id.mnAuth:
//                Toast.makeText(this, "mnAuth", Toast.LENGTH_SHORT).show();
                fr = new AuthFragment() ;
                break;
            case R.id.mnProfile:
//                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                fr = new ProfileFragment() ;
                Bundle args = new Bundle();
                args.putString("param1", AuthUid);
                args.putString("param2", "");
                fr.setArguments(args);
                break;
            case R.id.mnList:
//                Toast.makeText(this, "mnList", Toast.LENGTH_SHORT).show();
                fr = new ListFragment() ;
                break;
            case R.id.mnCreate:
//                Toast.makeText(this, "mnCreate", Toast.LENGTH_SHORT).show();
                fr = new CreateFragment() ;
                break;
            case R.id.mnParticipation:
//                Toast.makeText(this, "mnParticipation", Toast.LENGTH_SHORT).show();
                fr = new PartFragment() ;
                break;
        }

//        getSupportFragmentManager().beginTransaction().
//                replace(R.id.fragment_container, fr).commit();

        // Create new fragment and transaction
        android.support.v4.app.FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fr);
        transaction.addToBackStack(null);                  // and add the transaction to the back stack
        transaction.commit();

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
