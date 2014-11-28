package exam.androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * Map 비트맵을 그려줌
 * 
 * @author HJ
 *
 */
public class DrawMap extends View
{
    int            X, Y, Height, Width;
    private Camera cam;

    public DrawMap(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public void setImage(String ImagePath)
    {
        Bitmap Image = BitmapFactory.decodeResource(getResources(), R.drawable.img_map);

        cam = new Camera(Image);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // TODO Auto-generated method stub
        cam.TouchProcess(event);
        invalidate();
        return (true);
    }

    @Override
    public void draw(Canvas canvas)
    {
        // TODO Auto-generated method stub
        canvas.drawBitmap(cam.getImage(), null, cam.getRect(), null);

        super.draw(canvas);
    }
}
