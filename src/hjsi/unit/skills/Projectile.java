package hjsi.unit.skills;

import hjsi.common.Timer;
import hjsi.game.Mob;
import hjsi.game.Statue;
import hjsi.game.Unit;
import hjsi.game.Vector2d;
import hjsi.game.Unit.Type;
import hjsi.unit.attr.Attackable;
import hjsi.unit.attr.Hittable;
import hjsi.unit.attr.Movable;

import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 투사체
 * 
 * @author Administrator
 *
 */
public class Projectile extends Unit implements Movable, Attackable {
  /**
   * 타겟
   */
  Unit target;
  /**
   * 투사체 속도
   */
  protected int moveSpeed;
  /**
   * 투사체 종류
   */
  protected int attackType;
  /**
   * 타워의 공격 데미지
   */
  protected int damage;

  protected Timer timerMovement;

  protected Vector2d vector;

  protected static final int NORMAL = 1;
  protected static final int SLOW = 2;
  protected static final int SPLASH = 3;
  protected static final int POISON = 4;
  protected static final int ICED = 5;
  protected static final int CHAIN = 6;

  /**
   * @param x 초기 x 좌표 값
   * @param y 초기 y 좌표 값
   * @param damage 투사체 명중시 데미지
   * @param target 투사체가 향할 목표
   * @param face 투사체 이미지
   */
  public Projectile(float x, float y, int damage, Unit target, Bitmap face) {
    super(Type.PROJECTILE, 0, x, y, face);
    this.target = target;

    moveSpeed = 3;
    attackType = NORMAL;
    this.damage = damage;
    updateHitRect();

    vector = new Vector2d();

    timerMovement = Timer.create("투사체", 10);
    timerMovement.start();
  }

  @Override
  public void action() {
    // TODO Auto-generated method stub
  }

  @Override
  public LinkedList<Attackable> attack(LinkedList<Hittable> units) {
    return null;
  }

  @Override
  public void draw(Canvas canvas, float screenRatio) {
    super.draw(canvas, screenRatio);
    Paint pnt = new Paint();
    pnt.setStyle(Paint.Style.STROKE);
    pnt.setColor(Color.RED);

    setDrawingBox(getHitRect(), screenRatio);
    canvas.drawRect(getDrawingBox(), pnt);
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#freeze()
   */
  @Override
  public void freeze() {
    timerMovement.pause();
  }

  @Override
  public void move() {
    /* 충돌 검사 */
    if (target.isCollidedWith(this)) {
      setDestroyed(true);
      ((Hittable) target).hit(damage);
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

  @Override
  public void touch() {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#unfreeze()
   */
  @Override
  public void unfreeze() {
    timerMovement.resume();
  }
}
