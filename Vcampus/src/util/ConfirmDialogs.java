package util;

import javax.swing.*;
import java.awt.*;

/**
 * 删除/危险操作的统一确认对话框，避免各处自行拼 JOptionPane。
 */
public final class ConfirmDialogs {
    private ConfirmDialogs() {
    }

    /** 通用删除确认。parent 应传所属窗口/面板，避免 null 导致弹窗脱离父窗。 */
    public static boolean confirmDelete(Component parent, String what, String id) {
        String target = what;
        if (id != null && !id.isEmpty()) {
            target = what + "（" + id + "）";
        }
        String msg = "确定删除" + target + "？此操作不可撤销。";
        return JOptionPane.showConfirmDialog(parent, msg, "确认删除",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** 无具体 id 的删除确认。 */
    public static boolean confirmDelete(Component parent, String what) {
        return confirmDelete(parent, what, null);
    }
}
