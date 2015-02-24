package hjsi.activity;

import hjsi.common.AppManager;
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
   * 부모 액티비티와 통신하기 위한 레퍼런스
   */
  private Game gameAct = null;;

  /*
   * 상수 목록
   */
  public static final int DLG_BTN_RESUME = 0;
  public static final int DLG_BTN_SOUND = 1;
  public static final int DLG_BTN_QUIT = 2;

  public DlgSetting(Context context, Game gameAct) {
    super(context);
    this.gameAct = gameAct;
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
      hide();
      gameAct.handleDialog(DLG_BTN_RESUME);

    } else if (v == sound) {
      gameAct.handleDialog(DLG_BTN_SOUND);
      if (sound.isChecked())
        sound.setBackgroundDrawable(sound.getResources().getDrawable(R.drawable.img_set_soundoff));
      else
        sound.setBackgroundDrawable(sound.getResources().getDrawable(R.drawable.img_set_soundon));

    } else if (v == quit) {
      dismiss();
      gameAct.handleDialog(DLG_BTN_QUIT);
    }
  }

  @Override
  public void onBackPressed() {
    AppManager.printSimpleLog();
    hide();
  }
}
