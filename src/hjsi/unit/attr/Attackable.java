package hjsi.unit.attr;

import java.util.LinkedList;

/**
 * Attackable은 타워가 사정거리 내의 몹을 발견해서 투사체를 발사하는 것처럼 새로운 투사체를 생성하는 경우에 사용한다. 반대로, 투사체가 타겟과 충돌하여 폭발하는 경우는
 * Attackable의 attack 메소드가 처리하지 않는다.
 *
 */
public interface Attackable {
  LinkedList<Attackable> attack(LinkedList<Hittable> units);
}
