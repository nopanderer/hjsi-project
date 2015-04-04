package hjsi.game;

import java.util.ArrayList;
import java.util.LinkedList;

import hjsi.common.AppManager;
import hjsi.common.Timer;
import hjsi.game.Unit.Type;

/**
 * 게임을 진행시키는 인게임 스레드. 화면에 보이는지나 카메라에 관한 건 전혀 신경 쓸 필요 없다.
 */
public class GameMaster implements Runnable {
  /**
   * GameState
   */
  private GameState gState = null;
  /**
   * 게임을 진행시키는 스레드
   */
  private Thread workerThread;
  /**
   * GameMaster의 스레드를 완전히 종료하려면 값을 true로 바꾼다.
   */
  private boolean termination = false;
  /**
   * GameMaster의 스레드를 재생하려면 true로 설정하고, 일시적으로 멈추려면 false로 설정한다.
   */
  private boolean running = false;

  public GameMaster() {
    refreshGameState();

    workerThread = new Thread(this);
    workerThread.start();
  }

  public void refreshGameState() {
    gState = AppManager.getGameState();
  }

  @Override
  public void run() {
    /* fps 계산을 위한 변수 */
    int fpsRealFps = 0;
    long fpsRealTime; // 프레임당 실제 소요 시간
    long fpsElapsedTime = 0L; // 1초 측정을 위한 변수

    while (!termination) {
      while (running) {
        // 프레임 시작 시간을 구한다.
        Timer.timestamp();

        // 게임월드 시간이 1초마다 1씩 증가한다.
        gState.ticktock();

        // 웨이브가 종료되면 타이머를 멈추고 다음 웨이브를 준비한다.
        if (gState.isWaveStop()) {
          gState.finishWave();
        }
        // 웨이브가 아직 진행 중이면 몹 생성을 시도한다.
        else {
          gState.spawnMob();
        }

        /*
         * 파괴된 유닛 삭제
         */
        for (Unit unit : gState.getUnitsClone()) {
          if (unit.isDestroyed())
            gState.removeUnit(unit);
        }

        /*
         * Mob 루프
         */
        for (Unit unit : gState.getUnits(Type.MOB)) {
          Mob mob = (Mob) unit;

          if (mob.getLap() == 1) {
            LinkedList<Hittable> statues = gState.getHittables(Type.STATUE);
            LinkedList<Attackable> projs = mob.attack(statues);
            if (projs != null)
              gState.addUnits(projs);
          }

          mob.move();
        }

        /*
         * Tower 루프
         */
        for (Unit tower : gState.getUnits(Type.TOWER)) {
          LinkedList<Hittable> mobs = gState.getHittables(Type.MOB);
          LinkedList<Attackable> projs = ((Tower) tower).attack(mobs);
          if (projs != null)
            gState.addUnits(projs);
        }

        /*
         * Projectile 루프
         */
        for (Unit proj : gState.getUnits(Type.PROJECTILE)) {
          ((Projectile) proj).move();
        }

        /*
         * Statue 루프
         */
        for (Unit statue : gState.getUnits(Type.STATUE)) {
          statue.action();
        }

        /* 프레임 한 번의 소요 시간을 구해서 fps를 계산한다. */
        fpsRealFps++;
        fpsRealTime = (System.currentTimeMillis() - Timer.NOW);
        fpsElapsedTime += fpsRealTime;
        if (fpsElapsedTime >= 1000) { // 1초마다 프레임율 갱신
          AppManager.setLogicFps(fpsRealFps);
          fpsRealFps = 0;
          fpsElapsedTime -= 1000;
        }
      }
    }

    // 게임이 일시정지 중일 땐 인게임 스레드의 cpu time을 양보시킨다.
    Thread.yield();
    AppManager.printDetailLog("GameMaster 스레드 종료.");
  }

  /**
   * 게임을 종료할 때 호출한다. 게임 진행 스레드를 완전히 종료시킨다.
   */
  public void quitGame() {
    AppManager.printSimpleLog();
    termination = true;

    try {
      if (workerThread != null) {
        workerThread.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * 게임을 시작한다.
   */
  public void playGame() {
    AppManager.printSimpleLog();
    running = true;
    gState.resumeTimers();
  }

  /**
   * 게임을 일시정지한다.
   */
  public void pauseGame() {
    AppManager.printSimpleLog();
    running = false;
    gState.pauseTimers();
  }

  /**
   * @return 게임이 멈춰있으면 true, 진행 중이면 false를 반환한다.
   */
  public boolean isPaused() {
    return !running;
  }
}
