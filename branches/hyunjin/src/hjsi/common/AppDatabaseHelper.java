package hjsi.common;

import java.io.IOException;

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
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite .SQLiteDatabase)
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    AppManager.printSimpleLog();

    /* 유저정보 테이블 생성 */
    String sqlUserData = "CREATE TABLE " + DataManager.TABLE_USERDATA + " (";
    sqlUserData += "id INTEGER PRIMARY KEY NOT NULL, ";
    sqlUserData += "wave INTEGER NOT NULL, ";
    sqlUserData += "gold INTEGER NOT NULL, ";
    sqlUserData += "coin INTEGER NOT NULL, ";
    sqlUserData += "towers TEXT, ";
    sqlUserData += "recipes TEXT, ";
    sqlUserData += "upgrades TEXT);";
    db.execSQL(sqlUserData);
    AppManager.printInfoLog("query", sqlUserData);

    /* 유저정보 초기값 입력 (단 1개의 레코드) */
    ContentValues values = new ContentValues();
    values.put("id", 1);
    values.put("wave", 0);
    values.put("gold", 1000);
    values.put("coin", 3);
    db.insert(DataManager.TABLE_USERDATA, null, values);
    values = null;

    /* 타워정보 테이블 생성 및 데이터 입력 */
    createInfoTable(db, DataManager.TABLE_TOWERINFO);

    /* 몹정보 테이블 생성 및 데이터 입력 */
    createInfoTable(db, DataManager.TABLE_MOBINFO);
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

  /**
   * 파일로부터 데이터를 파싱하여 테이블을 생성하고 레코드를 입력한다.
   * 
   * @param tableName 데이터베이스에 입력할 데이터가 들어있는 테이블 이름
   */
  public void createInfoTable(SQLiteDatabase db, String tableName) {
    try {
      /*
       * 파일에서 모든 라인을 읽어온다.
       */
      String[] lines = AppManager.readTextFile(AppManager.getPathMap("db").get(tableName));
      for (int i = 0; i < lines.length; i++) {
        lines[i] = lines[i].trim();
      }
      /*
       * 테이블의 스키마를 분석한다. (속성명과 자료형을 분석함)
       */
      String[] attrNames = lines[0].split(",");
      String[] attrTypes = lines[1].split(",");
      int numberOfColumns = attrNames.length;
      if (numberOfColumns <= 0) {
        AppManager.printErrorLog(tableName + "의 형식이 올바르지 않습니다.");
      }
      /*
       * 1, 2 행의 열 갯수가 일치하는지 검사 (각각 속성 이름과 자료형)
       */
      if (attrTypes.length != numberOfColumns) {
        AppManager.printErrorLog(tableName + "의 자료형 갯수가 열 갯수(" + numberOfColumns + ")와 다릅니다.");
      }
      /*
       * 테이블 생성을 위한 sql statement를 조립한다.
       */
      String sql = "CREATE TABLE " + tableName + " (";
      for (int i = 0; i < numberOfColumns; i++) {
        // 속성명 + 자료형
        sql += attrNames[i] + " " + attrTypes[i];
        // 0번째 열은 무조건 <id>다!!!
        if (i == 0 && attrNames[0].equals("id"))
          sql += " PRIMARY KEY";
        // 모든 info table의 속성은 항상 NOT NULL이어야되지 않겠냐
        sql += " NOT NULL";
        // 마지막 속성을 제외한 속성들에만 쉼표를 붙인다.
        if (i < numberOfColumns - 1)
          sql += ", ";
      }
      // SQL statement 마무리
      sql += ");";
      // 조립한 sql statement 실행
      db.execSQL(sql);
      AppManager.printInfoLog("query", sql);

      /*
       * 실제 레코드 값이 들어있는 행들을 대상으로 수행한다. 각각의 열에 들어있는 값들을 각각의 짝이 맞는 속성에 mapping한다.
       */
      for (int i = 2; i < lines.length; i++) {
        String[] attrValues = lines[i].split(",");
        /*
         * 각 라인의 컬럼 수가 일치하는지 검사
         */
        if (attrValues.length != numberOfColumns) {
          AppManager.printErrorLog(tableName + "의 " + i + "행의 컬럼 수가 맞지 않습니다.");
        }
        /*
         * 한 개의 레코드에 대해서 각 속성(열)에 맞게 매핑해서 DB에 삽입한다.
         */
        ContentValues record = new ContentValues();
        for (int j = 0; j < numberOfColumns; j++) {
          /*
           * 각 열에 들어있는 값은 String(text) 형태이므로 자료형을 참조해서 각 속성에 맞게 자료형을 변환한다.
           */
          // 정수형
          if (attrTypes[j].equalsIgnoreCase("integer")) {
            record.put(attrNames[j], Integer.parseInt(attrValues[j]));
          }
          // 문자형
          else if (attrTypes[j].equalsIgnoreCase("text")) {
            record.put(attrNames[j], attrValues[j]);
          }
          // 이외의 경우는 예외적인 경우이므로 오류로 취급한다.
          else {
            AppManager.printErrorLog(i + " 행의 " + j + " 열의 데이터 형식이 올바르지 않습니다.");
          }
        }
        // 파싱한 레코드를 DB에 삽입한다.
        long rowId = db.insert(tableName, null, record);
        AppManager.printInfoLog(rowId + "번째 레코드(" + record.toString() + ")를 성공적으로 삽입함.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
