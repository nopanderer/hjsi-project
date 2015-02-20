package hjsi.game;

import hjsi.common.AppManager;
import hjsi.timer.TimeManager;
import hjsi.timer.TimerRunnable;

/**
 * 게임을 진행시키는 인게임 스레드. 화면에 보이는지나 카메라에 관한 건 전혀 신경 쓸 필요 없다.
 */
public class GameMaster implements Runnable {
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

  private boolean waveDone = false;

  public GameMaster() {
    workerThread = new Thread(this);
    workerThread.start();
  }

  @Override
  public void run() {
    /* fps 계산을 위한 변수 */
    int fpsRealFps = 0;
    long fpsStartTime;
    long fpsRealTime; // 프레임당 실제 소요 시간
    long fpsElapsedTime = 0L; // 1초 측정을 위한 변수

    while (!termination) {
      while (running) {
        // 프레임 시작 시간을 구한다.
        fpsStartTime = System.currentTimeMillis();

        /*
         * 대기가 끝난 작업을 수행한다.
         */
        TimerRunnable task = TimeManager.nextTask();
        while (task != null) {
          task.run();
          task = TimeManager.nextTask();
        }

        /* 최대 유닛까지 몹 생성 */
        if (waveDone == false)
          GameState.getInstance().addMob();

        /* 몹이 전부 죽으면 다음 스테이지 준비 */
        if (GameState.getInstance().nextWave()) {
          TimeManager.pauseTime();
          waveDone = true;
        }

        /*
         * 유닛 루프 시작
         */
        for (int i = 0; i < GameState.getInstance().getUnits().size(); i++) {
          /* 임시유닛 */
          Unit unit = GameState.getInstance().getUnits().get(i);

          if (unit.destroyed) {
            GameState.getInstance().units.remove(unit);
            continue;
          }

          if (unit instanceof Mob) {
            Mob mob;
            mob = (Mob) unit;
            if (mob.lap == 2) {
              mob.dead();
              continue;
            }
          }

          else if (unit instanceof Statue) {
            unit.action();
          }

          if (unit instanceof Movable) {
            ((Movable) unit).move();
          }

          if (unit instanceof Attackable) {
            ((Attackable) unit).attack();
          }

        }
        /*
         * 유닛 루프 끝
         */

        /* 프레임 한 번의 소요 시간을 구해서 fps를 계산한다. */
        fpsRealFps++;
        fpsRealTime = (System.currentTimeMillis() - fpsStartTime);
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
    waveDone = false;
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
