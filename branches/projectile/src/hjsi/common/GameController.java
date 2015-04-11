package hjsi.common;

import android.graphics.Canvas;

public interface GameController {
  /**
   * 게임을 재개하도록 요청한다.
   */
  public void resumeGame();

  /**
   * 게임을 멈추도록 요청한다.
   */
  public void pauseGame();

  /**
   * 게임을 종료하도록 요청한다.
   */
  public void quitGame();

  /**
   * 웨이브 시작 버튼을 그린다.
   */
  public void drawWaveButton(Canvas canvas);

  /**
   * 몹 생성을 하도록 요청한다.
   */
  public void startWave();

  /**
   * 몹 생성 버튼을 표시하도록 요청한다.
   */
  public void finishWave();

  /**
   * 사운드를 on/off 하도록 요청한다.
   */
  public void toggleSound();

}
