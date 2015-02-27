package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.DataManager;
import hjsi.common.GameController;
import hjsi.common.GameSurface;
import hjsi.common.Timer;
import hjsi.game.GameMaster;
import hjsi.game.GameState;
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
   * 게임에서 사용한 리소스 해제 타이밍을 위한 변수
   */
  private boolean explicitQuit = false;

  /* 자식 뷰들 */
  private Button btnBook, btnPause, btnStore, btnDeploy;
  private ToggleButton btnFF;
  private Drawable btnGen;
  private DlgSetting dlgSetting;

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

  @Override
  protected void onResume() {
    AppManager.printSimpleLog();
    super.onResume();

    if (bgMusic != null) {
      bgMusic.start();
    }
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

    if (explicitQuit) {
      // 사용했던 리소스를 해제한다.
      AppManager.allRecycle();
    }
  }

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
      consumed = surface.handleTouchEvent(event);
    }

    if (!consumed) {
      // 이 액티비티의 자식 뷰가 처리한 이벤트는 이 메소드까지 전달되지 않는다.
      // 위에서 버튼들과 서피스뷰가 이 액티비티의 자식으로 등록되어 있으니, 이 메소드에서는 유닛이나 다른 클릭 조작만 고려한다.

      // 터치로 입력받은 화면상의 좌표를 보여지는 게임월드 비율에 맞게 변환함
      event = surface.convertGameEvent(event);
      AppManager.printEventLog(event);

      Unit unit = gState.getUnit(event.getX(), event.getY());
      if (unit != null) {
        AppManager.printInfoLog(unit.toString());
      } else if (gState.checkDeployMode()) {
        gState.deployTower(event.getX(), event.getY());
      }

      consumed = super.onTouchEvent(event);
    }

    return consumed;
  }

  public void quitExplicitly() {
    AppManager.printSimpleLog();
    explicitQuit = true;
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

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#resumeGame()
   */
  public void resumeGame() {
    if (dlgSetting.isShowing())
      dlgSetting.dismiss();
    gameMaster.playGame();
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#pauseGame()
   */
  @Override
  public void pauseGame() {
    gameMaster.pauseGame();
    if (!explicitQuit && !(dlgSetting.isShowing())) {
      dlgSetting.show();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#quitGame()
   */
  @Override
  public void quitGame() {
    bgMusic.stop();
    bgMusic.release();
    bgMusic = null;
    quitExplicitly(); // 다음번 Game.onDestroy()가 호출될 때 리소스를 해제하라고 알림
    AppManager.quitApp();
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#startWave()
   */
  @Override
  public void startWave() {
    gState.waveReady();
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#finishWave()
   */
  @Override
  public void finishWave() {}

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#toggleSound()
   */
  @Override
  public void toggleSound() {
    if (!(bgmPlaying = !bgmPlaying))
      bgMusic.pause();
    else
      bgMusic.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.common.GameController#drawWaveButton(android.graphics.Canvas)
   */
  @Override
  public void drawWaveButton(Canvas canvas) {
    if (!gState.isWaveStarted()) {
      btnGen.draw(canvas);
    }
  }
}
