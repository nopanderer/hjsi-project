package hjsi.common;

import hjsi.activity.Base;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

/**
 * 애플리케이션의 시스템적인 부분을 총괄하는 싱글턴 클래스
 */
public class AppManager {
  /*
   * Fileds 시작
   */
  private static AppManager uniqueInstance; // 자신의 유일한 인스턴스를 가지고 있는다.
  /**
   * 실행 중인 액티비티 목록
   */
  private LinkedList<Base> runningActivities;
  private AssetManager assetManager;
  /**
   * 메모리에 로드된 비트맵 목록
   */
  private HashMap<String, Bitmap> loadedBitmap;
  /**
   * 기기의 해상도와 비교할 기준 해상도(가로)
   */
  private static final float standardWidth = 1920f;
  /**
   * 기기의 해상도와 비교할 기준 해상도(세로)
   */
  private static final float standardHeight = 1080f;
  /**
   * 해상도 조정을 위한 비율 변수
   */
  private float displayRatioFactor;
  /**
   * TODO 제거 대상
   */
  private volatile int logicFps;

  /*
   * 
   * Methods 시작
   */

  private AppManager() {
    runningActivities = new LinkedList<Base>();
    loadedBitmap = new HashMap<String, Bitmap>();
  }

  /**
   * @return 유일한 인스턴스를 반환함
   */
  public static AppManager getInstance() {
    if (AppManager.uniqueInstance == null) {
      synchronized (AppManager.class) {
        if (AppManager.uniqueInstance == null) {
          AppManager.uniqueInstance = new AppManager();
        }
      }
    }

    return AppManager.uniqueInstance;
  }

  /**
   * 로그를 출력하려는 개체의 클래스, 메소드 이름을 구한다.
   * 
   * @param deepMore TODO
   * 
   * @return class.method 형태의 문자열
   */
  private static String getClassMethodName(boolean deepMore) {
    StackTraceElement[] elements = null;

    try {
      throw new Exception("getMethodName");
    } catch (Exception e) {
      elements = e.getStackTrace();
    }

    int depth = (deepMore == true ? 3 : 2);
    String methodName = elements[depth].getMethodName();
    String className = elements[depth].getClassName();
    className = className.substring(className.lastIndexOf(".") + 1);

    return className + "." + methodName;
  }

  /**
   * LogCat에서 로그를 필터링하기 위해서 특정 문자열을 tag의 앞에 붙여준다. 일관성을 위해서 메소드로 제작.
   */
  private static String getTagPrefix(String tagBody) {
    return "[ETD] " + tagBody;
  }

  /**
   * 메소드의 호출 여부를 확인하기 위해 사용한다.
   */
  public static void printSimpleLog() {
    Log.v(getTagPrefix("메소드 호출"), getClassMethodName(true) + " --------> "
        + getClassMethodName(false) + " 호출되었음");
  }

  /**
   * 호출한 클래스의 이름과 메소드 이름을 TAG로 하는 로그메시지를 출력한다.
   * 
   * @param message 로그로 출력할 메시지
   */
  public static void printDetailLog(String message) {
    Log.d(getTagPrefix(getClassMethodName(false)), message);
  }

  public static void printDetailLog(String customTag, String message) {
    Log.d(getTagPrefix(customTag), message);
  }

  public static void printInfoLog(String message) {
    Log.i(getTagPrefix(getClassMethodName(false)), message);
  }

  public static void printInfoLog(String customTag, String message) {
    Log.i(getTagPrefix(customTag), message);
  }

  public static void printErrorLog(String message) {
    Log.e(getTagPrefix(getClassMethodName(false)), message);
  }

  public static void printErrorLog(String customTag, String message) {
    Log.e(getTagPrefix(customTag), message);
  }

  /**
   * byte 단위의 숫자를 적절한 단위로 환산한다.
   * 
   * @param byteCount 단위를 환산할 바이트 수
   * @return "#.### Bytes" 형식의 문자열
   */
  private String convertByteUnit(float byteCount) {
    String suffix[] = {"", "K", "M"};
    int unit = 0;
    while (byteCount > 1024f && unit < suffix.length) {
      byteCount /= 1024f;
      unit++;
    }
    byteCount = (long) (byteCount * 1000f + 0.5f) / 1000f;

    return byteCount + " " + suffix[unit] + "Bytes";
  }

