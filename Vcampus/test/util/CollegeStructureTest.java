package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CollegeStructure} 静态结构数据完整性测试。
 */
class CollegeStructureTest {

    @Test
    void sixCollegesDefined() {
        assertEquals(6, CollegeStructure.values().length);
    }

    @Test
    void namesAndIndices() {
        assertEquals("物理学院", CollegeStructure.WULI.getName());
        assertEquals("数学学院", CollegeStructure.SHUXUE.getName());
        assertEquals("计算机学院", CollegeStructure.JISUANJI.getName());
        assertEquals("化学学院", CollegeStructure.HUAXUE.getName());
        assertEquals("艺术学院", CollegeStructure.YISHU.getName());
        assertEquals("商学院", CollegeStructure.SHANG.getName());

        assertEquals(0, CollegeStructure.WULI.getIndex());
        assertEquals(5, CollegeStructure.SHANG.getIndex());
    }

    @Test
    void majorMatrixShape() {
        assertEquals(6, CollegeStructure.getMajor().length);
        assertEquals(6, CollegeStructure.getMajornum().length);
        assertEquals(6, CollegeStructure.secondaryStr.length);
    }

    @Test
    void computerScienceMajorsPresent() {
        String[] cs = CollegeStructure.getMajor()[2]; // JISUANJI = index 2
        boolean hasSoftware = false;
        for (String m : cs) {
            if ("软件工程".equals(m)) {
                hasSoftware = true;
                break;
            }
        }
        assertTrue(hasSoftware, "计算机学院应包含软件工程");
    }

    @Test
    void sexOptions() {
        assertEquals(2, CollegeStructure.getGradestr().length);
        assertEquals("男", CollegeStructure.getGradestr()[0]);
        assertEquals("女", CollegeStructure.getGradestr()[1]);
    }
}
