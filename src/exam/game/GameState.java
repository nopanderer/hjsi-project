package exam.game;

import hjsi.common.AppManager;
import hjsi.timer.TimeManager;
import hjsi.timer.TimerAction;

import java.util.LinkedList;

/**
 * 게임에 필요한 정보를 저장한다.
 */
public class GameState {
  private static final String tag = GameState.class.getSimpleName();
  private static GameState uniqueInstance;

  /**
   * 현재 게임이 진행된 시간을 나타낸다.
   */
  private volatile long worldTime = 0L;

  /**
   * 현재 단계
   */
  private int waveLevel = 0;

  /**
   * 테스트용 유닛 리스트
   */
  LinkedList<Unit> arTestUnits = new LinkedList<Unit>();

  private GameState() {
    AppManager.printSimpleLog();

    /*
     * 게임 진행 시간 측정을 위한 타이머를 생성해서 등록해둔다.
     */
    TimerAction clock = new TimerAction() {
      @Override
      public void doAction() {
        worldTime++;
      }
    };
    TimeManager.registerCallbackTimer(1000, clock, -1).start();

    /*
     * 불러온 유저 데이터를 토대로 동상을 생성한다. (유저 데이터의 남아있는 동상의 갯수, 체력, 업그레이드 등을 참조) 생성한 동상은 유닛 목록에 추가한다.
     */
    arTestUnits.add(new Statue(180, 120, AppManager.getInstance().getBitmap("statue")));
  }

  public static GameState getInstance() {
    if (GameState.uniqueInstance == null) {
      synchronized (GameState.class) {
        if (GameState.uniqueInstance == null) {
          GameState.uniqueInstance = new GameState();
        }
      }
    }
    return GameState.uniqueInstance;
  }

  /**
   * 현재의 게임 정보를 가지고 있는 GameState의 유일한 객체를 없애서 정보를 초기화한다.
   */
  public void purgeGameState() {
    synchronized (GameState.class) {
      GameState.uniqueInstance = null;
    }
  }

  public long getWorldTime() {
    return worldTime;
  }

  public LinkedList<Unit> getUnits() {
    return arTestUnits;
  }
}
