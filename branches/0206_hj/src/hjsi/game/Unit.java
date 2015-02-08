package hjsi.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/*
 * 게임 오브젝트의 기본이 되는 추상 클래스(가 될 예정)
 */
public abstract class Unit {
  /*
   * 위치 및 크기 관련 변수
   */
  protected int x;
  protected int y;
  protected int width;
  protected int height;
  /**
   * 유닛 정중앙
   */
  protected int cntrX, cntrY;


  protected Bitmap face;
  private Paint paint;

  /* 게임 속성 */
  public Unit() {
    // 임시 생성자
  }

  public Unit(int x, int y, Bitmap face) {
    this.x = x;
    this.y = y;
    width = face.getWidth();
    height = face.getHeight();
    cntrX = x + (width / 2);
    cntrY = y + (height / 2);

    this.face = face;
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  }

  /**
   * 단순히 멤버로 가지고 있는 비트맵 이미지를 출력해서 객체 자기 자신을 나타낸다.
   * 
   * @param canvas 그림이 출력될 캔버스.
   */
  public void draw(Canvas canvas) {
    if (face != null) {
      canvas.drawBitmap(face, x, y, paint);
    }
  }

  /**
   * 유닛으로 공통된 사용을 위해 정의했고, 유닛을 상속하는 세부적인 클래스에 따라 고유한 행동을 정의한다.
   */
  public abstract void action();

  /**
   * 사용자로부터 입력을 받았을 경우에 대한 처리를 실시한다.
   */
  public abstract void touch();

  /**
   * 범위 그려주는 메소드
   * 
   * @param range 타격 범위
   * @param canvas
   */
  public void showRange(int range, Canvas canvas) {
    Paint circle = new Paint();
    circle.setAntiAlias(true);
    circle.setStyle(Paint.Style.STROKE); // 원의 윤곽선만 그림
    circle.setStrokeWidth(3); // 윤곽선 두께
    circle.setColor(Color.GREEN); // 윤곽선은 초록색
    canvas.drawCircle(cntrX, cntrY, range, circle);

  }
}
