package com.example.blackjack;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;//토스트
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;//토스트

import com.example.blackjack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class gameActivity extends AppCompatActivity {
    private FirebaseAuth mAuth ;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;

    private card[] cardList=new card[52];
    Button btnHit, btnStay;
    String name, uid;
    int valueByA;//A의 값

    Intent intent=null, endIntent=null;

    private card[] playerCardList=new card[11];//플레이어용 카드 패
    private  int playerOrderNo=2;//플레이어용 카드 패 번호, 처음에 2장을 추가하니 2부터 시작 (살제 인수 값으로는 0~1)
    private  int playerScore=0;//플레이어용 점수 합산
    private card[] dealerCardList=new card[11];//딜러용 카드 패
    private  int dealerOrderNo=2;//딜러용 카드 패 번호, 처음에 2장을 추가하니 2부터 시작 (살제 인수 값으로는 0~1)
    private  int dealerScore=0;//딜러용 점수 합산

    private  int cardOrderNo=4;//전체 카드에 대한 순서 번호, cardList에 대해서 사용, 처음에 4장을 사용하니 4부터 시작 (살제 인수 값으로는 0~3)

    private  String dealerSay="";//딜러의 대사

    //딜레이용
    private TimerTask mTask;
    private Timer mTimer;

    //결과용
    private String gameResultToast;

    //테스트
    private String pList="";
    private String dList="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //============================ 변수 초기화 부분 시작 =========================================
        btnHit = (Button) findViewById(R.id.btn_hit);
        btnStay = (Button) findViewById(R.id.btn_stay);

        mAuth = FirebaseAuth.getInstance();

        //lobbyActivity의 intent 값 가져오기
        intent = getIntent(); /*데이터 수신*/
        //intent 값 기본 설정
        endIntent = new Intent(this, endActivity.class);

        if (user != null) {
            // Name, email address
            name = user.getDisplayName();
            uid = user.getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference();
        }//if(user!=null) 종료

        //A 카드의 값 가져오기
        valueByA = intent.getExtras().getInt("valueByA");
        setResult(Activity.RESULT_OK);//lobbyActivityForResult때문에 필요

        //카드 초기화 - 스페이드
        cardList[0]=new card("Spade","SpadeA",valueByA);
        for (int i=1; i<=9; i++){
            cardList[i]=new card("Spade","Spade"+(i+1),i+1);
        }
        cardList[10]=new card("Spade","SpadeJ",10);
        cardList[11]=new card("Spade","SpadeQ",10);
        cardList[12]=new card("Spade","SpadeK",10);

        //카드 초기화 - 하트
        cardList[13]=new card("Heart","HeartA",valueByA);
        for (int i=14; i<=22; i++){
            cardList[i]=new card("Heart","Heart"+(i-12),i-12);
        }
        cardList[23]=new card("Heart","HeartJ",10);
        cardList[24]=new card("Heart","HeartQ",10);
        cardList[25]=new card("Heart","HeartK",10);

        //카드 초기화 - 클로버
        cardList[26]=new card("Clover","CloverA",valueByA);
        for (int i=27; i<=35; i++){
            cardList[i]=new card("Clover","Clover"+(i-25),i-25);
        }
        cardList[36]=new card("Clover","CloverJ",10);
        cardList[37]=new card("Clover","CloverQ",10);
        cardList[38]=new card("Clover","CloverK",10);

        //카드 초기화 - 다이아몬드
        cardList[39]=new card("Diamond","DiamondA",valueByA);
        for (int i=40; i<=48; i++){
            cardList[i]=new card("Diamond","Diamond"+(i-38),i-38);
        }
        cardList[49]=new card("Diamond","DiamondJ",10);
        cardList[50]=new card("Diamond","DiamondQ",10);
        cardList[51]=new card("Diamond","DiamondK",10);

        mTimer = new Timer();
        //============================ 변수 초기화 부분 종료 =========================================

        //============================ 버튼 클릭 리스너 부분 시작 ====================================
        //Hit 버튼 클릭
        btnHit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerCardList[playerOrderNo]=cardList[cardOrderNo];//카드 목록에서 카드 받아오기
                playerScore+=cardList[cardOrderNo].value;//플레이어 점수 추가
                pList+=cardList[cardOrderNo].name;//테스트

                playerOrderNo++;//플레이어용 카드 패 번호
                cardOrderNo++;//전체 카드에 대한 순서 번호 값 증가

                //플레이어의 점수 확인
                if(playerScore>21){
                    btnHit.setEnabled(false);//Hit 버튼 비활성화
                    btnStay.setEnabled(false);//Stay 버튼 비활성화

                    endIntent.putExtra("playerResult","LOSE");

                    Context context=getApplicationContext();
                    int dur= Toast.LENGTH_SHORT;
                    /*Toast toast=Toast.makeText(context,"버스트 되었습니다.\n당신의 패배입니다...",dur);
                    ((TextView)((LinearLayout)toast.getView()).getChildAt(0))
                            .setGravity(Gravity.CENTER_HORIZONTAL);*/
                    Toast toast=Toast.makeText(context,"버스트 되었습니다. 당신의 패배입니다...",dur);
                    toast.show();

                    mTask = new TimerTask() {
                        @Override public void run() {
                            startActivityForResult(endIntent,333);
                        }
                    };
                    mTimer.schedule(mTask, 3000);
                }
            }
        });//Hit 클릭 리스너 종료

        //Stay 버튼 클릭
        btnStay.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnHit.setEnabled(false);//Hit 버튼 비활성화
                btnStay.setEnabled(false);//Stay 버튼 비활성화

                //딜러의 점수가 16점 이하면 카드 1장 더 받음
                if(dealerScore<=16){
                    dealerCardList[dealerOrderNo]=cardList[cardOrderNo];//카드 목록에서 카드 받아오기
                    dealerScore+=cardList[cardOrderNo].value;//플레이어 점수 추가
                    dList+=cardList[cardOrderNo].name;//테스트

                    dealerOrderNo++;//플레이어용 카드 패 번호
                    cardOrderNo++;//전체 카드에 대한 순서 번호 값 증가

                    endIntent.putExtra("dealerScore",dealerScore);//덧씌우기

                    //점수 계산 실행
                    mTask = new TimerTask() {
                        @Override public void run() {
                            gameActivity.this.runOnUiThread(new Runnable(){
                                public void run(){//실제 기능 구현
                                    calcResult();
                                }
                            });//runOnUiThread 종료
                        }
                    };
                    mTimer.schedule(mTask, 3000);
                }
                else{
                    //딜러의 점수가 17점 이상이면 바로 점수 계산 실행
                    /*mTask = new TimerTask() {
                        @Override public void run() {
                            calcResult();
                        }
                    };*/
                    mTask = new TimerTask() {
                        @Override public void run() {
                            gameActivity.this.runOnUiThread(new Runnable(){
                                public void run(){//실제 기능 구현
                                    calcResult();
                                }
                            });//runOnUiThread 종료
                        }
                    };
                    mTimer.schedule(mTask, 3000);
                }

                endIntent.putExtra("playerScore",playerScore);
                endIntent.putExtra("dealerScore",dealerScore);
                endIntent.putExtra("pList",pList);//테스트
                endIntent.putExtra("dList",dList);//테스트

                mTask = new TimerTask() {
                    @Override public void run() {
                        startActivityForResult(endIntent,333);
                    }
                };
                mTimer.schedule(mTask, 8000);
            }
        });//Stay 클릭 리스너 종료
        //============================ 버튼 클릭 리스너 부분 종료 ====================================

        //============================ 추가 작업 부분 시작 ===========================================
        //카드 분배 및 시작 작업 완료 시까지 버튼 클릭 방지
        btnHit.setEnabled(false);//Hit 버튼 비활성화
        btnStay.setEnabled(false);//Stay 버튼 비활성화

        //"카드를 섞는 중입니다." 출력
        shuffleToast();//시작 후 2초 뒤 작동

        //카드 섞기
        shuffle(cardList);
        shuffle(cardList);
        shuffle(cardList);

        //카드 분배 및 딜러의 블랙잭 여부 선언
        distributeCardAtStart();

        //사전 작업 완료 후 버튼의 활성화는 distributeCardAtStart()에서 작업

        //============================ 추가 작업 부분 종료 ===========================================
    }//onCreate 종료

    //startActivityForResult때문에 필요
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==333){
                this.finish();
            }
        }
    }//onActivityResult 종료

    //딜레이때문에 필요
    @Override
    protected void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }//onDestroy 종료

    //================================== 추가 내용 ==================================================

    //카드 클래스
    public class card{
        String mark;
        String name;
        int value;

        public card(String mark, String name, int value){
            this.mark=mark;
            this.name=name;
            this.value=value;
        }

        public card() {

        }

        public void setValue(int value) {
            this.value = value;
        }
    }//card 클래스 종료

    //카드 섞기
    public static card[] shuffle(card[] arr){
        for(int x=0;x<arr.length;x++){
            int i = (int)(Math.random()*arr.length);
            int j = (int)(Math.random()*arr.length);

            card tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }

        return arr;
    }//shuffle 함수 종료

    //카드 셔플 중임을 알리는 토스트 함수
    public void shuffleToast(){
        mTask = new TimerTask() {
            @Override public void run() {
                gameActivity.this.runOnUiThread(new Runnable(){
                    public void run(){//실제 기능 구현
                        Context context=getApplicationContext();
                        int dur= Toast.LENGTH_LONG;//3.5초
                        Toast toast=Toast.makeText(context,"카드를 섞는 중입니다.",dur);
                        toast.show();
                    }
                });//runOnUiThread 종료
            }
        };
        mTimer.schedule(mTask, 2000);
    }

    //게임 시작시 카드 분배
    public void distributeCardAtStart(){
        //====================== 분배 작업 시작 (shuffle 작업 완료 후 시작 )===========================
        mTask = new TimerTask() {
            @Override public void run() {
                //플레이어 1장 (공개)
                playerCardList[0]=cardList[0];
                playerScore+=playerCardList[0].value;
                pList+=cardList[0].name;//테스트
                //딜러 1장 (공개)
                dealerCardList[0]=cardList[1];
                dealerScore+=dealerCardList[0].value;
                dList+=cardList[1].name;//테스트
                //플레이어 1장 (공개)
                playerCardList[1]=cardList[2];
                playerScore+=playerCardList[1].value;
                pList+=cardList[2].name;//테스트
                //딜러 1장 (비공개)
                dealerCardList[1]=cardList[3];
                dealerScore+=dealerCardList[1].value;
                dList+=cardList[3].name;//테스트
            }
        };
        //모든 분배에 투자하는 시간을 합쳐서 3초로 계획
        //시작 후 2초 + 섞는 중 토스트 3.5초 + 여유 시간 0.5초 + 분배 3초 = 9초
        mTimer.schedule(mTask, 6000);
        //====================== 분배 작업 종료 =====================================================

        //플레이어의 카드 패 먼저 확인 후 딜러의 카드 패 값 확인
        if(playerScore>21){
            btnHit.setEnabled(false);//Hit 버튼 비활성화
            btnStay.setEnabled(false);//Stay 버튼 비활성화

            endIntent.putExtra("playerResult","LOSE");

            Context context=getApplicationContext();
            int dur= Toast.LENGTH_SHORT;
            /*Toast toast=Toast.makeText(context,"버스트 되었습니다.\n당신의 패배입니다...",dur);
            ((TextView)((LinearLayout)toast.getView()).getChildAt(0))
                    .setGravity(Gravity.CENTER_HORIZONTAL);*/
            Toast toast=Toast.makeText(context,"버스트 되었습니다. 당신의 패배입니다...",dur);
            toast.show();

            mTask = new TimerTask() {
                @Override public void run() {
                    startActivityForResult(endIntent,333);
                }
            };
            mTimer.schedule(mTask, 9500);
        }
        else{
            //딜러에 대한 체크 시작
            if(dealerScore==21){
                dealerSay="딜러 : 블랙잭입니다.";
                /*Context context=getApplicationContext();
                int dur= Toast.LENGTH_SHORT;
                Toast toast=Toast.makeText(context,dealerSay,dur);
                toast.show();*/

                mTask = new TimerTask() {
                    @Override public void run() {
                        gameActivity.this.runOnUiThread(new Runnable(){
                            public void run(){//실제 기능 구현
                                dealerSay(dealerSay);
                            }
                        });//runOnUiThread 종료
                    }
                };
                mTimer.schedule(mTask, 9500);

            }
            else if(dealerScore<21){
                dealerSay="딜러 : 노 블랙잭입니다.";
                mTask = new TimerTask() {
                    @Override public void run() {
                        gameActivity.this.runOnUiThread(new Runnable(){
                            public void run(){//실제 기능 구현
                                dealerSay(dealerSay);
                            }
                        });//runOnUiThread 종료
                    }
                };
                mTimer.schedule(mTask, 9500);
            }

            mTask = new TimerTask() {
                @Override public void run() {
                    gameActivity.this.runOnUiThread(new Runnable(){
                        public void run(){//실제 기능 구현
                            btnHit.setEnabled(true);//Hit 버튼 활성화
                            btnStay.setEnabled(true);//Stay 버튼 활성화
                        }
                    });//runOnUiThread 종료
                }
            };
            mTimer.schedule(mTask, 11000);
        }
    }//distributeCardAtStart 종료

    //딜러 대사
    public void dealerSay(String say){
        Context context=getApplicationContext();
        int dur= Toast.LENGTH_SHORT;
        Toast toast=Toast.makeText(context,say,dur);
        toast.show();
    }//dealerSay 종료

    public void calcResult(){
        if(playerScore>dealerScore && playerScore==21){
            //승리 + 블랙잭 O
            gameResultToast="당신의 승리입니다!";
            endIntent.putExtra("playerResult","WIN");
        }
        else if(playerScore>dealerScore&& playerScore!=21){
            //승리 + 블랙잭 X
            gameResultToast="블랙잭! 당신의 승리입니다!";
            endIntent.putExtra("playerResult","BLACK JACK");
        }
        else if(playerScore==dealerScore){
            //동점
            gameResultToast="동점입니다!";
            endIntent.putExtra("playerResult","DRAW");
        }
        else if(playerScore<dealerScore && dealerScore>21){
            //졌는데 딜러가 버스트인 경우
            gameResultToast="딜러의 버스트! 당신의 승리입니다!";
            endIntent.putExtra("playerResult","WIN");
        }
        else if(playerScore<dealerScore && dealerScore<=21){
            //졌는데 딜러가 버스트가 아닌 경우
            gameResultToast="당신의 패배입니다...";
            endIntent.putExtra("playerResult","LOSE");
        }

        Context context=getApplicationContext();
        int dur= Toast.LENGTH_LONG;
        Toast toast=Toast.makeText(context,gameResultToast,dur);
        toast.show();
    }
}//gameActivity 종료
