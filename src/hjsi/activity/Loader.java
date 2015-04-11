package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.DataManager;
import hjsi.game.GameState;
import hjsi.game.Unit.Type;

import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Loader extends Base {
  private AnimationDrawable mAni;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_loader);

    /* AppManager가 사용할 AssetManager를 설정하고 미리 초기화한다. */
    AppManager.setAssetManager(getAssets());

    /* 기기의 해상도를 구해서 AppManager에 각종 비율 변수를 설정한다. */
    Rect displayRect = new Rect();
    getWindowManager().getDefaultDisplay().getRectSize(displayRect);
    AppManager.calculateVariousFactors(displayRect.right, displayRect.bottom);

    /* TODO 제거할지 말지 결정, 도움말 불러오는 부분. */
    String[] strHelp = getResources().getStringArray(R.array.help_text);
    TextView helpTextView = (TextView) findViewById(R.id.helpTextView);
    helpTextView.setText(strHelp[0]);

    /* 데이터 로딩을 담당하는 스레드를 생성해서 돌린다. */
    Thread worker = new Thread(loadingMethod);
    worker.start();

    /* 로딩 애니메이션. 로고 재생 후 광고 보여주는 걸로 대체할 예정 */
    LinearLayout loading = (LinearLayout) findViewById(R.id.loading);
    mAni = (AnimationDrawable) loading.getBackground();
    mAni.setOneShot(false);
    mAni.start();
  }

  @Override
  public void onBackPressed() {
    // 실수로 Back 키 눌러도 동작하지 않도록 아무것도 하지 않음. (로그만 출력)
    AppManager.printSimpleLog();
  }

  private Runnable loadingMethod = new Runnable() {
    @Override
    public void run() {
      try {
        /*
         * 저장된 게임 진행 상태를 먼저 불러온다. 진행 상태를 토대로 앞으로 필요한 각종 리소스를 불러온다.
         */
        GameState gState = new GameState();
        DataManager.initializeDatabase(getApplicationContext(), 1);
        DataManager.loadUserData(gState);
        AppManager.putGameState(gState);

        Bitmap bitmap;
        Options opts = new Options();
        opts.inPreferredConfig = Config.RGB_565;

        /* 항상 사용되는 이미지를 준비한다. (특정 경로의 모든 이미지를 불러오는 방법) */
        HashMap<String, String> pathMap = AppManager.getPathMap("img/common");
        for (String key : pathMap.keySet()) {
          bitmap = AppManager.readImageFile(pathMap.get(key), opts);
          AppManager.addBitmap(key, bitmap);
        }

        /* 임시적인 투사체 비트맵 삽입 */
        opts.inSampleSize = 16;
        bitmap = AppManager.readUnitImageFile(Type.PROJECTILE, 1, opts);
        if (bitmap != null) {
          AppManager.addBitmap(Type.PROJECTILE.toString() + 1, bitmap);
        }

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }

      AppManager.printDetailLog("로딩 완료");

      mAni.stop();
      mAni = null;

      finish(); // Loader 종료 요청
      Intent lunchGame = new Intent(getApplicationContext(), Game.class);
      startActivity(lunchGame); // Game 실행 요청
    }
  };
}
