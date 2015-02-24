package hjsi.game;


public class Vector2d {
  public float x, y;
  public static float TO_RADIANS = (1 / 180.0f) * (float) Math.PI;
  public static float TO_DEGREES = (1 / (float) Math.PI) * 180;


  public Vector2d() {}

  public Vector2d(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vector2d(Vector2d other) {
    this.x = other.x;
    this.y = other.y;
  }

  public void set(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vector2d sub(Vector2d vector) {
    x -= vector.x;
    y -= vector.y;
    return this;
  }

  public void nor() {
    double len = len();
    if (len != 0) {
      x /= len;
      y /= len;
    }
  }

  public double len() {
    return Math.sqrt(x * x + y * y);
  }


  public void mul(float scalar) {
    x *= scalar;
    y *= scalar;
  }

  public float angle() {
    float angle = (float) Math.atan2(y, x) * TO_DEGREES;
    if (angle < 0)
      angle += 360;
    return angle;
  }

  public Vector2d rotate(float angle) {
    float rad = angle * TO_RADIANS;
    double cos = Math.cos(rad);
    double sin = Math.sin(rad);
    double newX = this.x * cos - this.y * sin;
    double newY = this.x * sin + this.y * cos;
    this.x = (float) newX;
    this.y = (float) newY;
    return this;
  }
}
