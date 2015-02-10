package hjsi.common;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * 서피스뷰에서 배경, 모든 유닛 클래스 등을 그려줄 때 필요한 기준 좌표와 크기(zoom) 값을 조작하는 클래스.
 */
public class Camera {
  /*
   * 카메라 스크롤 속도 배수에 대한 상수
   */
  private static final int maxScrollScale = 5;
  private static final int minScrollScale = 2;

  /*
   * 카메라 확대/축소 배율에 대한 상수
   */
  /**
   * 화면 최대 배율
   */
  private final float maxZoom = 2.0f;
  /**
   * 최소 화면 배율
   */
  private final float minZoom = 1.0f;
  private final float zoomStep = 0.01f; // 화면 배율 증감 단위 (+-1%)

  /*
   * OUT_XXXX: 카메라가 worldRect + worldMargin을 벗어난 경우 그 방향을 표시하기 위한 상수들
   */
  private static final int OUT_NONE = 0;
  /**
   * position.x < worldRect.left - worldMargin.left
   */
  private static final int OUT_LEFT = 1;
  /**
   * position.x > worldRect.right + worldMargin.right
   */
  private static final int OUT_RIGHT = 2;
  /**
   * position.y < worldRect.top - worldMargin.top
   */
  private static final int OUT_TOP = 4;
  /**
   * position.y > worldRect.bottom + worldMargin.bottom
   */
  private static final int OUT_BOTTOM = 8;

  /*
   * 카메라 현재 상태와 기본 정보 생성자에서 초기화한다
   */
  private Point position; // 카메라의 현재 위치
  private Rect viewport; // 카메라 뷰 영역
  private Rect worldRect; // 게임월드 영역
  private Rect worldMargin; // 카메라가 게임월드를 벗어날 때 여백의 제한 값

  /*
   * 카메라 스크롤에 필요한 변수
   */
  private PointF oldTouch = new PointF(); // 이전 터치 좌표
  private PointF sumMoveDistance = new PointF();
  private int scrollStep = 40; // 한 프레임에 카메라가 움직이는 스크롤 이동거리(px)
  /**
   * 사용자가 카메라 이동속도보다 빠르게 스크롤 했을 때, 카메라를 최종적으로 스크롤 속도에 비례한 위치에 위치시키기 위한 값.<br/>
   * 만약 카메라 이동속도를 20px이라하고 사용자가 1초만에 100px을 스크롤한 경우라면, 스크롤을 하는 1초 동안 20px까지
   * 움직이고, 나머지 80px을 4초에 걸쳐 이동하기 위해 scrollRemain에 80만큼을 저장한다.
   */
  private Point scrollRemain = new Point();

  /*
   * 멀리 벗어났을 수록 배수가 커져서 카메라 스크롤 속도가 증가함
   */
  private int hScrollScale = 1;
  private int vScrollScale = 1;

  private int outDirection = OUT_NONE; // 카메라가 벗어난 방향을 갖고 있음

  private int hGapLength = 0; // 좌우에 상관없이 카메라가 벗어난 거리
  private int vGapLength = 0; // 상하에 상관없이 카메라가 벗어난 거리

  /*
   * 카메라 줌에 필요한 변수
   */
  private float oldDistance; // 두 포인터 간의 거리
  private float zoom = 1.5f; // 현재 화면 배율
  private PointF midRatio = new PointF(); // 줌 터치한 중심점의 화면상의 백분율

  /*
   * 화면을 스크롤 중이면 카메라가 자동복귀하지 못하게 한다.
   */
  private boolean keepTouching = false;
  /*
   * 터치를 시작하고서 한 번이라도 줌 동작으로 들어갔으면 터치를 완전히 끝내기 전까지는 스크롤을 못하게 한다.
   */
  private boolean doNotScroll = false;

  public Camera(float factor) {
    position = new Point(0, 0);
    worldRect = new Rect(0, 0, (int) (1920 * factor + 0.5f), (int) (1080 * factor + 0.5f));
    int margin = (int) (125 * factor + 0.5f);
    worldMargin = new Rect(margin, margin, margin, margin);
  }

  public void setViewportSize(int width, int height) {
    if (viewport == null) {
      viewport = new Rect(0, 0, width, height);
    } else {
      viewport.right = width;
      viewport.bottom = height;
    }
  }

