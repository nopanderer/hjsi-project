package hjsi.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 게임 오브젝트의 기본이 되는 추상 클래스
 */
public abstract class Unit {
  public static final int TYPE_STATUE = 0;
  public static final int TYPE_TOWER = 1;
  public static final int TYPE_MOB = 2;
  public static final int TYPE_ETC = 3;

  /**
   * 유닛의 종류를 나타내는 값. Unit.TYPE_XXXX
   */
  private int type;
  /**
   * 유닛의 종류별로 고유한 아이디. 즉, 유닛의 종류가 다를 경우 같은 아이디를 가질 수 있으며, 같은 id를 가지는 개체도 존재할 수 있음.
   */
  private int id;

  /*
   * 위치 및 크기 관련 변수
   */
  protected int x;
  protected int y;
  /**
   * 유닛 정중앙
   */
  protected int cntrX, cntrY;
  protected int width;
  protected int height;

  protected Bitmap face;
  private Paint paint;

  // 임시 생성자
  public Unit() {}

  /**
   * @param type 유닛의 종류를 입력한다. -> Unit.TYPE_XXXX
   * @param id 해당하는 종류 안에서 특정 유닛을 가리키는 정수 값
   * @param x 이미지를 표시할 x 좌표
   * @param y 이미지를 표시할 y 좌표
   * @param face 화면에 표시할 이미지
   */
  public Unit(int type, int id, int x, int y, Bitmap face) {
    this.type = type;
    this.id = id;

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
   * 오버라이딩 금지.
   * 
   * @return 유닛의 타입을 의미하는 정수를 반환한다.
   */
  public final int getType() {
    return type;
  }

  /**
   * 오버라이딩 금지.
   * 
   * @return 유닛 타입별로 고유한 아이디를 나타내는 정수를 반환한다.
   */
  public final int getId() {
    return id;
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

  /**
   * 체력바를 그려주는 메소드
   * 
   * @param hpMax
   * @param hp
   * @param canvas
   */
  public void showHealthBar(int hpMax, int hp, Canvas canvas) {
    Paint paint = new Paint();
    paint.setColor(Color.GREEN);

    /* 체력량에 따라 체력바 길이가 결정 */
    float healthScale = (float) hp / hpMax;

    canvas.drawRect(x, y - 10, x + width * healthScale, y - 5, paint);
  }
}
