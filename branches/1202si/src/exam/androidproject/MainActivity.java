package exam.androidproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import exam.game.AppManager;

public class MainActivity extends BaseActivity
{
    // 액티비티 간 통신을 위한 요청코드 상수
    private static final int ACT_NEWGAME  = 0;
    private static final int ACT_CONTINUE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        AppManager.printSimpleLogInfo();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void mOnClick(View v)
    {
        Log.d(toString(), AppManager.getMethodName() + "() " + v.toString());

        switch (v.getId())
        {
        case R.id.btn_gamestart:
            Intent intentForNewGame = new Intent(MainActivity.this, Picturebook.class);
            startActivityForResult(intentForNewGame, ACT_NEWGAME);
            break;
        case R.id.btn_continue:
            Intent intentForContGame = new Intent(MainActivity.this, Continue.class);
            startActivityForResult(intentForContGame, ACT_CONTINUE);
            break;

        case R.id.btn_exit:
            // view가 alert 이면 팝업실행 즉 버튼을 누르면 팝업창이 뜨는 조건
            new AlertDialog.Builder(MainActivity.this).setTitle("게임종료") // 팝업창 타이틀바
                    .setMessage("종료하시겠습니까?")  // 팝업창 내용
                    .setPositiveButton("게임종료", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // TODO Auto-generated method stub
                            finish();
                        }
                    }).setNeutralButton("닫기", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dlg, int sumthin)
                        {
                            // 닫기 버튼을 누르면 아무것도 안하고 닫기 때문에 그냥 비움
                        }
                    }).show(); // 팝업창 보여줌
            break;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        AppManager.printSimpleLogInfo();
        switch (requestCode)
        {
        case ACT_NEWGAME:
        case ACT_CONTINUE:
            if (resultCode == RESULT_OK) {
                Intent intentForActMap = new Intent(getApplicationContext(), Map.class);
                startActivity(intentForActMap);
            }

            break;
        }
    }

    @Override
    protected void onDestroy()
    {
        AppManager.printSimpleLogInfo();
        super.onDestroy();
    }
}
