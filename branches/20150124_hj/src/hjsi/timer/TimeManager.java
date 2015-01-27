package hjsi.timer;

import hjsi.common.AppManager;

import java.util.Iterator;
import java.util.LinkedList;

public class TimeManager implements Runnable {
  private static String tag; // 로그 출력용 클래스 이름을 갖고 있음
  private static TimeManager uniqueInstance; // 자신의 유일한 인스턴스를 가지고 있는다.

  /**
   * 시간을 계산하고 타이머를 수행하는 스레드
   */
  private Thread workerThread;

  /**
   * 값이 true가 돼야 스레드가 완전히 종료된다.
   */
  private boolean termination = false;

  /**
   * 값에 따라 TimeManager를 스레드를 재생시키거나 일시적으로 멈춘다.
   */
  private boolean running = false;

  private volatile long startTime = 0L; // 시간 측정을 위한 변수
  private volatile long elapsedTime = 0L; // 시간 측정을 위한 변수

  /*
   * 타이머 목록
   */
  private LinkedList<Timer> countList; // 카운트다운이 끝나기를 기다리는 타이머 리스트
  private LinkedList<Timer> countDoneList; // 카운트다운이 끝나서 언제든지 행동을 할 수 있는 타이머 리스트

  /**
   * 비공개 생성자
   */
  private TimeManager() {
    TimeManager.tag = getClass().getSimpleName();

    countList = new LinkedList<Timer>();
    countDoneList = new LinkedList<Timer>();

    workerThread = new Thread(this);
    workerThread.start();
  }

  /**
   * @return 유일한 <strong>TimeManager</strong> 인스턴스를 반환함
   */
  private static TimeManager getInstance() {
    if (uniqueInstance == null) {
      synchronized (TimeManager.class) {
        if (uniqueInstance == null) {
          uniqueInstance = new TimeManager();
        }
      }
    }
    return uniqueInstance;
  }

  /**
   * TimeManager의 스레드를 시작한다.
   */
  public static void startTime() {
    AppManager.printSimpleLog();
    getInstance().running = true;
    getInstance().startTime = System.currentTimeMillis();
  }

  /**
   * TimeManager의 스레드를 일시정지한다.
   */
  public static void pauseTime() {
    AppManager.printSimpleLog();
    getInstance().running = false;
  }

  /**
   * TimeManager를 종료시킨다.
   */
  public static void stopTime() {
    AppManager.printSimpleLog();

    getInstance().termination = true;
    try {
      if (getInstance().workerThread != null) {
        getInstance().workerThread.join();
        getInstance().workerThread = null;

        synchronized (TimeManager.class) {
          uniqueInstance = null;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * 스레드 동기화 문제를 해결하기 위해서 countList와 countDoneList에 접근할 때는 락을 건다.
   */
  private static void addToCountList(Timer timer) {
    synchronized (getInstance().countList) {
      getInstance().countList.add(timer);
    }
  }

  /**
   * 스레드 동기화 문제를 해결하기 위해서 countList와 countDoneList에 접근할 때는 락을 건다.
   */
  private static void addToCountDoneList(Timer timer) {
    synchronized (getInstance().countDoneList) {
      getInstance().countDoneList.add(timer);
    }
  }

  /**
   * 스레드 동기화 문제를 해결하기 위해서 countList와 countDoneList에 접근할 때는 락을 건다.
   */
  private static void removeFromCountDoneList(Timer timer) {
    synchronized (getInstance().countDoneList) {
      getInstance().countDoneList.remove(timer);
    }
  }

  @Override
  public void run() {
    AppManager.printSimpleLog();

    long currentTime = 0L;

    while (!termination) {
      while (running) {
        currentTime = System.currentTimeMillis();
        elapsedTime = currentTime - startTime;

        if (elapsedTime >= 10) { // elapsedTime >= 10ms
          /*
           * TimerJob의 초읽기 진행
           */
          synchronized (countList) {
            Iterator<Timer> timers = countList.iterator();
            while (timers.hasNext()) {
              Timer timer = timers.next();
              timer.countdown(elapsedTime);

              if (timer.isCountDone()) { // 카운트가 끝났으면

                if (timer.isCallbackMode()) {
                  if (timer.doAction()) {
                    timer.restart();
                  } else {
                    countList.remove(timer); // 지정된 횟수만큼 작업을 반복했으므로 TimeManager의 count 리스트에서 제거
                    AppManager.printDetailLog(timer + "'s callback is finished.");
                  }

                } else {
                  // 콜백 모드가 아닌 수동 타이머는 카운트 완료 리스트로 보낸다.
                  countList.remove(timer);
                  addToCountDoneList(timer);
                }
              }
            }
          }

          startTime = currentTime;
        } else { // 경과 시간이 10ms 이하일 경우, 부족한 시간을 sleep으로 때운다.
          try {
            Thread.sleep(10 - elapsedTime);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      Thread.yield();
    }
    AppManager.printDetailLog("TimeManager 스레드 종료.");
  }

  /**
   * 수동 Timer 객체를 생성해서 TimeManager에 등록한다. 타이머의 초기 상태는 대기완료에 비활성화된 상태다. 바로 카운트다운을 시작하려면
   * TimeManager.resetTimerJob(Timer job)을 호출한다.
   *
   * @param timerId 타이머를 서로 구별하기 위한 0 이상의 상수 값을 지정한다. 값의 중복 여부는 검사하지 않는다.
   * @param period 타이머의 반복 주기(대기시간)를 정한다. 단위 ms
   * @return 생성된 Timer 객체
   * @see Timer
   */
  public static Timer registerPassiveTimer(int timerId, long period) {
    AppManager.printSimpleLog();

    Timer timer = new Timer(timerId, period);
    addToCountDoneList(timer);

    return timer;
  }

  /**
   * 콜백 Timer 객체를 생성해서 TimeManager에 등록한다. 등록된 타이머는 대기 상태이며 비활성화되어있다. 카운트 다운을 시작하려면 Timer.resume()을
   * 호출한다. 지정된 반복 횟수만큼 작업을 수행하고나면 타이머는 자동으로 제거된다.
   *
   * @param milliSec 지정한 시간마다 action을 반복 수행한다.
   * @param action 타이머가 수행할 작업인 TimerRunnable 인터페이스를 구현한 객체를 지정한다.
   * @param loop 지정된 값만큼 action을 반복 수행한다. 음수 값을 입력하면 무한 반복 수행한다.
   * @return 생성된 Timer 객체
   */
  public static Timer registerCallbackTimer(long milliSec, TimerRunnable action, int loop) {
    AppManager.printSimpleLog();

    Timer timer = new Timer(milliSec, action, loop);
    addToCountList(timer);

    return timer;
  }

  /**
   * 수동 타이머에 한해서 호출한다. 해당 타이머를 대기완료 리스트에서 대기 리스트로 돌려서 카운트다운을 시작한다.
   *
   * @param timer 대기완료에서 다시 대기 상태로 돌아갈 타이머를 지정한다.
   */
  public static void resetPassiveTimer(Timer timer) {
    try {
      if (timer.isCallbackMode()) {
        throw new Exception("콜백모드 타이머는 이 메소드를 사용할 수 없음.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    removeFromCountDoneList(timer);
    addToCountList(timer);
    timer.restart();
  }
}
