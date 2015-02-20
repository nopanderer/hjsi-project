package hjsi.common;

import hjsi.game.GameState;
import hjsi.game.Tower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

public class DataManager {
  /**
   * DB 도우미
   */
  private static AppDatabaseHelper databaseHelper;
  /**
   * DB 파일명
   */
  private static final String DB_NAME = "ElementTD.db";
  /**
   * 유저정보 테이블명
   */
  public static final String TABLE_USERDATA = "user_data";
  /**
   * 타워정보 테이블명
   */
  public static final String TABLE_TOWERINFO = "tower_info";
  /**
   * 몹정보 테이블명
   */
  public static final String TABLE_MOBINFO = "mob_info";

  public DataManager() {}

  /**
   * 유저 데이터를 로드한다. 파일로 저장되어 있는 DB의 버전보다 새로 입력된 DB의 버전이 더 최신일 경우 DB도우미 클래스에서 onUpdate를 호출한다.
   * 
   * @param gState 로드한 데이터를 채울 GameState 클래스
   */
  public static void loadDatabase(Context context, int version, GameState gState) {
    databaseHelper = new AppDatabaseHelper(context, DataManager.DB_NAME, null, version);
    // 최초 실행시 DB 생성도 같이 됨
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    Cursor cursor = db.rawQuery("select * from " + TABLE_USERDATA + " where id=1", null);
    cursor.moveToNext();
    int userWave = cursor.getInt(1);
    int userGold = cursor.getInt(2);
    int userCoin = cursor.getInt(3);
    String towers = cursor.getString(4);
    String recipes = cursor.getString(5);
    String upgrades = cursor.getString(6);
    AppManager.printDetailLog("유저정보 wave: " + userWave + ", gold: " + userGold + ", coin: "
        + userCoin + ", towers: " + towers);

    LinkedList<Tower> towerList = new LinkedList<Tower>();
    Tower tower = null;

    // 타워가 하나도 없는 경우 == 처음 실행시
    if (towers == null) {
      tower = createTower(Integer.toString(5));
      tower.setX(320);
      tower.setY(170);
      towerList.add(tower);

      // 타워가 저장되어 있는 경우 그대로 불러옴
    } else {
      String[] token = towers.split(",");
      for (int i = 0; i < token.length; i = i + 3) {
        tower = createTower(token[i]);
        tower.setX(Integer.parseInt(token[i + 1]));
        tower.setY(Integer.parseInt(token[i + 2]));
        towerList.add(tower);
      }
    }

    gState.setUserData(userWave, userGold, userCoin, towerList);
    cursor.close();
    db.close();
  }

  /**
   * 게임의 정보를 저장한다.
   * 
   * @param gState 게임 정보를 가지고 있는 GameState 객체
   */
  public static void save(GameState gState) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();

    // 타워 목록을 가져와서 타워의 id와 x, y 좌표를 저장한다.
    StringBuilder towers = new StringBuilder();
    for (Tower tower : gState.getTowers()) {
      towers.append(tower.getId()).append(',').append((int) tower.getX()).append(',')
          .append((int) tower.getY()).append(',');
    }

    ContentValues values = new ContentValues();
    // values.put("wave", gState.getWave());
    values.put("wave", 0); // 테스트를 위해서 웨이브는 저장 안함
    values.put("gold", gState.getGold() + 100);
    values.put("coin", gState.getCoin() + 3);
    values.put("towers", towers.toString());
    int affectedRows = db.update("user_data", values, "id=?", new String[] {"1"});
    AppManager.printDetailLog("db updated " + affectedRows + " row(s).");
    db.close();
  }

  public static void insertRecords(String tableName, ArrayList<ContentValues> values) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    for (ContentValues value : values) {
      long rowId = db.insert(tableName, null, value);
      AppManager.printInfoLog(rowId + "번째 레코드: " + value.toString() + " 삽입");
    }
    db.close();
  }

  public static Tower createTower(String towerId) {
    String[] columns = "id,name,tier,damage,attackspeed,range".split(",");

    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    Cursor cursor =
        db.query(TABLE_TOWERINFO, columns, "id=?", new String[] {towerId}, null, null, null);

    cursor.moveToNext();
    int id = cursor.getInt(0);
    String name = cursor.getString(1);
    int tier = cursor.getInt(2);
    int dmg = cursor.getInt(3);
    int atkSpeed = cursor.getInt(4);
    int range = cursor.getInt(5);

    String fileName = "tower" + id;
    Bitmap face = AppManager.getBitmap(fileName);
    if (face == null) {
      try {
        face = AppManager.readImageFile("img/towers/" + fileName + ".png", null);
        AppManager.addBitmap(fileName, face);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new Tower(id, name, tier, dmg, atkSpeed, range, face);
  }

  /**
   * 특정 등급의 타워들 중에 아무 타워를 랜덤으로 생성한다.
   * 
   * @param tierArg 랜덤으로 생성할 타워의 등급
   * @return 지정한 등급의 임의의 타워 객체 혹은 null
   */
  public static Tower createRandomTowerByTier(int tierArg) {
    String[] columns = "id,name,tier,damage,attackspeed,range".split(",");

    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    Cursor cursor =
        db.query(TABLE_TOWERINFO, columns, "tier=?", new String[] {String.valueOf(tierArg)}, null,
            null, null);
    // 지정된 티어의 전체 타워 범위 내에서 랜덤 넘버가 타워의 인덱스
    cursor.moveToPosition((int) (Math.random() * cursor.getCount()));

    int id = cursor.getInt(0);
    String name = cursor.getString(1);
    int tier = cursor.getInt(2);
    int dmg = cursor.getInt(3);
    int atkSpeed = cursor.getInt(4);
    int range = cursor.getInt(5);

    String fileName = "tower" + id;
    Bitmap face = AppManager.getBitmap(fileName);
    if (face == null) {
      try {
        face = AppManager.readImageFile("img/towers/" + fileName + ".png", null);
        AppManager.addBitmap(fileName, face);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new Tower(id, name, tier, dmg, atkSpeed, range, face);
  }
}
