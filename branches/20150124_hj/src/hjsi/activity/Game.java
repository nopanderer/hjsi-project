package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.GameSurface;
import hjsi.game.GameMaster;
import hjsi.game.GameState;
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

public class Game extends Base implements OnClickListener {
  private boolean explicitQuit = false; // Map에서 사용한 리소스 해제 타이밍을 위한 변수

  Button settingBtn, btnBook, btnStore;
  ToggleButton btnPlay;
  Drawable drawableBtnPlay_Pause;
  Drawable drawableBtnPlay_Play;
  DlgSetting dlgSetting;
  public static MediaPlayer music;

  /* 게임을 진행하는 인게임 스레드를 가진 개체 */
  private GameMaster gameMaster;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);
    
    GameState.getInstance().initState(getResources());
    
    // surfaceview 등록
    GameSurface gameView = new GameSurface(this);
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

    dlgSetting = new DlgSetting(Game.this);
    dlgSetting.setCanceledOnTouchOutside(false);
    dlgSetting.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface arg0) {
        AppManager.printSimpleLog();
        quitExplicitly(); // 이번에 Game.onDestroy() 될 때 리소스 해제하라고 알림
        AppManager.getInstance().quitApp();
      }
    });

    settingBtn.setOnClickListener(this);
    btnBook.setOnClickListener(this);
    btnPlay.setOnClickListener(this);
    btnStore.setOnClickListener(this);

    Game.music = MediaPlayer.create(this, R.raw.bgm);
    Game.music.setLooping(true);
    Game.music.start();


    /* GameMaster 생성 */
    gameMaster = new GameMaster();

    Log.d(getClass().getSimpleName(), AppManager.getMethodName() + " is finished.");
  }

  @Override
  protected void onPause() {
    AppManager.printSimpleLog();
    super.onStop();

    if (gameMaster != null) {
      gameMaster.pauseGame();
      TimeManager.pauseTime();
      btnPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_play));
    }

    if (Game.music != null) {
      Game.music.pause();
    }
  }

  @Override
  protected void onResume() {
    AppManager.printSimpleLog();
    super.onResume();

    if (Game.music != null) {
      Game.music.start();
    }
  }

  @Override
  protected void onDestroy() {
    AppManager.printSimpleLog();
    super.onDestroy();

    if (gameMaster != null) {
      gameMaster.quitGame();
      TimeManager.stopTime();
    }

    if (music != null) {
      music.stop();
      music.release();
    }

    if (explicitQuit) {
      /* 사용했던 리소스를 해제한다. */
      AppManager.getInstance().allRecycle();
      GameState.getInstance().purgeGameState(); // 게임 상태정보를 없앤다.
    }
  }

  /**
   * back 키를 누르면 옵션 메뉴가 열리도록 함
   */
  @Override
  public void onBackPressed() {
    AppManager.printSimpleLog();
    showSettingMenu();
  }

  @Override
  public void onClick(View v) {
    Log.d(toString(), AppManager.getMethodName() + "() " + v.toString());

    if (v == settingBtn) {
      showSettingMenu();
    } else if (v == btnBook) {
      Intent Book = new Intent(Game.this, RecipeBook.class);
      startActivity(Book);
    }
    /*
     * play 버튼
     */
    else if (v == btnPlay) {
      if (btnPlay.isChecked()) {
        btnPlay.setBackgroundDrawable(drawableBtnPlay_Pause);
        gameMaster.playGame();
        TimeManager.startTime();
      } else {
        btnPlay.setBackgroundDrawable(drawableBtnPlay_Play);
        gameMaster.pauseGame();
        TimeManager.pauseTime();
      }
    } else if (v == btnStore) {
      Intent Store = new Intent(Game.this, Store.class);
      startActivity(Store);
    }
  }

  public void quitExplicitly() {
    AppManager.printSimpleLog();
    explicitQuit = true;
  }

  private void showSettingMenu() {
    AppManager.printSimpleLog();
    dlgSetting.show();

    if (btnPlay.isChecked()) {
      btnPlay.setChecked(false);
      btnPlay.setBackgroundDrawable(drawableBtnPlay_Play);
      gameMaster.pauseGame();
      TimeManager.pauseTime();
    }
  }
}
