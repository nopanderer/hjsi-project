package hjsi.unit.attr;

public interface Hittable {
  /**
   * 방어력 x 당 데미지 감소 비율 x / (x + 100) * 100 </p> 예) 방어력 100일때 데미지 감소 비율 = 100 / (100 + 100) * 100 =
   * 50%
   * 
   * @param damage 받은 데미지
   */
  void hit(int damage);

  void dead();
}
