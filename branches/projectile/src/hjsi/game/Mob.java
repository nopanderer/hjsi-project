package hjsi.game;

import hjsi.common.AppManager;
import hjsi.common.Timer;
import hjsi.unit.attr.Attackable;
import hjsi.unit.attr.Hittable;
import hjsi.unit.attr.Movable;
import hjsi.unit.skills.Projectile;

import java.util.ArrayList;
import java.util.LinkedList;

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
  private int station;
  /**
   * 게임상의 모든 정류소
   */
  private ArrayList<Station> stations;

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

  public Mob(int x, int y, Bitmap face, int wave, ArrayList<Station> stations) {
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
    station = 0;
    this.stations = stations;

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
  public LinkedList<Attackable> attack(LinkedList<Hittable> hittables) {
    /* !!! TEST CODE !!! */
    if (lap < 1)
      return null;
    /* !!! TEST CODE !!! */

    if (timerAttack.isUsable()) {
      timerAttack.consumeTimer();
      LinkedList<Attackable> projs = new LinkedList<Attackable>();

      for (Hittable hittable : hittables) {
        if (((Unit) hittable).getType() != Type.STATUE)
          continue;

        Statue statue = (Statue) hittable;
        if (statue.isDestroyed() == false && inRange(this, statue)) {
          Projectile proj =
              new Projectile(x, y, damage, statue,
                  AppManager.getBitmap(Type.PROJECTILE.toString() + 1));
          projs.add(proj);
          break; // 기본적으로는 몹이 동상 한 개만 공격해야하니까 투사체 하나 만들었으면 반복문 종료
        }
      }
      return projs;
    } else
      return null;
  }

  @Override
  public void move() {
    if (isArrive()) {
      nextStation();
    }

    /* 원활한 테스트를 위해서 2바퀴 도달하면 장례식 */
    if (getLap() == 2) {
      dead();
    }
    /* 10밀리세컨드 마다 1 픽셀씩 이동 */
    else if (timerMovement.isUsable()) {
      timerMovement.consumeTimer();

      vector.set(stations.get(station).x - x, stations.get(station).y - y);
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
    return stations.get(station).arrive(this);
  }

  public void nextStation() {
    station = (station + 1) % stations.size();
    if (station == 0)
      setLap(getLap() + 1);
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
