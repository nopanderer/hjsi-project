package exam.androidproject;

import hjsi.timer.TimeManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import exam.game.AppManager;
import exam.game.GameMaster;
import exam.game.GameState;

public class Map extends BaseActivity implements OnClickListener {
  private boolean explicitQuit = false; // Map에서 사용한 리소스 해제 타이밍을 위한 변수

  Button settingBtn, btnBook, btnStore;
  ToggleButton btnPlay;
  Drawable drawableBtnPlay_Pause;
  Drawable drawableBtnPlay_Play;
  Setting setting;
  public static MediaPlayer music;

  /* 게임 정보를 갖고있는 개체 */
  private GameState gameState;
  /* 게임을 진행하는 인게임 스레드를 가진 개체 */
  private GameMaster gameMaster;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLogInfo();
    super.onCreate(savedInstanceState);

    // GameState 생성
    gameState = new GameState(getResources());

    // surfaceview 등록
    GameSurface gameView = new GameSurface(this, gameState);
    setContentView(gameView);

    LayoutInflater inflater = getLayoutInflater();
    View layout = inflater.inflate(R.layout.activity_map, null);
    addContentView(layout, new LinearLayout.LayoutParams(
        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT));

    drawableBtnPlay_Pause = getResources().getDrawable(R.drawable.btn_pause);
    drawableBtnPlay_Play = getResources().getDrawable(R.drawable.btn_play);

    settingBtn = (Button) findViewById(R.id.setting_btn);
    btnBook = (Button) findViewById(R.id.btn_book);
    btnPlay = (ToggleButton) findViewById(R.id.btn_play);
    btnStore = (Button) findViewById(R.id.btn_store);

    setting = new Setting(Map.this);
    setting.setCanceledOnTouchOutside(false);
    setting.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface arg0) {
        AppManager.printSimpleLogInfo();
        quitExplicitly(); // 이번에 Map.onDestroy() 될 때 리소스 해제하라고 알림
        AppManager.getInstance().quitApp();
      }
    });

    settingBtn.setOnClickListener(this);
    btnBook.setOnClickListener(this);
    btnPlay.setOnClickListener(this);
    btnStore.setOnClickListener(this);

    Map.music = MediaPlayer.create(this, R.raw.bgm);
    Map.music.setLooping(true);
    Map.music.start();

    /* GameMaster 생성 */
    gameMaster = new GameMaster(gameView.getHolder(), gameState);
  }

  @Override
  protected void onPause() {
    AppManager.printSimpleLogInfo();
    super.onStop();

    if (gameMaster != null) {
      gameMaster.pauseGame();
      TimeManager.getInstance().stop();
      btnPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_play));
    }

    if (Map.music != null) {
      Map.music.pause();
    }
  }

  @Override
  protected void onResume() {
    AppManager.printSimpleLogInfo();
    super.onResume();

    if (Map.music != null) {
      Map.music.start();
    }
  }

  @Override
  protected void onDestroy() {
    AppManager.printSimpleLogInfo();
    super.onDestroy();

    if (gameMaster != null) {
      gameMaster.quitGame();
      TimeManager.getInstance().stop();
    }

    if (Map.music != null) {
      Map.music.stop();
      Map.music.release();
    }

    if (explicitQuit) {
      /* 사용했던 리소스를 해제한다. */
      AppManager.getInstance().allRecycle();
    }
  }

  /**
   * back 키를 누르면 옵션 메뉴가 열리도록 함
   */
  @Override
  public void onBackPressed() {
    AppManager.printSimpleLogInfo();
    showSettingMenu();
  }

  @Override
  public void onClick(View v) {
    Log.d(toString(), AppManager.getMethodName() + "() " + v.toString());

    if (v == settingBtn) {
      showSettingMenu();
    } else if (v == btnBook) {
      Intent Book = new Intent(Map.this, Picturebook.class);
      startActivity(Book);
    } else if (v == btnPlay) {
      if (btnPlay.isChecked()) {
        btnPlay.setBackgroundDrawable(drawableBtnPlay_Pause);
        gameMaster.playGame();
        TimeManager.getInstance().start();
      } else {
        btnPlay.setBackgroundDrawable(drawableBtnPlay_Play);
        gameMaster.pauseGame();
        TimeManager.getInstance().stop();
      }
    } else if (v == btnStore) {
      Intent Store = new Intent(Map.this, Store.class);
      startActivity(Store);
    }
  }

  public void quitExplicitly() {
    AppManager.printSimpleLogInfo();
    explicitQuit = true;
  }

  private void showSettingMenu() {
    AppManager.printSimpleLogInfo();
    setting.show();

    if (btnPlay.isChecked()) {
      btnPlay.setBackgroundDrawable(drawableBtnPlay_Play);
      gameMaster.pauseGame();
      TimeManager.getInstance().stop();
    }
  }
}
