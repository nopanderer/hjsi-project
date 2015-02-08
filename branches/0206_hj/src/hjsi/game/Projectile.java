package hjsi.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 투사체
 * 
 * @author Administrator
 *
 */
public class Projectile {

  /**
   * 비트맵을 그려주기 위한 좌표
   */
  private int x, y;
  private int target;
  /**
   * 투사체 속도
   */
  private int speed;
  public boolean isHit;
  /**
   * 임시 타이머 변수
   */
  private long beforeTime;
  private int sleep = 15;
  /**
   * 비트맵 크기
   */
  public int width, height;
  public Bitmap face;
  public Paint paint;

  public Projectile(int x, int y, int target, Bitmap face) {
    this.x = x;
    this.y = y;
    this.target = target;
    width = face.getWidth();
    height = face.getHeight();
    face = this.face = face;
    paint = new Paint();

    speed = 3;
    isHit = false;

    beforeTime = System.currentTimeMillis();
  }

  public void move() {
    // GameState.getInstance().Mobs.get(target).

    if (System.currentTimeMillis() - beforeTime > sleep)
      beforeTime = System.currentTimeMillis();
    else
      return;

    if ((x >= targetX() && x <= targetXWidth()) && y >= targetY() && y <= targetYHeight())
      isHit = true;

    if (targetCntrX() < x) {
      x -= speed;
      if (targetCntrY() < y)
        y -= speed;
      else if (targetCntrY() > y)
        y += speed;
    }
    if (targetCntrX() > x) {
      x += speed;
      if (targetCntrY() < y)
        y -= speed;
      else if (targetCntrY() > y)
        y += speed;
    }
    if (targetCntrX() == x) {
      if (targetCntrY() < y)
        y -= speed;
      else if (targetCntrY() > y)
        y += speed;
    }

  }

  public void draw(Canvas canvas) {
    if (face != null) {
      canvas.drawBitmap(face, x, y, paint);
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
}
