package hjsi.timer;

import java.util.LinkedList;

import android.util.Log;

public class TimeManager implements Runnable {
  private static String tag; // 로그 출력용 클래스 이름을 갖고 있음
  private static TimeManager uniqueInstance; // 자신의 유일한 인스턴스를 가지고 있는다.

  /*
   * 쓰레드
   */
  private Thread worker;
  private boolean isRunning;

  /*
   * 시간 관련 변수
   */
  private long elapsedWorldTime = 0L;

  private LinkedList<TimerJob> timerList;

  private TimeManager() {
    TimeManager.tag = getClass().getSimpleName();
  }

  /**
   * @return 유일한 <strong>TimeManager</strong> 인스턴스를 반환함
   */
  public static TimeManager getInstance() {
    if (TimeManager.uniqueInstance == null) {
      synchronized (TimeManager.class) {
        if (TimeManager.uniqueInstance == null) {
          TimeManager.uniqueInstance = new TimeManager();
        }
      }
    }
    return TimeManager.uniqueInstance;
  }

  public void start() {
    isRunning = true;
    worker = new Thread(this);
    worker.start();
  }

  public void stop() {
    isRunning = false;

    try {
      worker.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    Log.i(TimeManager.tag, TimeManager.tag + " is start.");

    long startTime = System.currentTimeMillis();
    long elapsedTime = 0L;
    long currentTime = 0L;

    while (isRunning) {
      currentTime = System.currentTimeMillis();
      elapsedTime = currentTime - startTime;

      if (elapsedTime >= 10) { // elapsedTime >= 10ms
        elapsedWorldTime += elapsedTime;
        startTime = currentTime;
      } else {
        try {
          Thread.sleep(10 - elapsedTime);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    Log.i(TimeManager.tag, TimeManager.tag + " is stop.");
  }

  public long getWorldTime() {
    return elapsedWorldTime;
  }
}
