package hjsi.activity;

import hjsi.common.AppManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
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
    setContentView(R.layout.activity_loader);

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

    Rect displayRect = new Rect();
    getWindowManager().getDefaultDisplay().getRectSize(displayRect);
    AppManager.getInstance().setDisplayFactor(displayRect.right, displayRect.bottom);

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
         * 테스트용 코드
         */
        HashMap<String, String> pathMap = AppManager.getInstance().getPathMap("db");
        AppManager.getInstance().readTextFile(pathMap.get("unit_spec_table"));

        /*
         * 공통적인 리소스를 준비한다. (특정 경로의 모든 이미지를 불러오는 방법)
         */
        Bitmap bitmap;
        pathMap = AppManager.getInstance().getPathMap("img/common");
        Set<String> keySet = pathMap.keySet();
        Options opts = new Options();
        opts.inPreferredConfig = Config.RGB_565;
        for (String key : keySet) {
          bitmap = AppManager.getInstance().readImageFile(pathMap.get(key), opts);
          AppManager.getInstance().addBitmap(key, bitmap);
        }

        /*
         * 동상 이미지를 준비한다. 구체적인 경로 입력으로 바로 가져올 수도 있음.
         */
        bitmap = AppManager.getInstance().readImageFile("img/statues/statue1.png", opts);
        if (bitmap != null) {
          AppManager.getInstance().addBitmap("statue1", bitmap);
        }


        Thread.sleep(2000); // 여기서 로딩 작업을 한다고 치고..
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      mHandler.sendEmptyMessage(LOADING_COMPLETE);
    }
  };
}
