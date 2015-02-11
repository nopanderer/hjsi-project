package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.Camera;
import hjsi.common.GameSurface;
import hjsi.game.GameMaster;
import hjsi.game.GameState;
import hjsi.game.Unit;
import hjsi.timer.TimeManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ToggleButton;

public class Game extends Base implements OnClickListener {
  private boolean explicitQuit = false; // Map에서 사용한 리소스 해제 타이밍을 위한 변수

  Button btnSetting, btnBook, btnStore, btnDeploy;
  ToggleButton btnPlay;
  Drawable drawableBtnPlay_Pause;
  Drawable drawableBtnPlay_Play;
  DlgSetting dlgSetting;
  public static MediaPlayer music;

  /** 게임을 진행하는 인게임 스레드를 가진 개체 */
  private GameMaster gameMaster;
  /** 카메라 */
  private Camera camera;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);

    GameState.getInstance().initState();

    /*
     * 화면 비율을 구해서 카메라를 생성할 때 넘겨준다.
     */
    camera = new Camera(AppManager.getInstance().getDisplayFactor());
    /*
     * surfaceview 생성 및 등록
     */
    GameSurface gameView = new GameSurface(getApplicationContext(), camera);
    setContentView(gameView);

    /*
     * 기타 버튼 UI 등록
     */
    View layout = getLayoutInflater().inflate(R.layout.activity_game, null);
    addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    /*
     * 버튼 뷰 참조자 및 드로블 가져옴
     */
    drawableBtnPlay_Pause = getResources().getDrawable(R.drawable.btn_pause);
    drawableBtnPlay_Play = getResources().getDrawable(R.drawable.btn_play);

    btnSetting = (Button) findViewById(R.id.btn_setting);
    btnSetting.setOnClickListener(this);
    btnBook = (Button) findViewById(R.id.btn_book);
    btnBook.setOnClickListener(this);
    btnPlay = (ToggleButton) findViewById(R.id.btn_play);
    btnPlay.setOnClickListener(this);
    btnStore = (Button) findViewById(R.id.btn_store);
    btnStore.setOnClickListener(this);
    btnDeploy = (Button) findViewById(R.id.btn_deploy);
    btnDeploy.setOnClickListener(this);

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

    Game.music = MediaPlayer.create(this, R.raw.bgm);
    Game.music.setLooping(true);
    Game.music.start();

    /* GameMaster 생성 */
    gameMaster = new GameMaster();

    AppManager.printDetailLog(getClass().getSimpleName() + " 초기화 완료");
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
    AppManager.printDetailLog(v.toString());

    if (v == btnSetting) {
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
    } else if (v == btnDeploy) {
      if (GameState.getInstance().checkDeployMode() == false)
        GameState.getInstance().intoDeployMode();
      else
        GameState.getInstance().inHand = null;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    /*
     * 기본적인 View 객체에 대한 이벤트는 해당 객체가 먼저 이벤트를 받아서 처리하므로, 여기서는 카메라 및 유닛 등의 조작에 대해서만
     * 고려한다. 다음은 터치 이벤트 중 스크롤 및 핀치줌 인/아웃에 관한 동작은 카메라가 처리하도록 한다.
     */
    if (camera.touchHandler(event))
      return true;

    /*
     * 카메라가 처리할 이벤트가 아닌 경우는 보통의 클릭 동작이며, 여러가지 게임 개체에 대한 동작을 수행한다. 가장 먼저, 화면 터치
     * 좌표를 게임월드의 좌표로 변환한다.
     */
    int x = (int) ((event.getX() + camera.getX()) / camera.getScale());
    int y = (int) ((event.getY() + camera.getY()) / camera.getScale());

    Unit unit = GameState.getInstance().getUnit(x, y);
    if (unit != null) {
      AppManager.printInfoLog(unit.toString());
    }


    return super.onTouchEvent(event);
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
