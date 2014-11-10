package exam.androidproject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class Setting extends Dialog implements OnClickListener
{
    private Button resume, help, sound, credits, quit;

    public Setting(Context context)
    {
        super(context);
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_setting);

        resume = (Button) findViewById(R.id.set_resume_btn);
        help = (Button) findViewById(R.id.set_help_btn);
        sound = (Button) findViewById(R.id.set_sound_btn);
        credits = (Button) findViewById(R.id.set_credits_btn);
        quit = (Button) findViewById(R.id.set_quit_btn);

        resume.setOnClickListener(this);
        quit.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        if (v == resume)
        {
            hide();
        }
        else if (v == quit)
        {
            dismiss();

        }

    }

}
