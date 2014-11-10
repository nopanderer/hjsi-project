package exam.androidproject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Map extends Activity
{
    Button  settingBtn;
    Setting setting;

    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        settingBtn = (Button) findViewById(R.id.setting_btn);
        setting = new Setting(Map.this);

        setting.setCanceledOnTouchOutside(false);

        setting.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface arg0)
            {
                // TODO Auto-generated method stub
                finish();
            }
        });

        settingBtn.setOnClickListener(new OnClickListener()
        {
            public void onClick(View arg0)
            {
                setting.show();
            }
        });

    }

}
