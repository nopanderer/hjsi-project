package hjsi.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Mob 클래스
 * 
 */
public class Mob extends Unit {


  /**
   * 최대 체력
   */
  private int hpMax;
  /**
   * 현재 체력
   */
  private int hp;
  /**
   * 이동할 거리
   */
  private int step = 5;
  /**
   * step당 지연시간
   */
  private int sleep = 10;
  /**
   * 타격범위
   */
  private int range = 400;
  /**
   * 움직이기 전 시간
   */
  private long beforeTime;
  /**
   * 웨이브
   */
  private int wave;
  /**
   * 몹이 생성 되었는가
   */
  public boolean created;
  /**
   * 몹이 죽었는가
   */
  public boolean dead;
  /**
   * 몇 바퀴 돌았나
   */
  public int lap;

  /* 처리를 위한 변수 */

  /**
   * 초기 생성 위치
   */
  private int oldX, oldY;

  public Mob(int x, int y, Bitmap face, int wave) {
    super(x, y, face);

    created = false;
    dead = false;
    lap = 0;
    this.wave = wave;

    beforeTime = System.currentTimeMillis();

    oldX = x;
    oldY = y;

  }

  /**
   * (non-Javadoc) 몹 이동 액션
   * 
   * @see hjsi.game.Unit#isFinish()
   */
  @Override
  public void action() {
    // 10밀리세컨드 마다 5 픽셀씩 이동
    if (System.currentTimeMillis() - beforeTime > sleep)
      beforeTime = System.currentTimeMillis();
    else
      return;

    if (x == oldX && y == oldY)
      lap++;

    // 아래로
    if (x == oldX && y + step <= 2160 - 900) {
      y += step;
    }
    // 오른쪽
    else if (x + step <= 3840 - 1500 && y == 2160 - 900) {
      x += step;
    }
    // 위로
    else if (x == 3840 - 1500 && y - step >= oldY) {
      y -= step;
    }
    // 왼쪽
    else if (x - step >= oldX && y == oldY) {
      x -= step;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#draw(android.graphics.Canvas)
   */
  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    showRange(x, y, range, canvas);
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
}
