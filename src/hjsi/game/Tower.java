package hjsi.game;

import hjsi.common.AppManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Tower extends Unit implements Attackable {
  public String name;
  /**
   * 공격력
   */
  public int damage;
  /**
   * 공격속도
   */
  public int attackSpeed = 2000;
  /**
   * 등급
   */
  private int tier;
  public String imgName;

  private long beforeTime = 0l;

  private static final int PRIMITIVE = 1;
  private static final int BASIC = 2;
  private static final int SPECIAL = 3;
  private static final int MIGHTY = 4;
  private static final int TOP = 5;
  private static final int LEGEND = 6;
  private static final int HIDDEN = 7;

  public Tower() {
    super();
    name = "불";
    tier = 0;
    damage = 5;
    imgName = "element_match";

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
  }

  public Tower(int id, int x, int y, Bitmap face) {
    super(Unit.TYPE_TOWER, id, x, y, face);
    damage = 50;
    range = 300;
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
  }

  @Override
  public void attack(Unit unit) {
    if (GameMaster.gameTime > beforeTime + attackSpeed * GameMaster.ff)
      beforeTime = GameMaster.gameTime;
    else
      return;

    Mob mob;
    mob = (Mob) unit;

    if (mob == null)
      return;

    else if (mob.destroyed == false && inRange(this, mob))
      GameState.getInstance().units.add(new Projectile(cntrX, cntrY, damage, mob, AppManager
          .getBitmap("proj1")));
  }

}
