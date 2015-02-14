package hjsi.game;

import hjsi.common.AppManager;
import hjsi.timer.TimeManager;
import hjsi.timer.TimerRunnable;

import java.io.IOException;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

/**
 * 게임에 필요한 정보를 저장한다.
 */
public class GameState {
  private static GameState uniqueInstance;
  /**
   * 아직 자리가 확정되지 않은 배치할 타워를 가리킨다. null이 아니라면 게임 화면이 배치모드로 표시된다.
   */
  public Tower inHand = null;
  /**
   * 게임 상에 배치되어 있는 타워 자리 표
   */
  private boolean[][] deployTable = new boolean[8][10];

  /**
   * 현재 게임이 진행된 시간을 나타낸다.
   */
  private volatile long worldTime = 0L;
  /**
   * 현재 단계
   */
  public int wave = 1;
  /**
   * 유닛 통합 연결 리스트
   */
  LinkedList<Unit> units = new LinkedList<Unit>();

  public Bitmap mImgMob; // 몹 비트맵
  public long beforeRegen = System.currentTimeMillis(); // 리젠하기 전 시간
  public long pBeforeRegen = System.currentTimeMillis(); // 리젠하기 전 시간
  public long regen = 1000; // create mob per 1 sec
  public int usedMob = 0; // 몹이 실제로 생성된(내부적 카운터 위해)
  public int deadMob = 0; // 죽은 몹
  public int curMob = 0; // 현재 몹

  public static final int MAX_MOB = 10;

  private GameState() {
    AppManager.printSimpleLog();

    /*
     * 게임 진행 시간 측정을 위한 타이머를 생성해서 등록해둔다.
     */
    TimerRunnable clock = new TimerRunnable() {
      @Override
      public void run() {
        worldTime++;
        AppManager.printDetailLog("타이머 체크");
      }
    };
    TimeManager.registerCallbackTimer(1000, clock, -1).start();

    /*
     * 동상 타워 추가
     */
    makeFace();
    units.add(new Statue(500, 300, AppManager.getInstance().getBitmap("statue1")));
    units.add(new Tower(367, 467, AppManager.getInstance().getBitmap("tower1")));
  }

  public static GameState getInstance() {
    if (GameState.uniqueInstance == null) {
      synchronized (GameState.class) {
        if (GameState.uniqueInstance == null) {
          GameState.uniqueInstance = new GameState();
        }
      }
    }
    return GameState.uniqueInstance;
  }

  /**
   * 현재의 게임 정보를 가지고 있는 GameState의 유일한 객체를 없애서 정보를 초기화한다.
   */
  public void purgeGameState() {
    synchronized (GameState.class) {
      GameState.uniqueInstance = null;
    }
  }

  /**
   * 게임을 배치모드로 전환하고 랜덤으로 타워를 생성하여 반환한다.
   * 
   * @returns 토큰이 충분하다면 true를 반환하고, 토큰이 부족하다면 false를 반환한다.
   */
  public boolean intoDeployMode() {
    // 1. 유저 정보에서 가지고 있는 토큰 수를 확인한다. 부족하면 false를 반환한다.
    // 2. 토큰 수에 알맞은 등급의 임의의 타워를 생성한다.
    // 타워 구매 도우미(팩토리 클래스)를 이용해서 타워를 생성한다.
    inHand = new Tower();
    return true;
  }

  /**
   * 배치모드 여부를 반환한다.
   * 
   * @return 현재 배치할 타워가 있으면 true, 없으면 false를 반환한다.
   */
  public boolean checkDeployMode() {
    return (inHand != null);
  }

  public long getWorldTime() {
    return worldTime;
  }

  /**
   * 터치로 입력받은 게임 좌표를 통해서 유닛을 가져온다. 만약, 해당 좌표에 여러 유닛이 걸쳐져 있으면 게임 상에 늦게 추가된 순서로
   * 우선순위가 있다.
   * 
   * @param x 게임 x 좌표
   * @param y 게임 y 좌표
   * @return 주어진 게임 좌표 위에 유닛이 있다면 해당 유닛, 없으면 null을 반환한다.
   */
  public Unit getUnit(int x, int y) {
    for (Unit unit : units) {
      if ((unit.x < x && x < unit.x + unit.width) && (unit.y < y && y < unit.y + unit.height))
        return unit;
    }
    return null;
  }

  /**
   * 유닛 통합 연결 리스트 반환
   * 
   * @return units
   */
  public LinkedList<Unit> getUnits() {
    return units;
  }

  public void makeFace() {
    Options option = new Options();
    option.inSampleSize = 16;
    String key = "mob" + wave;

    try {
      mImgMob = AppManager.getInstance().readImageFile("img/mobs/" + key + ".png", option);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if ((mImgMob.getWidth() != 64) || (mImgMob.getHeight() != 64)) {
      mImgMob = Bitmap.createScaledBitmap(mImgMob, 64, 64, true);
    }

    AppManager.getInstance().addBitmap(key, mImgMob);
  }

  public void addMob() {
    if (System.currentTimeMillis() - beforeRegen > regen)
      beforeRegen = System.currentTimeMillis();
    else
      return;

    units.add(new Mob(90, 90, mImgMob, wave));
    usedMob++;
    curMob++;
  }

  public void destroyMob() {
    AppManager.getInstance().recycleBitmap("mob" + wave);
    for (int i = 0; i < units.size(); i++)
      if (units.get(i) instanceof Mob)
        GameState.getInstance().units.remove(i);

  }

  public void nextWave() {
    destroyMob();
    wave++;
    // 새로운 비트맵 추가
    makeFace();
    // init(임시)
    curMob = 0;
    usedMob = 0;
    deadMob = 0;
  }

}
