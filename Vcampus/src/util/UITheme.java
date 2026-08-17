package util;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * 全局 UI 主题：统一颜色、字体与 Look &amp; Feel，杜绝散落的魔法值。
 * 在程序入口 {@link App#main} 调用一次 {@link #setup()} 即可全局生效。
 */
public final class UITheme {

    // ---- 颜色 ----
    /** 主品牌蓝（统一替换原先 84,113,232 与 0x3874F7 两套冲突的蓝） */
    public static final Color PRIMARY = new Color(0x3874F7);
    public static final Color PRIMARY_HOVER = new Color(0x5085FA);
    /** 表头钢蓝（原 70,130,180） */
    public static final Color HEADER = new Color(0x4682B4);
    public static final Color DANGER = new Color(0xDC3545);
    public static final Color DANGER_HOVER = new Color(0xC82833);
    public static final Color SUCCESS = new Color(0x28A745);
    public static final Color SUCCESS_HOVER = new Color(0x1E8434);
    public static final Color APP_BG = new Color(0xE1E5EA);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color MUTED_TEXT = new Color(0x6B7280);
    public static final Color TABLE_GRID = new Color(220, 220, 220);
    public static final Color TABLE_SELECTION = new Color(0, 120, 215);

    // ---- 字体 ----
    public static final Font BODY = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font BOLD = new Font("微软雅黑", Font.BOLD, 14);
    public static final Font TITLE = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font SUBTITLE = new Font("微软雅黑", Font.PLAIN, 12);

    private UITheme() {
    }

    /** 安装 FlatLightLaf 并设置全局 UI 默认值。仅在程序入口调用一次。 */
    public static void setup() {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Table.showGrid", true);
        UIManager.put("Table.gridColor", TABLE_GRID);
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TextComponent.arc", 5);
        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("defaultFont", BODY);
    }
}
