package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.DataManager;

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
         * 공통적인 리소스를 준비한다. (특정 경로의 모든 이미지를 불러오는 방법)
         */
        Bitmap bitmap;
        HashMap<String, String> pathMap = AppManager.getPathMap("img/common");
        Set<String> keySet = pathMap.keySet();
        Options opts = new Options();
        opts.inPreferredConfig = Config.RGB_565;
        for (String key : keySet) {
          bitmap = AppManager.readImageFile(pathMap.get(key), opts);
          AppManager.addBitmap(key, bitmap);
        }

        /*
         * 동상 이미지를 준비한다. 구체적인 경로 입력으로 바로 가져올 수도 있음.
         */
        bitmap = AppManager.readImageFile("img/statues/statue1.png", opts);
        if (bitmap != null) {
          AppManager.addBitmap("statue1", bitmap);
        }

        /*
         * 임시적인 타워 비트맵 삽입
         */
        bitmap = AppManager.readImageFile("img/towers/tower1.png", opts);
        if (bitmap != null) {
          AppManager.addBitmap("tower1", bitmap);
        }

        /*
         * 임시적인 투사체 비트맵 삽입
         */
        opts.inSampleSize = 16;
        bitmap = AppManager.readImageFile("img/projectile/proj1.png", opts);
        if (bitmap != null) {
          AppManager.addBitmap("proj1", bitmap);
        }

        /*
         * 어플리케이션 최초 실행시, 사용할 데이터베이스를 구축해놓는다. TODO 이 부분을 로더에서 제일 먼저 실행해야 하는데, 현재 GameState
         * 생성자에서는 위에서 로드하는 이미지를 사용해서 어쩔 수 없이 이걸 밑에서 실행함.
         */
        DataManager.loadDatabase(getApplicationContext(), 1);



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
