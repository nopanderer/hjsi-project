package exam.androidproject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import exam.customwidget.ElementView;
import exam.game.EDElement;

public class DlgStore extends Dialog implements OnClickListener
{

    private View     page1;
    private View     page2;
    private TextView btnLeft;
    private TextView btnRight;

    public DlgStore(Context context)
    {
        super(context);
    }

    public DlgStore(Context context, int theme)
    {
        super(context, theme);
    }

    public DlgStore(Context context, boolean cancelable, OnCancelListener cancelListener)
    {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_store);

        setOnCancelListener(new OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                page1.setVisibility(View.VISIBLE);
                page2.setVisibility(View.INVISIBLE);
            }
        });

        page1 = findViewById(R.id.dlg_store_goods_page1);
        page2 = findViewById(R.id.dlg_store_goods_page2);
        btnLeft = (TextView) findViewById(R.id.dlg_store_btn_buy);
        btnLeft.setOnClickListener(this);
        btnRight = (TextView) findViewById(R.id.dlg_store_btn_cancel);
        btnRight.setOnClickListener(this);

        ((TextView) findViewById(R.id.dlg_store_btn_morebuy)).setOnClickListener(this);
        ((TextView) findViewById(R.id.dlg_store_btn_arrangement)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.dlg_store_btn_buy:
            page1.setVisibility(View.INVISIBLE);
            page2.setVisibility(View.VISIBLE);

            // TODO 구매 관련 메소드를 작성한다
            ElementView ev = (ElementView) findViewById(R.id.dlg_store_goods_element);
            ev.setElement(new EDElement());
            ev.changeDetailLevel(1);
            break;

        case R.id.dlg_store_btn_cancel:
            hide();
            // TODO 이 블럭은 배치를 누른 경우이므로 맵 액티비티로 빠져나가야됨
            break;
        }
    }
}
