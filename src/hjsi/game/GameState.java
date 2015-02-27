package hjsi.game;

import hjsi.common.AppManager;
import hjsi.common.DataManager;
import hjsi.common.Timer;
import hjsi.game.Unit.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 게임에 필요한 정보를 저장한다.
 */
public class GameState {
  public static final float WORLD_WIDTH = 3840f;
  public static final float WORLD_HEIGHT = 2160f;

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
  private static RectF towersArea = new RectF(AREA_LEFT, AREA_TOP, AREA_RIGHT, AREA_BOTTOM);
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
   * 게임 시스템에서 사용하는 타이머들
   */
  private LinkedList<Timer> timers;
  /**
   * 게임 시간을 재는 타이머
   */
  private Timer worldTimer;
  /**
   * 몹 생성 시간을 재는 타이머
   */
  private Timer spawnTimer;
  /**
   * 현재 단계
   */
  public int wave = 1;
  /**
   * 유닛 통합 연결 리스트
   */
  LinkedList<Unit> units = new LinkedList<Unit>();

  private Bitmap mImgMob; // 몹 비트맵
  public static int deadMob = 0; // 죽은 몹
  public static int curMob = 0; // 현재 몹

  private static final int MAX_MOB = 10;

  ArrayList<Station> stations = new ArrayList<Station>();

