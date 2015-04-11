package hjsi.activity;

import hjsi.common.AppManager;
import hjsi.common.GameController;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ToggleButton;

public class DlgSetting extends Dialog implements OnClickListener {
  private Button resume, help, credits, quit;
  private ToggleButton sound;

  /**
   * 통신을 위한 컨트롤러
   */
  private GameController controller = null;

  public DlgSetting(Context context, GameController controller) {
    super(context);
    this.controller = controller;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLog();
    super.onCreate(savedInstanceState);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    setContentView(R.layout.dialog_setting);

    resume = (Button) findViewById(R.id.set_resume_btn);
    help = (Button) findViewById(R.id.set_help_btn);
    sound = (ToggleButton) findViewById(R.id.set_sound_btn);
    credits = (Button) findViewById(R.id.set_credits_btn);
    quit = (Button) findViewById(R.id.set_quit_btn);

    resume.setOnClickListener(this);
    sound.setOnClickListener(this);
    quit.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    AppManager.printDetailLog(v.toString());

    if (v == resume) {
      controller.resumeGame();

    } else if (v == sound) {
      controller.toggleSound();
      if (sound.isChecked())
        sound.setBackgroundDrawable(sound.getResources().getDrawable(R.drawable.img_set_soundoff));
      else
        sound.setBackgroundDrawable(sound.getResources().getDrawable(R.drawable.img_set_soundon));

    } else if (v == quit) {
      controller.quitGame();
      dismiss();
    }
  }

  /**
   * 뒤로가기 버튼을 누르면 resume과 똑같은 명령을 수행하도록 기존 메소드를 덮어썼다.
   */
  @Override
  public void onBackPressed() {
    AppManager.printSimpleLog();
    onClick(resume);
  }
}
