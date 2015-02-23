package hjsi.game;

import hjsi.activity.Game;
import hjsi.common.AppManager;
import hjsi.timer.TimeManager;
import hjsi.timer.TimerRunnable;
import android.os.Handler;
import android.os.Message;
import android.view.View;

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
  private static boolean running = false;

  /**
   * 현재 웨이브를 완료했는지 여부를 의미한다. false라면 웨이브가 진행 중이라는 뜻.
   */

  public static long gameTime;

  public static int ff = 1;

  public GameMaster() {
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
        if (GameState.getInstance().isWaveDone()) {
          new Thread() {
            public void run() {
              Message msg = handler.obtainMessage();
              handler.sendMessage(msg);
            }
          }.start();
          // TimeManager.pauseTime();
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

          if (unit instanceof Tower)
            for (Mob mob : GameState.getInstance().getMobs())
              ((Tower) unit).attack(mob);
          else if (unit instanceof Mob)
            for (Statue statue : GameState.getInstance().getStatues())
              ((Mob) unit).attack(statue);


        }
        /*
         * 유닛 루프 끝
         */

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
  public static void playGame() {
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

  final Handler handler = new Handler() {
    public void handleMessage(Message msg) {
      Game.btnGen.setVisibility(View.VISIBLE);
      GameState.usedMob = 0;
      GameState.deadMob = 0;
      GameState.curMob = 0;
    }
  };

}
