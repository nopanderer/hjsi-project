package hjsi.common;

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
   * 타이머의 작동 유무를 강제로 설정함
   */
  private boolean enabled;
  /**
   * 디버깅용 타이머 이름
   */
  private final String name;
  /**
   * 타이머의 대기시간(반복주기) 값을 의미한다.
   */
  private final long wait;
  /**
   * 타이머 만료까지의 유효 횟수를 저장한다.
   */
  private final int expirationCount;
  /**
   * 타이머 정지 여부
   */
  private boolean stopped;
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
   * 타이머 사용가능(대기시간 완료) 여부
   */
  private boolean usable;

  /**
   * 타이머를 생성한다.
   * 
   * @param waitMilliSec 대기할 시간을 밀리초 단위로 설정한다.
   * @param expirationCount 타이머가 만료되는 횟수를 지정한다. 음수를 입력하면 무기한이다.
   */
  private Timer(String name, long waitMilliSec, int expirationCount) {
    setEnable(true);

    this.name = name;
    wait = waitMilliSec;
    this.expirationCount = expirationCount;

    stopped = true;
    loop = expirationCount;
    past = NOW;
    elapsed = 0;
    usable = false;
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
    return new Timer(name, waitMilliSec, expirationCount);
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
   * 타이머를 사용 가능하거나 불가능하게 설정한다.
   * 
   * @param enable
   */
  public void setEnable(boolean enable) {
    enabled = enable;
  }

  /**
   * 현재 타이머의 시간이 충분히 지났는지를 검사한다. 시간이 지나지 않았더라도 현재까지 흘러간 시간 값을 누적한다.
   * 
   * @return 타이머의 대기시간이 지났다면 true를 반환하고, 아직 시간이 부족하다면 false를 반환한다.
   */
  public boolean isUsable() {
    // 타이머의 대기시간이 남아서 아직 사용할 수 없는 경우에만 타이머의 시간을 측정한다.
    if (enabled && !stopped && !usable) {
      elapsed += (NOW - past) * FAST_FORWARD + 0.5f;
      past = NOW;

      if ((loop == -1 || loop > 0) && elapsed >= wait) {
        usable = true;
      }
    }
    return usable;
  }

  /**
   * 타이머 대기시간 측정을 계속해야 할 경우, 이 메소드를 반드시 호출한다. 타이머에 의해서 수행되는 작업이 있을 경우, 해당 작업을 실제로 수행한 경우에만 타이머가 다시
   * 대기하도록 한다.
   */
  public void consumeTimer() {
    if (usable) {
      usable = false;
      elapsed -= wait;
      loop = Math.max(-1, loop - 1);
      if (loop == 0) {
        setEnable(false);
      }
    }
  }

  /**
   * 0, 시작! 1, 2, 3, 4...
   */
  public void startDelayed() {
    startDelayed(wait);
  }

  /**
   * -5, -4, -3, -2, -1, 시작! 1, 2, 3, 4...
   * 
   * @param delay 주어진 시간만큼 카운트다운하고 시작한다.
   */
  public void startDelayed(long delay) {
    stopped = false;
    loop = expirationCount;
    past = NOW;
    elapsed = wait - delay;
    usable = false;
  }

  /**
   * 시작! 1, 2, 3, 4...
   */
  public void start() {
    stopped = false;
    loop = expirationCount;
    past = NOW;
    elapsed = wait;
    usable = false;
  }

  /**
   * 작동 중인 타이머를 일시정지한다.
   */
  public void pause() {
    stopped = true;
    if (!usable)
      elapsed += (NOW - past) * FAST_FORWARD + 0.5f;
  }

  /**
   * 일시정지 중인 타이머를 작동 시킨다.
   */
  public void resume() {
    stopped = false;
    if (!usable)
      past = NOW;
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
