package hjsi.game;

import hjsi.common.AppManager;

import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

/**
 * 게임 오브젝트의 기본이 되는 추상 클래스
 */
public abstract class Unit {
  public enum Type {
    STATUE(0), TOWER(1), MOB(2), PROJECTILE(3);
    private final int value;
    private static final Type[] types = values();

    Type(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public Type getType(int index) {
      return types[index];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return name().toString().toLowerCase(Locale.US);
    }
  }

  /**
   * 유닛의 종류를 나타낸다.
   */
  private Type type;
  /**
   * 유닛의 종류별로 고유한 아이디. 즉, 유닛의 종류가 다를 경우 같은 아이디를 가질 수 있으며, 같은 id를 가지는 개체도 존재할 수 있음.
   */
  private int id;

  /*
   * 위치 및 크기 관련 변수
   */
  protected float x;
  protected float y;
  protected float left;
  protected float top;
  protected float width;
  protected float height;

  public boolean destroyed;
  /**
   * 유닛 정중앙
   */

  /**
   * 사정거리
   */
  protected float range;
  /**
   * 충돌거리
   */
  protected float hitRange;
  protected RectF hitRect;

  protected Bitmap face;
  protected Paint paint;

  // 임시 생성자
  public Unit() {}

  /**
   * @param type 유닛의 종류를 입력한다. -> Unit.TYPE_XXXX
   * @param id 해당하는 종류 안에서 특정 유닛을 가리키는 정수 값
   * @param face 화면에 표시할 이미지
   */
  public Unit(Type type, int id, Bitmap face) {
    this(type, id, -1, -1, face);
  }

  /**
   * @param type 유닛의 종류를 입력한다.
   * @param id 해당하는 종류 안에서 특정 유닛을 가리키는 정수 값
   * @param x 이미지를 표시할 x 좌표
   * @param y 이미지를 표시할 y 좌표
   * @param face 화면에 표시할 이미지
   */
  public Unit(Type type, int id, float x, float y, Bitmap face) {
    this.type = type;
    this.id = id;

    if (face == null) {
      AppManager.printErrorLog("타워의 비트맵이 null이다!");
    }

    // TODO 비트맵에서 읽어오면 안됨
    width = face.getWidth() * 2;
    height = face.getHeight() * 2;

    setX(x);
    setY(y);
    hitRect = new RectF();

    hitRange = Math.max(width, height) / 2;
    destroyed = false;

    updateHitRect();

    this.face = face;
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Style.STROKE);
  }

  /**
   * 오버라이딩 금지.
   * 
   * @return 유닛의 타입을 의미하는 정수를 반환한다.
   */
  public final Type getType() {
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

  public final void setX(float x) {
    this.x = x;
    left = x - width * 0.5f;
  }

  public final void setY(float y) {
    this.y = y;
    top = y - height * 0.5f;
  }

  public final float getX() {
    return x;
  }

  public final float getY() {
    return y;
  }

  public void updateHitRect() {
    hitRect.set(left, top, left + width, top + height);
  }

  public RectF getHitRect() {
    return new RectF(hitRect);
  }

  /**
   * 타이머를 다시 작동시켜서 해당 유닛도 작동하게 한다.
   */
  abstract public void unfreeze();

  /**
   * 타이머를 멈춰서 해당 유닛이 모든 행동을 멈추게 한다.
   */
  abstract public void freeze();

  /**
   * 단순히 멤버로 가지고 있는 비트맵 이미지를 출력해서 객체 자기 자신을 나타낸다.
   * 
   * @param canvas 그림이 출력될 캔버스.
   */
  public void draw(Canvas canvas, float screenRatio) {
    if (face != null) {
      canvas.drawBitmap(face, left * screenRatio, top * screenRatio, paint);
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
  public void showRange(Canvas canvas, float screenRatio) {
    Paint circle = new Paint();
    circle.setAntiAlias(true);
    circle.setStyle(Paint.Style.STROKE); // 원의 윤곽선만 그림
    circle.setStrokeWidth(3); // 윤곽선 두께
    circle.setColor(Color.GREEN); // 윤곽선은 초록색
    canvas.drawCircle(x * screenRatio, y * screenRatio, range * screenRatio, circle);
  }

  /**
   * 체력바를 그려주는 메소드
   * 
   * @param hpMax
   * @param hp
   * @param canvas
   */
  public void showHealthBar(int hpMax, int hp, Canvas canvas, float screenRatio) {
    Paint paint = new Paint();

    /* 체력량에 따라 체력바 길이가 결정 */
    float healthScale = (float) hp / hpMax;

    if (healthScale >= 0.7)
      paint.setColor(Color.GREEN);
    else if (0.4 <= healthScale && healthScale < 0.7)
      paint.setColor(Color.YELLOW);
    else if (healthScale < 0.4)
      paint.setColor(Color.RED);

    canvas.drawRect(left * screenRatio, top * screenRatio - 10, left * screenRatio + width
        * screenRatio * healthScale, top * screenRatio - 5, paint);
  }

  /**
   * suspect의 사정거리 안에 victim이 있는지
   * 
   * @param suspect 때리는 놈
   * @param victim 맞는 놈
   * @return
   */
  public boolean inRange(Unit suspect, Unit victim) {
    /* 두 점 사이의 거리가 반지름의 합보다 작을 경우 충돌로 판단 */
    if (Math.sqrt(Math.pow(suspect.x - victim.x, 2) + Math.pow(suspect.y - victim.y, 2)) < suspect.range
        + victim.hitRange)
      return true;
    else
      return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return type.toString() + id + "@" + Integer.toHexString(hashCode());
  }
}
