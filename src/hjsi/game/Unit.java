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
  /**
   * 유닛의 종류를 나타내는 열거형
   */
  public enum Type {
    STATUE(0), TOWER(1), MOB(2), PROJECTILE(3);
    private final int value;
    private static final Type[] types = values();

    Type(int value) {
      this.value = value;
    }

    public Type getType(int index) {
      return types[index];
    }

    public int getValue() {
      return value;
    }

    @Override
    public String toString() {
      // 파일 읽어올 때 이걸로 쓰기 때문에 소문자로 바꿈.
      return name().toString().toLowerCase(Locale.US);
    }
  }

  protected static Paint borderPaint;
  protected static Paint rangePaint;
  /**
   * 유닛의 종류별로 고유한 아이디. 즉, 유닛의 종류가 다를 경우 같은 아이디를 가질 수 있으며, 같은 id를 가지는 개체도 존재할 수 있음.
   */
  private int id;
  /**
   * 유닛의 종류를 나타낸다.
   */
  private Type type;
  /**
   * 유닛의 중심 X 좌표
   */
  protected float x;
  /**
   * 유닛의 중심 Y 좌표
   */
  protected float y;
  /**
   * 유닛이 게임 상에서 차지하는 LEFT X 좌표. 비트맵 리소스에 투명한 여백이 없다면 이 좌표를 비트맵 출력에 사용해도 된다.
   */
  protected float left;
  /**
   * 유닛이 게임 상에서 차지하는 TOP Y 좌표. 비트맵 리소스에 투명한 여백이 없다면 이 좌표를 비트맵 출력에 사용해도 된다.
   */
  protected float top;
  /**
   * 유닛의 충돌 크기를 결정하는 가로 크기. 비트맵 리소스 자체의 보이지 않는 투명 픽셀 때문에 비트맵과는 사이즈가 다를 수도 있다.
   */
  protected float width;
  /**
   * 유닛의 충돌 크기를 결정하는 세로 크기. 비트맵 리소스 자체의 보이지 않는 투명 픽셀 때문에 비트맵과는 사이즈가 다를 수도 있다.
   */
  protected float height;
  /**
   * 사정거리 (반지름)
   */
  protected float range;
  /**
   * 유저에 의해 선택된 상태인지 아닌지를 구별함
   */
  private boolean selected;
  /**
   * 유닛의 파괴 상태. 게임 상에서 파괴된다고 해서 바로 삭제를 하지 않고, 임시로 이 변수의 값이 설정된다. 파괴된 프레임 다음의 프레임에서 이 유닛이 제거된다.
   */
  private boolean destroyed;
  /**
   * 충돌거리
   */
  protected float hitRange;
  protected RectF hitRect;

  /**
   * draw할 때마다 hitRect에 비율 변수를 곱한게 필요한데, 매번 메모리 할당을 안하려고 멤버 변수로 가지고 있는다.
   */
  private RectF drawingBox;
  protected Bitmap face;

  static {
    borderPaint = new Paint();
    borderPaint.setAntiAlias(true);
    borderPaint.setStyle(Style.STROKE);
    borderPaint.setStrokeWidth(3);
    borderPaint.setColor(Color.BLACK);

    rangePaint = new Paint();
    rangePaint.setAntiAlias(true);
    rangePaint.setStyle(Style.STROKE); // 원의 윤곽선만 그림
    rangePaint.setStrokeWidth(3); // 윤곽선 두께
    rangePaint.setColor(Color.GREEN); // 윤곽선은 초록색
  }

  {
    hitRect = new RectF();
    drawingBox = new RectF();

    setSelected(false);
    setDestroyed(false);
  }

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

    if (face == null)
      AppManager.printErrorLog("타워의 비트맵이 null이다!");
    else
      this.face = face;

    // TODO width, height를 단순히 비트맵 그대로 사용하는 건 부적절함.
    width = face.getWidth() * 2;
    height = face.getHeight() * 2;
    hitRange = Math.max(width, height) / 2;
    setX(x);
    setY(y);
  }

  /**
   * 유닛으로 공통된 사용을 위해 정의했고, 유닛을 상속하는 세부적인 클래스에 따라 고유한 행동을 정의한다.
   */
  public abstract void action();

  /**
   * 해당하는 좌표가 유닛이 차지하는 영역에 들어오는지 검사한다.
   * 
   * @param x 검사할 x 값
   * @param y 검사할 y 값
   * @return 점 x, y가 유닛이 차지하는 영역 안에 있으면 true, or false
   */
  public boolean contains(float x, float y) {
    return getHitRect().contains(x, y);
  }

  /**
   * 단순히 멤버로 가지고 있는 비트맵 이미지를 출력해서 객체 자기 자신을 나타낸다.
   * 
   * @param canvas 그림이 출력될 캔버스.
   * @param scale 게임 상의 단위를 화면에 맞게 바꿔줄 게임 대 화면의 비율 값
   */
  public void draw(Canvas canvas, float scale) {
    if (face != null) {
      canvas.drawBitmap(face, left * scale, top * scale, borderPaint);
    }
  }

  /**
   * 타이머를 멈춰서 해당 유닛이 모든 행동을 멈추게 한다.
   */
  abstract public void freeze();

  public RectF getHitRect() {
    return hitRect;
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
   * 오버라이딩 금지.
   * 
   * @return 유닛의 타입을 의미하는 정수를 반환한다.
   */
  public final Type getType() {
    return type;
  }

  public final float getX() {
    return x;
  }

  public final float getY() {
    return y;
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

  public boolean isDestroyed() {
    return destroyed;
  }

  /**
   * 이 유닛이 현재 선택된 상태인지 확인한다.
   * 
   * @return selected 값
   */
  public boolean isSelected() {
    return selected;
  }

  public void setDestroyed(boolean destroyed) {
    this.destroyed = destroyed;
  }

  /**
   * 유닛의 선택 상태를 설정한다.
   * 
   * @param selected 설정하려는 선택 상태 값
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public final void setX(float x) {
    this.x = x;
    left = x - width * 0.5f;
    updateHitRect();
  }

  public final void setY(float y) {
    this.y = y;
    top = y - height * 0.5f;
    updateHitRect();
  }

  /**
   * 체력바를 그려주는 메소드
   * 
   * @param hpMax
   * @param hp
   * @param canvas
   */
  public void showHealthBar(int hpMax, int hp, Canvas canvas, float scale) {
    Paint paint = new Paint();

    /* 체력량에 따라 체력바 길이가 결정 */
    float healthScale = (float) hp / hpMax;

    if (healthScale >= 0.7)
      paint.setColor(Color.GREEN);
    else if (0.4 <= healthScale && healthScale < 0.7)
      paint.setColor(Color.YELLOW);
    else if (healthScale < 0.4)
      paint.setColor(Color.RED);

    canvas.drawRect(left * scale, top * scale - 10, left * scale + width * scale * healthScale, top
        * scale - 5, paint);
  }

  /**
   * 범위 그려주는 메소드
   * 
   * @param range 타격 범위
   * @param canvas
   */
  public void showRange(Canvas canvas, float scale) {
    canvas.drawCircle(x * scale, y * scale, range * scale, rangePaint);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return type.toString() + id + "@" + (int) x + "," + (int) y;
  }

  /**
   * 사용자로부터 입력을 받았을 경우에 대한 처리를 실시한다.
   */
  public abstract void touch();

  /**
   * 타이머를 다시 작동시켜서 해당 유닛도 작동하게 한다.
   */
  abstract public void unfreeze();

  protected RectF getDrawingBox() {
    return drawingBox;
  }

  protected void setDrawingBox(RectF drawingBox) {
    this.drawingBox.set(drawingBox);
  }

  protected void setDrawingBox(RectF drawingBox, float scale) {
    setDrawingBox(drawingBox);
    this.drawingBox.left *= scale;
    this.drawingBox.top *= scale;
    this.drawingBox.right *= scale;
    this.drawingBox.bottom *= scale;
  }

  protected void updateHitRect() {
    hitRect.set(left, top, left + width, top + height);
  }
}
