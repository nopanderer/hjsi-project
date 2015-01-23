package hjsi.common;

import hjsi.game.GameState;
import hjsi.game.Mob;
import hjsi.game.Unit;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 게임 내용(맵, 타워, 투사체 등)을 그려줄 서피스뷰 클래스이다. 쓰레드 사용해서 canvas에 그림을 그릴 수 있는 유일한 방법이다. 때문에 게임에선 거의 서피스뷰를
 * 사용한다고 한다. 내부적으로 더블버퍼링을 사용한다. 시스템 UI는 Game 액티비티에서 처리하고(Button 등), 게임 자체를 위한 UI(타워 선택, 카메라 이동 등)
 * 이벤트는 이 클래스에서 처리한다.
 */
public class GameSurface extends SurfaceView implements SurfaceHolder.Callback, Runnable {
  private final String tag = getClass().getSimpleName();

  /* 서피스뷰 그리기에 필요한 객체 및 변수 */
  private Thread mThreadPainter; // 그리기 스레드
  private boolean mIsRunning; // 그리기 스레드 동작 상태
  /**
   * 메인스레드가 아닌 따로 생성한 스레드에서 그리는 걸 메인 스레드의 캔버스와 연결하는 역할을 한다. 서피스뷰는 액티비티에 컨텐트뷰로 설정되고, getHolder()를 사용해
   * 그에 대한 홀더를 얻을 수 있다. 홀더는 더블버퍼링 기법으로 사용하는 (아마 메모리상의) 캔버스를 반환한다. (lockCanvas()를 통해서) 스레드를 이용하므로
   * 캔버스에 락을 거는 것 같다. 홀더에도 synchronized를 써서 사용한다. 각 스레드가 언제 홀더에 접근하는지는 모르겠다. 홀더에서 구한 캔버스에 그림을 그리는 건
   * 아무 스레드나 해도 상관없다. (편의상 서피스뷰에 러너블 인터페이스 구현) 스레드에서 홀더로부터 캔버스를 받아서 그림을 그리고, 캔버스를 반환하면 메인 스레드는 실제로 그
   * 그림을 출력하는 메커니즘인것 같다.
   */
  private SurfaceHolder mHolder;

  /*
   * 테스트 값 출력용
   */
  private Paint mPaintInfo; // 텍스트 출력용 페인트 객체
  private int mFps; // 그리기 fps

  /** 기본적인 생성자 */
  public GameSurface(Context context) {
    super(context);

    init();
  }

  /** 생성자 호출시 공통 부문 초기화 */
  private void init() {
    /*
     * 홀더를 가져와서 Callback 인터페이스를 등록한다. 구현한 각 콜백은 surface의 변화가 있을 때마다 호출된다. 서피스뷰를 가진 액티비티가 화면에 보일 때
     * created(), changed() 호출 화면에서 보이지 않을 때 destroyed() 호출 가로, 세로 전환될 때도 changed() 호출 될거고...
     */
    mHolder = super.getHolder(); // 인게임 스레드에서 필요할 수도 있어서 똑같은 이름의 함수를 구현했기 때문에 super를 사용하게 됐음
    mHolder.addCallback(this); // SurfaceHolder.Callback 구현한 메소드를 등록하는 것
  }

