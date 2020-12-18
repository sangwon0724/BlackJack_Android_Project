package com.example.blackjack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.blackjack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class endActivity extends AppCompatActivity {
    private TextView view_result, view_score;
    private Button btn_return_lobby;

    private FirebaseAuth mAuth ;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;

    Intent intent=null, lobbyIntent=null;

    String playerResult="";

    String uid="";

    int tempAll;//전체 판 수 확인용
    int tempVictory;//승리 값 확인용
    int tempDraw;//무승부 값 확인용
    int tempLose;//패배 값 확인용

    int playerScore;
    int dealerScore;

    //딜레이용
    private TimerTask mTask;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_end);

        view_result = (TextView) findViewById(R.id.text_result);
        view_score = (TextView) findViewById(R.id.text_data);
        btn_return_lobby = (Button) findViewById(R.id.btn_return_lobby);

        //gameActivity의 intent 값 가져오기
        intent = getIntent(); /*데이터 수신*/
        //intent 값 기본 설정
        lobbyIntent = new Intent(this, lobbyActivity.class);

        //값 가져온 다음에 TEXTVIEW에 결과 출력
        playerResult = intent.getExtras().getString("playerResult");
        playerScore = intent.getExtras().getInt("playerScore");
        dealerScore = intent.getExtras().getInt("dealerScore");

        view_result.setText(playerResult);
        view_score.setText(playerScore+" : "+dealerScore);


        //타이머 설정
        mTimer=new Timer();

        //유저 아이디 설정
        if(user!=null){
            uid = user.getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference();
        }//if(user!=null) 종료

        //점수 추가
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object object = snapshot.getValue(Object.class);
                tempVictory= snapshot.child("user").child(uid.toString()).child("victory").getValue(Integer.class);
                tempDraw= snapshot.child("user").child(uid.toString()).child("draw").getValue(Integer.class);
                tempLose=snapshot.child("user").child(uid.toString()).child("lose").getValue(Integer.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mTask = new TimerTask() {
            @Override public void run() {
                endActivity.this.runOnUiThread(new Runnable(){
                    public void run(){//실제 기능 구현
                        if(playerResult.equals("WIN")){
                            tempVictory++;
                            mDatabase.child("user").child(uid.toString()).child("victory").setValue(tempVictory);
                        }
                        else if(playerResult.equals("BLACK JACK")){
                            tempVictory++;
                            mDatabase.child("user").child(uid.toString()).child("victory").setValue(tempVictory);
                        }
                        else if(playerResult.equals("DRAW")){
                            tempDraw++;
                            mDatabase.child("user").child(uid.toString()).child("draw").setValue(tempDraw);
                        }
                        else if(playerResult.equals("LOSE")){
                            tempLose++;
                            mDatabase.child("user").child(uid.toString()).child("lose").setValue(tempLose);
                        }

                        tempAll=tempVictory+tempDraw+tempLose;
                        mDatabase.child("user").child(uid.toString()).child("all").setValue(tempAll);
                    }
                });//runOnUiThread 종료
            }
        };
        mTimer.schedule(mTask, 150);

        //종료하기
        btn_return_lobby.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                //방 삭제
                mDatabase.child("room").child(uid.toString()).removeValue();

                //endActivity 종료
                setResult(Activity.RESULT_OK);//startActivityForResult때문에 필요
                startActivity(lobbyIntent);
                finish();
            }
        });//btn_return_lobby 클릭 리스너 종료
    }//onCreate 종료

    @Override
    protected void onDestroy() {
        if(mTimer!=null){
            mTimer.cancel();
            mTimer=null;
        }
        super.onDestroy();
    }//onDestroy 종료
}
