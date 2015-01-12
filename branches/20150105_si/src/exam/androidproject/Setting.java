package exam.androidproject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ToggleButton;
import exam.game.AppManager;

public class Setting extends Dialog implements OnClickListener {
  private Button resume, help, credits, quit;
  private ToggleButton sound;

  public Setting(Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppManager.printSimpleLogInfo();
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
    Log.d(toString(), AppManager.getMethodName() + "() " + v.toString());

    if (v == resume) {
      hide();
    } else if (v == sound) {
      if (sound.isChecked()) {
        sound.setBackgroundDrawable(sound.getResources().getDrawable(R.drawable.img_set_soundoff));
        Map.music.pause();
      } else {
        sound.setBackgroundDrawable(sound.getResources().getDrawable(R.drawable.img_set_soundon));
        Map.music.start();
      }
    } else if (v == quit) {
      Map.music.stop();
      Map.music.release();
      Map.music = null;

      dismiss();
    }
  }

  @Override
  public void onBackPressed() {
    AppManager.printSimpleLogInfo();
    hide();
  }
}
