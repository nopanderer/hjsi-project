package exam.customwidget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import exam.androidproject.R;
import exam.game.EDElement;

/**
 * ListView에 들어가는 한 줄짜리 View. <br/>
 * 한 가지의 조합법을 보여주는 역할을 한다.
 * 
 * @author 이상인
 * 
 */
public class RecipeView extends RelativeLayout implements View.OnClickListener
{
    /* 생성자 */
    public RecipeView(Context context)
    {
        super(context);
        init();
    }

    public RecipeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public RecipeView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    /* static 멤버 변수 */
    private static Drawable drawablePlus;
    private static Drawable drawableEqual;
    private static boolean  flag             = false;

    /* 멤버 변수 */
    private int             detailLevel      = 1;                 // Element의 이름까지 보이는 상태를 나타냄

    /* Views of layout */
    private ImageView       mImgPlus;
    private ImageView       mImgEqual;
    /**
     * 조합이 되는 재료인 두 개의 Element와 결과물 Element 한 개를 담는 변수다. <br/>
     * <ul>
     * <b>index 사용법</b>
     * <li>0: 왼쪽 재료 Element</li>
     * <li>1: 오른쪽 재료 Element</li>
     * <li>2: 오른쪽 재료 Element</li>
     * </ul>
     */
    private ElementView     arElementViews[] = new ElementView[3];

    /* Identifiers of views */
    private enum viewId
    {
        LHS1, LHS2, RESULT, IMG_PLUS, IMG_EQUAL, LAST;

        public int getIndex()
        {
            return this.ordinal();
        }

        public int getId()
        {
            return this.ordinal() + 1;
        }
    }

    private void init()
    {
        // 레이아웃에 들어가는 뷰를 생성하고 id 값도 할당해줌
        mImgPlus = new ImageView(getContext());
        mImgPlus.setId(viewId.IMG_PLUS.getId());
        mImgEqual = new ImageView(getContext());
        mImgEqual.setId(viewId.IMG_EQUAL.getId());

        // ElementView들을 이름까지 보이는 상태로 초기화한다.
        for (int i = 0; i < arElementViews.length; i++)
        {
            arElementViews[i] = new ElementView(getContext());
            arElementViews[i].setId(viewId.LHS1.getId() + i);
            arElementViews[i].changeDetailLevel(detailLevel);
        }

        // 더하기, 등호 리소스 불러옴
        if (flag == false)
        {
            // 전체 객체 중에서 객체를 처음 생성 할 때 수행함.
            drawablePlus = getResources().getDrawable(R.drawable.recipeview_plus_img);
            drawableEqual = getResources().getDrawable(R.drawable.recipeview_equal_img);
            flag = true;
        }
        mImgPlus.setImageDrawable(drawablePlus);
        mImgEqual.setImageDrawable(drawableEqual);

        setPadding(10, 10, 10, 10);

        // 레이아웃 설정
        designInnerLayout();

        // onClick 메소드를 등록함
        setOnClickListener(this);
    }

    /**
     * RecipeView 내부의 레이아웃을 디자인한다.
     */
    private void designInnerLayout()
    {
        int plusSize = 120;

        // 레이아웃 파라미터를 초기화
        LayoutParams lp[] = new LayoutParams[viewId.LAST.getIndex()];

        /* RecipeView에 각 view를 등록시킴 */
        // 왼쪽 재료 Element 설정
        int idx = viewId.LHS1.getIndex();
        lp[idx] = arElementViews[idx].getParams();
        lp[idx].addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(arElementViews[idx]);

        // + 이미지 설정
        idx = viewId.IMG_PLUS.getIndex();
        lp[idx] = new LayoutParams(plusSize, plusSize);
        lp[idx].addRule(RelativeLayout.RIGHT_OF, viewId.LHS1.getId());
        lp[idx].addRule(RelativeLayout.ALIGN_TOP, viewId.LHS1.getId());
        lp[idx].setMargins(10, 0, 10, 0);
        lp[idx].topMargin = (arElementViews[viewId.LHS1.getId()].getIconSize() - plusSize) / 2;
        addView(mImgPlus, lp[idx]);

        // 오른쪽 재료 Element 설정
        idx = viewId.LHS2.getIndex();
        lp[idx] = arElementViews[idx].getParams();
        lp[idx].addRule(RelativeLayout.RIGHT_OF, viewId.IMG_PLUS.getId());
        addView(arElementViews[idx]);

        // = 이미지 설정
        idx = viewId.IMG_EQUAL.getIndex();
        lp[idx] = new LayoutParams(plusSize, plusSize);
        lp[idx].addRule(RelativeLayout.RIGHT_OF, viewId.LHS2.getId());
        lp[idx].addRule(RelativeLayout.ALIGN_TOP, viewId.LHS2.getId());
        lp[idx].setMargins(10, 0, 10, 0);
        lp[idx].topMargin = (arElementViews[viewId.LHS2.getId()].getIconSize() - plusSize) / 2;
        addView(mImgEqual, lp[idx]);

        // 조합결과 Element 설정
        idx = viewId.RESULT.getIndex();
        lp[idx] = arElementViews[idx].getParams();
        lp[idx].addRule(RelativeLayout.RIGHT_OF, viewId.IMG_EQUAL.getId());
        addView(arElementViews[idx]);
    }

    /**
     * @param arElements
     *            하나의 리스트뷰 항목(O+O=O)에 들어갈 EDElement 객체 3개의 배열
     */
    public void setElements(EDElement[] arElements)
    {
        for (int i = 0; i < 3; i++)
        {
            arElementViews[i].setElement(arElements[i]);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (detailLevel == 1)
            detailLevel = 2;
        else
            detailLevel = 1;

        // 세 개의 Element의 자세히보기 상태를 전부 변경한다.
        for (ElementView ev : arElementViews)
        {
            ev.changeDetailLevel(detailLevel);
        }
    }
}
