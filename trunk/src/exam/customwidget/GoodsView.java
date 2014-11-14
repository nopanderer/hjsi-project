package exam.customwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import exam.androidproject.R;

public class GoodsView extends LinearLayout
{
    public GoodsView(Context context, OnClickListener l, int id)
    {
        super(context);
        setOnClickListener(l);
        this.id = id;
        init();
    }

    public GoodsView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public GoodsView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    /* 클래스 공용 자원 static 변수 */
    private static Bitmap    bmpTreasure;
    private static boolean   flag      = false;

    /* view id */
    private static final int IMG_GOODS = 0;
    private static final int TXT_NAME  = 1;
    private static final int TXT_VALUE = 2;
    /* view */
    private ImageView        imgGoods;
    public TextView          txtCaption;
    private int              id;
    private TextView         txtValue;
    private String           valueUnit = "G";
    private int              value     = 1000;

    private void init()
    {
        imgGoods = new ImageView(getContext());
        imgGoods.setId(IMG_GOODS);

        if (flag == false)
        {
            bmpTreasure = BitmapFactory.decodeResource(getResources(), R.drawable.goodsview_treasurebox_img);
            bmpTreasure = Bitmap.createScaledBitmap(bmpTreasure, 400, 300, true);
            flag = true;
        }
        imgGoods.setImageBitmap(bmpTreasure);

        txtCaption = new TextView(getContext());
        txtCaption.setId(TXT_NAME);
        txtValue = new TextView(getContext());
        txtValue.setId(TXT_VALUE);

        designInnerLayout();
    }

    private void designInnerLayout()
    {
        DisplayMetrics screen = getResources().getDisplayMetrics();

        setBackgroundColor(Color.DKGRAY);
        setOrientation(LinearLayout.VERTICAL);
        setWeightSum(5);

        float scaleWidth = 1f / 4f;

        LayoutParams lp = new LayoutParams((int) (screen.widthPixels * scaleWidth), screen.heightPixels * 3 / 5);
        lp.weight = 3;
        addView(imgGoods, lp);

        lp = new LayoutParams((int) (screen.widthPixels * scaleWidth), screen.heightPixels * 1 / 5);
        lp.weight = 1;
        txtCaption.setText("하급");
        txtCaption.setBackgroundColor(Color.RED);
        txtCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        txtCaption.setGravity(Gravity.CENTER);
        addView(txtCaption, lp);

        lp = new LayoutParams((int) (screen.widthPixels * scaleWidth), screen.heightPixels * 1 / 5);
        lp.weight = 1;
        txtValue.setText(String.valueOf(value) + valueUnit);
        txtValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
        txtValue.setGravity(Gravity.CENTER);
        addView(txtValue, lp);
    }

    public void setProperties(String caption, String valueUnit, int value)
    {
        setCaption(caption);
        setValueUnit(valueUnit);
        setValue(value);
    }

    public void setCaption(String caption)
    {
        txtCaption.setText(caption);
    }

    public void setValue(int value)
    {
        this.value = value;
        txtValue.setText(String.valueOf(value) + valueUnit);
    }

    public void setValueUnit(String unit)
    {
        valueUnit = unit;
    }

    public String getCaption()
    {
        return txtCaption.getText().toString();
    }

    public int getValue()
    {
        return value;
    }

    public int getId()
    {
        return id;
    }
}
