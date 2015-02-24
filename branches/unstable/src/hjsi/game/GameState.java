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
  private static final int TOWERS_COLUMNS = 10;
  private static final int TOWERS_ROWS = 8;
  public static final int TOWERS_WIDTH = (int) ((AREA_RIGHT - AREA_LEFT) / TOWERS_COLUMNS + 0.5);
  public static final int TOWERS_HEIGHT = (int) ((AREA_BOTTOM - AREA_TOP) / TOWERS_ROWS + 0.5);
  private boolean[][] areaUsedCells = new boolean[TOWERS_ROWS][TOWERS_COLUMNS];
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
      synchronized (units) {
        units.add(new Mob(80, 80, mImgMob, userWave, stations.get(0)));
      }
      usedMob++;
      curMob++;
    }
  };

  public GameState() {
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
    stations.add(new Station(200, WORLD_HEIGHT - 140));
    stations.add(new Station(WORLD_WIDTH - 200, WORLD_HEIGHT - 140));
    stations.add(new Station(WORLD_WIDTH - 200, 140));
    stations.add(new Station(200, 140));
  }

  /**
   * 현재의 게임 정보를 가지고 있는 GameState의 유일한 객체를 없애서 정보를 초기화한다.
   */
  public void purgeGameState() {
    synchronized (GameState.class) {
      DataManager.save(this);
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
   */
  public void onDeployMode() {
    // 키핑해둔 타워가 있으면 다시 배치모드로 꺼낸다.
    if (inHand == null && keepingTower != null) {
      inHand = keepingTower;
      keepingTower = null;

      // 배치해야되는 타워가 있는대 배치버튼을 눌렀으면 타워를 없애지 않고 키핑한다.
    } else if (inHand != null && keepingTower == null) {
      keepingTower = inHand;
      inHand = null;

      // 유저 정보에서 가지고 있는 토큰 수를 확인한다.
    } else if (getCoin() < 3) {
      // TODO 토큰이 모자랄 때를 표현한다

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
    }
  }

  public void deployTower(int x, int y) {
    // 1. 현재 터치한 좌표가 배치 가능한 구역인지 확인한다. (맵의 가운데)
    if (inArea(x, y)) {
      /*
       * 날좌표를 행렬 인덱스로 변환한다.
       */
      int row = getRow(y);
      int col = getColumn(x);
      if (row == -1 || col == -1) {
        AppManager.printErrorLog("배치격자 안을 클릭했을 텐데 행렬 인덱스에 에러가 있다니...");
      }
      AppManager.printInfoLog((row + 1) + "행 " + (col + 1) + "열을 클릭했다.");

      // 2. 현재 터치한 좌표의 자리가 비어있는지 검사한다. (칸 단위임)
      if (areaUsedCells[row][col] == false) {
        /*
         * 행렬 인덱스를 배치격자 칸의 가운데 값으로 일종의 정규화한다.
         */
        x = towersArea.left + (TOWERS_WIDTH * col) + (int) (TOWERS_WIDTH / 2.0 + 0.5);
        y = towersArea.top + (TOWERS_HEIGHT * row) + (int) (TOWERS_HEIGHT / 2.0 + 0.5);

        // 3. 정규화한 좌표에 타워를 설치한다.
        inHand.setX(x);
        inHand.setY(y);
        synchronized (units) {
          units.add(inHand);
        }
        inHand = null;
      } else {
        AppManager.printDetailLog("이미 타워가 배치되어 있다.");
      }
    }
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
    synchronized (units) {
      for (Unit unit : units) {
        if ((unit.x <= x && x <= unit.x + unit.width) && (unit.y <= y && y <= unit.y + unit.height))
          return unit;
      }
    }
    return null;
  }

  /**
   * 배치모드에서 표시되는 격자 칸의 첨자(2차원 배열처럼 생각하면 됨)를 주면 해당 칸에 있는 타워를 반환한다. 당장은 쓸 일이 없을 것 같다.
   * 
   * @param row
   * @param column
   * @return
   */
  public Tower getTower(int row, int column) {
    int cellsCenterX = (int) (TOWERS_WIDTH / 2.0 + 0.5);
    int cellsCenterY = (int) (TOWERS_HEIGHT / 2.0 + 0.5);

    int x = towersArea.left + (TOWERS_WIDTH * column) + cellsCenterX;
    int y = towersArea.top + (TOWERS_HEIGHT * row) + cellsCenterY;

    Unit unit = getUnit(x, y);
    if (unit == null) {
      return null;
    } else if (unit instanceof Tower == false) {
      AppManager.printErrorLog("왜 타워가 아닌게 여기 있냐");
      return null;
    } else
      return (Tower) unit;
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
   * 전체 유닛 목록에서 몹만 가져온다.
   * 
   * @return 몹이 0개 이상 들어있는 연결리스트를 반환한다.
   */
  public LinkedList<Mob> getMobs() {
    LinkedList<Mob> mobs = new LinkedList<Mob>();
    synchronized (units) {
      for (Unit unit : units) {
        if (unit.getType() == Unit.TYPE_MOB) {
          mobs.add((Mob) unit);
        }
      }
    }
    return mobs;
  }

  /**
   * 전체 유닛 목록에서 타워만 가져온다.
   * 
   * @return 타워가 0개 이상 들어있는 연결리스트를 반환한다.
   */
  public LinkedList<Tower> getTowers() {
    LinkedList<Tower> towers = new LinkedList<Tower>();
    synchronized (units) {
      for (Unit unit : units) {
        if (unit.getType() == Unit.TYPE_TOWER) {
          towers.add((Tower) unit);
        }
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
    synchronized (units) {
      for (Unit unit : units) {
        if (unit.getType() == Unit.TYPE_STATUE) {
          statues.add((Statue) unit);
        }
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

  public int getTowersWidth(float ratio) {
    return (int) (TOWERS_WIDTH * ratio + 0.5);
  }

  public int getTowersHeight(float ratio) {
    return (int) (TOWERS_HEIGHT * ratio + 0.5);
  }

  public boolean inArea(int x, int y) {
    if (x >= towersArea.left && x <= towersArea.right && y >= towersArea.top
        && y <= towersArea.bottom) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 타워를 설치할 수 있는 격자에 타워가 있는지, 없는지 갱신한다. 배치 격자를 표시하기 전에만 호출해주면 된다.
   */
  public void refreshArea() {
    for (int row = 0; row < TOWERS_ROWS; row++) {
      for (int col = 0; col < TOWERS_COLUMNS; col++) {
        Tower tower = getTower(row, col);

        if (tower != null)
          areaUsedCells[row][col] = true;
        else
          areaUsedCells[row][col] = false;
      }
    }
  }

  public boolean isUsedCell(int row, int column) {
    return areaUsedCells[row][column];
  }

  public int getRow(int y) {
    int retValue = -1;
    if (y >= towersArea.top && y <= towersArea.bottom) {
      y = y - towersArea.top;
      retValue = y / TOWERS_HEIGHT;
    }
    return retValue;
  }

  public int getColumn(int x) {
    int retValue = -1;
    if (x >= towersArea.left && x <= towersArea.right) {
      x = x - towersArea.left;
      retValue = x / TOWERS_WIDTH;
    }
    return retValue;
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
    synchronized (units) {
      for (Mob mob : getMobs())
        units.remove(mob);
    }
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

    return TimeManager.registerCallbackTimer(1000 / GameMaster.ff, spawnMob, MAX_MOB);
  }
}
