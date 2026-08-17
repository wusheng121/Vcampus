package util;

/**
 * HTML 转义工具，统一替代散落在 JournalNavigationPanel / TimetablePanel / AgentPanel 的重复实现。
 */
public final class Html {
    private Html() {
    }

    /** 转义 &lt; &gt; &amp; " 防止内容被当作 HTML 标签解析（Swing HTML 注入等价）。 */
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
