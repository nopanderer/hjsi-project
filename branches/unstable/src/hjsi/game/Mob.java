package hjsi.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Mob 클래스
 * 
 */
public class Mob extends Unit implements Movable, Attackable, Hittable {

  /**
   * 최대 체력
   */
  private int hpMax;
  /**
   * 현재 체력
   */
  private int hp;
  /**
   * 이동속도
   */
  private int moveSpeed;
  /**
   * 공격속도
   */
  private float attackSpeed;
  /**
   * 공격력
   */
  private int damage;
  /**
   * 방어력
   */
  private int armor;
  /**
   * 웨이브 번호
   */
  private int wave;

  /**
   * 몇 바퀴 돌았나
   */
  public int lap;

  /* 리젠 */
  private long beforeTime;
  private int sleep = 10;

  public Vector2d vector;
  private int stationIndex;
  private Station station;

  private Rect rect;
  private int frameNum;
  private int curFrame;
  private long lastTime;

  private int framePeriod;

  public Mob(int x, int y, Bitmap face, int wave) {
    super(Unit.TYPE_MOB, 0, x, y, face);

    lap = 0;
    this.wave = wave;
    beforeTime = System.currentTimeMillis();

    hpMax = 100;
    hp = hpMax;

    moveSpeed = 1;
    range = 400;

    vector = new Vector2d();
    stationIndex = 0;
    station = GameState.getInstance().stations.get(stationIndex);


    curFrame = 0;
    frameNum = 4;
    width = face.getWidth() / frameNum;
    height = face.getHeight();
    rect = new Rect(0, 0, this.width, this.height);
    framePeriod = 1000 / 4;
    lastTime = 0l;

    setX(x);
    setY(y);
  }

  @Override
  public void draw(Canvas canvas) {
    RectF destRect = new RectF(x, y, x + width, y + height);
    canvas.drawBitmap(face, rect, destRect, null);
    showHealthBar(hpMax, hp, canvas);
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#touch()
   */
  @Override
  public void touch() {
    // TODO Auto-generated method stub

  }

  @Override
  public void attack() {
    // TODO Auto-generated method stub

  }

  @Override
  public void move() {
    // TODO Auto-generated method stub
    // 10밀리세컨드 마다 1 픽셀씩 이동

    if (System.currentTimeMillis() - beforeTime > sleep)
      beforeTime = System.currentTimeMillis();
    else
      return;

    vector.set(station.x - x, station.y - y);
    vector.nor();
    vector.mul(moveSpeed);

    x += vector.x;
    y += vector.y;
    cntrX += vector.x;
    cntrY += vector.y;

    if (station.arrive(this)) {
      stationIndex++;
      if (stationIndex >= GameState.getInstance().stations.size()) {
        lap++;
        return;
      }

      station = GameState.getInstance().stations.get(stationIndex);
    }

  }

  @Override
  public void action() {
    // TODO Auto-generated method stub

  }

  @Override
  public void hit(int damage) {
    if (destroyed == false) {
      hp -= damage;
      if (hp <= 0)
        dead();
    }
  }

  public void dead() {
    destroyed = true;
    GameState.getInstance().curMob--;
    GameState.getInstance().deadMob++;
  }

  public void update(long gameTime) {
    if (gameTime > lastTime + framePeriod) {
      lastTime = gameTime;
      curFrame++;
      if (curFrame >= frameNum) {
        curFrame = 0;
      }
    }
    rect.left = curFrame * width;
    rect.right = rect.left + width;
  }
}
