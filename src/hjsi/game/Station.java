package hjsi.game;

public class Station {
  float x, y;
  int range = 10;

  public Station(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public boolean arrive(Mob mob) {
    if ((int) Math.sqrt(Math.pow(mob.x - x, 2) + Math.pow(mob.y - y, 2)) <= range)
      return true;
    else
      return false;
  }
}
