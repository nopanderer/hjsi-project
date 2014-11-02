package exam.androidproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import exam.androidproject.R;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.gamestart_btn).setOnClickListener(mClickListener);
        findViewById(R.id.continue_btn).setOnClickListener(mClickListener);
        findViewById(R.id.exit_btn).setOnClickListener(mClickListener);
    }

    Button.OnClickListener mClickListener = new View.OnClickListener()
                                          {

                                              @Override
                                              public void onClick(View v)
                                              {
                                                  // TODO Auto-generated method stub
                                                  {
                                                      switch (v.getId())
                                                      {
                                                      case R.id.gamestart_btn:
                                                          Intent intent1 = new Intent(MainActivity.this, NewGame.class);
                                                          startActivity(intent1);
                                                          break;
                                                      case R.id.continue_btn:
                                                          Intent intent2 = new Intent(MainActivity.this, Continue.class);
                                                          startActivity(intent2);
                                                          break;

                                                      case R.id.exit_btn:
                                                          // view가 alert 이면 팝업실행 즉 버튼을 누르면 팝업창이 뜨는 조건
                                                          new AlertDialog.Builder(MainActivity.this).setTitle("게임종료") // 팝업창 타이틀바
                                                                  .setMessage("종료하시겠습니까?")  // 팝업창 내용
                                                                  .setPositiveButton("게임종료", new DialogInterface.OnClickListener()
                                                                  {
                                                                      @Override
                                                                      public void onClick(DialogInterface dialog, int which)
                                                                      {
                                                                          // TODO Auto-generated method stub
                                                                          System.exit(0);
                                                                      }
                                                                  }).setNeutralButton("닫기", new DialogInterface.OnClickListener()
                                                                  {
                                                                      public void onClick(DialogInterface dlg, int sumthin)
                                                                      {
                                                                          // 닫기 버튼을 누르면 아무것도 안하고 닫기 때문에 그냥 비움
                                                                      }
                                                                  }).show(); // 팝업창 보여줌
                                                          break;

                                                      }
                                                  }
                                              }
                                          };

}
