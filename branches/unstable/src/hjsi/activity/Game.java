package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.DataManager;
import hjsi.common.GameController;
import hjsi.common.GameSurface;
import hjsi.common.Timer;
import hjsi.game.GameMaster;
import hjsi.game.GameState;
import hjsi.game.Tower;
import hjsi.game.Unit;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ToggleButton;

public class Game extends Base implements OnClickListener, GameController {
  /**
   * bgm 재생 객체
   */
  private MediaPlayer bgMusic;
  /**
   * bgm 음소거 여부
   */
  private boolean bgmPlaying = true;
  /**
   * 게임을 진행하는 인게임 스레드를 가진 개체
   */
  private GameMaster gameMaster = null;
  /**
   * 게임 정보 관리 객체
   */
  private GameState gState = null;
  /**
   * 게임 그리기 클래스
   */
  private GameSurface surface = null;
  /**
   * 액티비티가 destroy 될 때, 이 변수가 설정돼 있을 때만 리소스를 해제하기 위한 변수
   */
  private boolean explicitQuit = false;

  /* 자식 뷰들 */
  private Button btnBook, btnPause, btnStore, btnDeploy;
  private ToggleButton btnFF;
  private Drawable btnGen;
  private DlgSetting dlgSetting;

  @Override
  public void drawWaveButton(Canvas canvas) {
    if (!gState.isWaveStarted()) {
      btnGen.draw(canvas);
    }
  }

  @Override
  public void finishWave() {}

  /**
   * back 키를 누르면 옵션 메뉴가 열리도록 함
   */
  @Override
  public void onBackPressed() {
    AppManager.printSimpleLog();
    pauseGame();
  }

