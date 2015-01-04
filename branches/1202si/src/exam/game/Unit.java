package exam.game;

import android.graphics.Paint;

/*
 * 게임 오브젝트의 기본이 되는 추상 클래스(가 될 예정)
 */
public class Unit extends Bounds {
  /* 처리를 위한 변수 */
  public String mCaption;
  private Paint mPaintText;
  private Paint mPaintRect;

  /* 게임 속성 */

  public Unit(int x, int y, int width, int height) {
    super(x, y, width, height);
  }

  public String getBitmapKey() {
    return mCaption;
  }

  public void action() {}
}
