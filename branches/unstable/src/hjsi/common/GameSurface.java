package hjsi.common;

import hjsi.game.GameState;
import hjsi.game.Mob;
import hjsi.game.Unit;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 게임 내용(맵, 타워, 투사체 등)을 그려줄 서피스뷰 클래스이다. 쓰레드 사용해서 canvas에 그림을 그릴 수 있는 유일한 방법이다. 때문에 게임에선 거의 서피스뷰를
 * 사용한다고 한다. 내부적으로 더블버퍼링을 사용한다. 시스템 UI는 Game 액티비티에서 처리하고(Button 등), 게임 자체를 위한 UI(타워 선택, 카메라 이동 등)
 * 이벤트는 이 클래스에서 처리한다.
 */
public class GameSurface extends SurfaceView implements SurfaceHolder.Callback, Runnable {
  /* 서피스뷰 그리기에 필요한 객체 및 변수 */
  private Thread mThreadPainter; // 그리기 스레드
  private boolean mIsRunning; // 그리기 스레드 동작 상태
  /**
   * 카메라 클래스
   */
  private Camera camera;
  /**
   * 화면 대 게임월드 비율
   */
  private float screenRatio;
  /**
   * GameState
   */
  private GameState gState;

  /*
   * 각종 정보를 출력하는데 사용함
   */
  private Paint mPaintInfo; // 텍스트 출력용 페인트 객체
  private int mFps; // 그리기 fps

  /**
   * 배치모드에서 격자 그리기용 페인트
   */
  private Paint gridPaint;

