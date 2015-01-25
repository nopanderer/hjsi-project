package hjsi.activity;

import hjsi.common.AppManager;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Loader extends Base {
  private static final int LOGO_COMPLETE = 0;
  private static final int LOADING_COMPLETE = 1;

  AnimationDrawable mAni;

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

    // 앞으로 AppManager에서 사용할 AssetManager를 설정하고 미리 초기화한다.
    AppManager.getInstance().setAssetManager(getAssets());

    worker.start();
  }

  @Override
  public void onBackPressed() {
    // Back 키 눌러도 동작하지 않도록 아무것도 하지 않음. (로그만 출력)
    AppManager.printSimpleLog();
  }

  @SuppressLint("HandlerLeak")
  Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == LOADING_COMPLETE) {
        mAni.stop();
        mAni = null;

        Intent lunchGame = new Intent(getApplicationContext(), Game.class);
        startActivity(lunchGame); // Game 실행
        finish(); // Loader 종료

        AppManager.printDetailLog("로딩 완료");
      }
    }

  };

  Runnable loadingMethod = new Runnable() {
    @Override
    public void run() {
      try {
        /*
         * 저장된 게임 진행 상태를 먼저 불러온다. 진행 상태를 토대로 앞으로 필요한 각종 리소스를 불러온다.
         */

        /*
         * 공통적인 리소스를 준비한다. (일정 범주의 모든 이미지를 불러오는 방법)
         */
        HashMap<String, String> common = AppManager.getInstance().getFilesRecursively("img/common");
        if (common != null) {
          for (String key : common.keySet()) {
            AppManager.getInstance().loadBitmapAsset(key);
          }
        }

        /*
         * 동상 이미지를 준비한다. (특정한 이미지를 불러오는 방법)
         */
        AppManager.getInstance().loadBitmapAsset("owl");


        Thread.sleep(2000); // 여기서 로딩 작업을 한다고 치고..
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      mHandler.sendEmptyMessage(LOADING_COMPLETE);
    }
  };
}
