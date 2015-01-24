package hjsi.common;

import hjsi.activity.Base;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

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
   * assets 폴더 내의 경로 및 확장자를 포함한 파일 목록을 값으로, 이름 부분을 키로 저장한다. <br>
   * 예) <var>["background"] = img/common/background.png</var> <br>
   * InputSteam is = getAssets().open(filePath.get("background")); 같은 형식으로 사용한다.
   */
  private HashMap<String, String> filePath;
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
   * 현재 함수명 리턴(1.4 이후에서만 사용가능)
   *
   * @author 2007.11.27(김준호)
   * @since 1.4
   */
  public static String getMethodName() {
    StackTraceElement[] elements = null;

    try {
      throw new Exception("getMethodName");
    } catch (Exception e) {
      elements = e.getStackTrace();
    }

    String methodName = ((StackTraceElement) elements[1]).getMethodName();
    return methodName;
  }

  /**
   * 간단하게 클래스 이름을 tag로 하고 메소드 이름을 메시지로 하는 로그캣 로그를 출력한다. (메소드가 제대로 호출되는지 판단하려고)
   */
  public static void printSimpleLog() {
    printDetailLog(null);
  }

  /**
   * 간단한 로그에 자세한 메시지를 덧붙여서 출력한다. tag는 클래스 이름, 출력 메시지는 호출한 메소드명과 덧붙인 메시지.
   * 
   * @param detailMsg 덧붙여서 출력할 String
   */
  public static void printDetailLog(String detailMsg) {
    StackTraceElement[] elements = null;

    try {
      throw new Exception("getMethodName");
    } catch (Exception e) {
      elements = e.getStackTrace();
    }

    int depth; // printSimpleLog()를 거칠 경우 호출스택이 더 깊어진다.

    if (detailMsg != null) {
      detailMsg = " " + detailMsg;
      depth = 1;
    } else {
      detailMsg = "";
      depth = 2;
    }

    String methodName = elements[depth].getMethodName();
    String className = elements[depth].getClassName();
    className = className.substring(className.lastIndexOf(".") + 1);

    Log.d(className, methodName + detailMsg);
  }

  public void addActivity(Base act) {
    if (act != null) {
      if (runningActivities.contains(act) == false) {
        runningActivities.addFirst(act);
      }
    }

    printDetailLog(act.toString());
  }

  public void removeActivity(Base act) {
    if (act != null) {
      runningActivities.remove(act);
    }

    printDetailLog(act.toString());
  }

  /**
   * 현재 실행 중(arAct에 들어있음)인 Activity들을 종료시킴.
   */
  public void quitApp() {
    for (Activity act : runningActivities) {
      act.finish();
      Log.d(AppManager.tag, act.toString() + ".finish();");
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
      // assets 전체에 대한 파일 경로 목록을 만든다
      filePath = getFilesRecursively(null);
    }
  }

  /**
   * 주어진 경로 아래의 모든 파일을 HashMap에 넣어서 반환한다.
   * 
   * @param findPath 특정 경로 혹은 assets의 전체 목록을 구하기 위해서 null
   * @param assetManager assets 폴더에 접근하기 위한 <strong>AssetManager</strong> 객체
   * 
   * @return 경로 및 확장자를 제외한 파일 이름을 키로 하고, 전체 경로를 값으로 갖는 <strong>HashMap</strong> 객체, 아무 파일도 없다면
   *         <strong>null</strong>
   */
  public HashMap<String, String> getFilesRecursively(String findPath) {
    if (assetManager == null) {
      printDetailLog("먼저 AssetManager를 세팅하시오.");
    }

    // assets 폴더를 재귀적으로 탐색하기 위한 stack
    Stack<String> retrieveStack = new Stack<String>();
    // findPath에서 찾은 하위 파일 목록을 key(파일 이름)와 value(전체 경로) 형태로 구성한 HashMap
    HashMap<String, String> retValue = new HashMap<String, String>();
    // 찾은 파일의 수
    int countOfFiles = 0;

    try {
      if (findPath == null) {
        findPath = "";

        // 마지막에 붙은 / 문자는 제거해준다. (밑에서 붙이기도 함)
      } else if (findPath.charAt(findPath.length() - 1) == '/') {
        findPath = findPath.substring(0, findPath.length());
      }
      /*
       * findPath 내부를 탐색하기 위한 폴더 목록을 확인한다.
       */
      for (String fileOrDir : assetManager.list(findPath)) {
        retrieveStack.push(fileOrDir);
      }
      /*
       * AssetManager.list("");를 통해서 내용을 확인해보면 기본적으로 images, sounds, webkit 폴더가 들어있음. 각 폴더 안에는 시스템에서
       * 사용하는 것 같은 파일들이 있어서 해당 폴더는 탐색하지 않도록 제외함.
       */
      if (findPath.equals("")) {
        retrieveStack.remove("images");
        retrieveStack.remove("sounds");
        retrieveStack.remove("webkit");
      }

      String fullPath;
      String[] subList;
      String parentPath = "";

      while (!retrieveStack.isEmpty()) {
        fullPath = retrieveStack.pop();
        /*
         * fullPath가 가지고 있는 하위 디렉토리나 파일의 목록을 구한다. 그 갯수가 0이면 지금의 fullPath는 빈 폴더이거나 파일이다.
         */
        subList = assetManager.list(fullPath);
        if (subList.length == 0) {
          // 부모 디렉토리 경로->(img/common/) background (.png)<-확장자
          String fileName = fullPath.substring(parentPath.length(), fullPath.indexOf('.'));

          // 이미 fileName에 해당하는 개체가 들어가 있는 경우에 대한 처리
          String old = retValue.put(fileName, fullPath);
          if (old == null) {
            // 기존 파일이 존재하지 않으므로 정상적으로 파일 갯수 카운트
            countOfFiles++;
          } else {
            // 기존 파일이 존재하므로 기존 파일을 새로운 파일이 대체함. 그래서 카운트는 하지 않음.
            Log.e("Asset List", "Key 중복 발견: " + old + " --대체 됨--> " + fullPath);
          }

          Log.i("Asset List", "[" + countOfFiles + "] " + fullPath);

        } else {
          /*
           * fullPath가 내부에 폴더나 파일이 하나라도 있으면 현재까지 들어온 경로를 스택에 추가
           */
          parentPath = fullPath + "/";
          for (String subName : subList) {
            retrieveStack.push(parentPath + subName);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      Log.e(tag, getMethodName() + " 입출력 예외가 발생함");
    }

    // 찾은 파일이 하나도 없으므로 null을 반환 값으로 지정
    if (countOfFiles == 0)
      retValue = null;

    return retValue;
  }

  /**
   * key값만으로 assets에 존재하는 파일을 로드함
   * 
   * @param key
   */
  public void loadBitmapAsset(String key) {
    addBitmap(key, getBitmap(key));
  }

  /**
   * 리소스 관리 대상으로 비트맵을 추가함
   *
   * @param key
   * @param bitmap
   */
  public void addBitmap(String key, Bitmap bitmap) {
    String msg = "(\"" + key + "\", " + bitmap.toString();

    Bitmap old = loadedBitmap.put(key, bitmap);

    msg += ") was added";
    if (old != null) {
      msg += " instead of " + old.toString();
      old.recycle();
    }

    printDetailLog(msg);
  }

  /**
   * 로드된 비트맵 혹은 전체 assets 파일 목록에서 key에 해당하는 비트맵을 구한다.
   * 
   * @param key 구하려는 비트맵의 키 문자열
   * @return null or Bitmap
   */
  public Bitmap getBitmap(String key) {
    return getBitmap(filePath, key, null);
  }

  /**
   * 이미 로드된 비트맵이 있더라도 주어진 Options 객체가 적용된 새로운 비트맵을 생성해서 반환한다.
   * 
   * @param key 구하려는 비트맵의 키 문자열
   * @param opts 적용하려는 옵션을 입력한다. null을 입력할 경우는 <var>AppManager.getBitmap(String key)</var>와 동일한 작업을
   *        수행한다.
   * @return null or Bitmap
   */
  public Bitmap getBitmap(String key, Options opts) {
    return getBitmap(filePath, key, opts);
  }

  /**
   * 로드된 비트맵 혹은 전체 assets 파일 목록에서 key에 해당하는 비트맵을 구한다. <br>
   * Options 객체가 주어지면 이미 로드된 비트맵이 있더라도 주어진 Options 객체가 적용된 새로운 비트맵을 생성해서 반환한다.
   * 
   * @param filePathMap assets의 특정 폴더 내의 파일들로 제한할 경우, 해당 경로 목록의 맵
   * @param key 가져올 이미지의 확장자를 제외한 파일 이름
   * @param opts Options 객체 혹은 null 입력 가능
   * @return null or Bitmap
   */
  public Bitmap getBitmap(HashMap<String, String> filePathMap, String key, Options opts) {
    Bitmap retValue = loadedBitmap.get(key);

    // 로드된 비트맵 목록에 key가 없거나 Options 개체가 들어온 경우, 새로운 Bitmap을 생성한다.
    if (retValue == null || opts != null) {
      try {
        String path = filePathMap.get(key);
        // key에 해당하는 파일이 없을 경우는 어디선가 에러가 날 것이다.
        if (path == null) {
          printDetailLog("\"" + key + "\"에 해당하는 파일을 찾을 수 없음.");
        } else {
          InputStream is = assetManager.open(path);
          retValue = BitmapFactory.decodeStream(is, null, opts);
        }
      } catch (IOException e) {
        e.printStackTrace();
        printDetailLog("IOException 발생");
      }
    }

    return retValue;
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
