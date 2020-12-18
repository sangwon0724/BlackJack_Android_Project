package com.example.blackjack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.blackjack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class lobbyActivity extends AppCompatActivity /*implements View.OnClickListener*/ {
    Button btnInfo, btnSetting, btnOut, btnStart, btnRule;

    private FirebaseAuth mAuth ;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    String uid;

    private DatabaseReference mDatabase;

    Intent intent, gameIntent, myInfoIntent;

    Integer tempAll;//전체 판 수 확인용
    Integer tempVictory;//승리 값 확인용
    Integer tempDraw;//무승부 값 확인용
    Integer tempLose;//패배 값 확인용

    //딜레이용
    private TimerTask mTask;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lobby);

        btnInfo = (Button)findViewById(R.id.btn_info);
        btnSetting = (Button)findViewById(R.id.btn_setting);
        btnOut = (Button)findViewById(R.id.btn_out);
        btnStart = (Button)findViewById(R.id.btn_start);
        btnRule = (Button)findViewById(R.id.btn_rule);

        mAuth = FirebaseAuth.getInstance();

        intent = getIntent();//데이터 수신
        gameIntent = new Intent(this, gameActivity.class);//intent 값 기본 설정
        myInfoIntent = new Intent(this, myActivity.class);//intent 값 기본 설정

        if (user != null) {
            uid = user.getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference();

            //승리 값이 없으면 0으로 설정
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Object object = snapshot.getValue(Object.class);
                    tempAll=snapshot.child("user").child(uid.toString()).child("all").getValue(Integer.class);
                    tempVictory=snapshot.child("user").child(uid.toString()).child("victory").getValue(Integer.class);
                    tempDraw=snapshot.child("user").child(uid.toString()).child("draw").getValue(Integer.class);
                    tempLose=snapshot.child("user").child(uid.toString()).child("lose").getValue(Integer.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        //내 정보
        btnInfo.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                    startActivityForResult(myInfoIntent,123);
            }
        });

        //환경 설정
        btnSetting.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindowSetting();
            }
        });

        //어플 종료
        btnOut.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context=getApplicationContext();
                int dur= Toast.LENGTH_SHORT;
                Toast toast=Toast.makeText(context,"다음에 뵙겠습니다.",dur);
                toast.show();

                mTask = new TimerTask() {
                    @Override public void run() {
                        finish();
                    }
                };
                mTimer = new Timer();
                mTimer.schedule(mTask, 2050);
            }
        });

        //게임 시작
        btnStart.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindowByGameStart();
            }
        });

        //규칙 보기
        btnRule.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindowShowRules();
            }
        });
    }//onCreate 종료

    @Override
    protected void onStart() {
        super.onStart();

        mTask = new TimerTask() {
            @Override public void run() {
                if(tempAll==null){
                    mDatabase.child("user").child(uid.toString()).child("all").setValue(0);//값을 0으로 설정
                }
                if(tempVictory==null){
                    mDatabase.child("user").child(uid.toString()).child("victory").setValue(0);//값을 0으로 설정
                }
                if(tempDraw==null){
                    mDatabase.child("user").child(uid.toString()).child("draw").setValue(0);//값을 0으로 설정
                }
                if(tempLose==null){
                    mDatabase.child("user").child(uid.toString()).child("lose").setValue(0);//값을 0으로 설정
                }
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTask, 1500);
    }//onStart 종료

    //startActivityForResult때문에 필요
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==123){
                this.finish();
            }
        }
    }//onActivityResult 종료

    //딜레이때문에 필요
    @Override
    protected void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }


    //================================== 추가 내용 ==================================================
    //게임 시작
    private void gameStart(){

        Context context=getApplicationContext();

        int dur= Toast.LENGTH_LONG;

        Toast toast=Toast.makeText(context,"게임을 시작하겠습니다.",dur);
        toast.show();

        mDatabase.child("room").child(uid.toString()).setValue("playing...");;//방 생성

        startActivityForResult(gameIntent,123);
    }

    //게임 시작
    private void popupWindowByGameStart() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupLayout = inflater.inflate(R.layout.a1or11, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow mPopupWindow = new PopupWindow(popupLayout, width, height, focusable);//팝업 윈도우


        mPopupWindow.setTouchable(true); // PopupWindow 위에서 Button의 Click이 가능하도록 setTouchable(true); 임시

        Button btn_A1 = (Button) popupLayout.findViewById(R.id.Ais1);
        Button btn_A11 = (Button) popupLayout.findViewById(R.id.Ais11);

        mPopupWindow.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);

        //1 선택시
        btn_A1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameIntent.putExtra("valueByA",1);
                mPopupWindow.dismiss();
                gameStart();
            }
        });

        //11 선택시
        btn_A11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameIntent.putExtra("valueByA",11);
                mPopupWindow.dismiss();
                gameStart();
            }
        });
    }//popupWindowByGameStart 종료

    //환경 설정
    public void popupWindowSetting(){
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        //R.layout.dialog는 xml 파일명이고 R.id.popup은 보여줄 레이아웃 아이디
        View layout = inflater.inflate(R.layout.setting_land,(ViewGroup) findViewById(R.id.setting_land));
        AlertDialog.Builder aDialog = new AlertDialog.Builder(lobbyActivity.this);
        aDialog.setView(layout);// dialog.xml 파일을 뷰로 셋팅
        // 그냥 닫기버튼을 위한 부분
        aDialog.setNegativeButton("×", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        }); //팝업창 생성
        AlertDialog ad = aDialog.create();
        ad.show();//보여줌!
    }//popupWindowSetting 종료

    //규칙 보기
    public void popupWindowShowRules(){
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        //R.layout.dialog는 xml 파일명이고 R.id.popup은 보여줄 레이아웃 아이디
        View layout = inflater.inflate(R.layout.menu_land, (ViewGroup) findViewById(R.id.menu_land));
        AlertDialog.Builder aDialog = new AlertDialog.Builder(lobbyActivity.this);
        aDialog.setView(layout);// dialog.xml 파일을 뷰로 셋팅

        // 그냥 닫기버튼을 위한 부분
        aDialog.setNegativeButton("×", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        }); //팝업창 생성
        AlertDialog ad = aDialog.create();
        ad.show();//보여줌!
    }//popupWindowShowRules 종료
}
