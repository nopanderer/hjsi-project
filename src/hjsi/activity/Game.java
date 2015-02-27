package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.DataManager;
import hjsi.common.GameSurface;
import hjsi.common.Timer;
import hjsi.game.GameMaster;
import hjsi.game.GameState;
import hjsi.game.Unit;

import java.io.IOException;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ToggleButton;

public class Game extends Base implements OnClickListener, Handler.Callback {
  /**
   * 게임을 재개하도록 요청한다.
   */
  public static final int HANDLER_GAME_RESUME = 0;
  /**
   * 게임을 멈추도록 요청한다.
   */
  public static final int HANDLER_GAME_PAUSE = 1;
  /**
   * 사운드를 on/off 하도록 요청한다.
   */
  public static final int HANDLER_DLG_SOUND = 2;
  /**
   * 게임을 종료하도록 요청한다.
   */
  public static final int HANDLER_DLG_QUIT = 3;
  /**
   * 몹 생성 버튼을 표시하도록 요청한다.
   */
  public static final int HANDLER_SHOW_SPAWN_BTN = 4;
  /**
   * 몹 생성을 하도록 요청한다.
   */
  public static final int HANDLER_SPAWN_MOBS = 5;

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
  private Button btnBook, btnPause, btnStore, btnDeploy, btnGen;
  private ToggleButton btnFF;
  private DlgSetting dlgSetting;

  private final Handler gameHandler = new Handler(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);

    gState = AppManager.getGameState();

    /*
     * surfaceview 생성 및 등록
     */
    surface = new GameSurface(getApplicationContext());
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
    btnGen = (Button) findViewById(R.id.btn_gen);
    btnGen.setOnClickListener(this);
    btnFF = (ToggleButton) findViewById(R.id.btn_ff);
    btnFF.setOnClickListener(this);

    dlgSetting = new DlgSetting(Game.this, gameHandler);
    dlgSetting.setCanceledOnTouchOutside(false);

    bgMusic = MediaPlayer.create(this, R.raw.bgm);
    bgMusic.setLooping(true);
    bgMusic.start();

    /* GameMaster 생성 */
    gameMaster = new GameMaster(gameHandler);
    if (savedInstanceState == null) {
      // 서피스뷰가 생성되기까지 딜레이가 좀 있어서 게임스레드를 조금 늦게 실행하게 함
      gameHandler.sendMessageDelayed(AppManager.obtainMessage(HANDLER_GAME_RESUME), 300);
    } else {
      gameHandler.sendMessage(AppManager.obtainMessage(HANDLER_GAME_PAUSE));
    }

    AppManager.printDetailLog(getClass().getSimpleName() + " 초기화 완료");
  }

  @Override
  protected void onPause() {
    AppManager.printSimpleLog();
    super.onStop();

    if (gameMaster != null) {
      gameMaster.pauseGame();
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
    gameHandler.sendMessage(AppManager.obtainMessage(HANDLER_GAME_PAUSE));
  }

  @Override
  public void onClick(View v) {
    AppManager.printDetailLog(v.toString());

    if (v == btnBook) {
      Intent Book = new Intent(Game.this, RecipeBook.class);
      startActivity(Book);
    }

    else if (v == btnPause) {
      gameHandler.sendMessage(AppManager.obtainMessage(HANDLER_GAME_PAUSE));
    }

    else if (v == btnStore) {
      Intent Store = new Intent(Game.this, Store.class);
      startActivity(Store);
    }

    else if (v == btnDeploy) {
      gState.refreshArea();
      gState.onDeployMode();
    }

    else if (v == btnGen) {
      gameHandler.sendMessage(AppManager.obtainMessage(HANDLER_SPAWN_MOBS));
    }

    else if (v == btnFF) {
      Timer.fastForward();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
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

    return super.onTouchEvent(event);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.os.Handler.Callback#handleMessage(android.os.Message)
   */
  @Override
  public boolean handleMessage(Message msg) {
    AppManager.printDetailLog(AppManager.msgToString(msg));

    switch (msg.what) {
      case Game.HANDLER_GAME_RESUME:
        AppManager.printDetailLog("핸들러 resume");
        dlgSetting.dismiss();
        gameMaster.playGame();
        break;

      case Game.HANDLER_GAME_PAUSE:
        AppManager.printDetailLog("핸들러 pause");
        gameMaster.pauseGame();
        if (!explicitQuit) {
          dlgSetting.show();
        }
        break;

      case Game.HANDLER_DLG_SOUND:
        AppManager.printDetailLog("핸들러 sound toggle");
        if (bgmPlaying) {
          bgmPlaying = false;
          bgMusic.pause();
        } else {
          bgmPlaying = true;
          bgMusic.start();
        }
        break;

      case Game.HANDLER_DLG_QUIT:
        AppManager.printDetailLog("핸들러 game quit");
        bgMusic.stop();
        bgMusic.release();
        bgMusic = null;
        quitExplicitly(); // 다음번 Game.onDestroy()가 호출될 때 리소스를 해제하라고 알림
        AppManager.quitApp();
        break;

      case Game.HANDLER_SHOW_SPAWN_BTN:
        AppManager.printDetailLog("핸들러 ready spawn button");
        btnGen.setVisibility(View.VISIBLE);
        break;

      case Game.HANDLER_SPAWN_MOBS:
        AppManager.printDetailLog("핸들러 push spawn button");
        btnGen.setVisibility(View.GONE);
        gState.waveReady();
        break;

      default:
        AppManager.printErrorLog("Game 액티비티에 예외 메시지(" + AppManager.msgToString(msg) + ")가 왔습니다.");
        return false;
    }
    return true;
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
}