  /* get 메소드 */
  public int getX() {
    return position.x;
  }

  public int getY() {
    return position.y;
  }

  /**
   * @return viewport.right / zoom
   */
  public float getWidthScaled() {
    return viewport.right / zoom;
  }

  /**
   * @return viewport.bottom / zoom
   */
  public float getHeightScaled() {
    return viewport.bottom / zoom;
  }

  public float getScale() {
    return zoom;
  }

  public int getWorldWidth() {
    return worldRect.right;
  }

  public int getWorldHeight() {
    return worldRect.bottom;
  }

  /**
   * @return worldRect.left - (int) (worldMargin.left / zoom)
   */
  private int getLeftLimit() {
    return worldRect.left - (int) (worldMargin.left / zoom);
  }

  /**
   * @return worldRect.right + (int) (worldMargin.right / zoom)
   */
  private int getRightLimit() {
    return worldRect.right + (int) (worldMargin.right / zoom);
  }

  private int getTopLimit() {
    return worldRect.top - (int) (worldMargin.top / zoom);
  }

  private int getBottomLimit() {
    return worldRect.bottom + (int) (worldMargin.bottom / zoom);
  }

  /*
   * 멀티터치 이벤트에서 손가락 사이의 거리를 계산한다.
   */
  private int getDistance(MotionEvent event) {
    int dx = (int) (event.getX(0) - event.getX(1));
    int dy = (int) (event.getY(0) - event.getY(1));

    return (int) Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * 터치 이벤트가 발생했을 때 카메라가 이동이나 확대/축소를 한다. 카메라가 이벤트를 처리한다면 이외의 UI에서는 이벤트를 받지 않는다.
   * 
   * @param event 처리할 화면 터치 이벤트
   * @return 카메라가 이벤트를 소모했으면 true, 아니면 false를 반환한다.
   */
  public boolean touchHandler(MotionEvent event) {
    AppManager.printEventLog(event);
    boolean eventConsumed = true;
    float dx = 0f, dy = 0f; // 터치 이동 값

    switch (event.getActionMasked()) {
    /*
     * 화면 한손가락 터치
     */
      case MotionEvent.ACTION_DOWN:
        /*
         * 스크롤 중임을 표시하고, 아직 덜 처리된 자동 스크롤이 남아있어도, 사용자가 직접 스크롤하려고 하는 경우므로 남아있는
         * 스크롤을 초기화한다.
         */
        scrollRemain.set(0, 0);
        keepTouching = true;

        if (checkCameraOut() == true) {
          doNotScroll = true; // 새로 터치를 시작할 때도 카메라가 벗어난 상태면 스크롤 금지
        }

        oldTouch.set(event.getX(), event.getY()); // 이전 좌표를 구해놓음
        sumMoveDistance.set(0f, 0f);
        break;

      /*
       * 화면 두손가락 터치
       */
      case MotionEvent.ACTION_POINTER_DOWN:
        doNotScroll = true; // 두 손가락이 닿는 순간 줌은 시작된거므로 스크롤 금지
        oldDistance = getDistance(event); // 두 손가락의 터치 좌표 사이의 거리를 구함

        // 줌 터치의 중심점을 구한다
        PointF midpoint = new PointF();
        midpoint.x = (event.getX(0) + event.getX(1)) / 2;
        midpoint.y = (event.getY(0) + event.getY(1)) / 2;

        // 줌 터치 중심점의 화면 상의 백분율을 구함. 0.01 아래로는 버림
        midRatio.x = Math.round(midpoint.x / viewport.right * 100) / 100f;
        midRatio.y = Math.round(midpoint.y / viewport.bottom * 100) / 100f;
        break;

      /*
       * 화면 터치 후 이동
       */
      case MotionEvent.ACTION_MOVE:
        /*
         * 화면 스크롤의 경우 == 한 손가락
         */
        if (event.getPointerCount() == 1) {
          // ACTION_UP하기 전까지 한 번이라도 줌을 했으면 스크롤 못 함
          if (doNotScroll)
            break;

          // 현재 좌표와 이전 좌표의 거리를 구해서 그만큼 카메라 좌표를 이동해준다.
          float nowX = event.getX(), nowY = event.getY();
          dx = oldTouch.x - nowX;
          dy = oldTouch.y - nowY;

          oldTouch.set(nowX, nowY); // 현재 들어온 좌표를 다음 이벤트에서 활용하기 위해 기록해둠

          moveCamera(dx, dy); // 카메라 위치 갱신
        }
        // 화면 확대/축소의 경우 == 두 손가락
        else {
          doNotScroll = true;

          int nowDistance = getDistance(event); //
          float delta = nowDistance - oldDistance; // 줌 터치가 새로 이동한 거리
          int cellWidth = viewport.right / 100; // 가로 길이를 기준으로 100칸으로 나눴을 때, 한
                                                // 칸이 차지하는 길이를 구함.

          // 배율이 한 번에 0.05씩만 바뀌도록 제한함
          if (Math.abs(delta) >= cellWidth) {
            oldDistance = nowDistance; // 현재 거리는 더 이상 쓸 일이 없으니 이전 거리에 보관

            int movedCells = (int) (delta / cellWidth); // 터치가 이동한 거리를 전체 30칸의 칸
                                                        // 단위로 환산

            // 기존 zoom 값을 토대로 새로운 zoom 값을 계산 (기존 배율 +- 이동한 칸 수 * 5%)
            float freshZoom = Math.min(maxZoom, Math.max(minZoom, zoom + movedCells * zoomStep));
            freshZoom = (int) (freshZoom * 100f + 0.05f) / 100f; // 소수점 보정 (정밀도
                                                                 // 그지같음)

            /*
             * 구 width와 신 width의 차이를 구하고, 처음 줌 터치할 때의 중점의 X 좌표 값을 화면에 대한 백분율로
             * 환산한 값과 곱한다. 그 값만큼 카메라의 left, top을 이동시키면 확대/축소 지점에 적당한 방향으로 카메라가
             * 이동
             */
            float diff_width = getWidthScaled() - (viewport.right / freshZoom);
            // 이전 가로 크기와 새로운 가로 크기의 차이를 구함
            float diff_height = getHeightScaled() - (viewport.bottom / freshZoom);
            // 이전 세로 크기 - 새로운 세로 크기
            dx = diff_width * midRatio.x;
            dy = diff_height * midRatio.y;
            moveCamera(dx, dy);

            /*
             * 기존 zoom 값이 필요했던 연산(widthScaled()...)을 마쳤으니 이번에 새로 구한 freshZoom
             * 값으로 업데이트
             */
            zoom = freshZoom;
          }
        }
        sumMoveDistance.offset(Math.abs(dx), Math.abs(dy));
        break;

      case MotionEvent.ACTION_POINTER_UP:
        break;

      case MotionEvent.ACTION_UP:
        if (sumMoveDistance.equals(0f, 0f))
          eventConsumed = false;
        keepTouching = false; // 손을 떼야 카메라가 복귀 할 수 있음
        doNotScroll = false; // 한 번이라도 줌을 했으면 완전히 손을 떼야 스크롤할 수 있도록 함.
        break;
    }

    return eventConsumed;
  }

  /**
   * 과도하게 스크롤 된 카메라의 위치(x, y)를 제자리로 서서히 되돌린다.
   */
  public void autoScroll() {
    /*
     * 스크롤(터치) 중이지도 않고, 카메라가 게임월드영역을 벗어났다면 제자리도 되돌린다.
     */
    if ((doNotScroll == true || keepTouching == false) && checkCameraOut() == true) {
      String msg = new String();
      getCameraOutLength();

      int dx = 0, dy = 0;

      // 카메라를 움직일 거리와 방향을 계산함.
      if (isOutOfLeft()) {
        dx = Math.min(scrollStep * hScrollScale, hGapLength);
        msg += "left ";

        scrollRemain.set(0, scrollRemain.y); // 화면을 벗어난다면 해당 방향에 대한 빚을 청산한다.
      } else if (isOutOfRight()) {
        dx = -Math.min(scrollStep * hScrollScale, hGapLength);
        msg += "right ";

        scrollRemain.set(0, scrollRemain.y); // 화면을 벗어난다면 해당 방향에 대한 빚을 청산한다.
      }

      if (isOutOfTop()) {
        dy = Math.min(scrollStep * vScrollScale, vGapLength);
        msg += "top ";

        scrollRemain.set(scrollRemain.x, 0); // 화면을 벗어난다면 해당 방향에 대한 빚을 청산한다.
      } else if (isOutOfBottom()) {
        dy = -Math.min(scrollStep * vScrollScale, vGapLength);
        msg += "bottom ";

        scrollRemain.set(scrollRemain.x, 0); // 화면을 벗어난다면 해당 방향에 대한 빚을 청산한다.
      }

      // 방향에 맞게 dx, dy만큼 카메라를 이동시킴.
      dx = (int) (dx / getScale() * 10 + (dx > 0 ? 5 : -5)) / 10;
      dy = (int) (dy / getScale() * 10 + (dy > 0 ? 5 : -5)) / 10;
      position.offset(dx, dy);

      // autoScroll을 마치면 현재 터치한 상태에서 스크롤 가능함
      if (checkCameraOut() == false) {
        doNotScroll = false;
      }

      msg += "gap(" + hGapLength + "," + vGapLength + "), (dx: " + dx + ", dy: " + dy + ")";
      AppManager.printDetailLog(msg);
    }
    /*
     * 스크롤 해야할 빚이 남아있으면 계속해서 이동시킨다.
     */
    else if (scrollRemain.equals(0, 0) == false) {
      moveCamera(0, 0);
    }
  }

  /**
   * 카메라가 가로, 세로 방향에 대해 게임월드를 벗어난 길이를 구한다. 해당 값은 hGapLength, vGapLength 필드에
   * 저장된다.
   */
  private void getCameraOutLength() {
    hGapLength = 0;
    vGapLength = 0;

    if (isOutOfLeft()) {
      hGapLength = getLeftLimit() - position.x;
    } else if (isOutOfRight()) {
      hGapLength = position.x + (int) getWidthScaled() - getRightLimit();
    }

    if (isOutOfTop()) {
      vGapLength = getTopLimit() - position.y;
    } else if (isOutOfBottom()) {
      vGapLength = position.y + (int) getHeightScaled() - getBottomLimit();
    }

    int speedScaleCell = viewport.right / (maxScrollScale * 2);
    hScrollScale = (int) (hGapLength / speedScaleCell);
    hScrollScale = Math.max(minScrollScale, Math.min(maxScrollScale, hScrollScale));

    speedScaleCell = viewport.bottom / (maxScrollScale * 2);
    vScrollScale = (int) (vGapLength / speedScaleCell);
    vScrollScale = Math.max(minScrollScale, Math.min(maxScrollScale, vScrollScale));

    AppManager.printDetailLog("gap level: " + hScrollScale + ", " + vScrollScale + "(per " + speedScaleCell + ")");
  }

  /**
   * 카메라가 게임월드의 크기를 벗어났는지 검사한다. 벗어났다면 true를 반환하고, 한 방향도 벗어나지 않았으면 false를 반환한다.
   * 만약, 좌/우/상/하 어디로든 벗어난 곳이 있다면 outDirection 필드에 벗어난 방향을 기록한다.
   */
  private boolean checkCameraOut() {
    // 현재 상태를 초기화한다.
    outDirection = OUT_NONE;

    // 가로 범위를 벗어났는지 체크한다.
    if (position.x < getLeftLimit()) {
      outDirection = OUT_LEFT;
    } else if ((position.x + viewport.width()) / zoom > getRightLimit()) {
      outDirection = OUT_RIGHT;
    }

    // 세로 범위를 벗어났는지 체크한다.
    if (position.y < getTopLimit()) {
      outDirection |= OUT_TOP;
    } else if ((position.y + viewport.height()) / zoom > getBottomLimit()) {
      outDirection |= OUT_BOTTOM;
    }

    // 어느 하나라도 벗어났으면 true를 리턴한다.
    return outDirection != OUT_NONE;
  }

  /**
   * 카메라를 지정한 좌표로 완전히 이동시킨다.
   */
  private void gotoCamera(int x, int y) {
    position.set(x, y);
  }

  /**
   * 카메라를 현재 위치에서 지정한 값만큼 이동시킨다.
   */
  private void moveCamera(int dx, int dy) {
    int debtX = 0, debtY = 0;

    /*
     * x축 이동 거리가 없을 경우, 스크롤 빚 x를 이용한다. 스크롤 빚이 남아있는지 자체는 이 메소드가 호출되기 전에 검사된다.
     */
    if (dx == 0) {
      if (Math.abs(scrollRemain.x) > scrollStep) {
        // 스크롤 빚이 scrollStep보다 크면 scrollStep만큼 움직이고, 움직인 거리만큼은 빚을 갚는다.

        if (scrollRemain.x < 0) {
          debtX = scrollStep; // 이동하려던 방향이 음수이므로 scrollStep은 더한다.
          dx = -scrollStep; // 왼쪽(left) 방향으로 이동할 때는 x 값이 감소
        } else {
          debtX = -scrollStep; // 이동하려던 방향이 양수이므로 scrollStep은 뺀다.
          dx = scrollStep; // 오른쪽(right) 방향으로 이동할 때는 x 값이 증가
        }
      } else {
        // 남은 빚이 scrollStep보다 작으면, 남은 빚을 다 청산한다.
        debtX = -scrollRemain.x;
      }
    }
    /*
     * x축 이동 거리가 scrollStep보다 크면 scrollStep만큼 이동하고, 남은 거리는 빚진다.
     */
    else if (Math.abs(dx) > scrollStep) {
      if (dx < 0) {
        debtX = dx + scrollStep; // 이동하려던 방향이 음수이므로 scrollStep은 더한다.
        dx = -scrollStep; // 왼쪽(left) 방향으로 이동할 때는 x 값이 감소
      } else {
        debtX = dx - scrollStep; // 이동하려던 방향이 양수이므로 scrollStep은 뺀다.
        dx = scrollStep; // 오른쪽(right) 방향으로 이동할 때는 x 값이 증가
      }
    }

    /*
     * y축 이동 거리가 없을 경우, 스크롤 빚 y를 이용한다. 스크롤 빚이 남아있는지 자체는 이 메소드가 호출되기 전에 검사된다.
     */
    if (dy == 0) {
      if (Math.abs(scrollRemain.y) > scrollStep) {
        // 스크롤 빚이 scrollStep보다 크면 scrollStep만큼 움직이고, 움직인 거리만큼은 빚을 갚는다.

        if (scrollRemain.y < 0) {
          debtY = scrollStep; // 이동하려던 방향이 음수이므로 scrollStep은 더한다.
          dy = -scrollStep; // 위(top) 방향으로 이동할 때는 y 값이 감소
        } else {
          debtY = -scrollStep; // 이동하려던 방향이 양수이므로 scrollStep은 뺀다.
          dy = scrollStep; // 아래(bottom) 방향으로 이동할 때는 y 값이 증가
        }
      } else {
        // 남은 빚이 scrollStep보다 작으면, 남은 빚을 다 청산한다.
        debtY = -scrollRemain.y;
      }
    }
    /*
     * y축 이동 거리가 scrollStep보다 크면 scrollStep만큼 이동하고, 남은 거리는 빚진다.
     */
    else if (Math.abs(dy) > scrollStep) {
      if (dy < 0) {
        debtY = dy + scrollStep; // 이동하려던 방향이 음수이므로 scrollStep은 더한다.
        dy = -scrollStep; // 위(top) 방향으로 이동할 때는 y 값이 감소
      } else {
        debtY = dy - scrollStep; // 이동하려던 방향이 양수이므로 scrollStep은 뺀다.
        dy = scrollStep; // 아래(bottom) 방향으로 이동할 때는 y 값이 증가
      }
    }

    scrollRemain.offset(debtX, debtY); // debtX, debtY 값만큼 스크롤 빚을 가감한다.

    position.offset(dx, dy); // 계산된 dx, dy만큼 이동한다.
  }

  private void moveCamera(float dx, float dy) {
    moveCamera((int) dx, (int) dy);
  }

  /* 현재 어느 방향으로 나가있는 상태인지 플래그를 확인하는 메소드들 */
  private boolean isOutOfLeft() {
    return ((outDirection & OUT_LEFT) == OUT_LEFT);
  }

  private boolean isOutOfRight() {
    return ((outDirection & OUT_RIGHT) == OUT_RIGHT);
  }

  private boolean isOutOfTop() {
    return ((outDirection & OUT_TOP) == OUT_TOP);
  }

  private boolean isOutOfBottom() {
    return ((outDirection & OUT_BOTTOM) == OUT_BOTTOM);
  }
}
