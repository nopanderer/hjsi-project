package exam.game;

import hjsi.timer.TimeManager;
import hjsi.timer.TimerAction;

import java.util.ArrayList;

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
  ArrayList<Unit> arTestUnits = new ArrayList<Unit>();

  private GameState() {
    TimerAction clock = new TimerAction() {
      @Override
      public void doAction() {
        worldTime++;
      }
    };

    TimeManager.getInstance().registerCallbackTimer(1000, clock, -1).start();
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

  public ArrayList<Unit> getUnits() {
    return arTestUnits;
  }
}
