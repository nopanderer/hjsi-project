package exam.androidproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MobDBHelper extends SQLiteOpenHelper
{
    public MobDBHelper(Context context)
    {
        super(context, "Mob.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // TODO Auto-generated method stub
        String sql = "create table mob (" + "wave integer primary key, " + "hp integer, " + "step integer, " + "sleep integer);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO Auto-generated method stub
        String sql = "drop table if exists mob";
        db.execSQL(sql);

        // 테이블 지우고 다시 만듦
        onCreate(db);
    }

}
