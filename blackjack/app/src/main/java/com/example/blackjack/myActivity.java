package com.example.blackjack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class myActivity extends AppCompatActivity {
    TextView total, win, draw, lose;
    Button close, logOut, deleteUser;

    private FirebaseAuth mAuth ;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;

    String uid;

    int totalCount=0, winCount=0, drawCount=0, loseCount=0;

    Intent mainIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        total = (TextView) findViewById(R.id.countTotal);
        win = (TextView) findViewById(R.id.countWin);
        draw = (TextView) findViewById(R.id.countDraw);
        lose = (TextView) findViewById(R.id.countLose);

        close = (Button) findViewById(R.id.myInfoBtnClose);
        logOut = (Button) findViewById(R.id.myInfoBtnLogOut);
        deleteUser = (Button) findViewById(R.id.myInfoBtnDeleteUser);

        mainIntent = new Intent(this, MainActivity.class);//intent 값 기본 설정

        mAuth = FirebaseAuth.getInstance();

        if (user != null) {
            uid = user.getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference();

            //승리 값이 없으면 0으로 설정
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    totalCount = snapshot.child("user").child(uid.toString()).child("all").getValue(Integer.class);
                    winCount = snapshot.child("user").child(uid.toString()).child("victory").getValue(Integer.class);
                    drawCount = snapshot.child("user").child(uid.toString()).child("draw").getValue(Integer.class);
                    loseCount = snapshot.child("user").child(uid.toString()).child("lose").getValue(Integer.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                total.setText("총 플레이 횟수 : "+totalCount);
                win.setText("승리 횟수 : "+winCount);
                draw.setText("무승부 횟수 : "+drawCount);
                lose.setText("패배 횟수 : "+loseCount);
            }
        },500);

        close.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        logOut.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        deleteUser.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                revokeAccess();
            }
        });
    }//onCreate 종료

    //로그아웃
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        setResult(Activity.RESULT_OK);//lobbyActivityForResult때문에 필요
        mDatabase.child("room").child(uid.toString()).removeValue();
        startActivity(mainIntent);
        finish();
    }//signOut 종료

    //회원탈퇴
    //현재 안 먹힘
    private void revokeAccess() {
        //FirebaseAuth.getInstance().getCurrentUser().delete();//기존

        FirebaseAuth.getInstance().signOut();//테스트
        setResult(Activity.RESULT_OK);//lobbyActivityForResult때문에 필요//테스트
        
         mDatabase.child("room").child(uid.toString()).removeValue();//기존
         mDatabase.child("user").child(uid.toString()).removeValue();//기존

        startActivity(mainIntent);
        finish();
    }//revokeAccess 종료
}//myActivity 종료