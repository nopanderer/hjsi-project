package hjsi.game;

import hjsi.activity.Game;
import hjsi.common.AppManager;
import hjsi.timer.TimeManager;
import hjsi.timer.TimerRunnable;

import java.util.LinkedList;

/**
 * 게임을 진행시키는 인게임 스레드. 화면에 보이는지나 카메라에 관한 건 전혀 신경 쓸 필요 없다.
 */
public class GameMaster implements Runnable {
  /**
   * Game 액티비티
   */
  private Game gameAct = null;
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

  /**
   * 현재 웨이브를 완료했는지 여부를 의미한다. false라면 웨이브가 진행 중이라는 뜻.
   */
  public static long gameTime;

  public static int ff = 1;

  public GameMaster(Game gameAct, GameState gameState) {
    this.gameAct = gameAct;
    gState = gameState;

    workerThread = new Thread(this);
    workerThread.start();
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
        gameTime = System.currentTimeMillis();

        /*
         * 대기(쿨타임)가 끝난 작업을 수행한다.
         */
        TimerRunnable task;
        while ((task = TimeManager.nextTask()) != null) {
          task.run();
        }

        // 웨이브가 종료되면 타이머를 멈추고 다음 웨이브를 준비한다.
        if (gState.isWaveDone()) {
          gameAct.readySpawnButton();
          GameState.usedMob = 0;
          GameState.deadMob = 0;
          GameState.curMob = 0;
        }

        /*
         * 각각의 스테이션은 자신에게 도달한 몹을 다음 스테이션으로 향하도록 설정한다.
         */
        for (Mob mob : gState.getMobs()) {
          if (mob.isArrive()) {
            mob.nextStation(gState.stations);
          }
        }

        /*
         * 파괴된 유닛 삭제
         */
        LinkedList<Unit> clone = new LinkedList<Unit>(gState.getUnits());

        for (Unit unit : clone) {
          if (unit.destroyed)
            gState.removeUnit(unit);
        }

        /*
         * Mob 루프
         */
        for (Mob mob : gState.getMobs()) {
          if (mob.getLap() == 2) {
            mob.dead();
            continue;
          }

          else if (mob.getLap() == 1) {
            for (Statue statue : gState.getStatues()) {
              if (statue.destroyed == false && mob.inRange(mob, statue)) {
                Projectile proj = ((Mob) mob).attack(statue);
                if (proj != null)
                  gState.addUnit(proj);
              }
            }
          }
          mob.move();
        }

        /*
         * Tower 루프
         */
        for (Tower tower : gState.getTowers()) {
          for (Mob mob : gState.getMobs()) {
            if (mob.destroyed == false && tower.inRange(tower, mob)) {
              Projectile proj = tower.attack(mob);
              if (proj != null)
                gState.addUnit(proj);
            }
          }
        }

        /*
         * Projectile 루프
         */
        for (Projectile proj : gState.getProjs()) {
          proj.move();
        }

        /*
         * Statue 루프
         */
        for (Statue statue : gState.getStatues()) {
          statue.action();
        }



        /* 프레임 한 번의 소요 시간을 구해서 fps를 계산한다. */
        fpsRealFps++;
        fpsRealTime = (System.currentTimeMillis() - gameTime);
        fpsElapsedTime += fpsRealTime;
        if (fpsElapsedTime >= 1000) { // 1초마다 프레임율 갱신
          AppManager.getInstance().setLogicFps(fpsRealFps);
          fpsRealFps = 0;
          fpsElapsedTime = 0L;
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

    TimeManager.stopTime();
  }

  /**
   * 게임을 시작한다.
   */
  public void playGame() {
    AppManager.printSimpleLog();
    /*
     * 일시정지했다가 다시 시작하는건지, 한 웨이브가 끝난 후 새로운 웨이브를 시작하는건지 구별할 필요가 있다. (새로운 정보를 세팅하는 과정이 필요하니까)
     */
    running = true;
    // workerThread.interrupt(); // 대기 중인 스레드 바로 깨우기 (되는지 모르겠음)
    TimeManager.startTime();
  }

  /**
   * 게임을 일시정지한다.
   */
  public void pauseGame() {
    AppManager.printSimpleLog();
    running = false;
    TimeManager.pauseTime();
  }
}
