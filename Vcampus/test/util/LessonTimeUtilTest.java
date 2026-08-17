package util;

import common.model.LessonTime;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link LessonTimeUtil#formatTimes} 单元测试：
 * 空输入、单条、同日连续合并、跨日排序、非连续不合并。
 */
class LessonTimeUtilTest {

    private LessonTime lt(int dow, int start, int end) {
        return new LessonTime(0, 0, dow, start, end, null);
    }

    @Test
    void nullOrEmptyReturnsEmptyString() {
        assertEquals("", LessonTimeUtil.formatTimes(null));
        assertEquals("", LessonTimeUtil.formatTimes(Collections.emptyList()));
    }

    @Test
    void singleTimeSlot() {
        List<LessonTime> list = Collections.singletonList(lt(1, 1, 2));
        assertEquals("周一 第1-2节", LessonTimeUtil.formatTimes(list));
    }

    @Test
    void sameDayConsecutiveMerged() {
        // 周一 第1-2节 + 第3-4节：3 == 2+1 → 合并为 第1-4节
        List<LessonTime> list = Arrays.asList(lt(1, 1, 2), lt(1, 3, 4));
        assertEquals("周一 第1-4节", LessonTimeUtil.formatTimes(list));
    }

    @Test
    void sameDayNonConsecutiveNotMerged() {
        // 周一 第1-2节 + 第5-6节：5 != 2+1 → 不合并
        List<LessonTime> list = Arrays.asList(lt(1, 1, 2), lt(1, 5, 6));
        assertEquals("周一 第1-2节；周一 第5-6节", LessonTimeUtil.formatTimes(list));
    }

    @Test
    void crossDaySortedAndJoined() {
        // 乱序输入：周二在前、周一在后 → 按天排序后输出
        List<LessonTime> list = Arrays.asList(lt(2, 3, 4), lt(1, 1, 2));
        assertEquals("周一 第1-2节；周二 第3-4节", LessonTimeUtil.formatTimes(list));
    }

    @Test
    void sundayIsDaySeven() {
        List<LessonTime> list = Collections.singletonList(lt(7, 1, 1));
        assertEquals("周日 第1-1节", LessonTimeUtil.formatTimes(list));
    }
}
