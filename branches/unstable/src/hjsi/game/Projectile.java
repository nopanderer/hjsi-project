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
   * 타겟 몹
   */
  Mob targetMob;
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

  public Projectile(int x, int y, int damage, Mob targetMob, Bitmap face) {
    super(x, y, face);
    this.targetMob = targetMob;

    moveSpeed = 3;
    isHit = false;
    type = NORMAL;

    beforeTime = System.currentTimeMillis();
  }

  @Override
  public void move() {
    if (System.currentTimeMillis() - beforeTime > sleep)
      beforeTime = System.currentTimeMillis();
    else
      return;

    /* 충돌검사 */
    if ((x >= targetMob.x && x <= targetXWidth()) && y >= targetMob.y && y <= targetYHeight()) {
      isHit = true;
      targetMob.hit(damage);
    }

    /* 유도 알고리즘 */
    if (targetMob.cntrX < x) {
      x -= moveSpeed;
      if (targetMob.cntrY < y)
        y -= moveSpeed;
      else if (targetMob.cntrY > y)
        y += moveSpeed;
    }
    if (targetMob.cntrX > x) {
      x += moveSpeed;
      if (targetMob.cntrY < y)
        y -= moveSpeed;
      else if (targetMob.cntrY > y)
        y += moveSpeed;
    }
    if (targetMob.cntrX == x) {
      if (targetMob.cntrY < y)
        y -= moveSpeed;
      else if (targetMob.cntrY > y)
        y += moveSpeed;
    }

  }

  private int targetXWidth() {
    return targetMob.x + targetMob.width;
  }

  private int targetYHeight() {
    return targetMob.y + targetMob.height;
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
