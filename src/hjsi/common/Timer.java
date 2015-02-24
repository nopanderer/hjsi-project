package hjsi.common;

import java.util.LinkedList;


/**
 * 타이머 클래스
 */
public class Timer {
  /**
   * 빨리감기 배속을 나타낸다.
   */
  private volatile static int FAST_FORWARD = 1;
  /**
   * 현재 시간을 가진다.
   */
  public volatile static long NOW = 0L;
  /**
   * 타이머를 관리하기 위한 리스트
   */
  private static LinkedList<Timer> TIMERS = new LinkedList<Timer>();
  /**
   * 디버깅용 타이머 이름
   */
  private String name;
  /**
   * 타이머 개별적인 사용여부
   */
  private boolean enabled;
  /**
   * 타이머가 특정 객체에 의해서 사용되고 있는지를 체크하기 위한 변수.
   */
  private boolean used;
  /**
   * 타이머의 대기시간(반복주기) 값을 의미한다.
   */
  private final long wait;
  /**
   * 타이머 만료까지의 유효 횟수를 저장한다.
   */
  private final int expirationCount;
  /**
   * 타이머가 만료될 때까지의 유효 횟수를 나타낸다. 음수일 경우 무한 반복한다.
   */
  private int loop;
  /**
   * 시간 측정을 위해 과거 시간 값을 가진다.
   */
  private long past;
  /**
   * 직전에 시간을 측정한 프레임과 지금 시간을 측정한 프레임 사이에 흐른 시간을 누적한다.
   */
  private long elapsed;


  /**
   * 타이머를 생성한다.
   * 
   * @param waitMilliSec 대기할 시간을 밀리초 단위로 설정한다.
   * @param expirationCount 타이머가 만료되는 횟수를 지정한다. 음수를 입력하면 무기한이다.
   */
  private Timer(long waitMilliSec, int expirationCount) {
    enabled = true;
    used = false;
    wait = waitMilliSec;
    this.expirationCount = expirationCount;
    loop = expirationCount;
    past = System.currentTimeMillis();
    elapsed = 0;
  }

  /**
   * 횟수 무제한 타이머를 생성한다.
   * 
   * @param name 로그 출력시 보여지는 이름
   * @param waitMilliSec 대기할 시간을 밀리초 단위로 설정한다.
   */
  public static Timer create(String name, long waitMilliSec) {
    return create(name, waitMilliSec, -1);
  }

  /**
   * 횟수 제한도 있는 타이머를 생성한다.
   * 
   * @param name 로그 출력시 보여지는 이름
   * @param waitMilliSec 대기할 시간을 밀리초 단위로 설정한다.
   * @param expirationCount 타이머가 만료되는 횟수를 지정한다. 음수를 입력하면 무기한이다.
   */
  public static Timer create(String name, long waitMilliSec, int expirationCount) {
    Timer t = new Timer(waitMilliSec, expirationCount);
    t.name = name;
    addTimer(t);
    return t;
  }

  /**
   * 타이머를 관리하기 위한 리스트에 타이머 객체를 추가함.
   * 
   * @param t 추가할 타이머 객체
   */
  private static void addTimer(Timer t) {
    synchronized (TIMERS) {
      TIMERS.add(t);
      AppManager.printDetailLog(t.toString() + "가 추가되었습니다.");
    }
  }

  /**
   * 가비지콜렉터처럼, 다른 객체에서 사용하지 않는 타이머를 찾아서 제거한다.
   */
  public static void removeUnusedTimer() {
    synchronized (TIMERS) {
      LinkedList<Timer> temp = new LinkedList<Timer>(TIMERS);
      for (Timer t : temp) {
        if (t.used)
          t.used = false;
        else {
          TIMERS.remove(t);
          AppManager.printDetailLog(t.toString() + "가 제거되었습니다.");
        }
      }
    }
  }

  /**
   * 게임을 일시정지한 경우 타이머도 같이 멈춘 것과 같은 효과를 내기 위한 메소드이다.
   */
  public static void resume() {
    NOW = System.currentTimeMillis();
    synchronized (TIMERS) {
      for (Timer t : TIMERS) {
        // t.start();
        t.past = NOW;
      }
    }
  }

  /**
   * 게임을 일시정지하게 되는 경우, 마지막 측정 시간으로부터 조금 지난 시간도 반영해둔다.
   */
  public static void pause() {
    NOW = System.currentTimeMillis();
    synchronized (TIMERS) {
      for (Timer t : TIMERS) {
        // t.stop();
        t.elapsed += (NOW - t.past) * FAST_FORWARD;
      }
    }
  }

  /**
   * 빨리감기 속도를 1 -> 2 -> 3 -> 1배속으로 토글한다.
   */
  public static void fastForward() {
    FAST_FORWARD = FAST_FORWARD % 3 + 1;
    AppManager.printInfoLog("빨리감기 " + FAST_FORWARD + "배속");
  }

  /**
   * 타이머 객체들이 시간을 비교할 기준 시간 값을 갱신한다.
   */
  public static void timestamp() {
    NOW = System.currentTimeMillis();
  }

  /**
   * 타이머를 시작한다.
   */
  public void start() {
    enabled = true;
    // past = NOW;
  }

  /**
   * 타이머를 멈춘다.
   */
  public void stop() {
    enabled = false;
    // elapsed += (NOW - past) * FAST_FORWARD;
  }

  /**
   * 현재 타이머의 시간이 충분히 지났는지를 검사한다. 시간이 지나지 않았더라도 현재까지 흘러간 시간 값을 누적한다.
   * 
   * @return 타이머의 대기시간이 지났다면 true를 반환하고, 아직 시간이 부족하다면 false를 반환한다.
   */
  public boolean isAvailable() {
    // 타이머를 사용하는 객체가 살아있으면 무조건 이 코드를 수행한다는 뜻임.
    used = true;
    boolean available = false;

    if (enabled) {
      elapsed += (NOW - past) * FAST_FORWARD;
      past = NOW;

      if ((loop == -1 || loop > 0) && elapsed >= wait) {
        elapsed -= wait;
        loop = Math.max(-1, loop - 1);
        available = true;
      }
    }

    return available;
  }

  /**
   * 사용횟수가 만료된 타이머를 재사용한다.
   */
  public void refill() {
    loop = expirationCount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "타이머(" + name + ")";
  }
}