  /**
   * 로그 출력용으로 만듦. 비트맵 객체를 구별하고 용량을 알아보기 위함.
   * 
   * @param bm 비트맵 객체
   * @return 비트맵 정보
   */
  private String bitmapToString(Bitmap bm) {
    String bitmapId = bm.toString().substring(bm.toString().lastIndexOf('@') + 1);
    return bitmapId + ", " + convertByteUnit(bm.getByteCount());
  }

  public void addActivity(Base act) {
    if (act != null) {
      if (runningActivities.contains(act) == false) {
        runningActivities.addFirst(act);
      }
    }

    printDetailLog(act.toString() + " 액티비티가 추가됨");
  }

  public void removeActivity(Base act) {
    if (act != null) {
      runningActivities.remove(act);
    }

    printDetailLog(act.toString() + " 액티비티가 제거됨");
  }

  /**
   * 현재 실행 중(arAct에 들어있음)인 Activity들을 종료시킴.
   */
  public void quitApp() {
    for (Activity act : runningActivities) {
      act.finish();
      printDetailLog(act.toString() + " finish() 요청함.");
    }
  }

  /**
   * AppManager가 assets 폴더에 접근하기 위한 AssetManager를 설정한다.
   * 
   * @param assetManager
   */
  public void setAssetManager(AssetManager assetManager) {
    if (assetManager == null) {
      printDetailLog("AssetManager 객체가 null이 들어옴.");
      throw new NullPointerException();
    } else {
      this.assetManager = assetManager;
    }
  }

  public void setDisplayFactor(int deviceWidth, int deviceHeight) {
    float horizontalRatio = deviceWidth / standardWidth;
    float verticalRatio = deviceHeight / standardHeight;
    displayRatioFactor = Math.max(horizontalRatio, verticalRatio);
    printDetailLog("비율 변수: " + displayRatioFactor);
  }

  public float getDisplayFactor() {
    return displayRatioFactor;
  }

