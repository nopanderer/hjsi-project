package hjsi.game;

import hjsi.common.AppManager;
import hjsi.common.Timer;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Tower extends Unit implements Attackable {
  public enum Tier {
    PRIMITIVE(0, "원시적인"), BASIC(1, "기초적인"), SPECIAL(3, "특별한"), MIGHTY(4, "강력한"), TOP(5, "최상의"), LEGEND(
        6, "전설의"), HIDDEN(7, "숨겨진");

    public static Tier getTier(int order) {
      return tiers[order];
    }
    private final int value;
    private final String caption;

    private static final Tier[] tiers = values();

    Tier(int value, String caption) {
      this.value = value;
      this.caption = caption;
    }

    public String getCaption() {
      return caption;
    }

    public int getValue() {
      return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return caption;
    }
  }

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
  private Tier tier;


  /**
   * 공격횟수를 제어하는 타이머
   */
  private Timer timerAttack;

  public Tower() {
    super();
    name = "불";
    tier = Tier.PRIMITIVE;
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
  public Tower(int id, String name, Tier tier, int damage, int attackSpeed, int range, Bitmap face) {
    super(Type.TOWER, id, face);
    this.name = name;
    this.tier = tier;
    this.damage = damage;
    this.attackSpeed = attackSpeed;
    this.range = range;

    timerAttack = Timer.create(name, attackSpeed);
    timerAttack.start();
  }

  @Override
  public void action() {
    // TODO Auto-generated method stub

  }

  @Override
  public Projectile attack(Hittable unit) {
    if (timerAttack.isUsable()) {
      timerAttack.startDelayed();
      Mob target = (Mob) unit;
      return new Projectile(x, y, damage, target, AppManager.getBitmap("projectile1"));
    }
    return null;
  }

  @Override
  public void draw(Canvas canvas, float scale) {
    super.draw(canvas, scale);

    if (isSelected()) {
      showRange(canvas, scale);
      updateHitRect();
      setDrawingBox(getHitRect(), scale);
      canvas.drawRect(getDrawingBox(), borderPaint);
    }
  }

  @Override
  public void freeze() {
    timerAttack.pause();
  }

  public Tier getTier() {
    return tier;
  }

  @Override
  public String toString() {
    return "<" + tier + " " + name + "> 타워";
  }

  @Override
  public void touch() {
    // TODO Auto-generated method stub

  }

  @Override
  public void unfreeze() {
    timerAttack.resume();
  }
}
