package hjsi.common;

import java.util.ArrayList;

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
    ArrayList<String> attributes = new ArrayList<String>();
    attributes.add("id integer primary key" + notNull);
    attributes.add("wave integer" + notNull);
    attributes.add("gold integer" + notNull);
    attributes.add("coin integer" + notNull);
    attributes.add("towers text");
    attributes.add("recipes text");
    attributes.add("upgrades text");
    sql = DataManager.getSqlCreateTable(DataManager.TABLE_USERDATA, attributes);
    attributes.clear();
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
    attributes.add("id integer primary key" + notNull);
    attributes.add("name text" + notNull);
    attributes.add("tier integer" + notNull);
    attributes.add("damage integer" + notNull);
    attributes.add("attackspeed integer" + notNull);
    attributes.add("range integer" + notNull);
    sql = DataManager.getSqlCreateTable(DataManager.TABLE_TOWERINFO, attributes);
    attributes.clear();
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
