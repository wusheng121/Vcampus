package util;

import common.model.LessonTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 上课时间工具：格式化 LessonTime 列表为友好字符串
 */
public class LessonTimeUtil {

    private static final String[] WEEK = {"一","二","三","四","五","六","日"};

    /**
     * 将一组 LessonTime 格式化为中文字符串，例如：
     * 周一 第1-2节；周三 第3-4节
     *
     * @param list LessonTime 列表
     * @return 格式化后的字符串
     */
    public static String formatTimes(List<LessonTime> list) {
        if (list == null || list.isEmpty()) return "";

        List<LessonTime> copy = new ArrayList<>(list);
        copy.sort(Comparator
                .comparingInt(LessonTime::getDayOfWeek)
                .thenComparingInt(LessonTime::getStartSec));

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < copy.size()) {
            int dow = copy.get(i).getDayOfWeek();
            int start = copy.get(i).getStartSec();
            int end   = copy.get(i).getEndSec();

            // 合并同一天连续节次（假设 startSec/endSec 表示“第几节”）
            int j = i + 1;
            while (j < copy.size()
                    && copy.get(j).getDayOfWeek() == dow
                    && copy.get(j).getStartSec() == end + 1) {
                end = copy.get(j).getEndSec();
                j++;
            }

            if (sb.length() > 0) sb.append("；");
            String weekStr = (dow >= 1 && dow <= 7) ? WEEK[dow - 1] : String.valueOf(dow);
            sb.append("周").append(weekStr)
              .append(" 第").append(start).append("-").append(end).append("节");

            i = j;
        }
        return sb.toString();
    }
}