  /**
   * GameSurface 생성자
   * 
   * @param context getApplicationContext()를 이용하여 컨텍스트 객체를 넣어주셈
   * @param camera 카메라
   */
  public GameSurface(Context context, Camera camera, GameState gameState) {
    super(context);
    this.camera = camera;
    screenRatio = camera.getScreenWidth() / (float) GameState.WORLD_WIDTH;
    gState = gameState;

    // 게임 내 변수 출력용 페인트 객체 생성
    mPaintInfo = new Paint();
    mPaintInfo.setAntiAlias(true);
    mPaintInfo.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources()
        .getDisplayMetrics()));

    // displayInformation용 좌표값
    xForText =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getResources()
            .getDisplayMetrics());
    yForText =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources()
            .getDisplayMetrics());

    // 배치모드 표시용 페인트 객체 생성
    gridPaint = new Paint();
    gridPaint.setAntiAlias(true);
    gridPaint.setStyle(Style.STROKE);
    gridPaint.setStrokeWidth(3);
    gridPaint.setColor(Color.RED);

    // 홀더를 가져와서 Callback 인터페이스를 등록한다. 구현한 각 콜백은 surface의 변화가 있을 때마다 호출된다.
    getHolder().addCallback(this);
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

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    AppManager.printDetailLog("width: " + width + "px, height: " + height + "px");
    camera.setViewportSize(width, height);
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

  /* 실제로 그리기를 처리할 부분이다 */
  @Override
  public void run() {
    /* fps 계산을 위한 변수 */
    long fpsStartTime;
    long fpsElapsedTime = 0L;
    int fps = 0;

    /*
     * 타워 배치 격자를 미리 준비한다.
     */
    Rect area = gState.getTowersArea(screenRatio);
    int cellsWidth = gState.getTowersWidth(screenRatio);
    int cellsHeight = gState.getTowersHeight(screenRatio);

    while (mIsRunning) {
      // 프레임 시작 시간을 구한다.
      fpsStartTime = System.currentTimeMillis();

      // 전체 그리기 수행
      synchronized (getHolder()) {
        // 캔버스를 잠그는 듯
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
          break;
        }

        camera.autoScroll();

        canvas.drawColor(Color.DKGRAY); // 게임 배경 바깥 범위를 회색으로 채운다.

        /* 캔버스를 이동, 확대/축소하기 전에 기존 상태를 저장함 */
        canvas.save();

        /* 현재 카메라 위치에 맞게 캔버스를 이동시킴 */
        canvas.translate(-camera.getX(), -camera.getY());

        /* 현재 카메라 배율에 맞게 캔버스를 확대/축소함 */
        canvas.scale(camera.getScale(), camera.getScale(), 0, 0);

        /* 맵 배경을 그린다. */
        canvas.drawBitmap(AppManager.getBitmap("background"), 0, 0, null);

        // 배치모드 UI를 그린다.
        if (gState.checkDeployMode()) {
          for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 8; j++) {
              canvas.drawRect(area.left + i * cellsWidth, area.top + j * cellsHeight, area.left
                  + (i + 1) * cellsWidth, area.top + (j + 1) * cellsHeight, gridPaint);
            }
          }
        }

        /**
         * game 오브젝트를 그린다
         */

        // 게임의 유닛들을 그린다.
        for (int i = 0; i < gState.getUnits().size(); i++) {
          Unit unit = gState.getUnits().get(i);
          /* 파괴되었는지 검사 */
          if (unit.destroyed)
            continue;

          /* 살아있으면 그리기 */
          else
            unit.draw(canvas, screenRatio);

          /*
           * 스레드 종료가 필요한 경우 최대한 빨리 끝내기 위해 그림을 그리는 도중에도 스레드 종료 조건을 검사한다.
           */
          if (mIsRunning == false) {
            break;
          }
          if (unit instanceof Mob)
            ((Mob) unit).update(System.currentTimeMillis());

        }

        canvas.restore(); // 이동, 확대/축소했던 캔버스를 원상태로 복원

        // 테스트 정보 표시
        displayInformation(canvas);

        // 캔버스의 락을 풀고 실제 화면을 갱신한다.
        getHolder().unlockCanvasAndPost(canvas);
      }

      // 프레임을 구한다.
      fps++;
      fpsElapsedTime += System.currentTimeMillis() - fpsStartTime;
      if (fpsElapsedTime >= 1000) // 프레임율 표시는 1초마다 갱신함
      {
        mFps = fps;
        fps = 0;
        fpsElapsedTime -= 1000L;
      }
    }

    AppManager.printDetailLog("GameSurface 스레드 종료");
  }

  /* 개발 참고용 정보 표시 */
  private int xForText = 0;
  private int yForText = 0;

  @SuppressLint("DefaultLocale")
  private void displayInformation(Canvas canvas) {
    // 현재 메모리 정보 출력용
    long totMem = (long) (Runtime.getRuntime().maxMemory() / 1024f / 1024f + 0.5f);;
    long allocMem =
        (long) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024f + 0.5f);

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
    canvas.drawText("CAM left: " + camera.getX() + " / top: " + camera.getY() + " / scale: "
        + (int) (camera.getScale() * 100 + 0.5) + "%", xForText, yForText, mPaintInfo);

    /*
     * 메모리 정보 표시
     */
    canvas.translate(0, yForText);
    canvas.drawText("Used Memory: " + allocMem + " / " + totMem + "MB", xForText, yForText,
        mPaintInfo);

    /*
     * 게임 시계 출력
     */
    canvas.translate(0, yForText);
    String min = String.format("%02d", (int) (gState.getWorldTime() / 60));
    String sec = String.format("%02d", (int) (gState.getWorldTime() % 60));
    canvas.drawText("World Time: " + min + ":" + sec, xForText, yForText, mPaintInfo);

    /*
     * 현재 생성된 몹수
     */
    canvas.translate(0, yForText);
    canvas.drawText("Mob: " + GameState.curMob, xForText, yForText, mPaintInfo);

    /*
     * 현재 죽은 몹수
     */
    canvas.translate(0, yForText);
    canvas.drawText("Dead Mob: " + GameState.deadMob, xForText, yForText, mPaintInfo);

    /*
     * 현재 웨이브
     */
    canvas.translate(0, yForText);
    canvas.drawText("Wave: " + gState.getWave(), xForText, yForText, mPaintInfo);

    canvas.restore();
  }
}
