package exam.androidproject;

import hjsi.common.AppManager;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Continue extends BaseActivity {
  AnimationDrawable mAni;

  Runnable loadingMethod = new Runnable() {
    @Override
    public void run() {
      try {
        /*
         * 저장된 게임 진행 상태를 먼저 불러온다. 진행 상태를 토대로 앞으로 필요한 각종 리소스를 불러온다.
         */



        /*
         * 공통적인 리소스를 불러온다.
         */
        Bitmap imgRes = BitmapFactory.decodeResource(getResources(), R.drawable.img_tempmap);
        AppManager.getInstance().addBitmap("background", imgRes);

        Options opt = new Options();
        opt.outWidth = 80;
        opt.outHeight = 100;
        imgRes = BitmapFactory.decodeResource(getResources(), R.drawable.img_unit_statue_owl, opt);
        AppManager.getInstance().addBitmap("statue", imgRes);

        Thread.sleep(2000); // 여기서 로딩 작업을 한다고 치고..
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      mHandler.sendEmptyMessage(0);
    }
  };

  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == 0) {
        mAni.stop();
        mAni = null;

        setResult(Activity.RESULT_OK);
        finish();
        AppManager.printSimpleLog();
      }
    }

  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_continue);

    String[] strHelp = getResources().getStringArray(R.array.help_text);
    TextView helpTextView = (TextView) findViewById(R.id.helpTextView);
    helpTextView.setText(strHelp[0]);

    Thread worker = new Thread(loadingMethod);

    LinearLayout loading = (LinearLayout) findViewById(R.id.loading);
    mAni = (AnimationDrawable) loading.getBackground();
    mAni.setOneShot(false);
    mAni.start();
    worker.start();
  }

  @Override
  public void onBackPressed() {
    // Back 키 눌러도 동작하지 않도록 아무것도 하지 않음. (로그만 출력)
    AppManager.printSimpleLog();
  }
}
