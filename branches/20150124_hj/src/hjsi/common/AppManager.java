package hjsi.common;

import hjsi.activity.Base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
  private static String tag; // 로그 출력용 클래스 이름을 갖고 있음
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

  private volatile int logicFps;

  private AppManager() {
    AppManager.tag = getClass().getSimpleName();

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
   * @return class.method 형태의 문자열
   */
  private static String getClassMethodName() {
    StackTraceElement[] elements = null;

    try {
      throw new Exception("getMethodName");
    } catch (Exception e) {
      elements = e.getStackTrace();
    }
    String methodName = elements[2].getMethodName();
    String className = elements[2].getClassName();
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
    Log.d(getTagPrefix("메소드 호출"), getClassMethodName() + " 메소드 호출 됨");
  }

  /**
   * 호출한 클래스의 이름과 메소드 이름을 TAG로 하는 로그메시지를 출력한다.
   * 
   * @param message 로그로 출력할 메시지
   */
  public static void printDetailLog(String message) {
    Log.d(getTagPrefix(getClassMethodName()), message);
  }

  public static void printDetailLog(String customTag, String message) {
    Log.d(getTagPrefix(customTag), message);
  }

  public static void printInfoLog(String message) {
    Log.i(getTagPrefix(getClassMethodName()), message);
  }

  public static void printInfoLog(String customTag, String message) {
    Log.i(getTagPrefix(customTag), message);
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
   * 주어진 경로 아래의 모든 파일의 전체 경로 목록을 구한다. 특정 경로를 지칭하는 것을 권장한다. 루트에서부터 검색하기 위해 "/"를 입력하면 안드로이드 장비 내의 기본적인
   * 파일들도 검색 대상에 포함돼서 제대로 된 파일을 얻을 수 없다. 다만, 그 결과는 이 메소드에서 마지막 "/"를 제거하여 ""로 만들기 때문이다. 정말 "/"로부터
   * 검색한다면 assets 폴더보다 더 상위에서 시작하는데, 권한이 없는 것인지 아무런 결과도 얻을 수 없다.
   * 
   * @param path 검색할 경로
   * @return 파일 경로를 포함하는 ArrayList 객체
   */
  public ArrayList<String> getFilesList(String path) {
    /*
     * 가장 끝에 있는 '/' 글자를 제거한다. lastIndexOf('/')는 전체의 마지막이 '/'가 아니고, 중간에 '/'가 있어도 그 글자가 마지막 '/'이므로 쓰지
     * 않는다.
     */
    int lastIndex = path.length();
    if (lastIndex-- > 0) {
      if (path.charAt(lastIndex) == '/') {
        path = path.substring(0, lastIndex);
      }
    }

    return filesList(path);
  }

  /**
   * 특정 경로 아래에 있는 모든 파일들의 전체 경로를 구한다.
   * 
   * @param workingDir 현재 검색 중인 경로
   * @return 파일 경로를 포함하는 ArrayList
   */
  private ArrayList<String> filesList(String workingDir) {
    ArrayList<String> retValue = new ArrayList<String>();

    try {
      String[] files = assetManager.list(workingDir);

      if (files.length == 0) {
        retValue.add(workingDir);
        printInfoLog("파일: " + workingDir);
      } else {
        if (workingDir.length() > 0)
          workingDir += "/";

        for (String file : files) {
          retValue.addAll(filesList(workingDir + file));
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      printDetailLog("입출력 예외가 발생함");
    }

    return retValue;
  }

  /**
   * 입력한 키에 해당하는 파일의 전체 경로를 반환한다.
   * 
   * @param key 전체 경로를 구할 파일의 확장자를 제외한 이름
   * @param path 입력한 경로 하위에 속한 파일로 제한
   * @return 키에 해당하는 파일의 전체 경로, 그런 파일이 없으면 null
   */
  private String getPathOf(String key, String path) {
    String pathOfKey = null;

    for (String file : getFilesList(path)) {
      int lastIndexOfSlash = file.lastIndexOf('/');
      int lastIndexOfExt = file.lastIndexOf('.');
      String fileNameNoExt = file.substring(lastIndexOfSlash + 1, lastIndexOfExt);

      if (key.equalsIgnoreCase(fileNameNoExt)) {
        pathOfKey = new String(file);
        break;
      }
    }

    return pathOfKey;
  }

  /**
   * readTextFile(key, "")를 호출하는 wrapping 메소드. 자세한 내용은 readTextFile(String key, String path)의 내용을
   * 참조.
   * 
   * @param key 내용을 읽어오려는 파일의 이름 (경로 및 확장자는 제외한다)
   * @return key에 해당하는 파일이 있으면 해당 파일 내용, 없으면 null
   * @throws IOException
   */
  public String readTextFile(String key) throws IOException {
    return readTextFile(key, "");
  }

  /**
   * 입력한 경로 아래에 속하는 모든 경로에서 파일을 찾아서 내용을 읽어온다.
   * 
   * @param key 내용을 읽어오려는 파일의 이름 (경로 및 확장자는 제외한다)
   * @param path 주어진 경로 아래에서만 대상 파일을 찾는다
   * @return key에 해당하는 파일이 있으면 해당 파일 내용, 없으면 null
   * @throws IOException
   */
  public String readTextFile(String key, String path) throws IOException {
    String retValue = null;
    String pathOfKey = getPathOf(key, path);

    if (pathOfKey == null) {
      throw new IOException("Not found " + key + ".");
    } else {
      InputStream is = assetManager.open(pathOfKey);
      byte[] buffer = new byte[is.available()];
      is.read(buffer);
      is.close();
      retValue = new String(buffer);

      /* 읽어온 파일의 로그 출력 */
      printInfoLog(pathOfKey, "\"" + key + "\", " + convertByteUnit(buffer.length) + " 읽기 성공");
      printInfoLog(pathOfKey, retValue);
    }

    return retValue;
  }

  /**
   * 입력한 경로 아래에 속하는 모든 경로에서 파일을 찾아서 비트맵 객체를 생성한다.
   * 
   * @param key 읽어올 이미지 파일의 이름 (경로 및 확장자는 제외한다)
   * @param path 입력된 경로 아래에서만 대상 파일을 찾는다
   * @param opts 비트맵 생성시 적용할 옵션 객체. 옵션을 적용하지 않을 경우는 null
   * @return 비트맵 객체 혹은 null
   * @throws IOException
   */
  public Bitmap readImageFile(String key, String path, Options opts) throws IOException {
    Bitmap bm = null;
    String pathOfKey = getPathOf(key, path);

    if (pathOfKey == null) {
      throw new IOException("Not found " + key + ".");
    } else {
      InputStream is = assetManager.open(pathOfKey);
      printInfoLog(pathOfKey, "\"" + key + "\", 원본 용량:" + convertByteUnit(is.available()));
      bm = BitmapFactory.decodeStream(is, null, opts);
      is.close();
      printInfoLog(pathOfKey, "\"" + key + "\", " + bitmapToString(bm) + " 읽기 성공");
    }

    return bm;
  }

  /**
   * 모든 리소스를 반환 (지금은 비트맵만)
   */
  public void allRecycle() {
    String msg = new String();

    Set<String> arKey = loadedBitmap.keySet();
    synchronized (loadedBitmap) {
      for (String key : arKey) {
        msg = key + ",";
        loadedBitmap.get(key).recycle();
      }
      loadedBitmap.clear();

      if (msg.length() > 0) {
        msg = msg.substring(0, msg.length() - 1);
        msg += " cleared";
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

    if (loadedBitmap.containsKey(key)) {
      loadedBitmap.get(key).recycle();
      msg += key + " recycled";
    } else {
      msg = key + "를 찾을 수 없음.";
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
