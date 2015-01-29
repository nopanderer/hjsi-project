package hjsi.game;

import hjsi.common.AppManager;
import hjsi.timer.TimeManager;
import hjsi.timer.TimerRunnable;

import java.io.IOException;
import java.util.LinkedList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

/**
 * 게임에 필요한 정보를 저장한다.
 */
public class GameState {
  private static final String tag = GameState.class.getSimpleName();
  private static GameState uniqueInstance;

  /**
   * 현재 게임이 진행된 시간을 나타낸다.
   */
  private volatile long worldTime = 0L;

  /**
   * 현재 단계
   */
  public int wave = 1;

  /**
   * 테스트용 유닛 리스트
   */
  LinkedList<Unit> arTestUnits = new LinkedList<Unit>();

  LinkedList<Mob> Mobs = new LinkedList<Mob>();

  public Resources res;
  public Bitmap mImgMob; // 몹 비트맵
  public long beforeRegen = System.currentTimeMillis(); // 리젠하기 전 시간
  public long regen = 1000; // create mob per 1 sec
  public int usedMob = 0; // 몹이 실제로 생성된(내부적 카운터 위해)
  public int deadMob = 0; // 죽은 몹
  public int curMob = 0; // 현재 몹

  public final int MAX_MOP = 10;

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
     * 불러온 유저 데이터를 토대로 동상을 생성한다. (유저 데이터의 남아있는 동상의 갯수, 체력, 업그레이드 등을 참조) 생성한 동상은 유닛 목록에 추가한다.
     */
    arTestUnits.add(new Statue(180, 120, AppManager.getInstance().getBitmap("statue1")));
  }

  public void initState(Resources res) {
    this.res = res;
    makeFace();
    createMobs();
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

  public long getWorldTime() {
    return worldTime;
  }

  public LinkedList<Unit> getUnits() {
    return arTestUnits;
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

  public void createMobs() {
    for (int i = 0; i < MAX_MOP; i++)
      // 여기서는 10마리까지지만 실제로는 파일입력을 통해서
      Mobs.add(new Mob(90, 90, mImgMob, wave));
  }

  public void addMob() {
    if (System.currentTimeMillis() - beforeRegen > regen)
      beforeRegen = System.currentTimeMillis();
    else
      return;

    Mobs.get(usedMob).created = true;
    usedMob++;
    curMob++;
  }

  public void destroyMob() {
    AppManager.getInstance().recycleBitmap("mob" + wave);
    Mobs.clear();
  }

  public LinkedList<Mob> getMobs() {
    return Mobs;
  }
}
