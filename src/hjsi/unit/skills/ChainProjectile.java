package hjsi.unit.skills;

import hjsi.game.Mob;
import hjsi.game.Statue;
import hjsi.game.Unit;
import hjsi.game.Vector2d;
import hjsi.unit.attr.Attackable;
import hjsi.unit.attr.Hittable;

import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Bitmap;

public class ChainProjectile extends Projectile {
  /**
   * 투사체가 튕기는 횟수
   */
  private int timeToLive;
  /**
   * 현재 타겟을 가리키는 번호
   */
  private int targetIndex;
  /**
   * 투사체가 튕길 전체 타겟
   */
  private ArrayList<Hittable> targets;
  /**
   * 튕겨나가는 거리
   */
  private int chainRange;

  public ChainProjectile(float x, float y, int damage, ArrayList<Hittable> targets, Bitmap face,
      int ttl) {
    super(x, y, damage, (Unit) targets.get(0), face);

    timeToLive = ttl;
    targetIndex = 0;
    targets = new ArrayList<Hittable>(timeToLive);
  }

  /**
   * 방문횟수를 다 채웠는지 검사한다.
   * 
   * @return 더 이상 방문할 수 없으면 true, or false.
   */
  private boolean checkTimeToLive() {
    return targets.size() >= timeToLive;
  }

  private boolean checkCollision() {
    return target.isCollidedWith(this);
  }

  @Override
  public LinkedList<Attackable> attack(LinkedList<Hittable> units) {
    // TODO 이 메소드에서 다음 타겟을 새로 구하도록 한다.
    return super.attack(units);
  }

  @Override
  public void move() {
    /* 충돌 검사 */
    if (checkCollision()) {
      ((Hittable) target).hit(damage);
      targets.add((Hittable) target);

      if (checkTimeToLive()) {
        setDestroyed(true);
      }

      // TODO 다음 타겟으로 변경
    }
    /* 충돌하지 않은 경우에 한해서만 타이머를 체크해서 이동 처리 */
    else if (timerMovement.isUsable()) {
      timerMovement.consumeTimer();

      /* 투사체에서 몹까지의 벡터 */
      vector.set(target.getX() - x, target.getY() - y);
      /* 벡터 정규화 */
      vector.nor();
      /* 투사체 이동속도 스칼라 곱 */
      vector.mul(moveSpeed);

      /* 유도 알고리즘 */
      if (target instanceof Mob) {
        Vector2d desired =
            new Vector2d(((Mob) target).vector.x + vector.x, ((Mob) target).vector.y + vector.y);

        setX(x + desired.x);
        setY(y + desired.y);
      } else if (target instanceof Statue) {
        setX(x + vector.x);
        setY(y + vector.y);
      }
      updateHitRect();
    }
  }
}
