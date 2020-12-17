package com.example.blackjack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private TextView view_result;
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
    int tempBlackJack;//패배 값 확인용

    int playerScore;
    int dealerScore;

    //딜레이용
    private TimerTask mTask;
    private Timer mTimer;

    //테스트
    private String pList="";
    private String dList="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        view_result = (TextView) findViewById(R.id.view_result);
        btn_return_lobby = (Button) findViewById(R.id.btn_return_lobby);

        //gameActivity의 intent 값 가져오기
        intent = getIntent(); /*데이터 수신*/
        //intent 값 기본 설정
        lobbyIntent = new Intent(this, lobbyActivity.class);

        //값 가져온 다음에 TEXTVIEW에 결과 출력
        playerResult = intent.getExtras().getString("playerResult");
        playerScore = intent.getExtras().getInt("playerScore");
        dealerScore = intent.getExtras().getInt("dealerScore");
        pList = intent.getExtras().getString("pList");
        dList = intent.getExtras().getString("dList");
        view_result.setText("결과 : "+playerResult+" 내 점수 : "+playerScore+" VS 딜러 점수 : "+dealerScore+"\n내 리스트 : "+pList+"딜러 리스트 : "+dList);

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
                tempBlackJack= snapshot.child("user").child(uid.toString()).child("blackjack").getValue(Integer.class);

                //Log.e("tempVictory", String.valueOf(tempVictory));
                //Log.e("tempDraw", String.valueOf(tempDraw));
                //Log.e("tempLose", String.valueOf(tempLose));
                //Log.e("tempBlackJack", String.valueOf(tempBlackJack));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mTask = new TimerTask() {
            @Override public void run() {
                endActivity.this.runOnUiThread(new Runnable(){
                    public void run(){//실제 기능 구현
                        if(playerResult.equals("WIN")){
                            /*Context context=getApplicationContext();
                            int dur= Toast.LENGTH_SHORT;
                            Toast toast=Toast.makeText(context,"이전 승리 횟수 : "+tempVictory,dur);
                            toast.show();*/

                            tempVictory++;
                            mDatabase.child("user").child(uid.toString()).child("victory").setValue(tempVictory);

                            //view_result.setText(playerResult+" 승리 횟수 : "+tempVictory);
                        }
                        else if(playerResult.equals("BLACK JACK")){
                            /*Context context=getApplicationContext();
                            int dur= Toast.LENGTH_SHORT;
                            Toast toast=Toast.makeText(context,"이전 승리 횟수 : "+tempVictory,dur);
                            toast.show();*/

                            tempVictory++;
                            tempBlackJack++;
                            mDatabase.child("user").child(uid.toString()).child("victory").setValue(tempVictory);
                            mDatabase.child("user").child(uid.toString()).child("blackjack").setValue(tempBlackJack);

                            //view_result.setText(playerResult+" 승리 횟수 : "+tempVictory);
                        }
                        else if(playerResult.equals("DRAW")){
                            /*Context context=getApplicationContext();
                            int dur= Toast.LENGTH_SHORT;
                            Toast toast=Toast.makeText(context,"이전 무승부 횟수 : "+tempDraw,dur);
                            toast.show();*/

                            tempDraw++;
                            mDatabase.child("user").child(uid.toString()).child("draw").setValue(tempDraw);

                            //view_result.setText(playerResult+" 무승부 횟수 : "+tempDraw);
                        }
                        else if(playerResult.equals("LOSE")){
                            /*Context context=getApplicationContext();
                            int dur= Toast.LENGTH_SHORT;
                            Toast toast=Toast.makeText(context,"이전 패배 횟수 : "+String.valueOf(tempLose),dur);
                            toast.show();*/

                            tempLose++;
                            mDatabase.child("user").child(uid.toString()).child("lose").setValue(tempLose);

                            //view_result.setText(playerResult+" 패배 횟수 : "+tempLose);
                        }

                        tempAll=tempVictory+tempDraw+tempLose;
                        mDatabase.child("user").child(uid.toString()).child("all").setValue(tempAll);
                    }
                });//runOnUiThread 종료
            }
        };
        mTimer.schedule(mTask, 150);


        /*mTask = new TimerTask() {
            @Override public void run() {
                tempAll=tempVictory+tempDraw+tempLose;
                mDatabase.child("user").child(uid.toString()).child("all").setValue(tempAll);
            }
        };
        mTimer.schedule(mTask, 300);*/

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
