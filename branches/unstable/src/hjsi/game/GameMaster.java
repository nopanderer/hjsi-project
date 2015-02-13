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

        /*
         * 게임 로직 실행
         */
        for (Unit unit : GameState.getInstance().getUnits()) {
          unit.action();
        }

        if (GameState.getInstance().usedMob < 10) {
          GameState.getInstance().addMob();
        }
        // 몹이 다 죽으면 새로운 웨이브 시작 및 정지
        else if (GameState.getInstance().deadMob == 10) {
          nextWave();
          TimeManager.pauseTime();
          pauseGame();
          break;
        }

        for (Mob mob : GameState.getInstance().getMobs()) {
          // 몹이 죽지 않았고 1바퀴 돌았으면
          if (mob.lap == 2 && mob.dead == false) {
            mob.dead = true;
            GameState.getInstance().curMob--;
            GameState.getInstance().deadMob++;
            continue;
          }

          // 몹이 생성되어 있다면 이동
          else if (mob.created)
            mob.move();
        }

        /*
         * 몹이 타워 사정거리에 들어오면 일정 시간마다 투사체 생성
         */
        GameState.getInstance().tower.attack();
        /*
         * 투사체 전체 돌면서 몹을 향해 이동. 맞으면 사라짐
         */
        for (int i = 0; i < GameState.getInstance().projs.size(); i++) {
          GameState.getInstance().projs.get(i).move();
          /* 투사체가 몹과 충돌한다면 */
          if (GameState.getInstance().projs.get(i).isHit)
            GameState.getInstance().projs.remove(i);
        }
        // TODO Auto-generated method stub

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

      // 게임이 일시정지 중일 땐 인게임 스레드의 cpu time을 양보시킨다.
      Thread.yield();
    }
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
    /*
     * 일시정지했다가 다시 시작하는건지, 한 웨이브가 끝난 후 새로운 웨이브를 시작하는건지 구별할 필요가 있다. (새로운 정보를 세팅하는 과정이 필요하니까)
     */
    running = true;
    // workerThread.interrupt(); // 대기 중인 스레드 바로 깨우기 (되는지 모르겠음)
  }

  /**
   * 게임을 일시정지한다.
   */
  public void pauseGame() {
    AppManager.printSimpleLog();
    running = false;
  }

  public void nextWave() {
    GameState gameState = GameState.getInstance();

    gameState.destroyMob();
    gameState.wave++;
    // 새로운 비트맵 추가
    gameState.makeFace();
    // 새로운 몹 생성
    gameState.createMobs();
    // init(임시)
    gameState.curMob = 0;
    gameState.usedMob = 0;
    gameState.deadMob = 0;
  }
}
