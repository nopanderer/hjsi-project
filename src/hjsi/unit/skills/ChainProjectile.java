package hjsi.unit.skills;

import hjsi.game.Mob;
import hjsi.game.Projectile;
import hjsi.game.Unit;

import java.util.LinkedList;

import android.graphics.Bitmap;

public class ChainProjectile extends Projectile {
  private int timeToLive;
  private LinkedList<Mob> visited;

  public ChainProjectile(float x, float y, int damage, Unit target, Bitmap face) {
    super(x, y, damage, target, face);
    
    timeToLive = 0;
  }

}
