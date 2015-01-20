package exam.game;

import android.util.Log;
import android.view.SurfaceHolder;

/*
 * 게임을 진행시키는 인게임 스레드. 화면에 보이는지나 카메라에 관한 건 전혀 신경 쓸 필요 없다.
 */
public class GameMaster implements Runnable
{
    private final String  tag        = getClass().getSimpleName();

    /* 안드로이드 시스템 필요 멤버 변수 */
    Thread                mThreadMaster;                          // 게임 진행 스레드
    private boolean       mIsRunning = true;                      // 진행 스레드 동작 상태
    private boolean       mIsPlaying = false;                     // 게임 플레이 상태 (게임 중 or 일시정지)

    @SuppressWarnings("unused")
    private SurfaceHolder mHolder;                                // 동기화를 위해서 홀더를 갖는다.

    /* 게임 관련 변수 */
    GameState             mState;                                 // 현재 게임 정보

    public GameMaster(SurfaceHolder holder, GameState state)
    {
        mThreadMaster = new Thread(this);
        mThreadMaster.start();
        mHolder = holder; // 단순히 동기화용
        mState = state;
    }

    @Override
    public void run()
    {
        /* fps 계산을 위한 변수 */
        int fpsTargetFPS = 100; // 초당 목표 프레임 수
        int fpsRealFps = 0;
        long fpsTargetTime = 1000 / fpsTargetFPS; // 프레임당 목표 소요 시간
        long fpsStartTime;
        long fpsRealTime; // 프레임당 실제 소요 시간
        long fpsDelayTime; // 목표 소요시간 - 실제 소요시간만큼 스레드 sleep
        long fpsElapsedTime = 0L; // 1초 측정을 위한 변수

        while (mIsRunning)
        {
            while (mIsPlaying)
            {
                // 프레임 시작 시간을 구한다.
                fpsStartTime = System.currentTimeMillis();

                // 다른 스레드와 공통적으로 사용하는 mState를 동기화한다.
                synchronized (mState)
                {
                    if (mState.usedMob < 10)
                        mState.addMob();

                    for (Mob mob : mState.getMobs())
                    {
                        // 몹이 다 죽으면 게임 정지 및 새로운 웨이브 시작
                        if (mState.deadMob == 10)
                        {
                            mState.destroyMob();
                            nextWave();
                            pauseGame();
                            break;
                        }

                        // 몹이 죽지 않았고 2바퀴 돌았으면
                        else if (mob.lap == 2 && mob.dead == false)
                        {
                            mob.dead = true;
                            mState.curMob--;
                            mState.deadMob++;
                            continue;
                        }

                        // 몹이 생성되어 있다면 이동
                        else if (mob.created)
                            mob.move();
                    }

                    // 게임 로직 실행
                }

                /* 프레임 한 번의 소요 시간을 구해서 fps를 계산한다. */

                // 프레임당 실제 소요시간
                fpsRealFps++;
                fpsRealTime = (System.currentTimeMillis() - fpsStartTime);
                fpsElapsedTime += fpsRealTime;

                if (fpsElapsedTime >= 1000) // 1초마다 프레임율 갱신
                {

                    AppManager.getInstance().setLogicFps(fpsRealFps);
                    fpsRealFps = 0;
                    fpsElapsedTime = 0L;
                }

                /* 너무 빠르면 목표 시간만큼 딜레이 */
                try
                {
                    fpsDelayTime = fpsTargetTime - fpsRealTime;
                    fpsElapsedTime += fpsDelayTime;
                    if (fpsDelayTime > 0)
                    {
                        Thread.sleep(fpsDelayTime);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            // 게임이 일시정지 중일 땐 인게임 스레드의 cpu time을 양보시킨다.
            Thread.yield();
        }
        Log.i(tag, AppManager.getMethodName() + "() is end.");
    }

    /**
     * 게임을 종료할 때 호출한다. 게임 진행 스레드를 완전히 종료시킨다.
     */
    public void quitGame()
    {
        AppManager.printSimpleLogInfo();
        mIsRunning = false;

        try
        {
            mThreadMaster.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 게임을 시작한다.
     */
    public void playGame()
    {
        AppManager.printSimpleLogInfo();
        /*
         * 일시정지했다가 다시 시작하는건지, 한 웨이브가 끝난 후 새로운 웨이브를 시작하는건지 구별할 필요가 있다. (새로운 정보를 세팅하는 과정이 필요하니까)
         */
        mIsPlaying = true;
        // mThreadMaster.interrupt(); // 대기 중인 스레드 바로 깨우기 (되는지 모르겠음)
    }

    /**
     * 게임을 일시정지한다.
     */
    public void pauseGame()
    {
        AppManager.printSimpleLogInfo();
        mIsPlaying = false;
    }

    public void nextWave()
    {
        mState.makeFace(++mState.wave);
        mState.createMobs();
        mState.curMob = 0;
        mState.usedMob = 0;
        mState.deadMob = 0;

    }
}
