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
    try {

      String[] keywords = {"statue", "tower", "mob"};
      ArrayList<LinkedList<String>> linePerType =
          new ArrayList<LinkedList<String>>(keywords.length);

      // 텍스트 한 덩이를 \n으로 줄로 나눔
      String[] lines = AppManager.getInstance().readTextFile("db").split("\n");

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



      db.execSQL("create table " + TABLE_NAME + " (");
    } catch (IOException e) {
      e.printStackTrace();
    }

    AppManager.printDetailLog("새 DB가 생성되었습니다.");
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
