package hjsi.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppDatabase {
  private static AppDatabase uniqueInstance;
  /**
   * DB 도우미
   */
  private AppDatabaseHelper dbHelper;
  /**
   * DB 파일명
   */
  private static final String DB_NAME = "ElementTD.db";

  private AppDatabase() {}

  public static AppDatabase getInstance() {
    if (uniqueInstance == null) {
      synchronized (AppDatabase.class) {
        if (uniqueInstance == null)
          uniqueInstance = new AppDatabase();
      }
    }
    return uniqueInstance;
  }

  /**
   * 어플리케이션에서 사용할 유저 데이터, 타워 및 몹의 정보 등을 로드한다. 파일로 저장되어 있는 DB의 버전보다 새로 입력된 DB의
   * 버전이 더 최신일 경우 DB도우미 클래스에서 onUpdate를 호출한다.
   * 
   * @param context getApplicationContext()
   * @param version DB의 버전
   */
  public static void loadDatabase(Context context, int version) {
    AppDatabase appdb = getInstance();

    appdb.dbHelper = new AppDatabaseHelper(context, DB_NAME, null, version);
    // 최초 실행시 DB 생성도 같이 됨
    SQLiteDatabase db = appdb.dbHelper.getReadableDatabase();

    Cursor cursor = db.rawQuery("select * from USER_DATA where _id = 1", null);
    while (cursor.moveToNext()) {
      int wave = cursor.getInt(1);
      int gold = cursor.getInt(2);
      int coin = cursor.getInt(3);
      String towers = cursor.getString(4);
      String recipes = cursor.getString(5);
      String upgrades = cursor.getString(6);
      AppManager.printDetailLog("wave: " + wave + ", gold: " + gold + ", coin: " + coin + ", towers: " + towers);
    }
    cursor.close();
    db.close();
  }

  public static void execSQL(SQLiteDatabase db, String sql) {
    if (db != null) {
      AppManager.printDetailLog(sql + " ---> query executed.");
      db.execSQL(sql);
    }
  }
}
