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

    private View page1;
    private View page2;

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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이얼로그 제목 표시를 없앤다
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 다이얼로그 배경을 없앤다 (테두리도 없음)
        setContentView(R.layout.dialog_store);

        // 구매를 취소하거나 구매 후 "계속 구매", "배치"도 누르지 않는 경우를 위한 리스너
        setOnCancelListener(new OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                // 구매 버튼을 다시 누를 수 있게 팝업에서 표시할 창을 바꿔주는 것
                page1.setVisibility(View.VISIBLE);
                page2.setVisibility(View.INVISIBLE);
            }
        });

        page1 = findViewById(R.id.dlg_store_goods_page1); // 원소 구매 확인창
        page2 = findViewById(R.id.dlg_store_goods_page2); // 구매한 원소 표시 및 계속 구매할지 배치할지 묻는 창

        // 각 버튼들(두 개의 창에 두 개의 버튼)에 리스너를 등록한다.
        ((TextView) findViewById(R.id.dlg_store_btn_buy)).setOnClickListener(this);
        ((TextView) findViewById(R.id.dlg_store_btn_cancel)).setOnClickListener(this);
        ((TextView) findViewById(R.id.dlg_store_btn_morebuy)).setOnClickListener(this);
        ((TextView) findViewById(R.id.dlg_store_btn_arrangement)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.dlg_store_btn_buy: // 정말로 원소 구매할지 확인하는 창의 구매 버튼
            page1.setVisibility(View.INVISIBLE);   // 현재 창을 숨기고
            page2.setVisibility(View.VISIBLE);     // 다음 창을 보여줌

            // TODO 구매 관련 메소드를 작성한다

            // 테스트용으로 기본 element 만들어서 뷰에 설정한다.
            ElementView ev = (ElementView) findViewById(R.id.dlg_store_goods_element);
            ev.setElement(new EDElement());
            ev.changeDetailLevel(1);
            break;

        case R.id.dlg_store_btn_cancel: // 취소 버튼을 누르는 경우
            hide();
            break;

        case R.id.dlg_store_btn_morebuy: // 더 구매하려는 경우
            // TODO 위의 element 구매 관련 메소드를 실행하면 될듯.
            break;

        case R.id.dlg_store_btn_arrangement: // 구매한 원소를 바로 배치하려는 경우다
            // TODO 현재 구매해서 나온 원소를 맵에 바로 배치할 수 있게 처리한다
            break;
        }
    }
}
