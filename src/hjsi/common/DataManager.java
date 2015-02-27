package hjsi.common;

import hjsi.game.GameState;
import hjsi.game.Tower;
import hjsi.game.Tower.Tier;
import hjsi.game.Unit;
import hjsi.game.Unit.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;

/**
 * SQLite 데이터베이스를 새로 만들거나 열고, 업데이트한다.
 */
public class DataManager extends SQLiteOpenHelper {
  /**
   * DB 도우미
   */
  private static DataManager openHelper = null;
  /**
   * DB 파일명
   */
  private static final String DB_FILE_NAME = "ElementTD.db";
  /**
   * 유저정보 테이블명
   */
  private static final String TABLE_USERDATA = "user_data";
  /**
   * 타워정보 테이블명
   */
  private static final String TABLE_TOWERINFO = "tower_info";
  /**
   * 몹정보 테이블명
   */
  private static final String TABLE_MOBINFO = "mob_info";


  /**
   * Create a helper object to create, open, and/or manage a database. This method always returns
   * very quickly. The database is not actually created or opened until one of
   * {@link #getWritableDatabase} or {@link #getReadableDatabase} is called.
   *
   * @param context to use to open or create the database
   * @param name of the database file, or null for an in-memory database
   * @param factory to use for creating cursor objects, or null for the default
   * @param version number of the database (starting at 1); if the database is older,
   *        {@link #onUpgrade} will be used to upgrade the database; if the database is newer,
   *        {@link #onDowngrade} will be used to downgrade the database
   */
  private DataManager(Context context, String name, CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  /**
   * Create a helper object to create, open, and/or manage a database. The database is not actually
   * created or opened until one of {@link #getWritableDatabase} or {@link #getReadableDatabase} is
   * called.
   *
   * <p>
   * Accepts input param: a concrete instance of {@link DatabaseErrorHandler} to be used to handle
   * corruption when sqlite reports database corruption.
   * </p>
   *
   * @param context to use to open or create the database
   * @param name of the database file, or null for an in-memory database
   * @param factory to use for creating cursor objects, or null for the default
   * @param version number of the database (starting at 1); if the database is older,
   *        {@link #onUpgrade} will be used to upgrade the database; if the database is newer,
   *        {@link #onDowngrade} will be used to downgrade the database
   * @param errorHandler the {@link DatabaseErrorHandler} to be used when sqlite reports database
   *        corruption, or null to use the default error handler.
   */
  private DataManager(Context context, String name, CursorFactory factory, int version,
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
    String sqlUserData = "CREATE TABLE " + TABLE_USERDATA + " (";
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
    db.insert(TABLE_USERDATA, null, values);
    values = null;

    /* 타워정보 테이블 생성 및 데이터 입력 */
    createInfoTable(db, TABLE_TOWERINFO);

    /* 몹정보 테이블 생성 및 데이터 입력 */
    createInfoTable(db, TABLE_MOBINFO);
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
  public static void createInfoTable(SQLiteDatabase db, String tableName) {
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

  public static void initializeDatabase(Context context, int version) {
    if (openHelper == null)
      openHelper = new DataManager(context, DB_FILE_NAME, null, version);
  }

  /**
   * 유저 데이터를 로드한다. 파일로 저장되어 있는 DB의 버전보다 새로 입력된 DB의 버전이 더 최신일 경우 DB도우미 클래스에서 onUpdate를 호출한다.
   * 
   * @param outState 로드한 데이터를 채울 GameState 클래스
   * @throws IOException
   */
  public static void loadUserData(GameState outState) throws IOException {
    // 최초 실행시 DB 생성도 같이 됨
    SQLiteDatabase db = openHelper.getReadableDatabase();

    Cursor cursor = db.rawQuery("select * from " + TABLE_USERDATA + " where id=1", null);
    cursor.moveToNext();
    int userWave = cursor.getInt(1);
    int userGold = cursor.getInt(2);
    int userCoin = cursor.getInt(3);
    String towers = cursor.getString(4);
    String recipes = cursor.getString(5);
    String upgrades = cursor.getString(6);
    AppManager.printDetailLog("유저정보 wave: " + userWave + ", gold: " + userGold + ", coin: "
        + userCoin + ", towers: " + towers);

    LinkedList<Tower> towerList = new LinkedList<Tower>();
    Tower tower = null;
    if (towers != null) {
      String[] token = towers.split(",");
      for (int i = 0; i < token.length; i = i + 3) {
        tower = createTower("id", token[i]);
        tower.setX(Integer.parseInt(token[i + 1]));
        tower.setY(Integer.parseInt(token[i + 2]));
        towerList.add(tower);
      }
    }

    outState.setUserData(userWave, userGold, userCoin, towerList);
    cursor.close();
    db.close();
  }

  /**
   * 게임의 정보를 저장한다.
   * 
   * @param gameState 게임 정보를 가지고 있는 GameState 객체
   */
  public static void saveUserData(GameState gameState) {
    SQLiteDatabase db = openHelper.getWritableDatabase();

    // 타워 목록을 가져와서 타워의 id와 x, y 좌표를 저장한다.
    String towers = new String();
    for (Unit tower : gameState.getUnits(Type.TOWER)) {
      towers += tower.getId() + "," + (int) tower.getX() + "," + (int) tower.getY() + ",";
    }

    ContentValues values = new ContentValues();
    // values.put("wave", gState.getWave());
    values.put("wave", 0); // 테스트를 위해서 웨이브는 저장 안함
    values.put("gold", gameState.getGold() + 100);
    values.put("coin", gameState.getCoin() + 3);
    values.put("towers", towers.toString());
    int affectedRows = db.update(TABLE_USERDATA, values, "id=?", new String[] {"1"});
    AppManager.printDetailLog("Table " + TABLE_USERDATA + " updated " + affectedRows + " row(s).");
    db.close();
  }

  public static void insertRecords(String tableName, ArrayList<ContentValues> values) {
    SQLiteDatabase db = openHelper.getWritableDatabase();
    for (ContentValues value : values) {
      long rowId = db.insert(tableName, null, value);
      AppManager.printInfoLog(rowId + "번째 레코드: " + value.toString() + " 삽입");
    }
    db.close();
  }

  /**
   * 주어진 검색 조건으로 데이터베이스에서 타워 정보를 구해 타워 객체를 생성한다. 검색된 타워 정보 레코드가 다수일 경우는 그 중 임의의 타워가 생성된다.
   * 
   * @param selection 검색할 파라미터로 "id" 혹은 "tier"가 가능하다.
   * @param arg 검색할 파라미터의 값
   * @return 조건에 해당하는 타워 객체
   * @throws IOException
   */
  public static Tower createTower(String selection, String arg) throws IOException {
    SQLiteDatabase db = openHelper.getReadableDatabase();
    Cursor cursor =
        db.query(TABLE_TOWERINFO, null, selection + "=?", new String[] {arg}, null, null, null);
    cursor.moveToPosition((int) (Math.random() * cursor.getCount()));

    int id = cursor.getInt(0);
    String name = cursor.getString(1);
    int tier = cursor.getInt(2);
    int dmg = cursor.getInt(3);
    int atkSpeed = cursor.getInt(4);
    int range = cursor.getInt(5);

    Bitmap face = AppManager.getBitmap(Type.TOWER.toString() + id);

    if (face == null) {
      Options opts = new Options();
      opts.inPreferredConfig = Config.RGB_565;
      face = AppManager.readUnitImageFile(Type.TOWER, id, opts);
      AppManager.addBitmap(Type.TOWER.toString() + id, face);
    }

    return new Tower(id, name, Tier.getTier(tier), dmg, atkSpeed, range, face);
  }
}
