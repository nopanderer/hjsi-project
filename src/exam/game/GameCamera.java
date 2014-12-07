package exam.game;

import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

/*
 * 카메라 클래스. 화면에 보이는 영역을 관리한다.
 * 게임 오브젝트를 화면에 출력할 때 필요한 좌표 값 등도 변환한다.
 */
/**
 * @author 이상인
 */
/**
 * @author 이상인
 */
/**
 * @author 이상인
 */
public class GameCamera extends Bounds
{
    /*
     * 그리기 판정에 관한 변수들
     * 구체적인 상황에 따라 상수를 지정해놨으나 현재 자세한 의미는 없다.
     */
    private static final int   SHOWN_HIDDEN = 0;    // 개체가 카메라(화면)에 아예 안 보이는 상태
    private static final int   SHOWN_LEFT   = 1;    // 개체의 왼쪽 부분이 카메라에 보이는 상태
    private static final int   SHOWN_RIGHT  = 2;    // ... 오른쪽 부분이 ...
    private static final int   SHOWN_TOP    = 4;    // 상동
    private static final int   SHOWN_BOTTOM = 8;
    private static final int   SHOWN_ENTIRE = 15;   // 개체의 전체 모습이 보이는 상태

    /*
     * 게임 자체의 크기 (= 맵 크기)
     */
    public static final Bounds gameSize;

    /*
     * 카메라 자동 복귀에 관한 변수들
     */
    private boolean            scrolling    = false; // 카메라 스크롤 중일 때는 원위치로 돌아가지 않도록 하기 위한, 스크롤 중인가를 나타내는 변수
    private int                minSpeed     = 20;   // 원위치 최저 속도
    private int                maxSpeed     = 128;  // 원위치 최고 속도
    private int                hDestination;        // 카메라가 복귀해야 하는 수평 좌표
    private int                vDestination;        // 카메라가 복귀해야 하는 수직 좌표
    private int                hScrollSpeed;        // 카메라의 현재 수평 방향 복귀 속도
    private int                vScrollSpeed;        // 카메라의 현재 수직 방향 복귀 속도
    private boolean            hBorderOut   = false; // 카메라가 게임의 수평 경계를 넘었는지 여부
    private boolean            vBorderOut   = false; // 카메라가 게임의 수직 경계를 넘었는지 여부

    /* 쓰레기 */
    private static final float STEP         = 300;
    private float              scale        = 1.0f;
    private float              mPrevScale;
    private int                mPrevDist;

    /* static 변수 초기화 */
    static {
        gameSize = new Bounds(0, 0, 3840, 2160);
    }

    /**
     * 카메라의 가로, 세로 크기는 서피스뷰의 표면이 생성되면서 surfaceChanged()가 호출 될 때 값을 얻어온다.
     */
    public GameCamera()
    {
        super(0, 0, 0, 0);
    }

    /**
     * 화면 크기를 설정한다.
     * @param width
     *        화면의 가로 크기
     * @param height
     *        화면의 세로 크기
     */
    public void setCamSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * 현재 화면에 보이는 대상이면 true를 반환한다.
     * 지금은 그냥 보이냐, 안 보이냐만 따진다.
     * 비트플래그의 결과 자체는 의도대로 되지 않았으니 자세히 보면 손해
     */
    public boolean showInCamera(Unit unit)
    {
        // 필요할까봐 비트플래그로 만들어둠 (비트맵을 보이는 부분만 그려야할 때)
        int visibleFlag = SHOWN_HIDDEN;

        if (unit.left() < this.right())
            visibleFlag = visibleFlag | SHOWN_LEFT;

        if (unit.right() > this.left())
            visibleFlag = visibleFlag | SHOWN_RIGHT;

        if (unit.top() < this.bottom())
            visibleFlag = visibleFlag | SHOWN_TOP;

        if (unit.bottom() > this.top())
            visibleFlag = visibleFlag | SHOWN_BOTTOM;

        if (visibleFlag == SHOWN_ENTIRE)
            return true;
        else
            return false;
    }

    /*
     * 인게임 좌표를 화면 좌표로 변환한다.
     * 현재 테스트용으로 성냥 이미지 출력하는데 쓰고 있으나 제거 예정
     */
    public void getPhysicalBound(Rect output, int x, int y, int width, int height)
    {
        if (output == null)
            output = new Rect();

        output.left = x - this.x;
        output.top = y - this.y;
        output.right = x + width - this.x;
        output.bottom = y + height - this.y;
    }

    /**
     * @return 현재 화면 배율을 반환한다.
     */
    public float scale()
    {
        return scale;
    }

    /**
     * 카메라가 게임 경계 밖으로 넘어갔는지 검사한다.
     * 수평과 수직에 대한 결과를 hBorderOut과 vBorderOut에 반영한다.
     * 또한, 카메라 자동 복귀에 사용하는 수직/수평 스크롤 속도를 계산한다. (멀리 벗어났을 수록 빠르게 복귀)
     */
    private void checkCameraBorderOut()
    {
        int overLength1; // 카메라가 왼쪽이나 위쪽으로 넘어갔을 때, 카메라 좌표와 게임 좌표의 거리를 의미한다.
        int overLength2; // 마찬가지 (오른쪽이나 아래쪽)
        int scrollFactor = 100;

        // 가로 범위 검사
        overLength1 = 0 - left();
        overLength2 = right() - gameSize.right();

        if (overLength1 > 0) {
            hDestination = 0;
            hScrollSpeed = Math.max(minSpeed, Math.min(maxSpeed, 2 << overLength1 / scrollFactor)); // 오른쪽으로 (+ x)
            hBorderOut = true;
        }
        else if (overLength2 > 0) {
            hDestination = gameSize.right() - width;
            hScrollSpeed = -Math.max(minSpeed, Math.min(maxSpeed, 2 << overLength2 / scrollFactor)); // 왼쪽으로 (- x)
            hBorderOut = true;
        }

        // 세로 범위 검사
        overLength1 = 0 - top();
        overLength2 = bottom() - gameSize.bottom();

        if (overLength1 > 0) {
            vDestination = 0;
            vScrollSpeed = Math.max(minSpeed, Math.min(maxSpeed, 2 << overLength1 / scrollFactor)); // 아래로 (+ y)
            vBorderOut = true;
        }
        else if (overLength2 > 0) {
            vDestination = gameSize.bottom() - height;
            vScrollSpeed = -Math.max(minSpeed, Math.min(maxSpeed, 2 << overLength2 / scrollFactor)); // 위로 (- y)
            vBorderOut = true;
        }
    }

