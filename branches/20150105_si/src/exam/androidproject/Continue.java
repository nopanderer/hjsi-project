package exam.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;
import exam.game.AppManager;

public class Continue extends BaseActivity {
  AnimationDrawable mAni;
  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == 0) {
        setResult(RESULT_OK);
        finish();
        AppManager.printSimpleLogInfo();
      }
    }

  };

  Bitmap background;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLogInfo();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_continue);

    String[] strHelp = getResources().getStringArray(R.array.help_text);
    TextView helpTextView = (TextView) findViewById(R.id.helpTextView);
    helpTextView.setText(strHelp[0]);

    Thread worker = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          /*
           * 그리기에 필요한 각종 리소스를 불러온다
           */
          background = BitmapFactory.decodeResource(getResources(), R.drawable.img_tempmap); // 맵 배경
          AppManager.getInstance().addBitmap("background", background);

          Thread.sleep(2000); // 여기서 로딩 작업을 한다고 치고..
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        mHandler.sendEmptyMessage(0);
        mAni.stop();
        mAni = null;
      }
    });

    LinearLayout loading = (LinearLayout) findViewById(R.id.loading);
    mAni = (AnimationDrawable) loading.getBackground();
    mAni.setOneShot(false);
    mAni.start();
    worker.start();
  }

  @Override
  public void onBackPressed() {
    // Back 키 눌러도 동작하지 않도록 아무것도 하지 않음. (로그만 출력)
    AppManager.printSimpleLogInfo();
  }
}
