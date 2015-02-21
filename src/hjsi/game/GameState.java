package hjsi.game;

import hjsi.common.AppManager;
import hjsi.common.DataManager;
import hjsi.timer.TimeManager;
import hjsi.timer.Timer;
import hjsi.timer.TimerRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

/**
 * 게임에 필요한 정보를 저장한다.
 */
public class GameState {
  private static GameState uniqueInstance;
  public static final int WORLD_WIDTH = 3840;
  public static final int WORLD_HEIGHT = 2160;

  /**
   * 마지막으로 클리어한 웨이브
   */
  private int userWave;
  /**
   * 보유 중인 골드
   */
  private int userGold;
  /**
   * 보유 중인 배치 코인
   */
  private int userCoin;

  /**
   * 아직 자리가 확정되지 않은 배치할 타워를 가리킨다. null이 아니라면 게임 화면이 배치모드로 표시된다.
   */
  private Tower inHand = null;
  /**
   * 타워 배치를 완료하기 전에 배치모드를 끄면, 코인으로 되돌려 받는게 아니라 타워를 잠시 보관한다.
   */
  private Tower keepingTower = null;

  private static final int AREA_LEFT = 800;
  private static final int AREA_RIGHT = 3040;
  private static final int AREA_TOP = 450;
  private static final int AREA_BOTTOM = 1710;
  /**
   * 타워가 배치되는 구역
   */
  private Rect towersArea = new Rect(AREA_LEFT, AREA_TOP, AREA_RIGHT, AREA_BOTTOM);
  public static final int TOWERS_WIDTH = (int) ((AREA_RIGHT - AREA_LEFT) / 10 + 0.5);
  public static final int TOWERS_HEIGHT = (int) ((AREA_BOTTOM - AREA_TOP) / 8 + 0.5);
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
  public long beforeRegen = 0l; // 리젠하기 전 시간
  public long regen = 1000; // create mob per 1 sec
  public static int usedMob = 0; // 몹이 실제로 생성된(내부적 카운터 위해)
  public static int deadMob = 0; // 죽은 몹
  public static int curMob = 0; // 현재 몹

  public static final int MAX_MOB = 10;

  public ArrayList<Station> stations = new ArrayList<Station>();

  private TimerRunnable spawnMob = new TimerRunnable() {
    @Override
    public void run() {
      units.add(new Mob(80, 80, mImgMob, userWave));
      usedMob++;
      curMob++;
    }
  };