    /**
     * 과도하게 스크롤 된 카메라의 위치(x, y)를 제자리로 서서히 되돌린다.
     * autoBackCamera()를 호출하기에 앞서, 되도록이면 isBorderOut()의 반환 값이 true일 경우에만 호출하도록 한다.
     */
    public void autoBackCamera()
    {
        // 카메라 스크롤을 안하고 있으면
        if (scrolling == false) {
            // 카메라가 수평으로 넘어갔으면
            if (hBorderOut) {
                // 왼쪽으로 넘어간 경우
                if (hDestination == 0)
                    x = Math.min(hDestination, x + hScrollSpeed);
                // 오른쪽으로 넘어간 경우
                else
                    x = Math.max(hDestination, x + hScrollSpeed);

                // 카메라 x 좌표가 자동 복귀 목적지에 도달하면 수평으로 넘어갔다는 표시를 끈다.
                if (x == hDestination)
                    hBorderOut = false;
            }

            // 카메라가 수직으로 넘어갔으면
            if (vBorderOut) {
                // 위쪽으로 넘어간 경우
                if (vDestination == 0)
                    y = Math.min(vDestination, y + vScrollSpeed);
                // 아래쪽으로 넘어간 경우
                else
                    y = Math.max(vDestination, y + vScrollSpeed);

                // 카메라 y 좌표가 자동 복귀 목적지에 도달하면 수직으로 넘어갔다는 표시를 끈다.
                if (y == vDestination)
                    vBorderOut = false;
            }
        }
    }

    /**
     * @return 카메라가 게임의 경계를 수직으로든 수평으로든 넘어간 상태라면 <strong>true</strong>를 반환하고, 정상적인 경계 내에 있으면 <strong>false</strong>를
     *         반환한다.
     */
    public boolean isBorderOut()
    {
        return hBorderOut || vBorderOut;
    }

    /*
     * 멀티터치 이벤트에서 손가락 사이의 거리를 계산한다.
     */
    private int getDistance(MotionEvent event)
    {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));

        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    private float prevX; // 이전 터치 x 좌표
    private float prevY; // 이전 터치 y 좌표

    /**
     * 이 주석 자체는 안 중요함
     * @param event
     *        MotionEvent 클래스를 인수로 받는다.
     * @return 카메라에 관련된 터치(드래그 혹은 핀치 줌 인&아웃)가 제대로 처리되었으면 <strong>true</strong>를 반환하고,
     *         카메라 클래스에서 처리하지 않는 이벤트라면 <strong>false</strong>를 반환한다.
     */
    public boolean touchHandler(MotionEvent event)
    {
        String caption = "empty";

        float dx = 0f, dy = 0f; // 터치 이동 값

        switch (event.getActionMasked())
        {
        case MotionEvent.ACTION_DOWN:
            caption = "action_down ";
            prevX = event.getX(); // 이전 좌표를 구해놓음
            prevY = event.getY();
            break;

        case MotionEvent.ACTION_POINTER_DOWN:
            break;

        case MotionEvent.ACTION_MOVE:
            caption = "action_move ";

            if (event.getPointerCount() == 1) // MotionEvent.getPointerCount()로 터치 수를 가져온다.
            {
                /* 손가락 하나일 때 */

                // 카메라 스크롤을 하고 있다고 표시함
                scrolling = true;
                // 카메라가 범위를 벗어났는지 체크해서 멤버 변수(hBorderOut, vBorderOut)에 기록함
                checkCameraBorderOut();

                /* 현재 좌표와 이전 좌표의 거리를 구해서 그만큼 카메라 좌표를 이동해준다. */
                // 현재 좌표와 기록된 이전 좌표의 이동 거리를 구함
                dx = event.getX() - prevX;
                dy = event.getY() - prevY;
                // 현재 들어온 좌표를 다음 이벤트에서 활용하기 위해 기록해둠
                prevX = event.getX();
                prevY = event.getY();
                // 카메라 위치 갱신
                x = (int) (x - dx);
                y = (int) (y - dy);
            }
            else {
                /* 손가락 둘 */
            }
            break;

        case MotionEvent.ACTION_POINTER_UP:
            break;

        case MotionEvent.ACTION_UP:
            // 스크롤이 끝났다고 표시한다
            scrolling = false;
            caption = "action_up ";
            break;

        default:
            break;
        }

        caption = caption + "(" + dx + ", " + dy + ")";
        Log.i("camera", caption);

        return true;
    }

    /* 재활용 쓰레기 */
    private void zoomCamera(MotionEvent event)
    {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            mPrevDist = getDistance(event);
            mPrevScale = getScale();
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            float delta = (getDistance(event) - mPrevDist) / STEP;
            scale = Math.min(3.0f, Math.max(0.5f, mPrevScale + delta));

            // Log.i("scale", "delta: " + delta + ", scale: " + scale);
        }
    }

    /* 잉여 */
    public float getScale()
    {
        return scale;
    }
}
