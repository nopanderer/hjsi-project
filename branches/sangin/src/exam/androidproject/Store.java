package exam.androidproject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import exam.customwidget.GoodsView;

/**
 * @author 이상인
 * 
 */
public class Store extends Activity implements View.OnClickListener
{
    /* 상점 아이템들의 ID 상수 목록 */
    // 등급별 원소 상자
    private static final int LOW     = 0;
    private static final int MIDDLE  = 1;
    private static final int HIGH    = 2;
    private static final int SPECIAL = 3;
    private static final int LEGEND  = 4;
    // 타워 관련 아이템
    private static final int REPAIR  = LEGEND + 1; // 체력 회복
    private static final int UPGRADE = REPAIR + 1; // 최대 체력 상승
    private static final int REBUILD = UPGRADE + 1; // 재건설

    DlgStore                 dlgStore;             // 상점 내 아이템 클릭시 뜰 팝업창

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_store);

        // 뒤로가기 버튼 기능 구현
        // TODO 아래의 중복되는 코드 없앨 수 있을듯
        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        // 구매확인 팝업창 생성
        dlgStore = new DlgStore(this);

        // 상점 아이템별 탭화면 초기화
        initTabElement(); // 원소 상자
        initTabTower(); // 타워 아이템
    }

    private void initTabTower()
    {
        LinearLayout vg = (LinearLayout) findViewById(R.id.store_tab2_layout);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.rightMargin = 30;

        GoodsView gv = new GoodsView(this, this, REPAIR);
        gv.setProperties("타워 체력 회복", "원", 3000);
        gv.txtCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        vg.addView(gv, lp);

        gv = new GoodsView(this, this, UPGRADE);
        gv.setProperties("최대 체력 증가", "원", 10000);
        gv.txtCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        vg.addView(gv, lp);

        gv = new GoodsView(this, this, REBUILD);
        gv.setProperties("타워 재건설", "원", 20000);
        gv.txtCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        vg.addView(gv, lp);
    }

    private void initTabElement()
    {
        LinearLayout vg = (LinearLayout) findViewById(R.id.store_tab1_layout);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.rightMargin = 30;

        GoodsView gv = new GoodsView(this, this, LOW);
        gv.setProperties("하급", "G", 1000);
        vg.addView(gv, lp);

        gv = new GoodsView(this, this, MIDDLE);
        gv.setProperties("중급", "G", 2500);
        vg.addView(gv, lp);

        gv = new GoodsView(this, this, HIGH);
        gv.setProperties("상급", "G", 5000);
        vg.addView(gv, lp);

        gv = new GoodsView(this, this, SPECIAL);
        gv.setProperties("특별", "G", 10000);
        vg.addView(gv, lp);

        gv = new GoodsView(this, this, LEGEND);
        gv.setProperties("전설", "G", 20000);
        vg.addView(gv, lp);
    }

    /**
     * 상점의 탭을 바꾸는 메소드
     */
    public void switchTab(View v)
    {
        // 일단 전부 안 보이게 한다
        findViewById(R.id.store_tab1).setVisibility(View.INVISIBLE);
        findViewById(R.id.store_tab2).setVisibility(View.INVISIBLE);
        findViewById(R.id.store_tab3).setVisibility(View.INVISIBLE);

        // 그리고 한 놈만 보여준다
        switch (v.getId())
        {
        case R.id.store_btn_elementbox:
            findViewById(R.id.store_tab1).setVisibility(View.VISIBLE);
            break;
        case R.id.store_btn_tower:
            findViewById(R.id.store_tab2).setVisibility(View.VISIBLE);
            break;
        case R.id.store_btn_cashonly:
            findViewById(R.id.store_tab3).setVisibility(View.VISIBLE);
            break;
        }
    }

    @Override
    public void onClick(View v)
    {
        GoodsView gv = (GoodsView) v;
        Log.i("info", "caption: " + gv.getCaption());

        switch (gv.getGoodsId())
        {
        case LOW:
            dlgStore.show(); // 구매 확인창을 띄운다
            Log.i("info", "value: " + gv.getValue());
            break;

        case MIDDLE:
            break;

        case HIGH:
            break;
        }
    }
}
