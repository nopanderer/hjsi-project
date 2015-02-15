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
  private int sleep = 10;

  private Vector2d vector;

  private static final int NORMAL = 1;
  private static final int SLOW = 2;
  private static final int SPLASH = 3;
  private static final int POISON = 4;
  private static final int ICED = 5;
  private static final int CHAIN = 6;

  public Projectile(float x, float y, int damage, Mob targetMob, Bitmap face) {
    super(Unit.TYPE_ETC, 0, x, y, face);
    this.targetMob = targetMob;

    moveSpeed = 3;
    isHit = false;
    type = NORMAL;
    this.damage = damage;

    vector = new Vector2d();

    beforeTime = System.currentTimeMillis();
  }

  @Override
  public void move() {
    if (System.currentTimeMillis() - beforeTime > sleep)
      beforeTime = System.currentTimeMillis();
    else
      return;

    /* 투사체가 이동중일때 몹이 죽어버린 경우, 투사체를 삭제 */
    if (targetMob == null)
      GameState.getInstance().units.remove(this);

    /* 투사체에서 몹까지의 벡터 */
    vector.set(targetMob.cntrX - x, targetMob.cntrY - y);
    /* 벡터 정규화 */
    vector.nor();
    /* 투사체 이동속도 스칼라 곱 */
    vector.mul(moveSpeed);

    /* 충돌검사 */
    if ((x >= targetMob.x && x <= targetXWidth()) && y >= targetMob.y && y <= targetYHeight()) {
      isHit = true;
      targetMob.hit(damage);
    }

    /* 유도 알고리즘 */

    Vector2d desired = new Vector2d(targetMob.vector.x + vector.x, targetMob.vector.y + vector.y);

    x += desired.x;
    y += desired.y;
    cntrX += desired.x;
    cntrY += desired.y;
  }

  private float targetXWidth() {
    return targetMob.x + targetMob.width;
  }

  private float targetYHeight() {
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
