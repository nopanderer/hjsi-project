package hjsi.activity;

import hjsi.common.AppManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

/**
 * 기존의 액티비티 리스트는 AppManager 클래스로 옮김. 이 클래스를 상속하는 액티비티들은 모두 자동으로 AppManager 클래스에 등록과 삭제를 자동으로 하게 된다.
 */
@SuppressLint("Registered")
public class Base extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppManager.getInstance().addActivity(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    AppManager.getInstance().removeActivity(this);
  }

  /**
   * 로그 출력용으로 클래스의 이름을 반환함
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
