package client.ui;
import util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageCarouselPanel extends JPanel {
    /** 预加载为 BufferedImage 缓存，避免 paintComponent 每帧重新光栅化 */
    private final List<BufferedImage> images = new ArrayList<>();
    private int currentIndex = 0;
    private int nextIndex = 0;

    private float alpha = 1f; // 当前图片透明度
    private Timer fadeTimer;  // 控制淡入淡出
    private Timer autoTimer;  // 自动轮播

    private JPanel dotsPanel;
    private List<JLabel> dots = new ArrayList<>();

    public ImageCarouselPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        loadImages();
        initDots();

        // 左右箭头
        JButton leftBtn = new JButton("◀");
        JButton rightBtn = new JButton("▶");
        styleArrowButton(leftBtn);
        styleArrowButton(rightBtn);
        leftBtn.addActionListener(e -> prevImage());
        rightBtn.addActionListener(e -> nextImage());

        // 小圆点 + 箭头容器
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(leftBtn);
        for (JLabel dot : dots) bottomPanel.add(dot);
        bottomPanel.add(rightBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // 自动轮播（仅在可见时运行，见 addNotify/removeNotify）
        autoTimer = new Timer(3000, e -> nextImage());

        // 淡入淡出Timer
        fadeTimer = new Timer(30, e -> {
            alpha += 0.1f;
            if (alpha >= 1f) {
                alpha = 1f;
                fadeTimer.stop();
                currentIndex = nextIndex;
            }
            repaint();
        });
    }

    /** 面板加入窗口时启动轮播 */
    @Override
    public void addNotify() {
        super.addNotify();
        if (autoTimer != null && !autoTimer.isRunning()) autoTimer.start();
    }

    /** 面板移出窗口时停止所有 timer，避免不可见时仍消耗 CPU */
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (autoTimer != null) autoTimer.stop();
        if (fadeTimer != null) fadeTimer.stop();
    }

    /** 加载图片为 BufferedImage（缺失资源跳过，不 NPE） */
    private void loadImages() {
        String[] paths = {
                "/pictures/全国科普月.jpg",
                "/pictures/迎新.jpg",
                "/pictures/文化自信.jpg",
                "/pictures/梅庵.jpg",
                "/pictures/学习.jpg"
        };
        for (String path : paths) {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("[ImageCarousel] 缺失资源: " + path);
                continue;
            }
            Image img = new ImageIcon(url).getImage();
            BufferedImage bi = new BufferedImage(
                    Math.max(1, img.getWidth(null)),
                    Math.max(1, img.getHeight(null)),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            images.add(bi);
        }
    }

    /** 初始化小圆点 */
    private void initDots() {
        dots.clear();
        dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        dotsPanel.setOpaque(false);
        for (int i = 0; i < images.size(); i++) {
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("Dialog", Font.PLAIN, 14));
            dot.setForeground(i == 0 ? UITheme.PRIMARY : Color.LIGHT_GRAY);
            dots.add(dot);
        }
    }

    private void updateDots() {
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setForeground(i == nextIndex ? UITheme.PRIMARY : Color.LIGHT_GRAY);
        }
    }

    private void prevImage() {
        if (images.isEmpty()) return;
        nextIndex = (currentIndex - 1 + images.size()) % images.size();
        startFade();
    }

    private void nextImage() {
        if (images.isEmpty()) return;
        nextIndex = (currentIndex + 1) % images.size();
        startFade();
    }

    private void startFade() {
        alpha = 0f;
        updateDots();
        if (!fadeTimer.isRunning()) fadeTimer.start();
    }

    /** 箭头按钮样式 */
    private void styleArrowButton(JButton btn) {
        btn.setFont(new Font("Dialog", Font.BOLD, 24));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 0, 0, 120));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setToolTipText("上一张/下一张");
    }

    /** 绘制图片（淡入淡出效果）。直接对缓存的 BufferedImage 做 drawImage 缩放，避免每帧创建临时图。 */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (images.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        BufferedImage curImg = images.get(currentIndex);
        BufferedImage nextImg = images.get(nextIndex);

        // 按面板尺寸等比缩放（保留长宽比，不硬编码比例）
        double imgRatio = (double) curImg.getWidth() / (double) curImg.getHeight();
        int newWidth = w;
        int newHeight = (int) (newWidth / imgRatio);
        if (newHeight > h) {
            newHeight = h;
            newWidth = (int) (newHeight * imgRatio);
        }
        int x = (w - newWidth) / 2;
        int y = (h - newHeight) / 2;

        // 绘制当前图片（淡出）
        if (alpha < 1f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - alpha));
            g2d.drawImage(curImg, x, y, newWidth, newHeight, null);
        }

        // 绘制下一张图片（淡入）
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(nextImg, x, y, newWidth, newHeight, null);

        g2d.dispose();
    }
}
