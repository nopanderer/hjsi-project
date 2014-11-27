package exam.androidproject;

/**
 * 설명
 * - 원소 배치 정보를 담고 있는 2차원 배열
 * 기능
 * - 원소 배치 정보를 담고 있는 6 by 8 2차원 함수
 * - LEFTTOP의 좌표 정보
 * - 가로 세로의 실제 크기 정보
 * 
 * @author HJ
 *
 */
public class ElementMatrix
{
    private static final int EMPTY      = 0;            // 0: 빈 자리
    private static final int RESERVED   = 1;            // 1: 타워가 이미 배치되어 있음
    private static final int RESTRICTED = 2;            // 2: 배치 불가능

    public int[][]           matrix     = new int[6][8];

    /**
     * 배열 초기화
     */
    public ElementMatrix()
    {
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = 0;
    }

    /**
     * 비어 있는지 확인하는 함수
     */
    public boolean isEmpty(int x, int y)
    {
        if (matrix[x][y] == EMPTY)
            return true;
        else
            return false;
    }

    /**
     * 배치하는 함수
     * 
     * @return 자리가 있거나 배치불가일 경우 false
     *         아니면 RESERVED로 바꾸고 true
     */
    public boolean deployElement(int x, int y)
    {
        if (matrix[x][y] == RESERVED || matrix[x][y] == RESTRICTED)
            return false;
        else
        {
            matrix[x][y] = RESERVED;
            return true;
        }
    }

}
