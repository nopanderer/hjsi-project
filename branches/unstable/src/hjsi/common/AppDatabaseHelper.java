package hjsi.common;

import java.io.IOException;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 데이터베이스를 새로 만들거나 열고, 업데이트한다.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {
  /**
   * 테이블 이름
   */
  private final static String[] TABLE_LIST = {"userdata", "tower"};

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
   * @see
   * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
   * .SQLiteDatabase)
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    AppManager.printSimpleLog();
    String sql = null;

    /*
     * 각 테이블 스키마 생성
     */
    for (String tableName : TABLE_LIST) {
      try {
        sql = AppManager.getInstance().readTextFile("db/create_table_" + tableName + ".sql");
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (sql != null) {
        db.execSQL(sql);
      }
    }

    // user data 초기값 세팅
    db.execSQL("insert into USER_DATA (_id, wave, gold, coin) values (1, 0, 1000, 3);");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
   * .SQLiteDatabase, int, int)
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    AppManager.printSimpleLog();
  }
}
