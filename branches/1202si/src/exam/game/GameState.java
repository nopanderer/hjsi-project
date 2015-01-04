package exam.game;

import java.util.ArrayList;

import android.content.res.Resources;

/*
 * 현재는 테스트용으로 아주 대충 만듦 여기에 게임 오브젝트들을 보관한다. 스테이지 정보와는 다르다. 예를 들어, 이 클래스에서는 몹이 죽으면 그 개체는 제거된다.
 */
public class GameState {
  ArrayList<Unit> arTestUnits = new ArrayList<Unit>();

  public GameState(Resources res) {
    EDElement mElement = new EDElement();
  }

  public ArrayList<Unit> getUnits() {
    return arTestUnits;
  }
}
