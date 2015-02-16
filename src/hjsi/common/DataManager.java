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
   */
  public static void loadDatabase(Context context, int version) {
    databaseHelper = new AppDatabaseHelper(context, DataManager.DB_NAME, null, version);
    // 최초 실행시 DB 생성도 같이 됨
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    Cursor cursor = db.rawQuery("select * from " + TABLE_USERDATA + " where id=1", null);
    while (cursor.moveToNext()) {
      int userWave = cursor.getInt(1);
      int userGold = cursor.getInt(2);
      int userCoin = cursor.getInt(3);
      String towers = cursor.getString(4);
      String recipes = cursor.getString(5);
      String upgrades = cursor.getString(6);
      AppManager.printDetailLog("유저정보 wave: " + userWave + ", gold: " + userGold + ", coin: "
          + userCoin + ", towers: " + towers);

      // TODO 문자열 형태의 tower 목록을 파싱해서 타워 객체를 생성하고 링크드리스트<Tower> 형태로 만든다.

      GameState.getInstance().setUserData(userWave, userGold, userCoin, new LinkedList<Tower>());
    }
    cursor.close();
    db.close();
  }

  /**
   * 게임의 정보를 저장한다.
   * 
   * @param state
   */
  public static void save() {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();

    StringBuilder towers = new StringBuilder();
    for (Tower tower : GameState.getInstance().getTowers()) {
      towers.append(tower.getId()).append(',');
    }

    ContentValues values = new ContentValues();
    values.put("wave", GameState.getInstance().getWave());
    values.put("gold", GameState.getInstance().getGold() + 100);
    values.put("coin", GameState.getInstance().getCoin() + 1);
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

  public static Tower createTower(int towerId) {
    String[] columns = "id,name,tier,damage,attackspeed,range".split(",");

    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    Cursor cursor =
        db.query(TABLE_TOWERINFO, columns, "id=?", new String[] {String.valueOf(towerId)}, null,
            null, null);

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
}
