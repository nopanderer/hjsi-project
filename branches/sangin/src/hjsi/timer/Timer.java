package hjsi.timer;

import hjsi.activity.BuildConfig;

/**
 * @author 이상인
 */
public class Timer {

  /**
   * @brief 타이머를 구별하기 위한 값. 지정하지 않으면 TimeManager에서 작업을 호출하는 callback 형태로 작동한다.
   */
  private int timerId;

  /**
   * 타이머의 카운트다운을 진행시키거나 중단시킬 때 사용하는 변수.
   */
  private boolean enabled = false;

  /**
   * 타이머의 대기시간 값으로, 생성시 한 번 입력된 이후로는 값이 바뀌지 않는다. 단위는 밀리초.
   */
  private final long period;

  /**
   * 현재 남아있는 대기시간 값으로 리셋하면 period 값과 동일한 값으로 설정된다. 단위는 밀리초.
   */
  private volatile long remain;

  /**
   * callback 모드인 Timer 스스로 주기적인 작업을 수행하도록 할 때, TimerRunnable로 구현한 작업을 수행한다.
   */
  private TimerRunnable task;

  /**
   * task로 지정된 작업을 수행할 횟수를 나타낸다. 음수일 경우 무한 반복한다.
   */
  private int loop;

  /**
   * 지정된 작업을 일정 주기마다 정해진 횟수만큼 반복 수행하는 타이머를 생성한다. 타이머는 바로 시작되지 않는다.
   *
   * @param milliSec 타이머의 반복 주기(대기시간)를 정한다.
   * @param task 정해진 주기마다 반복해서 수행할 작업을 지정한다.
   * @param loop 작업의 반복 횟수를 지정한다. 음수를 입력할 경우 무한 반복한다.
   */
  protected Timer(long milliSec, TimerRunnable task, int loop) {
    try {
      if (task == null) {
        throw new Exception("TimerRunnable task must be not null.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    timerId = -1;
    period = milliSec;
    remain = period;

    this.task = task;
    this.loop = loop;
  }

  /**
   * 타이머를 사용하는 측에서 타이머의 상태를 직접 확인하고, 초기화해서 사용하는 Timer을 생성한다. 타이머는 바로 시작되지 않는다.
   *
   * @param timerId 타이머를 서로 구별하기 위한 0 이상의 상수를 지정한다. 값의 중복 여부는 검사하지 않는다.
   * @param milliSec 타이머의 반복 주기(대기시간)를 정한다.
   */
  protected Timer(int timerId, long milliSec) {
    try {
      if (timerId < 0) {
        throw new Exception("timerId's value couldn't under 0.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.timerId = timerId;
    period = milliSec;
    remain = 0;

    task = null;
  }

  /**
   * 타이머의 countdown == 초읽기를 한다.
   *
   * @param milliSec 현재 남아있는 대기시간(remain)을 감소시킬 값
   */
  protected void countdown(long milliSec) {
    if (enabled == true) {
      remain -= milliSec;
    }
  }

  /**
   * @return CallBack 형태의 타이머라면 제대로된 타이머러너블 객체를, 아니라면 null
   */
  protected TimerRunnable getCallBackTask() {
    return task;
  }

  /**
   * callback 모드 타이머에서만 사용한다. TimerRunnable action에 지정된 작업을 수행하고 남은 반복 횟수를 차감한다.
   *
   * @return 이후로도 작업을 계속해야 한다면 true, 이번이 마지막 작업이라면 false
   */
  protected boolean isFinish() {
    if (BuildConfig.DEBUG & task == null) {
      throw new AssertionError();
    };

    boolean isFinish = (loop == 0);

    if (!isFinish) {
      // loop 초기값이 양수일 경우는 loop - 1로 대입되고, 음수는 계속 -1을 유지하게 된다. (-1이 아닌 음수 자체에 의미가 있음.)
      loop = Math.max(-1, loop - 1);
    }

    return isFinish;
  }

  /**
   * Timer의 카운트다운을 초기화하고 처음부터 시작한다.
   */
  public void restart() {
    remain = period;
    enabled = true;
  }

  /**
   * Timer의 카운트다운을 시작한다.
   */
  public void start() {
    enabled = true;
  }

  /**
   * Timer의 카운트다운을 현재 상태에 이어서 시작한다.
   */
  public void resume() {
    enabled = true;
  }

  /**
   * Timer의 카운트다운을 멈춘다.
   */
  public void pause() {
    enabled = false;
  }

  /**
   * 타이머의 대기시간 완료 여부를 반환한다.
   *
   * @return 대기완료라면 true, 아직 대기시간이 남았다면 false
   */
  public boolean isCountDone() {
    return remain <= 0;
  }
}
