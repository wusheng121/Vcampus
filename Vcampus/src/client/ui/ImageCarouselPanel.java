package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageCarouselPanel extends JPanel {
    private List<ImageIcon> images = new ArrayList<>();
    private int currentIndex = 0;
    private int nextIndex = 0;

    private float alpha = 1f; // 当前图片透明度
    private Timer fadeTimer;  // 控制淡入淡出
    private Timer autoTimer;  // 自动轮播

    private JPanel dotsPanel;
    private List<JLabel> dots = new ArrayList<>();
    private JButton leftBtn, rightBtn;

    public ImageCarouselPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        loadImages();
        initDots();

        // 左右箭头
        leftBtn = new JButton("◀");
        rightBtn = new JButton("▶");
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

        // 显示第一张
        if (!images.isEmpty()) repaint();

        // 自动轮播
        autoTimer = new Timer(3000, e -> nextImage());
        autoTimer.start();

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

    /** 加载图片 */
    private void loadImages() {
        String[] paths = {
                "/pictures/全国科普月.jpg",
                "/pictures/迎新.jpg",
                "/pictures/文化自信.jpg",
                "/pictures/梅庵.jpg",
                "/pictures/学习.jpg"
        };
        for (String path : paths) {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            images.add(icon);
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
            dot.setForeground(i == 0 ? new Color(84, 113, 232) : Color.LIGHT_GRAY);
            dots.add(dot);
        }
    }

    private void updateDots() {
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setForeground(i == nextIndex ? new Color(84, 113, 232) : Color.LIGHT_GRAY);
        }
    }

    private void prevImage() {
        nextIndex = (currentIndex - 1 + images.size()) % images.size();
        startFade();
    }

    private void nextImage() {
        nextIndex = (currentIndex + 1) % images.size();
        startFade();
    }

    private void startFade() {
        alpha = 0f;
        updateDots();
        fadeTimer.start();
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
    }

    /** 绘制图片（淡入淡出效果） */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (images.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // 当前图片
        ImageIcon current = images.get(currentIndex);
        BufferedImage curImg = new BufferedImage(current.getIconWidth(), current.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gCur = curImg.createGraphics();
        gCur.drawImage(current.getImage(), 0, 0, null);
        gCur.dispose();

        // 下张图片
        ImageIcon next = images.get(nextIndex);
        BufferedImage nextImg = new BufferedImage(next.getIconWidth(), next.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gNext = nextImg.createGraphics();
        gNext.drawImage(next.getImage(), 0, 0, null);
        gNext.dispose();

        // 缩放保持比例
        double targetRatio = 3840.0 / 1800.0;
        int newWidth = w;
        int newHeight = (int) (newWidth / targetRatio);
        if (newHeight > h) {
            newHeight = h;
            newWidth = (int) (newHeight * targetRatio);
        }
        int x = (w - newWidth) / 2;
        int y = (h - newHeight) / 2;

        // 绘制当前图片
        if (alpha < 1f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - alpha));
            g2d.drawImage(curImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), x, y, null);
        }

        // 绘制下一张图片
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(nextImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), x, y, null);

        g2d.dispose();
    }
}
