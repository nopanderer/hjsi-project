/**
 *
 */
package hjsi.game;

import hjsi.timer.TimeManager;
import hjsi.timer.Timer;
import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * 체력, 공격속도 등과 같은 능력치는 변수 타입은 정수형이지만 소수점으로 생각하고 다룬다. float 계산의 성능과 정확성 때문이다. 예를 들면, 1초당 10.5의 체력을
 * 회복할 경우 변수에 들어갈 값은 1050이다.
 */
public class Statue extends Unit implements Hittable {

  /**
   * 체력 재생량
   */
  private int hpRegen;
  /**
   * 최대 체력
   */
  private int hpMax;
  /**
   * 현재 체력
   */
  private int hp;
  /**
   * 방어력
   */
  private int armor;

  /*
   * 쿨타임
   */
  private static final int HP_REGEN = 0;
  private Timer timerHpRegen;

  /**
   *
   */
  public Statue(int x, int y, Bitmap face) {
    super(Unit.TYPE_STATUE, 0, x, y, face);

    hpRegen = 1500; // 1초당 1.5 재생 = 0.1초당 0.15 재생 = hpRegen 150
    hpMax = 1000000; // 1000.000
    hp = 100000; // 100.000
    armor = 10000; // 10.000

    /*
     * 타이머 생성
     */
    timerHpRegen = TimeManager.registerPassiveTimer(HP_REGEN, 10);
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#doAction()
   */
  @Override
  public void action() {
    if (timerHpRegen.isCountDone()) {
      hp = Math.min(hp + hpRegen, hpMax);
      TimeManager.resetPassiveTimer(timerHpRegen);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#draw(android.graphics.Canvas)
   */
  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    showHealthBar(hpMax, hp, canvas);
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#touch()
   */
  @Override
  public void touch() {
    // TODO Auto-generated method stub

  }

  @Override
  public void hit(int damage) {
    // TODO Auto-generated method stub

  }
}
