package hjsi.game;

import hjsi.common.AppManager;
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

  /* 최근 이동 시간 */
  private long moveTime;
  /* 최근 공격 시간 */
  private long attackTime;

  private int sleep = 10;

  public Vector2d vector;
  /**
   * 정류소 번호
   */
  private int stationIndex;
  /**
   * 정류소
   */
  private Station station;

  /* 스프라이트 이미지를 위한 임시 변수 */
  private Rect rect;
  /**
   * 프레임 갯수
   */
  private int frameNum;
  /**
   * 현재 프레임
   */
  private int curFrame;
  /**
   * 최근 업데이트된 시간
   */
  private long lastTime;
  /**
   * 프레임 간격
   */
  private int framePeriod;

  public Mob(int x, int y, Bitmap face, int wave) {
    super(Unit.TYPE_MOB, 0, x, y, face);

    lap = 0;
    this.wave = wave;
    moveTime = 0l;
    attackTime = 0l;

    hpMax = 100;
    hp = hpMax;

    damage = 10;
    attackSpeed = 2000;
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
    if (lap == 1) {
      if (GameMaster.gameTime > attackTime + attackSpeed)
        attackTime = GameMaster.gameTime;
      else
        return;

      for (int i = 0; i < GameState.getInstance().units.size(); i++) {

        if (GameState.getInstance().units.get(i) instanceof Statue) {

          Statue statue;
          statue = (Statue) GameState.getInstance().units.get(i);

          if (statue == null)
            continue;

          else if (statue.destroyed == false && inRange(this, statue)) {
            GameState.getInstance().units.add(new Projectile(cntrX, cntrY, damage, statue,
                AppManager.getBitmap("proj1")));
            break;
          }
        }
      }
    } else
      return;
  }

  @Override
  public void move() {
    // 10밀리세컨드 마다 1 픽셀씩 이동
    if (GameMaster.gameTime > moveTime + sleep)
      moveTime = GameMaster.gameTime;
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
        stationIndex = 0;
        station = GameState.getInstance().stations.get(stationIndex);
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

  @Override
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
