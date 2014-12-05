package exam.game;

import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

/*
 * 카메라 클래스. 화면에 보이는 영역을 관리한다.
 * 게임 오브젝트를 화면에 출력할 때 필요한 좌표 값 등도 변환한다.
 */
public class GameCamera
{
    private int                mLeft          = 0;           // 카메라 x 좌표
    private int                mTop           = 0;           // 카메라 y 좌표
    public int                 mPhysicalWidth;               // 실제 화면 가로 크기
    public int                 mPhysicalHeight;              // 실제 화면 세로 크기
    public static final int    mLogicalWidth  = 3840;        // 논리적인 가로 크기(인게임)
    public static final int    mLogicalHeight = 2160;        // 논리적인 세로 크기(인게임)

    /* 카메라 조작에 관한 변수들 */
    private PointF             mPtPrevTouch   = new PointF();    // 드래그에 사용할 좌표
    private PointF             ptActionUp     = new PointF();

    boolean                    modeZoom       = false;

    private static final float STEP           = 300;
    private float              mScale         = 1.0f;        // 화면 배율
    private float              mPrevScale;
    private int                mPrevDist;

    public GameCamera()
    {
    }

    /*
     * 실제 화면 크기를 설정한다.
     */
    public void setPhysicalSize(int width, int height)
    {
        mPhysicalWidth = width;
        mPhysicalHeight = height;
    }

    /*
     * 현재 화면에 보이는 대상이면 true를 반환한다.
     */
    public boolean isInScreen(Rect physicalBound)
    {
        boolean bHorizontal = (physicalBound.right >= 0) && (physicalBound.left < (mPhysicalWidth));
        boolean bVertical = (physicalBound.bottom >= 0) && (physicalBound.top < (mPhysicalHeight));

        if (bHorizontal && bVertical)
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

        output.left = x - mLeft;
        output.top = y - mTop;
        output.right = (x + width) - mLeft;
        output.bottom = (y + height) - mTop;
    }

    /*
     * 입력받은 x를 기준으로 실제로 화면상에 그려줄 x 좌표를 계산한다.
     */
    public int getDrawX(int x)
    {
        return x - mLeft;
    }

    /*
     * 입력받은 y를 기준으로 실제로 화면상에 그려줄 y 좌표를 계산한다.
     */
    public int getDrawY(int y)
    {
        return y - mTop;
    }

    public int getLeft()
    {
        return mLeft;
    }

    public int getTop()
    {
        return mTop;
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

    /**
     * <strong>false</strong>를 반환하는 경우, 이 함수를 호출한 곳에서 나머지 처리를 하면 된다.
     * 
     * @param event
     *            MotionEvent 클래스를 인수로 받는다.
     * @return 카메라에 관련된 터치(드래그 혹은 핀치 줌 인&아웃)가 제대로 처리되었으면 <strong>true</strong>를 반환하고,
     *         카메라 클래스에서 처리하지 않는 이벤트라면 <strong>false</strong>를 반환한다.
     */
    public boolean controlCamera(MotionEvent event)
    {

        float x = event.getX();
        float y = event.getY();

        float dx = 0f, dy = 0f; // 터치 이동 값

        String caption = "empty";

        // 손가락 두 개일 때
        if (event.getPointerCount() == 2)
        {
            modeZoom = true;
            zoomCamera(event);
        }
        else
        {
            switch (event.getAction())
            {
            case MotionEvent.ACTION_DOWN:
                modeZoom = false;

                mPtPrevTouch.x = x;
                mPtPrevTouch.y = y;
                caption = "action_down ";
                break;

            case MotionEvent.ACTION_MOVE:
                if (modeZoom == false)
                {
                    dx = x - mPtPrevTouch.x;
                    dy = y - mPtPrevTouch.y;
                    mPtPrevTouch.set(x, y);

                    slideCamera(dx, dy);

                    caption = "action_move ";
                }
                break;

            case MotionEvent.ACTION_UP:
                ptActionUp.x = x;
                ptActionUp.y = y;
                caption = "action_up ";
                break;

            default:
                return false;
            }
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
            mPrevScale = getmScale();
        }
        else if (action == MotionEvent.ACTION_MOVE)
        {
            float delta = (getDistance(event) - mPrevDist) / STEP;
            mScale = (Math.min(3.0f, Math.max(0.5f, mPrevScale + delta)));

            // Log.i("scale", "delta: " + delta + ", mScale: " + mScale);
        }
    }

    /*
     * 카메라의 좌상단 좌표를 설정한다.
     * 주의사항으로 카메라의 좌상단 좌표가 좌상단 한계점(0, 0)보다 작거나,
     * 카메라의 우하단 좌표(mLeft + mPhysicalWidth...)가 우하단 한계점(mLogicalWidth...)을 넘지 않도록 제한한다.
     */
    private void slideCamera(float dx, float dy)
    {
        mLeft = (int) (mLeft - dx);
        mTop = (int) (mTop - dy);

        if (dx > 0)
        {
            mLeft = Math.max(0, mLeft);
        }
        else if ((mLeft + mPhysicalWidth) > mLogicalWidth)
        {
            mLeft = mLogicalWidth - mPhysicalWidth;
        }

        if (dy > 0)
        {
            mTop = Math.max(0, mTop);
        }
        else if ((mTop + mPhysicalHeight) > mLogicalHeight)
        {
            mTop = mLogicalHeight - mPhysicalHeight;
        }
    }

    public float getmScale()
    {
        return mScale;
    }
}
