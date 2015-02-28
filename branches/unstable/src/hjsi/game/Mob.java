package hjsi.game;

import hjsi.common.AppManager;
import hjsi.common.Timer;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

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
  private int lap;

  private Timer timerMovement;
  private Timer timerAttack;
  private Timer timerSprite;

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
  private Rect spriteSrc;
  /**
   * 프레임 갯수
   */
  private int frameNum;
  /**
   * 현재 프레임
   */
  private int curFrame;
  /**
   * 프레임 간격
   */
  private long framePeriod;

  public Mob(int x, int y, Bitmap face, int wave, Station dest) {
    super(Type.MOB, 0, x, y, face);

    setLap(0);
    this.wave = wave;

    hpMax = 100;
    hp = hpMax;

    damage = 10;
    attackSpeed = 2000;
    moveSpeed = 1;
    range = 400;

    vector = new Vector2d();
    stationIndex = 0;
    station = dest;

    curFrame = 0;
    frameNum = 4;
    framePeriod = 1000 / frameNum;
    // TODO 가로, 세로 비트맵에서 가져오면 안됨
    width = face.getWidth() / frameNum;
    height = face.getHeight();
    spriteSrc = new Rect(0, 0, (int) this.width, (int) this.height);

    setX(x);
    setY(y);
    hitRange = Math.max(width, height) / 2;
    updateHitRect();

    timerMovement = Timer.create("몹 이동", 10);
    timerMovement.start();
    timerAttack = Timer.create("몹 공격", (long) (attackSpeed + 0.5));
    timerAttack.start();
    timerSprite = Timer.create("몹 프레임", framePeriod);
    timerSprite.start();
  }

  @Override
  public void draw(Canvas canvas, float scale) {
    showHealthBar(hpMax, hp, canvas, scale);
    setDrawingBox(getHitRect(), scale);
    canvas.drawBitmap(face, spriteSrc, getDrawingBox(), null);
    canvas.drawRect(getDrawingBox(), borderPaint);
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
  public Projectile attack(Hittable unit) {
    if (timerAttack.isUsable()) {
      timerAttack.consumeTimer();
      Statue target = (Statue) unit;
      return new Projectile(x, y, damage, target, AppManager.getBitmap("proj1"));
    }

    return null;
  }

  @Override
  public void move() {
    // 10밀리세컨드 마다 1 픽셀씩 이동
    if (timerMovement.isUsable()) {
      timerMovement.consumeTimer();

      vector.set(station.x - x, station.y - y);
      vector.nor();
      vector.mul(moveSpeed);

      setX(x + vector.x);
      setY(y + vector.y);
      updateHitRect();
    }
  }

  @Override
  public void action() {
    // TODO Auto-generated method stub

  }

  @Override
  public void hit(int damage) {
    if (isDestroyed() == false) {
      hp -= damage;
      if (hp <= 0)
        dead();
    }
  }

  @Override
  public void dead() {
    setDestroyed(true);
    GameState.curMob--;
    GameState.deadMob++;
  }

  public boolean isArrive() {
    return station.arrive(this);
  }

  public void nextStation(ArrayList<Station> stations) {
    stationIndex = (stationIndex + 1) % stations.size();
    if (stationIndex == 0)
      setLap(getLap() + 1);

    station = stations.get(stationIndex);
  }

  public void update(long gameTime) {
    if (timerSprite.isUsable()) {
      timerSprite.consumeTimer();

      curFrame++;
      if (curFrame >= frameNum) {
        curFrame = 0;
      }
    }
    spriteSrc.left = curFrame * (int) width;
    spriteSrc.right = spriteSrc.left + (int) width;
  }

  public int getLap() {
    return lap;
  }

  public void setLap(int lap) {
    this.lap = lap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#unfreeze()
   */
  @Override
  public void unfreeze() {
    timerMovement.resume();
    timerAttack.resume();
    timerSprite.resume();
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#freeze()
   */
  @Override
  public void freeze() {
    timerMovement.pause();
    timerAttack.pause();
    timerSprite.pause();
  }
}
