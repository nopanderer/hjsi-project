package exam.game;

import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

/*
 * 카메라 클래스. 화면에 보이는 영역을 관리한다.
 * 게임 오브젝트를 화면에 출력할 때 필요한 좌표 값 등도 변환한다.
 */
public class GameCamera extends Bounds
{
    /* 그리기 판정에 관한 변수들 */
    private static final int   SHOWN_HIDDEN = 0;
    private static final int   SHOWN_LEFT   = 1;
    private static final int   SHOWN_RIGHT  = 2;
    private static final int   SHOWN_TOP    = 4;
    private static final int   SHOWN_BOTTOM = 8;
    private static final int   SHOWN_ENTIRE = SHOWN_LEFT | SHOWN_RIGHT | SHOWN_TOP | SHOWN_BOTTOM;

    /*
     * 게임 자체의 크기 (= 맵 크기)
     */
    public static final Bounds gameSize     = new Bounds(0, 0, 3840, 2160);

    /* 카메라 조작에 관한 변수들 */
    private int                scrollFactor = 20;

    private boolean            modeZoom     = false;

    private static final float STEP         = 300;
    private float              scale        = 1.0f;
    private float              mPrevScale;
    private int                mPrevDist;

    /**
     * @param width
     *            화면의 가로 크기
     * @param height
     *            화면의 세로 크기
     */
    public GameCamera(int width, int height)
    {
        super(0, 0, width, height);
    }

    /**
     * 화면 크기를 설정한다.
     * 
     * @param width
     *            화면의 가로 크기
     * 
     * @param height
     *            화면의 세로 크기
     */
    public void setCamSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * 현재 화면에 보이는 대상이면 true를 반환한다.
     */
    public boolean showInCamera(Unit unit)
    {
        // 필요할까봐 비트플래그로 만들어둠 (비트맵을 보이는 부분만 그려야할 때)
        int visibleFlag = SHOWN_HIDDEN;

        if (unit.left() < this.right())
        {
            visibleFlag = visibleFlag | SHOWN_LEFT;
        }

        if (unit.right() > this.left())
        {
            visibleFlag = visibleFlag | SHOWN_RIGHT;
        }

        if (unit.top() < this.bottom())
        {
            visibleFlag = visibleFlag | SHOWN_TOP;
        }

        if (unit.bottom() > this.top())
        {
            visibleFlag = visibleFlag | SHOWN_BOTTOM;
        }

        if (visibleFlag == SHOWN_ENTIRE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
     * 인게임 좌표를 화면 좌표로 변환한다.
     */
    public void getPhysicalBound(Rect output, int x, int y, int width, int height)
    {
        if (output == null)
        {
            output = new Rect();
        }

        output.left = x - this.x;
        output.top = y - this.y;
        output.right = (x + width) - this.x;
        output.bottom = (y + height) - this.y;
    }

    public float scale()
    {
        return scale;
    }

    public void fillSpace()
    {
        if (left() < 0)
        {
            x = Math.min(0, x + scrollFactor);
        }
        else if (right() > gameSize.right())
        {
            x = Math.max(gameSize.right() - width, x - scrollFactor);
        }

        if (top() < 0)
        {
            y = Math.min(0, y + scrollFactor);
        }
        else if (bottom() > gameSize.bottom())
        {
            y = Math.max(gameSize.bottom() - height, y - scrollFactor);
        }
    }

    /*
     * 멀티터치 이벤트에서 손가락 사이의 거리를 계산한다.
     */
    private int getDistance(MotionEvent event)
    {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));

        return (int) (Math.sqrt((dx * dx) + (dy * dy)));
    }

    private float prevX;
    private float prevY;

    /**
     * <strong>false</strong>를 반환하는 경우, 이 함수를 호출한 곳에서 나머지 처리를 하면 된다.
     * 
     * @param event
     *            MotionEvent 클래스를 인수로 받는다.
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
            prevX = event.getX();
            prevY = event.getY();
            break;

        case MotionEvent.ACTION_POINTER_DOWN:
            break;

        case MotionEvent.ACTION_MOVE:
            caption = "action_move ";

            if (event.getPointerCount() == 1) // MotionEvent.getPointerCount()로 터치 수를 가져온다.
            {
                /* 손가락 하나 */
                dx = event.getX() - prevX;
                dy = event.getY() - prevY;
                prevX = event.getX();
                prevY = event.getY();

                x = (int) (x - dx);
                y = (int) (y - dy);
            }
            else
            {
                /* 손가락 둘 */
            }
            break;

        case MotionEvent.ACTION_POINTER_UP:
            break;

        case MotionEvent.ACTION_UP:
            caption = "action_up ";
            break;

        default:
            break;
        }

        caption = caption + "(" + dx + ", " + dy + ")";
        Log.i("camera", caption);

        return true;
    }

    private void zoomCamera(MotionEvent event)
    {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_POINTER_DOWN)
        {
            mPrevDist = getDistance(event);
            mPrevScale = getScale();
        }
        else if (action == MotionEvent.ACTION_MOVE)
        {
            float delta = (getDistance(event) - mPrevDist) / STEP;
            scale = (Math.min(3.0f, Math.max(0.5f, mPrevScale + delta)));

            // Log.i("scale", "delta: " + delta + ", scale: " + scale);
        }
    }

    public float getScale()
    {
        return scale;
    }
}