  private GameState() {
    AppManager.printSimpleLog();

    /*
     * 게임 진행 시간 측정을 위한 타이머를 생성해서 등록해둔다.
     */
    TimerRunnable clock = new TimerRunnable() {
      @Override
      public void run() {
        worldTime++;
        AppManager.printDetailLog("딸깍...");
      }
    };
    TimeManager.registerCallbackTimer(1000, clock, -1).start();

    /*
     * 동상 추가
     */
    units.add(new Statue(5, 5, AppManager.getBitmap("statue1")));

    /* 정류장 삽입 */
    stations.add(new Station(80, 580));
    stations.add(new Station(1100, 580));
    stations.add(new Station(1100, 80));
    stations.add(new Station(80, 80));
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
      DataManager.save(this);
      GameState.uniqueInstance = null;
    }
  }

  public void setUserData(int wave, int gold, int coin, LinkedList<Tower> towers) {
    userWave = wave;
    userGold = gold;
    userCoin = coin;
    units.addAll(towers);
  }

  /**
   * 게임을 배치모드로 전환하고 랜덤으로 타워를 생성하여 반환한다.
   * 
   * @returns 토큰이 충분하다면 임의의 타워를 반환하고, 토큰이 부족하다면 null을 반환한다.
   */
  public boolean onDeployMode() {
    // 키핑해둔 타워가 있으면 다시 배치모드로 꺼낸다.
    if (inHand == null && keepingTower != null) {
      inHand = keepingTower;
      keepingTower = null;
      return true;

      // 배치해야되는 타워가 있는대 배치버튼을 눌렀으면 타워를 없애지 않고 키핑한다.
    } else if (inHand != null && keepingTower == null) {
      keepingTower = inHand;
      inHand = null;
      return false;

      // 유저 정보에서 가지고 있는 토큰 수를 확인한다. 부족하면 false를 반환한다.
    } else if (getCoin() < 3) {
      return false;

    } else {
      // 토큰 수에 알맞은 등급의 임의의 타워를 생성한다.
      int spentCoin = 0;
      int purchaseTier = 0;
      if (getCoin() >= 7) {
        spentCoin = 7;
        purchaseTier = 2;
      } else if (getCoin() >= 5) {
        spentCoin = 5;
        purchaseTier = 1;
      } else if (getCoin() >= 3) {
        spentCoin = 3;
        purchaseTier = 0;
      }
      setCoin(getCoin() - spentCoin);

      // 타워를 생성한다.
      inHand = DataManager.createRandomTowerByTier(0);
      AppManager.printInfoLog(inHand.getId() + "번 타워 구매함.");
      return true;
    }
  }

  public void deployTower(int x, int y) {
    // 1. 현재 터치한 좌표가 배치 가능한 구역인지 확인한다. (맵의 가운데)

    // 2. 현재 터치한 좌표의 자리가 비어있는지 검사한다. (칸 단위로)

    inHand.setX(x);
    inHand.setY(y);
    units.add(inHand);
    inHand = null;
  }

  /**
   * 배치모드 여부를 반환한다.
   * 
   * @return 현재 배치할 타워가 있으면 true, 없으면 false를 반환한다.
   */
  public boolean checkDeployMode() {
    return (inHand != null);
  }

  public int getWave() {
    return userWave;
  }

  public int getGold() {
    return userGold;
  }

  public int getCoin() {
    return userCoin;
  }

  public long getWorldTime() {
    return worldTime;
  }

  /**
   * 터치로 입력받은 게임 좌표를 통해서 유닛을 가져온다. 만약, 해당 좌표에 여러 유닛이 걸쳐져 있으면 게임 상에 늦게 추가된 순서로 우선순위가 있다.
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

  /**
   * 전체 유닛 목록에서 타워만 가져온다.
   * 
   * @return 타워가 0개 이상 들어있는 연결리스트를 반환한다.
   */
  public LinkedList<Tower> getTowers() {
    LinkedList<Tower> towers = new LinkedList<Tower>();
    for (Unit unit : units) {
      if (unit.getType() == Unit.TYPE_TOWER) {
        towers.add((Tower) unit);
      }
    }
    return towers;
  }

  /**
   * 전체 유닛 목록에서 동상만 가져온다.
   * 
   * @return 동상이 0개 이상 들어있는 연결리스트를 반환한다.
   */
  public LinkedList<Statue> getStatues() {
    LinkedList<Statue> statues = new LinkedList<Statue>();
    for (Unit unit : units) {
      if (unit.getType() == Unit.TYPE_STATUE) {
        statues.add((Statue) unit);
      }
    }
    return statues;
  }

  public Rect getTowersArea() {
    return towersArea;
  }

  /**
   * 그릴 때만 호출하시오.
   * 
   * @param ratio
   * @return
   */
  public Rect getTowersArea(float ratio) {
    return new Rect((int) (towersArea.left * ratio), (int) (towersArea.top * ratio),
        (int) (towersArea.right * ratio), (int) (towersArea.bottom * ratio));
  }

  public void setCoin(int coin) {
    userCoin = coin;
  }

  public boolean isWaveDone() {
    return deadMob >= MAX_MOB;
  }

  public void makeFace(int wave) {
    Options option = new Options();
    // option.inSampleSize = 16;
    String key = "mob" + wave;

    try {
      mImgMob = AppManager.readImageFile("img/mobs/" + key + ".png", option);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if ((mImgMob.getWidth() != 256) || (mImgMob.getHeight() != 64)) {
      mImgMob = Bitmap.createScaledBitmap(mImgMob, 256, 64, true);
    }

    AppManager.addBitmap(key, mImgMob);
  }

  /**
   * 지나간 웨이브의 몹은 지워버린다.
   * 
   * @param wave 웨이브 정수 값
   */
  public void destroyMob(int wave) {
    AppManager.getInstance().recycleBitmap("mob" + wave);
    for (int i = 0; i < units.size(); i++)
      if (units.get(i) instanceof Mob)
        GameState.getInstance().units.remove(i);

  }

  public Timer waveReady() {
    // 새로운 비트맵 추가
    userWave++;
    makeFace(userWave);

    curMob = 0;
    usedMob = 0;
    deadMob = 0;

    // 이전 웨이브의 몹은 폐기처분한다.
    destroyMob(userWave - 1);

    return TimeManager.registerCallbackTimer(1000, spawnMob, MAX_MOB);
  }
}