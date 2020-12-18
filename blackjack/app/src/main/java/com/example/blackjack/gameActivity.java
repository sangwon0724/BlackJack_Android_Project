package com.example.blackjack;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;//토스트
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
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

    //카드 이미지 리스트
    ImageView cardImage[]=new ImageView[13];//플레이어 카드 최대 11장 + 딜러 카드 2장 = 13장

    //딜러 전용
    ImageView cardDealerPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        //============================ 변수 초기화 부분 시작 =========================================
        btnHit = (Button) findViewById(R.id.btn_hit);
        btnStay = (Button) findViewById(R.id.btn_stay);

        cardImage[0] = (ImageView) findViewById(R.id.cardBack1);
        cardImage[1] = (ImageView) findViewById(R.id.cardBack2);
        cardImage[2] = (ImageView) findViewById(R.id.cardBack3);
        cardImage[3] = (ImageView) findViewById(R.id.cardBack4);
        cardImage[4] = (ImageView) findViewById(R.id.cardBack5);
        cardImage[5] = (ImageView) findViewById(R.id.cardBack6);
        cardImage[6] = (ImageView) findViewById(R.id.cardBack7);
        cardImage[7] = (ImageView) findViewById(R.id.cardBack8);
        cardImage[8] = (ImageView) findViewById(R.id.cardBack9);
        cardImage[9] = (ImageView) findViewById(R.id.cardBack10);
        cardImage[10] = (ImageView) findViewById(R.id.cardBack11);
        cardImage[11] = (ImageView) findViewById(R.id.cardBack12);
        cardImage[12] = (ImageView) findViewById(R.id.cardBack13);
        cardDealerPlus = (ImageView) findViewById(R.id.cardBackDealerPlus);

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

                whatCard(cardImage[cardOrderNo], playerCardList[playerOrderNo].name) ;
                cardAnimation(cardOrderNo);

                playerOrderNo++;//플레이어용 카드 패 번호
                cardOrderNo++;//전체 카드에 대한 순서 번호 값 증가

                //플레이어의 점수 확인
                if(playerScore>21){
                    btnHit.setEnabled(false);//Hit 버튼 비활성화
                    btnStay.setEnabled(false);//Stay 버튼 비활성화

                    endIntent.putExtra("playerResult","LOSE");

                    endIntent.putExtra("playerScore",playerScore);
                    endIntent.putExtra("dealerScore",dealerScore);

                    Context context=getApplicationContext();
                    int dur= Toast.LENGTH_SHORT;
                    Toast toast=Toast.makeText(context,"버스트 되었습니다. 당신의 패배입니다...",dur);
                    toast.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            whatCard(cardImage[3], dealerCardList[1].name);//빠끄
                        }
                    },1500);

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

                whatCard(cardImage[3], dealerCardList[1].name);

                //딜러의 점수가 16점 이하면 카드 1장 더 받음
                if(dealerScore<=16){
                    dealerCardList[dealerOrderNo]=cardList[cardOrderNo];//카드 목록에서 카드 받아오기
                    dealerScore+=cardList[cardOrderNo].value;//플레이어 점수 추가

                    dealerOrderNo++;//플레이어용 카드 패 번호
                    cardOrderNo++;//전체 카드에 대한 순서 번호 값 증가

                    whatCard(cardDealerPlus, dealerCardList[2].name);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CardDealerPlus();
                        }
                    },500);

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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //플레이어 1장 (공개)
                playerCardList[0]=cardList[0];
                playerScore+=playerCardList[0].value;
                whatCard(cardImage[0], playerCardList[0].name) ;
                cardAnimation(0);
            }
        },6000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //딜러 1장 (공개)
                dealerCardList[0]=cardList[1];
                dealerScore+=dealerCardList[0].value;
                whatCard(cardImage[1], dealerCardList[0].name) ;
                cardAnimation(1);
            }
        },6700);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //플레이어 1장 (공개)
                playerCardList[1]=cardList[2];
                playerScore+=playerCardList[1].value;
                whatCard(cardImage[2], playerCardList[1].name) ;
                cardAnimation(2);
            }
        },7400);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //딜러 1장 (비공개)
                dealerCardList[1]=cardList[3];
                dealerScore+=dealerCardList[1].value;
                cardAnimation(3);
            }
        },8100);
        //====================== 분배 작업 종료 =====================================================

        //플레이어의 카드 패 먼저 확인 후 딜러의 카드 패 값 확인
        if(playerScore>21){
            btnHit.setEnabled(false);//Hit 버튼 비활성화
            btnStay.setEnabled(false);//Stay 버튼 비활성화

            endIntent.putExtra("playerResult","LOSE");

            Context context=getApplicationContext();
            int dur= Toast.LENGTH_SHORT;
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

    //점수 계산
    public void calcResult(){
        if(playerScore>dealerScore && playerScore!=21){
            //승리 + 블랙잭 O
            gameResultToast="당신의 승리입니다!";
            endIntent.putExtra("playerResult","WIN");
        }
        else if(playerScore>dealerScore&& playerScore==21){
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
    }//calcResult 종료

    //카드 애니메이션
    public void cardAnimation(int no){
        Animation card;

        switch (no){
            case 0:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card1);
                cardImage[no].startAnimation(card);
                break;
            case 1:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card2);
                cardImage[no].startAnimation(card);
                break;
            case 2:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card3);
                cardImage[no].startAnimation(card);
                break;
            case 3:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card4);
                cardImage[no].startAnimation(card);
                break;
            case 4:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card5);
                cardImage[no].startAnimation(card);
                break;
            case 5:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card6);
                cardImage[no].startAnimation(card);
                break;
            case 6:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card7);
                cardImage[no].startAnimation(card);
                break;
            case 7:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card8);
                cardImage[no].startAnimation(card);
                break;
            case 8:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card9);
                cardImage[no].startAnimation(card);
                break;
            case 9:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card10);
                cardImage[no].startAnimation(card);
                break;
            case 10:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card11);
                cardImage[no].startAnimation(card);
                break;
            case 11:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card12);
                cardImage[no].startAnimation(card);
                break;
            case 12:
                card = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card13);
                cardImage[no].startAnimation(card);
                break;
        }
        
    }//cardAnimation 종료
    public void CardDealerPlus(){//딜러 추가 카드 (공개)
        Animation cardDealPlus = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.carddealerplus);
        cardDealerPlus.startAnimation(cardDealPlus);
    }//카드 에니메이션 부분 종료

    //카드 내용
    public void whatCard(ImageView imageView, String cardname){
        switch (cardname){
            case "SpadeA":
                imageView.setImageResource(R.drawable.c01_2);
                break;
            case "Spade2":
                imageView.setImageResource(R.drawable.c02_2);
                break;
            case "Spade3":
                imageView.setImageResource(R.drawable.c03_2);
                break;
            case "Spade4":
                imageView.setImageResource(R.drawable.c04_2);
                break;
            case "Spade5":
                imageView.setImageResource(R.drawable.c05_2);
                break;
            case "Spade6":
                imageView.setImageResource(R.drawable.c06_2);
                break;
            case "Spade7":
                imageView.setImageResource(R.drawable.c07_2);
                break;
            case "Spade8":
                imageView.setImageResource(R.drawable.c08_2);
                break;
            case "Spade9":
                imageView.setImageResource(R.drawable.c09_2);
                break;
            case "Spade10":
                imageView.setImageResource(R.drawable.c10_2);
                break;
            case "SpadeJ":
                imageView.setImageResource(R.drawable.c11_2);
                break;
            case "SpadeQ":
                imageView.setImageResource(R.drawable.c12_2);
                break;
            case "SpadeK":
                imageView.setImageResource(R.drawable.c13_2);
                break;
            case "HeartA":
                imageView.setImageResource(R.drawable.c01_1);
                break;
            case "Heart2":
                imageView.setImageResource(R.drawable.c02_1);
                break;
            case "Heart3":
                imageView.setImageResource(R.drawable.c03_1);
                break;
            case "Heart4":
                imageView.setImageResource(R.drawable.c04_1);
                break;
            case "Heart5":
                imageView.setImageResource(R.drawable.c05_1);
                break;
            case "Heart6":
                imageView.setImageResource(R.drawable.c06_1);
                break;
            case "Heart7":
                imageView.setImageResource(R.drawable.c07_1);
                break;
            case "Heart8":
                imageView.setImageResource(R.drawable.c08_1);
                break;
            case "Heart9":
                imageView.setImageResource(R.drawable.c09_1);
                break;
            case "Heart10":
                imageView.setImageResource(R.drawable.c10_1);
                break;
            case "HeartJ":
                imageView.setImageResource(R.drawable.c11_1);
                break;
            case "HeartQ":
                imageView.setImageResource(R.drawable.c12_1);
                break;
            case "HeartK":
                imageView.setImageResource(R.drawable.c13_1);
                break;
            case "CloverA":
                imageView.setImageResource(R.drawable.c01_3);
                break;
            case "Clover2":
                imageView.setImageResource(R.drawable.c02_3);
                break;
            case "Clover3":
                imageView.setImageResource(R.drawable.c03_3);
                break;
            case "Clover4":
                imageView.setImageResource(R.drawable.c04_3);
                break;
            case "Clover5":
                imageView.setImageResource(R.drawable.c05_3);
                break;
            case "Clover6":
                imageView.setImageResource(R.drawable.c06_3);
                break;
            case "Clover7":
                imageView.setImageResource(R.drawable.c07_3);
                break;
            case "Clover8":
                imageView.setImageResource(R.drawable.c08_3);
                break;
            case "Clover9":
                imageView.setImageResource(R.drawable.c09_3);
                break;
            case "Clover10":
                imageView.setImageResource(R.drawable.c10_3);
                break;
            case "CloverJ":
                imageView.setImageResource(R.drawable.c11_3);
                break;
            case "CloverQ":
                imageView.setImageResource(R.drawable.c12_3);
                break;
            case "CloverK":
                imageView.setImageResource(R.drawable.c13_3);
                break;
            case "DiamondA":
                imageView.setImageResource(R.drawable.c01_4);
                break;
            case "Diamond2":
                imageView.setImageResource(R.drawable.c02_4);
                break;
            case "Diamond3":
                imageView.setImageResource(R.drawable.c03_4);
                break;
            case "Diamond4":
                imageView.setImageResource(R.drawable.c04_4);
                break;
            case "Diamond5":
                imageView.setImageResource(R.drawable.c05_4);
                break;
            case "Diamond6":
                imageView.setImageResource(R.drawable.c06_4);
                break;
            case "Diamond7":
                imageView.setImageResource(R.drawable.c07_4);
                break;
            case "Diamond8":
                imageView.setImageResource(R.drawable.c08_4);
                break;
            case "Diamond9":
                imageView.setImageResource(R.drawable.c09_4);
                break;
            case "Diamond10":
                imageView.setImageResource(R.drawable.c10_4);
                break;
            case "DiamondJ":
                imageView.setImageResource(R.drawable.c11_4);
                break;
            case "DiamondQ":
                imageView.setImageResource(R.drawable.c12_4);
                break;
            case "DiamondK":
                imageView.setImageResource(R.drawable.c13_4);
                break;
        }
    }//whatCard 종료
}//gameActivity 종료