  @Override
  public void onClick(View v) {
    AppManager.printDetailLog(v.toString());

    if (v == btnBook) {
      Intent Book = new Intent(Game.this, RecipeBook.class);
      startActivity(Book);
    }

    else if (v == btnPause) {
      pauseGame();
    }

    else if (v == btnStore) {
      Intent Store = new Intent(Game.this, Store.class);
      startActivity(Store);
    }

    else if (v == btnDeploy) {
      gState.refreshArea();
      gState.onDeployMode();
    }

    else if (v == btnFF) {
      Timer.fastForward();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean consumed = false;

    // 웨이브 대기 중일 때 웨이브 시작 버튼을 클릭한 경우
    if (btnGen.getBounds().contains((int) event.getX(), (int) event.getY())
        && !(gState.isWaveStarted())) {
      startWave();
      consumed = true;
    } else {
      // 뷰 및 버튼에 관련된 조작은 여기까지 전달되지 않는다.
      consumed = surface.handleTouchEvent(event);
    }

    /*
     * 게임 자체에 대한 클릭 이벤트 처리
     */
    /* 이 액티비티의 자식 뷰가 처리한 이벤트는 이 블록까지 전달되지 않는다. */
    /* 위에서 버튼들과 서피스뷰가 이 액티비티의 자식으로 등록되어 있으니, 이 블록에서는 유닛이나 다른 클릭 조작만 고려한다. */
    if (!consumed) {
      consumed = true;

      // 터치로 입력받은 화면상의 좌표를 보여지는 게임월드 비율에 맞게 변환함
      event = surface.convertGameEvent(event);
      AppManager.printEventLog(event);

      // 눌러진 유닛을 구한다.
      Unit unit = gState.getUnit(event.getX(), event.getY());

      // 아무 유닛이나 눌러졌을 때
      if (unit != null) {
        unit.setSelected(true);

        if (unit instanceof Tower) {
          gState.setShowTowerMode(true);
          gState.towerToShow((Tower) unit);
        }

        AppManager.printInfoLog(unit.toString());
      }
      // 유닛이 선택된 상태라도 타워 배치를 할 수 있게 앞에 둠
      else if (gState.isDeployMode()) {
        gState.deployTower(event.getX(), event.getY());
      }

      else if (gState.isShowTowerMode()) {
        gState.setShowTowerMode(false);
        for (Unit u : gState.getUnitsClone()) {
          u.setSelected(false);
        }
      }

      // 그 이외의 경우
      else {
        consumed = super.onTouchEvent(event);
      }
    }

    return consumed;
  }

  @Override
  public void pauseGame() {
    gameMaster.pauseGame();
    if (!isExplicitQuit() && !(dlgSetting.isShowing())) {
      dlgSetting.show();
    }
  }

  @Override
  public void quitGame() {
    AppManager.printDetailLog("게임 종료 요청 들어옴");
    bgMusic.stop();
    bgMusic.release();
    bgMusic = null;
    setExplicitQuit(true);
    AppManager.quitApp();
  }

  public void resumeGame() {
    if (dlgSetting.isShowing())
      dlgSetting.dismiss();
    gameMaster.playGame();
  }

  @Override
  public void startWave() {
    gState.waveReady();
  }

  @Override
  public void toggleSound() {
    if (!(bgmPlaying = !bgmPlaying))
      bgMusic.pause();
    else
      bgMusic.start();
  }

  /**
   * @return the explicitQuit
   */
  protected boolean isExplicitQuit() {
    return explicitQuit;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);

    gState = AppManager.getGameState();

    /*
     * surfaceview 생성 및 등록
     */
    surface = new GameSurface(getApplicationContext(), this);
    setContentView(surface);

    /*
     * 기타 버튼 UI 등록
     */
    View layout = getLayoutInflater().inflate(R.layout.activity_game, null);
    addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    /*
     * 버튼 뷰 참조자 및 드로블 가져옴
     */
    btnBook = (Button) findViewById(R.id.btn_book);
    btnBook.setOnClickListener(this);
    btnPause = (Button) findViewById(R.id.btn_pause);
    btnPause.setOnClickListener(this);
    btnStore = (Button) findViewById(R.id.btn_store);
    btnStore.setOnClickListener(this);
    btnDeploy = (Button) findViewById(R.id.btn_deploy);
    btnDeploy.setOnClickListener(this);
    btnFF = (ToggleButton) findViewById(R.id.btn_ff);
    btnFF.setOnClickListener(this);

    int margin =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources()
            .getDisplayMetrics());
    int size =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
            .getDisplayMetrics());
    btnGen = (Drawable) getResources().getDrawable(R.drawable.btn_gen);
    btnGen.setBounds(margin, margin, margin + size, margin + size);

    dlgSetting = new DlgSetting(Game.this, this);
    dlgSetting.setCanceledOnTouchOutside(false);

    bgMusic = MediaPlayer.create(this, R.raw.bgm);
    bgMusic.setLooping(true);
    bgMusic.start();

    /* GameMaster 생성 */
    gameMaster = new GameMaster();
    if (savedInstanceState == null) {
      resumeGame();
    } else {
      pauseGame();
    }

    AppManager.printDetailLog(getClass().getSimpleName() + " 초기화 완료");
  }

  @Override
  protected void onDestroy() {
    AppManager.printSimpleLog();
    super.onDestroy();

    if (dlgSetting != null) {
      if (dlgSetting.isShowing())
        dlgSetting.dismiss();
    }

    if (gameMaster != null) {
      gameMaster.quitGame();
      gameMaster = null;
    }

    if (bgMusic != null) {
      bgMusic.stop();
      bgMusic.release();
      bgMusic = null;
    }

    // 게임 정보를 저장한다.
    synchronized (GameState.class) {
      DataManager.saveUserData(gState);
    }

    if (isExplicitQuit()) {
      // 사용했던 리소스를 해제한다.
      AppManager.allRecycle();
    }
  }

  @Override
  protected void onPause() {
    AppManager.printSimpleLog();
    super.onStop();

    if (gameMaster != null) {
      pauseGame();
    }

    if (bgMusic != null) {
      bgMusic.pause();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
   */
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onRestoreInstanceState(savedInstanceState);

    gState = new GameState();
    try {
      DataManager.loadUserData(gState);
    } catch (IOException e) {
      e.printStackTrace();
    }
    AppManager.putGameState(gState);
    gameMaster.refreshGameState();
    surface.refreshGameState();
    surface.loadCameraState(savedInstanceState.getFloatArray("camera"));
  }

  @Override
  protected void onResume() {
    AppManager.printSimpleLog();
    super.onResume();

    if (bgMusic != null) {
      bgMusic.start();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    AppManager.printSimpleLog();
    super.onSaveInstanceState(outState);

    outState.putFloatArray("camera", surface.saveCameraState());
  }

  /**
   * @param explicitQuit the explicitQuit to set
   */
  protected void setExplicitQuit(boolean explicitQuit) {
    this.explicitQuit = explicitQuit;
  }
}
