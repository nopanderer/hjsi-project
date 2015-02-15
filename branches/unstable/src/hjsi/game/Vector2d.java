package hjsi.game;

import android.util.FloatMath;

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

  public void nor() {
    float len = len();
    if (len != 0) {
      x /= len;
      y /= len;
    }
  }

  public float len() {
    return FloatMath.sqrt(x * x + y * y);
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
    float cos = FloatMath.cos(rad);
    float sin = FloatMath.sin(rad);
    float newX = this.x * cos - this.y * sin;
    float newY = this.x * sin + this.y * cos;
    this.x = newX;
    this.y = newY;
    return this;
  }
}
