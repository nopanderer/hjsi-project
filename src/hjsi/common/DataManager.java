package hjsi.common;

import hjsi.game.GameState;
import hjsi.game.Tower;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

  public DataManager() {}

  public static String getSqlCreateTable(String tableName, ArrayList<String> attributes) {
    String sql = "create table " + tableName + " (";

    for (String attribute : attributes) {
      sql += " " + attribute + ",";
    }
    if (sql.charAt(sql.length() - 1) == ',')
      sql = sql.substring(0, sql.length() - 1);
    sql += ");";

    return sql;
  }

  /**
   * 유저 데이터를 로드한다. 파일로 저장되어 있는 DB의 버전보다 새로 입력된 DB의 버전이 더 최신일 경우 DB도우미 클래스에서 onUpdate를
   * 호출한다.
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
      AppManager.printDetailLog("유저정보 wave: " + userWave + ", gold: " + userGold + ", coin: " + userCoin + ", towers: "
          + towers);

      // TODO 문자열 형태의 tower 목록을 파싱해서 타워 객체를 생성하고 링크드리스트<Tower> 형태로 만든다.

      GameState.getInstance().setUserData(userWave, userGold, userCoin, new LinkedList<Tower>());
    }
    cursor.close();
    db.close();
  }

  public static void save(GameState state) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();

    StringBuilder towers = new StringBuilder();
    for (Tower tower : state.getTowers()) {
      towers.append(tower.getId()).append(',');
    }

    ContentValues values = new ContentValues();
    values.put("wave", state.getWave());
    values.put("gold", state.getGold() + 100);
    values.put("coin", state.getCoin() + 1);
    values.put("towers", towers.toString());
    int affectedRows = db.update("user_data", values, "id=?", new String[] {"1"});
    AppManager.printDetailLog("db updated " + affectedRows + " row(s).");
    db.close();
  }
}
