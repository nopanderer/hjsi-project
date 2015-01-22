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

public class GoodsView extends LinearLayout {
  public GoodsView(Context context, OnClickListener l, int goodsId) {
    super(context);
    setOnClickListener(l);
    this.goodsId = goodsId;
    init();
  }

  public GoodsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public GoodsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  /* 클래스 공용 자원 static 변수 */
  private static Bitmap bmpTreasure; // 원소 상자 이미지를 지금은 공통으로 써서 static으로 해뒀는데,
                                     // 나중에 등급별로 다른 이미지를 쓰면 static으로 하면 안됨
  private static boolean flag = false; // 처음에 한 번 생성을 해서 원소 상자 이미지를 로딩 시켰는지 체크

  /* view id (지금은 필요는 없음) */
  private static final int IMG_GOODS = 0;
  private static final int TXT_NAME = 1;
  private static final int TXT_VALUE = 2;
  /* view */
  private ImageView imgGoods; // 상품 이미지
  public TextView txtCaption; // 상품명
  private int goodsId; // 상품 종류를 구별할 식별자 (하급 상자인지 타워 재건설인지 등)
  private TextView txtValue; // 가격 표시용 텍스트
  private String valueUnit = "G"; // 가격 단위
  private int value = 1000; // 상품 가격

  private void init() {
    imgGoods = new ImageView(getContext());
    imgGoods.setId(IMG_GOODS); // 지금 당장은 안 쓰는데 일단 써둠. 나머지 view도 마찬가지

    /* 이 뷰를 한 번도 안 만들었다면 원소 상자 이미지를 불러와서 static 변수에 저장 */
    if (flag == false) {
      bmpTreasure =
          BitmapFactory.decodeResource(getResources(), R.drawable.img_goodsview_treasurebox);
      bmpTreasure = Bitmap.createScaledBitmap(bmpTreasure, 400, 300, true);
      flag = true;
    }
    imgGoods.setImageBitmap(bmpTreasure);

    // 나머지 뷰 생성
    txtCaption = new TextView(getContext());
    txtCaption.setId(TXT_NAME);
    txtValue = new TextView(getContext());
    txtValue.setId(TXT_VALUE);

    // 뷰 속성이나 위치 등 자세한 설정
    designInnerLayout();
  }

  private void designInnerLayout() {
    DisplayMetrics screen = getResources().getDisplayMetrics(); // 화면 크기 구하기 위한 객체

    setBackgroundColor(Color.DKGRAY);
    setOrientation(LinearLayout.VERTICAL);
    setWeightSum(5); // 리니어 레이아웃의 크기를 5로 잡고, 나머지 차일드 뷰들의 크기를 5분의 1이나 5분의 2와 같은 식으로 설정

    float scaleWidth = 1f / 4f; // 하나의 아이템 버튼이 화면에서 차지하는 비율

    LayoutParams lp =
        new LayoutParams((int) (screen.widthPixels * scaleWidth), screen.heightPixels * 3 / 5);
    lp.weight = 3; // 부모 뷰의 weightSum 값에서 차지하는 비율 (imgGoods는 3/5)
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
    txtValue.setText(String.valueOf(value) + valueUnit); // 가격 + "단위" 형식으로 표시함
    txtValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
    txtValue.setGravity(Gravity.CENTER);
    addView(txtValue, lp);
  }

  // 이미지 제외한 상품명, 가격, 화폐단위 설정
  public void setProperties(String caption, String valueUnit, int value) {
    setCaption(caption);
    setValueUnit(valueUnit);
    setValue(value);
  }

  public void setCaption(String caption) {
    txtCaption.setText(caption);
  }

  public void setValue(int value) {
    this.value = value;
    txtValue.setText(String.valueOf(value) + valueUnit);
  }

  public void setValueUnit(String unit) {
    valueUnit = unit;
    txtValue.setText(String.valueOf(value) + valueUnit);
  }

  public String getCaption() {
    return txtCaption.getText().toString();
  }

  public int getValue() {
    return value;
  }

  public int getGoodsId() {
    return goodsId;
  }
}
