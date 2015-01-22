/**
 *
 */
package hjsi.common;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 데이터베이스를 생성하거나 불러온다. 2015.01.20 현재 시점에서는 당장 필요한 클래스는 아니다. 일단 책보고 만들어뒀음. 자동 생성된 상태에서 바꾼건 거의
 * 없다.
 *
 * @author SANGIN
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {
  private final static String TABLE_NAME = "";

  /**
   * @param context
   * @param name
   * @param factory
   * @param version
   */
  public AppDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  /**
   * @param context
   * @param name
   * @param factory
   * @param version
   * @param errorHandler
   */
  public AppDatabaseHelper(Context context, String name, CursorFactory factory, int version,
      DatabaseErrorHandler errorHandler) {
    super(context, name, factory, version, errorHandler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + AppDatabaseHelper.TABLE_NAME + " (");
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
   * int, int)
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO Auto-generated method stub
  }
}