  public GameState() {
    AppManager.printSimpleLog();

    /*
     * 동상 추가
     */
    synchronized (units) {
      Options opts = new Options();
      opts.inPreferredConfig = Config.RGB_565;
      try {
        Bitmap face = AppManager.readUnitImageFile(Type.STATUE, 1, opts);
        AppManager.addBitmap(Type.STATUE.toString() + 1, face);
        units.add(new Statue(5, 5, face));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /* 정류장 삽입 */
    stations.add(new Station(400, WORLD_HEIGHT - 280));
    stations.add(new Station(WORLD_WIDTH - 400, WORLD_HEIGHT - 280));
    stations.add(new Station(WORLD_WIDTH - 400, 280));
    stations.add(new Station(400, 280));

    timers = new LinkedList<Timer>();
    worldTimer = Timer.create("월드시계", 1000);
    timers.add(worldTimer);
    spawnTimer = Timer.create("몹 생성 시계", 4000, MAX_MOB);
    spawnTimer.setEnable(false);
    timers.add(spawnTimer);
  }

  public void setUserData(int wave, int gold, int coin, LinkedList<Tower> towers) {
    userWave = wave;
    userGold = gold;
    userCoin = coin;
    synchronized (units) {
      units.addAll(towers);
    }
  }

  /**
   * 1초 증가
   */
  public void ticktock() {
    if (worldTimer.isUsable()) {
      worldTimer.consumeTimer();
      worldTime++;
      AppManager.printInfoLog("딸깍...");
    }
  }

  /**
   * 시스템과 유닛들의 타이머를 재개한다.
   */
  public void resumeTimers() {
    Timer.timestamp();

    synchronized (timers) {
      for (Timer timer : timers) {
        timer.resume();
      }
    }
    synchronized (units) {
      for (Unit unit : units) {
        unit.unfreeze();
      }
    }
  }

  /**
   * 시스템과 유닛들의 타이머를 정지한다.
   */
  public void pauseTimers() {
    synchronized (timers) {
      for (Timer timer : timers)
        timer.pause();
    }
    synchronized (units) {
      for (Unit unit : units)
        unit.freeze();
    }
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

    // TODO inHand 변수에 타워가 들어있으면 Game activity에 표시해줘야한다.
  }

  public void deployTower(float x, float y) {
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

  public Tower getInHandTower() {
    return inHand;
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
  public Unit getUnit(float x, float y) {
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
    float cellsCenterX = TOWERS_WIDTH / 2f;
    float cellsCenterY = TOWERS_HEIGHT / 2f;

    float x = towersArea.left + (float) (TOWERS_WIDTH * column) + cellsCenterX;
    float y = towersArea.top + (float) (TOWERS_HEIGHT * row) + cellsCenterY;

    Unit unit = getUnit(x, y);
    if (unit == null) {
      return null;
    } else if (unit instanceof Tower == false) {
      AppManager.printErrorLog("왜 타워가 아닌게 여기 있냐??");
      return null;
    } else
      return (Tower) unit;
  }

  public void addUnit(Unit newbie) {
    synchronized (units) {
      units.add(newbie);
    }
  }

  public void removeUnit(Unit trash) {
    synchronized (units) {
      units.remove(trash);
    }
  }

  /**
   * 유닛 통합 연결 리스트 반환
   * 
   * @return units
   */
  public LinkedList<Unit> getUnits() {
    return units;
  }

  public LinkedList<Unit> getUnitsClone() {
    return new LinkedList<Unit>(units);
  }

  /**
   * 매개변수로 지정한 종류의 유닛 목록을 반환한다.
   * 
   * @param type 유닛 종류 열거형
   * @return 해당 타입이 0개 이상 들어있는 연결리스트를 반환한다.
   */
  public LinkedList<Unit> getUnits(Type type) {
    LinkedList<Unit> clone = new LinkedList<Unit>();
    synchronized (units) {
      for (Unit unit : units) {
        if (unit.getType() == type)
          clone.add(unit);
      }
    }
    return clone;
  }

  public RectF getTowersArea() {
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

  public static int getTowersWidth(float ratio) {
    return (int) (TOWERS_WIDTH * ratio + 0.5);
  }

  public static int getTowersHeight(float ratio) {
    return (int) (TOWERS_HEIGHT * ratio + 0.5);
  }

  /**
   * 해당 좌표가 배치 영역 내부인지 아닌지를 검사한다.
   * 
   * @param x 좌표 형태 값
   * @param y 좌표 형태 값
   * @return x, y 좌표가 배치 영역 내부에 있다면 true, 벗어난다면 false를 반환한다.
   */
  public boolean inArea(float x, float y) {
    if (x >= towersArea.left && x <= towersArea.right && y >= towersArea.top
        && y <= towersArea.bottom) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 타워를 설치할 수 있는 격자 칸마다 타워가 있는지, 없는지 갱신한다. 배치 격자를 표시하기 전에만 호출해주면 된다.
   */
  public void refreshArea() {
    // 배열 초기화
    for (int row = 0; row < TOWERS_ROWS; row++) {
      for (int col = 0; col < TOWERS_COLUMNS; col++) {
        areaUsedCells[row][col] = false;
      }
    }
    // 타워가 있는 칸만 true
    for (Unit tower : getUnits(Type.TOWER)) {
      int row = getRow((int) tower.getY());
      int col = getColumn((int) tower.getX());
      areaUsedCells[row][col] = true;
    }
  }

  /**
   * 지정된 row, column 위치를 타워가 사용 중인지 반환한다. 사용 전에는 refreshArea() 메소드를 호출해주는 것이 좋다.
   * 
   * @param row 0번 인덱스부터 시작하는 행 번호
   * @param column 0번 인덱스부터 시작하는 열 번호
   * @return 해당 배치 격자 칸에 타워가 존재하면 true, 빈 공간이면 false를 반환한다.
   */
  public boolean isUsedCell(int row, int column) {
    return areaUsedCells[row][column];
  }

  /**
   * 입력한 좌표 형태 y 값을 배치영역의 행 번호로 변환한다.
   * 
   * @param y 좌표 형태의 세로 위치 값
   * @return y 좌표 값이 배치 영역 내에 속한다면 그에 알맞는 행 번호를 반환하고, 범위를 벗어난다면 -1을 반환한다.
   */
  public static int getRow(float y) {
    int retValue = -1;
    if (y >= towersArea.top && y <= towersArea.bottom) {
      y = y - towersArea.top;
      retValue = (int) (y / TOWERS_HEIGHT);
    }
    return retValue;
  }

  /**
   * 입력한 좌표 형태 x 값을 배치영역의 열 번호로 변환한다.
   * 
   * @param x 좌표 형태의 가로 위치 값
   * @return x 좌표 값이 배치 영역 내에 속한다면 그에 알맞는 열 번호를 반환하고, 범위를 벗어난다면 -1을 반환한다.
   */
  public static int getColumn(float x) {
    int retValue = -1;
    if (x >= towersArea.left && x <= towersArea.right) {
      x = x - towersArea.left;
      retValue = (int) (x / TOWERS_WIDTH);
    }
    return retValue;
  }

  public void setCoin(int coin) {
    userCoin = coin;
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

    if ((mImgMob.getWidth() != 512) || (mImgMob.getHeight() != 128)) {
      mImgMob = Bitmap.createScaledBitmap(mImgMob, 1024, 256, true);
    }

    AppManager.addBitmap(key, mImgMob);
  }

  /**
   * 지나간 웨이브의 몹은 지워버린다.
   * 
   * @param wave 웨이브 정수 값
   */
  public void destroyMob(int wave) {
    AppManager.recycleBitmap("mob" + wave);
    synchronized (units) {
      for (Unit mob : getUnits(Type.MOB))
        units.remove(mob);
    }
  }

  /**
   * @return deadMob == 0 && curMob == 0
   */
  public boolean isWaveStop() {
    return deadMob >= MAX_MOB; // deadMob == 0 && curMob == 0;
  }

  public void finishWave() {
    curMob = 0;
    deadMob = 0;
    spawnTimer.setEnable(false);
  }

  public void waveReady() {
    // 새로운 비트맵 추가
    userWave++;
    makeFace(userWave);

    // 이전 웨이브의 몹은 폐기처분한다.
    destroyMob(userWave - 1);

    // 몹 리스폰 타이머를 리필함.
    spawnTimer.setEnable(true);
    spawnTimer.startDelayed(2000);
  }

  public void spawnMob() {
    if (spawnTimer.isUsable()) {
      spawnTimer.consumeTimer();
      addUnit(new Mob(80, 80, mImgMob, userWave, stations.get(0)));
      curMob++;
    }
  }
}
