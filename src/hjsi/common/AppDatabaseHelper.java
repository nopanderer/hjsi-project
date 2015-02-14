/**
 *
 */
package hjsi.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 데이터베이스를 생성하거나 불러온다.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {
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

    for (String tableName : TABLE_LIST) {
      try {
        sql = AppManager.getInstance().readTextFile("db/create_table_" + tableName + ".sql");
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (sql != null) {
        execSQL(db, sql);
      }
    }

    // user data 초기화
    execSQL(db, "insert into USER_DATA (_id, wave, gold, coin) values (1, 0, 1000, 3);");
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

  private void parseUnitTable() {
    try {
      String[] keywords = {"statue", "tower", "mob"};
      ArrayList<LinkedList<String>> linePerType = new ArrayList<LinkedList<String>>(keywords.length);

      // 텍스트 한 덩이를 \n으로 줄로 나눔
      String[] lines;
      lines = AppManager.getInstance().readTextFile("db").split("\n");

      for (String line : lines) {
        line = line.trim();

        // 주석이나 빈 줄은 통과
        if (line.startsWith("#") || line.length() <= 0) {
          continue;
        }

        // 쉼표로 나눔
        String[] tokens = line.split(",", 2);

        int index = Integer.parseInt(tokens[0]);

        // 이미지를 찾을 수 있는 형태를 만듦. ex) "statue" + "1"
        String headString = keywords[index] + Integer.parseInt(tokens[1]);

        String tailString = line.substring(line.indexOf(',') + 1);
        tailString = tailString.substring(tailString.indexOf(','));


        linePerType.get(index).add(headString + tailString);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected static void execSQL(SQLiteDatabase db, String sql) {
    if (db != null) {
      AppManager.printDetailLog(sql + " ---> query executed.");
      db.execSQL(sql);
    }
  }
}