  /* SurfaceHolder.Callback 구현 */
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    AppManager.printSimpleLog();
    /*
     * 표면이 생성될 때 그리기 스레드를 시작한다. 표면은 아마 화면상에 실제로 보이는 그림을 말하는 것 같다. lockCanvas() 할 때 뱉어내는 캔버스가 더블버퍼링을
     * 위한 메모리 상의 캔버스인 것 같고
     */
    mThreadPainter = new Thread(this);
    mIsRunning = true;
    mThreadPainter.start();
  }

  /*
   * ☆★☆★☆★중요☆★☆★☆★☆★ 게임 화면을 띄운채로 핸드폰 잠궜다가 켜면, 게임 화면도 세로로 돌아가는 일이 생긴다. 그 때 에러남 나중에 액티비티 생명주기에 따른
   * 처리(전화왔을 때 내려가는 경우 등등) 화면이 180도 돌아가거나 하는 경우도 처리해줘야겠다.
   */
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.i(tag, AppManager.getMethodName() + "() (width: " + width + ", height: " + height + ")");
    // 표면의 크기가 바뀔 때 그 크기를 기록한다.
    Camera.getInstance().setCamSize(width, height);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    AppManager.printSimpleLog();
    /*
     * 표면이 파괴되기 직전에 그리기를 중지한다. 이 콜백이 끝나면 완전히 파괴된다. 파괴된 후에도 스레드가 죽지않으면 canvas에 그리기를 시도할 경우 에러가 난다.
     * 조건문 false를 한다고 스레드가 바로 멈추는 건 아님 그래서 join을 통해 그리기 스레드가 끝날 때까지 표면 파괴를 늦춘다.
     */
    mIsRunning = false;
    try {
      mThreadPainter.join();
    } catch (Exception e) {
    }
  }

  /**
   * 지금은 터치이벤트를 카메라에 짬때린다.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean isCameraEvent = Camera.getInstance().touchHandler(event);

    if (event.getAction() == MotionEvent.ACTION_UP) {
      /*
       * 터치 이벤트 처리에서 ACTION_UP의 경우에 무조건 performClick을 호출해줘야한다고 함. 안 하면 경고 뜸. performClick() 자체도
       * 오버라이드 해줘야함
       */
      performClick();
    }

    if (isCameraEvent == false) {
      return super.onTouchEvent(event); // 내가 사용할 이벤트와 전혀 상관없으면 슈퍼클래스에서 처리
    } else {
      return true;
    }
  }

  /* 필요하다고 해서 했음 */
  @Override
  public boolean performClick() {
    AppManager.printSimpleLog();
    return super.performClick();
  }

  /* 실제로 그리기를 처리할 부분이다 */
  @Override
  public void run() {
    Canvas canvas; // lockCanvas()로 얻어온 캔버스를 가리킬 변수
    Bitmap face; // 게임 개체들의 이미지를 가리킬 변수

    /* fps 계산을 위한 변수 */
    long fpsStartTime;
    long fpsElapsedTime = 0L;
    int fps = 0;

    while (mIsRunning) {
      // 프레임 시작 시간을 구한다.
      fpsStartTime = System.currentTimeMillis();

      // 전체 그리기 수행
      synchronized (mHolder) {
        // 캔버스를 잠그는 듯
        canvas = mHolder.lockCanvas();
        if (canvas == null) {
          break;
        }

        Camera.getInstance().autoScroll();

        canvas.drawColor(Color.DKGRAY); // 게임 배경 바깥 범위를 회색으로 채운다.

        /* 캔버스를 이동, 확대/축소하기 전에 기존 상태를 저장함 */
        canvas.save();

        /* 현재 카메라 위치에 맞게 캔버스를 이동시킴 */
        canvas.translate(-Camera.getInstance().x(), -Camera.getInstance().y());

        /* 현재 카메라 배율에 맞게 캔버스를 확대/축소함 */
        canvas
            .scale(Camera.getInstance().scale(), Camera.getInstance().scale(), 0, 0);

        /* 맵 배경을 그린다. */
        canvas.drawBitmap(AppManager.getInstance().getBitmap("background"), 0, 0, null);

        /**
         * game 오브젝트를 그린다
         */

        // 게임의 유닛들을 그린다.
        for (Mob mob : GameState.getInstance().getMobs()) {
          // 1. 보이는지 검사
          if (mob.dead)
            continue;

          // 보이므로 그린다
          else if (mob.created)
            mob.draw(canvas);
        }

        // 게임의 유닛들을 그린다.
        for (Unit unit : GameState.getInstance().getUnits()) {
          // 1. 보이는지 검사
          // if (camera.showInCamera(unit)) {
          // 보이므로 그린다
          unit.draw(canvas);
          // }

          /*
           * 스레드 종료가 필요한 경우 최대한 빨리 끝내기 위해 그림을 그리는 도중에도 스레드 종료 조건을 검사한다.
           */
          if (mIsRunning == false) {
            break;
          }
        }
        canvas.restore(); // 이동, 확대/축소했던 캔버스를 원상태로 복원

        // 테스트 정보 표시
        displayInformation(canvas);

        // 캔버스의 락을 풀고 실제 화면을 갱신한다.
        mHolder.unlockCanvasAndPost(canvas);
      }

      // 프레임을 구한다.
      fps++;
      fpsElapsedTime += System.currentTimeMillis() - fpsStartTime;
      if (fpsElapsedTime >= 1000) // 프레임율 표시는 1초마다 갱신함
      {
        mFps = fps;
        fps = 0;
        fpsElapsedTime = 0L;
      }
    }

    Log.i(tag, AppManager.getMethodName() + "() is end.");
  }

  /* 개발 참고용 정보 표시 */
  private int xForText = 0;
  private int yForText = 0;

  @SuppressLint("DefaultLocale")
  private void displayInformation(Canvas canvas) {
    // 현재 메모리 정보 출력용
    long totMem = 0L;
    long freeMem;

    if (mPaintInfo == null) {
      mPaintInfo = new Paint();
      mPaintInfo.setAntiAlias(true);
      mPaintInfo.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14,
          getResources().getDisplayMetrics()));

      xForText =
          (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getResources()
              .getDisplayMetrics());
      yForText =
          (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources()
              .getDisplayMetrics());

      totMem = Runtime.getRuntime().totalMemory();
    }

    freeMem = Runtime.getRuntime().freeMemory();

    canvas.save();
    /*
     * 그리기 fps 출력
     */
    canvas.drawText(mFps + " fps (" + AppManager.getInstance().getLogicFps() + " fps)", xForText,
        yForText, mPaintInfo);

    /*
     * 카메라 좌상단 좌표 (논리적인 기준점) 출력
     */
    canvas.translate(0, yForText);
    canvas.drawText("CAM left: " + Camera.getInstance().x() + " / top: "
        + Camera.getInstance().y() + " / scale: "
        + (int) (Camera.getInstance().scale() * 100 + 0.5) + "%", xForText, yForText,
        mPaintInfo);

    /*
     * 메모리 정보 표시
     */
    canvas.translate(0, yForText);
    canvas.drawText("Used Memory: " + (totMem - freeMem) / (1024 * 1024) + " / " + totMem
        / (1024 * 1024) + "M", xForText, yForText, mPaintInfo);

    /*
     * 게임 시계 출력
     */
    canvas.translate(0, yForText);
    String min = String.format("%02d", (int) (GameState.getInstance().getWorldTime() / 60));
    String sec = String.format("%02d", (int) (GameState.getInstance().getWorldTime() % 60));
    canvas.drawText("World Time: " + min + ":" + sec, xForText, yForText, mPaintInfo);

    /*
     * 현재 생성된 몹수
     */
    canvas.translate(0, yForText);
    canvas.drawText("Mob: " + GameState.getInstance().curMob, xForText, yForText, mPaintInfo);

    /*
     * 현재 죽은 몹수
     */
    canvas.translate(0, yForText);
    canvas.drawText("Dead Mob: " + GameState.getInstance().deadMob, xForText, yForText, mPaintInfo);

    /*
     * 현재 웨이브
     */
    canvas.translate(0, yForText);
    canvas.drawText("Wave: " + GameState.getInstance().wave, xForText, yForText, mPaintInfo);

    canvas.restore();
  }
}
