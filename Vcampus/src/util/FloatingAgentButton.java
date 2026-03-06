// + 文件：src/util/FloatingAgentButton.java
package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * 在任意容器右下角放一个悬浮按钮（圆形或图标）。
 * 用法：
 *   FloatingAgentButton.Handle h = FloatingAgentButton.attach(parentPanel, onClick, icon, 56, 16);
 *   add(h.layeredPane, BorderLayout.CENTER);     // 把返回的 layeredPane 加到界面上
 *   h.button.setVisible(true/false);             // 需要时控制显示/隐藏
 */
public class FloatingAgentButton {

    /** 返回句柄：包含承载的 JLayeredPane 和按钮本体，便于外部控制可见性等 */
    public static class Handle {
        public final JLayeredPane layeredPane;
        public final JButton button;
        private Handle(JLayeredPane lp, JButton b) { this.layeredPane = lp; this.button = b; }
    }

    public static Handle attach(JComponent content,
                                Runnable onClick,
                                Icon icon,       // 可为 null，则用 "🤖" 文本兜底
                                int size,        // 按钮边长（正方形）
                                int margin) {    // 距离右/下边距
        // 用分层面板把 content 包起来
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        lp.add(content, JLayeredPane.DEFAULT_LAYER);
        content.setBounds(0, 0, content.getPreferredSize().width, content.getPreferredSize().height);

        // 悬浮按钮
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("打开智能客服");

        // 图标：等比平滑缩放，避免“变形/可怕”
        if (icon != null) {
            Image img = ((ImageIcon) icon).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } else {
            btn.setText("🤖");
            btn.setFont(btn.getFont().deriveFont(22f));
        }

        // 只有在「不是拖动」的情况下才触发点击
        btn.putClientProperty("allowClick", Boolean.TRUE);
        btn.addActionListener(e -> {
            if (Boolean.TRUE.equals(btn.getClientProperty("allowClick"))) {
                if (onClick != null) onClick.run();
            }
        });

        lp.add(btn, JLayeredPane.PALETTE_LAYER);
        btn.setSize(size, size);

        // 初始放右下角
        lp.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                content.setBounds(0, 0, lp.getWidth(), lp.getHeight());
                // 如果按钮还没被拖过，就贴右下角
                if (btn.getClientProperty("dragged") == null) {
                    btn.setLocation(lp.getWidth() - size - margin, lp.getHeight() - size - margin);
                }
            }
        });

        // === 拖动逻辑 ===
        final Point[] offset = {null};
        final Point[] pressPt = {null};
        final int DRAG_THRESHOLD = 1; // 像素：超过就视为拖动（不会触发点击）

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                offset[0] = e.getPoint();
                pressPt[0] = e.getPoint();
                btn.putClientProperty("allowClick", Boolean.TRUE); // 先假定允许点击
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                offset[0] = null;
                // 如果之前判定过是拖动，这里显式取消按钮“armed/pressed”，防止误触
                if (!Boolean.TRUE.equals(btn.getClientProperty("allowClick"))) {
                    ButtonModel m = btn.getModel();
                    m.setArmed(false);
                    m.setPressed(false);
                }                
            }
        });
        btn.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] != null) {
                    int newX = btn.getX() + e.getX() - offset[0].x;
                    int newY = btn.getY() + e.getY() - offset[0].y;
                    // 限制不超出容器
                    newX = Math.max(0, Math.min(newX, lp.getWidth() - btn.getWidth()));
                    newY = Math.max(0, Math.min(newY, lp.getHeight() - btn.getHeight()));
                    btn.setLocation(newX, newY);
                    btn.putClientProperty("dragged", true); // 标记已拖动
                    // 判断是否为拖动：位移是否超过阈值
                    int dx = e.getX() - pressPt[0].x;
                    int dy = e.getY() - pressPt[0].y;
                    if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) {
                        btn.putClientProperty("allowClick", Boolean.FALSE);
                    }
                }
            }
        });        

        return new Handle(lp, btn);
    }

    public static JButton createButton(Runnable onClick, Icon icon, int size) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("打开智能客服");

        if (icon != null) {
            Image img = ((ImageIcon) icon).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } else {
            btn.setText("🤖");
            btn.setFont(btn.getFont().deriveFont(22f));
        }

        btn.addActionListener(e -> { if (onClick != null) onClick.run(); });
        btn.setSize(size, size);
        return btn;
    }

}