  /**
   * 주어진 경로 아래의 모든 파일을 HashMap에 넣어서 반환한다.
   * 
   * @param findPath 의도한 결과를 얻기 위해서는 반드시 한 단계의 경로 정도는 입력해야 한다.
   * @param assetManager assets 폴더에 접근하기 위한 <strong>AssetManager</strong> 객체
   * 
   * @return 경로 및 확장자를 제외한 파일 이름을 키로 하고, 전체 경로를 값으로 갖는 <strong>HashMap</strong> 객체, 아무 파일도 없다면
   *         <strong>null</strong>
   */
  public HashMap<String, String> getPathMap(String findPath) {
    if (assetManager == null) {
      printErrorLog("먼저 AssetManager를 세팅하시오.");
      return null;
    } else if (findPath == null) {
      printErrorLog("findPath를 입력하시오.");
      return null;
    } else if (findPath.equalsIgnoreCase("/")) {
      printErrorLog("findPath의 값으로 \"/\"을 사용하지마시오.");
      return null;
    } else if (findPath.equalsIgnoreCase("")) {
      printErrorLog("최소한 한 단계의 경로는 지정하시오.");
      return null;
    }

    // 파일을 찾으면서 들어간 경로를 스택에 넣는다. 해당 경로를 빠져나오면 스택에서도 없어지는 꼴.
    LinkedList<String> enteredDir = new LinkedList<String>();
    // findPath에서 찾은 하위 파일 목록을 key(파일 이름)와 value(전체 경로) 형태로 구성한 HashMap
    HashMap<String, String> pathMap = new HashMap<String, String>();

    try {
      // 마지막 글자가 '/'라면 차후 탐색 과정에서 //처럼 연속으로 붙을 가능성이 있으므로 제거함.
      if (findPath.charAt(findPath.length() - 1) == '/') {
        findPath = findPath.substring(0, findPath.length());
      }
      enteredDir.add(findPath);

      while (!enteredDir.isEmpty()) {
        String workingPath = enteredDir.poll();

        // 현재 디렉터리가 가지고 있는 하위 디렉터리나 파일의 목록을 구한다.
        String[] subList = assetManager.list(workingPath);

        if (subList.length == 0) {
          // 하위 목록 수가 0이면 현재 작업경로는 파일인 경우에 해당한다. (빈 폴더는 아예 list() 메소드에서 반환되지 않는 듯)
          // 부모 디렉토리 경로->(img/common/) background (.png)<-확장자
          String fileName =
              workingPath.substring(workingPath.lastIndexOf('/') + 1, workingPath.indexOf('.'));

          // 이미 fileName(동일한 key)에 해당하는 개체가 들어가 있는 경우에 대한 처리
          String old = pathMap.put(fileName, workingPath);
          printDetailLog("[\"" + fileName + "\"] = \"" + workingPath + "\"");

          if (old != null) {
            printErrorLog("중복 파일 발견!", "기존 파일: " + old + ", 발견 파일: " + workingPath);
          }

        } else {
          // 현재 작업경로가 내부에 폴더나 파일을 하나라도 가지고 있는 경우에 해당한다.
          for (String subPath : subList)
            enteredDir.add(workingPath + "/" + subPath);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return pathMap;
  }

  /**
   * 리소스 관리 대상으로 비트맵을 추가함
   *
   * @param key
   * @param bitmap
   */
  public void addBitmap(String key, Bitmap bitmap) {
    String msg = "\"" + key + "\", " + bitmapToString(bitmap) + " 추가됨";
    Bitmap old = loadedBitmap.put(key, bitmap);
    // 동일한 key의 객체가 이미 있었던 경우 구 객체의 할당을 해제한다.
    if (old != null) {
      msg += " (제거됨: " + bitmapToString(old) + ")";
      old.recycle();
    }

    printDetailLog(msg);
  }

  /**
   * 로드된 비트맵을 가져온다.
   * 
   * @param key 구하려는 비트맵의 이름
   * @return 비트맵 객체 혹은 null
   */
  public Bitmap getBitmap(String key) {
    return loadedBitmap.get(key);
  }

  /**
   * 입력한 경로 아래에 속하는 모든 경로에서 파일을 찾아서 내용을 읽어온다.
   * 
   * @param path 주어진 경로 아래에서만 대상 파일을 찾는다
   * 
   * @return key에 해당하는 파일이 있으면 해당 파일 내용, 없으면 null
   * @throws IOException
   */
  public String readTextFile(String path) throws IOException {
    String textData = null;

    if (path == null) {
      throw new IOException("Not found \"" + path + "\".");
    } else {
      InputStream is = assetManager.open(path);
      byte[] buffer = new byte[is.available()];
      is.read(buffer);
      is.close();
      textData = new String(buffer);

      /* 읽어온 파일의 로그 출력 */
      printInfoLog("\"" + path + "\", " + convertByteUnit(buffer.length) + " 읽기 성공");
      printInfoLog(path, textData);
    }

    return textData;
  }

  /**
   * 입력한 경로 아래에 속하는 모든 경로에서 파일을 찾아서 비트맵 객체를 생성한다.
   * 
   * @param path 입력된 경로 아래에서만 대상 파일을 찾는다
   * @param opts 비트맵 생성시 적용할 옵션 객체. 옵션을 적용하지 않을 경우는 null
   * @return 비트맵 객체 혹은 null
   * @throws IOException
   */
  public Bitmap readImageFile(String path, Options opts) throws IOException {
    Bitmap bm = null;

    if (path == null) {
      throw new IOException("Not found \"" + path + "\".");
    } else {
      InputStream is = assetManager.open(path);
      printInfoLog("\"" + path + "\"", "원본 용량: " + convertByteUnit(is.available()));
      bm = BitmapFactory.decodeStream(is, null, opts);
      bm =
          Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * displayRatioFactor + 0.5f),
              (int) (bm.getHeight() * displayRatioFactor + 0.5f), false);
      is.close();
      printInfoLog("\"" + path + "\"", bitmapToString(bm) + " 읽기 성공");
    }

    return bm;
  }

  /**
   * 모든 리소스를 반환 (지금은 비트맵만)
   */
  public void allRecycle() {
    String msg = new String();

    Set<String> keySet = loadedBitmap.keySet();
    synchronized (loadedBitmap) {
      for (String key : keySet) {
        msg += key + ", ";
        loadedBitmap.get(key).recycle();
      }
      loadedBitmap.clear();

      if (msg.length() > 0) {
        msg = "\"" + msg.substring(0, msg.length() - 1);
        msg += "\" recycled";
      }
    }

    printDetailLog(msg);
  }


  /**
   * 주어진 키가 가리키는 비트맵을 메모리에서 해제한다.
   * 
   * @param key 해제하려는 비트맵의 이름
   */
  public void recycleBitmap(String key) {
    String msg = new String();

    synchronized (loadedBitmap) {
      if (loadedBitmap.containsKey(key)) {
        loadedBitmap.get(key).recycle();
        msg += "\"" + key + "\" recycled";
      } else {
        msg = "\"" + key + "\"를 찾을 수 없음.";
      }
    }

    printDetailLog(msg);
  }


  public int getLogicFps() {
    return logicFps;
  }

  public void setLogicFps(int fps) {
    logicFps = fps;
  }
}
