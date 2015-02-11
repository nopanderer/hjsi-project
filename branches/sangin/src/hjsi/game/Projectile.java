package hjsi.game;

import android.graphics.Bitmap;

/**
 * 투사체
 * 
 * @author Administrator
 *
 */
public class Projectile extends Unit implements Movable {

  /**
   * 목표로 한 몹의 인덱스
   */
  private int target;
  /**
   * 투사체 속도
   */
  private int moveSpeed;
  /**
   * 투사체 종류
   */
  private int type;
  /**
   * 타워의 공격 데미지
   */
  private int damage;
  /**
   * 몹과 충돌했는지
   */
  public boolean isHit;
  /**
   * 임시 타이머 변수
   */
  private long beforeTime;
  private int sleep = 15;

  private static final int NORMAL = 1;
  private static final int SLOW = 2;
  private static final int SPLASH = 3;
  private static final int POISON = 4;
  private static final int ICED = 5;
  private static final int CHAIN = 6;

  public Projectile(int x, int y, int damage, int target, Bitmap face) {
    super(x, y, face);
    this.target = target;

    moveSpeed = 3;
    isHit = false;

    beforeTime = System.currentTimeMillis();
  }

  @Override
  public void move() {
    if (System.currentTimeMillis() - beforeTime > sleep)
      beforeTime = System.currentTimeMillis();
    else
      return;

    /* 충돌검사 */
    if ((x >= targetX() && x <= targetXWidth()) && y >= targetY() && y <= targetYHeight()) {
      isHit = true;
      GameState.getInstance().Mobs.get(target).hit(damage);
    }

    /* 유도 알고리즘 */
    if (targetCntrX() < x) {
      x -= moveSpeed;
      if (targetCntrY() < y)
        y -= moveSpeed;
      else if (targetCntrY() > y)
        y += moveSpeed;
    }
    if (targetCntrX() > x) {
      x += moveSpeed;
      if (targetCntrY() < y)
        y -= moveSpeed;
      else if (targetCntrY() > y)
        y += moveSpeed;
    }
    if (targetCntrX() == x) {
      if (targetCntrY() < y)
        y -= moveSpeed;
      else if (targetCntrY() > y)
        y += moveSpeed;
    }

  }

  private int targetX() {
    return GameState.getInstance().Mobs.get(target).x;
  }

  private int targetY() {
    return GameState.getInstance().Mobs.get(target).y;
  }

  private int targetCntrX() {
    return GameState.getInstance().Mobs.get(target).cntrX;
  }

  private int targetCntrY() {
    return GameState.getInstance().Mobs.get(target).cntrY;
  }

  private int targetXWidth() {
    return GameState.getInstance().Mobs.get(target).x
        + GameState.getInstance().Mobs.get(target).width;
  }

  private int targetYHeight() {
    return GameState.getInstance().Mobs.get(target).y
        + GameState.getInstance().Mobs.get(target).height;
  }

  @Override
  public void action() {
    // TODO Auto-generated method stub

  }

  @Override
  public void touch() {
    // TODO Auto-generated method stub
  }
}
