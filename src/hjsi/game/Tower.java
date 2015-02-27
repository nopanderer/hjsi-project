package hjsi.game;

import hjsi.common.AppManager;
import hjsi.common.Timer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public class Tower extends Unit implements Attackable {
  /**
   * 타워 이미지 리소스 이름
   */
  public String resourceFileName;
  /**
   * 타워 이름
   */
  public String name;
  /**
   * 공격력
   */
  public int damage;
  /**
   * 공격속도
   */
  public int attackSpeed;
  /**
   * 등급
   */
  private int tier;


  /**
   * 공격횟수를 제어하는 타이머
   */
  private Timer timerAttack;

  private static final int PRIMITIVE = 1;
  private static final int BASIC = 2;
  private static final int SPECIAL = 3;
  private static final int MIGHTY = 4;
  private static final int TOP = 5;
  private static final int LEGEND = 6;
  private static final int HIDDEN = 7;

  // private static final String[] TIER_CAPTIONS = {"원시적인",

  public Tower() {
    super();
    name = "불";
    tier = 0;
    damage = 5;
    resourceFileName = "element_match";

  }

  /**
   * @param id 타워 식별자
   * @param name 게임에 표시될 타워 이름
   * @param tier 타워 등급
   * @param damage 타워 데미지
   * @param attackSpeed 타워의 공격속도
   * @param range 타워 사정거리
   * @param face 타워 이미지
   */
  public Tower(int id, String name, int tier, int damage, int attackSpeed, int range, Bitmap face) {
    super(Unit.TYPE_TOWER, id, face);
    this.name = name;
    this.tier = tier;
    this.damage = damage;
    this.attackSpeed = attackSpeed;
    this.range = range;

    timerAttack = Timer.create(name, attackSpeed);
    timerAttack.start();
  }

  public int getTier() {
    return tier;
  }

  @Override
  public void action() {
    // TODO Auto-generated method stub

  }

  @Override
  public void touch() {
    // TODO Auto-generated method stub

  }

  @Override
  public void draw(Canvas canvas, float screenRatio) {
    super.draw(canvas, screenRatio);
    showRange(canvas, screenRatio);

    updateHitRect();
    RectF drawingBox = getHitRect();
    drawingBox.left *= screenRatio;
    drawingBox.right *= screenRatio;
    drawingBox.top *= screenRatio;
    drawingBox.bottom *= screenRatio;

    canvas.drawRect(drawingBox, paint);
  }

  @Override
  public Projectile attack(Hittable unit) {
    if (timerAttack.isUsable()) {
      timerAttack.consumeTimer();
      Mob target = (Mob) unit;
      return new Projectile(x, y, damage, target, AppManager.getBitmap("proj1"));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#unfreeze()
   */
  @Override
  public void unfreeze() {
    timerAttack.resume();
  }

  /*
   * (non-Javadoc)
   * 
   * @see hjsi.game.Unit#freeze()
   */
  @Override
  public void freeze() {
    timerAttack.pause();
  }
}
