package exam.customwidget;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import exam.game.EDElement;

/**
 * 원소의 정보를 보여주는 뷰<br/>
 * <br/>
 * <<<할 일>>><br/>
 * 클릭 가능 여부 구현해야함.
 * 
 * @author 이상인
 */
public class ElementView extends RelativeLayout
{
    /* 생성자 (별도의 초기화는 반드시 init()을 통해서 해야한다!) */
    public ElementView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(null);
    }

    public ElementView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(null);
    }

    public ElementView(Context context)
    {
        super(context);
        init(null);
    }

    /* static 멤버 변수 */
    static private HashMap<String, RoundedDrawable> arDrawable;     // 리소스 보관용

    /* 기타 멤버 변수 */
    private EDElement                               mElement;       // 엘리먼트 자체
    private int                                     viewWidth;      // 뷰 자체의 가로 크기
    private int                                     viewHeight;     // 뷰 자체의 세로 크기
    private int                                     elementIconSize; // 화면 해상도, 사용하는 상황에 맞게 크기를 정해야함.
    private int                                     detailLevel;    // 자세히보기 레벨
    private LayoutParams                            mParam;         // 이 뷰의 레이아웃 속성

    /* 레이아웃 구성 요소들 */
    private ImageView                               mElementIcon;   // 엘리먼트 아이콘을 보여줌
    private TextView                                mName;          // 엘리먼트 등급과 이름을 표시함
    private TextView                                mDmg;           // 엘리먼트 공격력을 표시함
    private TextView                                mRate;          // 엘리먼트 공격속도를 표시함

    /* Identifiers of views */
    private enum viewId
    {
        ICON, NAME, DAMAGE, RATE, LAST;

        @SuppressWarnings("unused")
        public int getIndex()
        {
            return this.ordinal();
        }

        public int getId()
        {
            return this.ordinal() + 1;
        }
    }

    /* static 초기화 */
    static
    {
        arDrawable = new HashMap<String, RoundedDrawable>();
    }

    private void init(EDElement element)
    {
        // element가 null이면 기본값 Element를 할당한다. 게임 다 만들면 기본값 Element 쓸 일은 없음.
        if (element != null)
            mElement = element;
        else
            mElement = new EDElement();

        // 내부에 들어가는 뷰를 생성하고 id 값도 할당해줌
        mElementIcon = new ImageView(getContext());
        mElementIcon.setId(viewId.ICON.getId());
        mName = new TextView(getContext());
        mName.setId(viewId.NAME.getId());
        mDmg = new TextView(getContext());
        mDmg.setId(viewId.DAMAGE.getId());
        mRate = new TextView(getContext());
        mRate.setId(viewId.RATE.getId());

        // 기타 멤버 변수를 초기화함.
        viewWidth = Math.max(280, getWidth());
        viewHeight = Math.max(0, getHeight());
        elementIconSize = 180;
        detailLevel = 0;

        // ElementView 자체의 레이아웃 속성을 설정한다. (크기)
        mParam = new LayoutParams(Math.max(viewWidth, elementIconSize), Math.max(viewHeight, elementIconSize));
        setLayoutParams(mParam);
        setPadding(10, 10, 10, 10);
        setBackgroundColor(Color.argb(128, 255, 0, 0));// 테스트용 배경색

        // element의 각 속성 값들을 view를 통해 표시한다.
        updateText();
        // 그림만 보이는 상태로 초기화함
        changeDetailLevel(detailLevel);

        // 내부 레이아웃을 설정함.
        designInnerLayout();
    }

    /**
     * ElementView 내부의 레이아웃을 디자인한다.
     */
    private void designInnerLayout()
    {
        // 아이콘 표시부분 레이아웃
        LayoutParams lpIcon = new LayoutParams(elementIconSize, elementIconSize);
        lpIcon.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lpIcon.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        addView(mElementIcon, lpIcon);

        LayoutParams lpName = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams lpDmg = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams lpRate = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // 이름, 등급 표시부분 레이아웃
        // 아이콘의 밑에 붙여서 표시
        lpName.addRule(RelativeLayout.BELOW, viewId.ICON.getId());
        addView(mName, lpName);

        // 공격력 표시부분 레이아웃
        // 이름, 등급 밑에 표시
        lpDmg.addRule(RelativeLayout.BELOW, viewId.NAME.getId());
        addView(mDmg, lpDmg);

        // 공격속도 표시부분 레이아웃
        // 공격력 밑에 표시
        lpRate.addRule(RelativeLayout.BELOW, viewId.DAMAGE.getId());
        addView(mRate, lpRate);
    }

    public LayoutParams getParams()
    {
        return mParam;
    }

    public int getIconSize()
    {
        return elementIconSize;
    }

    /**
     * ElementView에 보여질 EDElement를 할당하여 이 뷰를 구성하는 위젯들에게 데이터를 설정함.
     * 
     * @param element
     *            이 뷰에 보여줄 원소
     */
    public void setElement(EDElement element)
    {
        if (element == null)
            Log.e("null error", "EDElement 요소가 null 값임!");
        else
            mElement = element;

        RoundedDrawable roundIcon;

        // drawable을 생성한 적이 있으면 새로 만들지 않고 가져다 쓴다.
        // 안하면 리스트뷰에서 렉걸림..
        if (arDrawable.containsKey(mElement.imgName))
        {
            roundIcon = arDrawable.get(mElement.imgName);
        }
        else
        {
            // 파일 이름으로 리소스 ID 구해서 비트맵 파일을 만든다
            int drawableId = getResources().getIdentifier(mElement.imgName, "drawable", "exam.androidproject");
            Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId);

            // 테스트용으로 사이즈 다른 이미지 대충 쓰니까 크기 조절해야함.
            if (bm.getWidth() != elementIconSize || bm.getHeight() != elementIconSize)
                bm = Bitmap.createScaledBitmap(bm, elementIconSize, elementIconSize, true);

            roundIcon = new RoundedDrawable(bm);
            // 한 번 생성한 드로블 객체는 맵 구조에 넣어서 보관함. 재활용을 위한 조치
            arDrawable.put(mElement.imgName, roundIcon);
        }

        mElementIcon.setImageDrawable(roundIcon);

        // 바뀐 것들 갱신
        updateText();
        updateHeight();
    }

    /**
     * Element의 속성을 보여주는 정도를 바꾼다.<br/>
     * 보여주는 양이 바뀌므로 뷰의 크기도 바뀐다.
     * 
     * @param detailLevel
     *            0~2 사이의 값을 설정한다.
     *            <ul>
     *            <li>0: 그림만 보여준다.</li>
     *            <li>1: 등급, 이름까지 보여준다.</li>
     *            <li>2: 공격력, 공격속도까지 보여준다.</li>
     *            </ul>
     */
    public void changeDetailLevel(int detailLevel)
    {
        this.detailLevel = detailLevel;
        switch (detailLevel)
        {
        case 0:
            mName.setVisibility(GONE);
            mDmg.setVisibility(GONE);
            mRate.setVisibility(GONE);
            break;

        case 1:
            mName.setVisibility(VISIBLE);
            mDmg.setVisibility(GONE);
            mRate.setVisibility(GONE);
            break;

        case 2:
            mName.setVisibility(VISIBLE);
            mDmg.setVisibility(VISIBLE);
            mRate.setVisibility(VISIBLE);
            break;
        }

        updateHeight();
    }

    /**
     * 현재 갖고 있는 EDElement의 내용으로 View를 갱신한다.
     */
    private void updateText()
    {
        mName.setText(mElement.toString());
        mDmg.setText("DMG: " + String.valueOf(mElement.dmg));
        mRate.setText("RATE: " + String.valueOf(mElement.rate));
    }

    /**
     * 세로 크기를 적당히 계산하여 ElementView의 크기를 변경함.
     */
    private void updateHeight()
    {
        mParam.height = Math.max(viewHeight, elementIconSize + (mName.getLineHeight() * 2) * detailLevel);
    }
}
