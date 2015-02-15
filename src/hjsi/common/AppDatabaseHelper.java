package hjsi.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 데이터베이스를 새로 만들거나 열고, 업데이트한다.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {
  public AppDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  public AppDatabaseHelper(Context context, String name, CursorFactory factory, int version,
      DatabaseErrorHandler errorHandler) {
    super(context, name, factory, version, errorHandler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
   * .SQLiteDatabase)
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    AppManager.printSimpleLog();
    final String notNull = " not null";
    String sql = null;

    /* 유저정보 테이블 생성 */
    ContentValues columns = new ContentValues();
    columns.put("id", "integer primary key" + notNull);
    columns.put("wave", "integer" + notNull);
    columns.put("gold", "integer" + notNull);
    columns.put("coin", "integer" + notNull);
    columns.put("towers", "text");
    columns.put("recipes", "text");
    columns.put("upgrades", "text");
    sql = DataManager.getSqlCreateTable(DataManager.TABLE_USERDATA, columns);
    columns.clear();
    db.execSQL(sql);
    AppManager.printInfoLog("query", sql);

    /* 유저정보 초기값 입력 */
    ContentValues values = new ContentValues();
    values.put("id", 1);
    values.put("wave", 0);
    values.put("gold", 1000);
    values.put("coin", 3);
    db.insert(DataManager.TABLE_USERDATA, null, values);
    values = null;

    /* 타워정보 테이블 생성 */
    columns.put("id", "integer primary key" + notNull);
    columns.put("name", "text" + notNull);
    columns.put("tier", "integer" + notNull);
    columns.put("damage", "integer" + notNull);
    columns.put("attackspeed", "integer" + notNull);
    columns.put("range", "integer" + notNull);
    sql = DataManager.getSqlCreateTable(DataManager.TABLE_TOWERINFO, columns);
    columns.clear();
    db.execSQL(sql);
    AppManager.printInfoLog("query", sql);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
   * .SQLiteDatabase, int, int)
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    AppManager.printSimpleLog();
  }
}
