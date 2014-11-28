package exam.androidproject;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

public class Camera
{
    // 이미지
    private Bitmap   Image;

    private float    X;
    private float    Y;

    private float    Width;
    private float    Height;

    // 처음 이미지를 선택했을 때, 이미지의 X,Y 값과 클릭 지점 간의 거리
    private float    offsetX;
    private float    offsetY;

    // 드래그시 좌표 저장

    int              posX1   = 0, posX2 = 0, posY1 = 0, posY2 = 0;

    // 핀치시 두좌표간의 거리 저장

    float            oldDist = 1f;
    float            newDist = 1f;

    // 드래그 모드인지 핀치줌 모드인지 구분
    static final int NONE    = 0;
    static final int DRAG    = 1;
    static final int ZOOM    = 2;

    int              mode    = NONE;

    // Image를 인자로 받는다.
    public Camera(Bitmap Image)
    {
        // TODO Auto-generated constructor stub
        this.Image = Image;

        setSize(Image.getHeight(), Image.getWidth());
        setXY(0, 0);

    }

    public void TouchProcess(MotionEvent event)
    {
        int act = event.getAction();
        switch (act & MotionEvent.ACTION_MASK)
        {
        case MotionEvent.ACTION_DOWN:    // 첫번째 손가락 터치
            posX1 = (int) event.getX();
            posY1 = (int) event.getY();
            offsetX = posX1 - X;
            offsetY = posY1 - Y;

            Log.d("zoom", "mode=DRAG");

            mode = DRAG;
            break;

        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG)
            {   // 드래그 중이면, 이미지의 X,Y값을 변환시키면서 위치 이동.
                X = posX2 - offsetX;
                Y = posY2 - offsetY;
                posX2 = (int) event.getX();
                posY2 = (int) event.getY();
                if (Math.abs(posX2 - posX1) > 20 || Math.abs(posY2 - posY1) > 20)
                {
                    posX1 = posX2;
                    posY1 = posY2;
                    Log.d("drag", "mode=DRAG");
                }

            }
            else if (mode == ZOOM)
            {    // 핀치줌 중이면, 이미지의 거리를 계산해서 확대를 한다.
                newDist = spacing(event);

                if (newDist - oldDist > 20)
                {  // zoom in
                    float scale = FloatMath.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (Height * Height + Width * Width));
                    Y = Y - (Height * scale / 2);
                    X = X - (Width * scale / 2);

                    Height = Height * (1 + scale);
                    Width = Width * (1 + scale);

                    oldDist = newDist;

                }
                else if (oldDist - newDist > 20)
                {  // zoom out
                    float scale = FloatMath.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (Height * Height + Width * Width));
                    scale = 0 - scale;
                    Y = Y - (Height * scale / 2);
                    X = X - (Width * scale / 2);

                    Height = Height * (1 + scale);
                    Width = Width * (1 + scale);

                    oldDist = newDist;
                }
            }
            break;

        case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
        case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
            mode = NONE;
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            // 두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
            mode = ZOOM;
            newDist = spacing(event);
            oldDist = spacing(event);
            Log.d("zoom", "newDist=" + newDist);
            Log.d("zoom", "oldDist=" + oldDist);
            Log.d("zoom", "mode=ZOOM");

            break;
        case MotionEvent.ACTION_CANCEL:
        default:
            break;
        }

    }

    // Rect 형태로 넘겨준다.
    public Rect getRect()
    {
        Rect rect = new Rect();
        rect.set((int) X, (int) Y, (int) (X + Width), (int) (Y + Height));
        return rect;
    }

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);

    }

    public void setSize(float Height, float Width)
    {
        this.Height = Height;
        this.Width = Width;

    }

    public void setXY(float X, float Y)
    {
        this.X = X;
        this.Y = Y;
    }

    public Bitmap getImage()
    {
        return Image;
    }
}
