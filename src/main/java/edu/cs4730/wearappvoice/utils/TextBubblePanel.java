package edu.cs4730.wearappvoice.utils;

import javax.swing.*;
import java.awt.*;

public class TextBubblePanel extends JPanel {
    private String fullText;
    private JLabel label;
    private final Image bg;
    private final int patch = 40;
    private final int maxCharsPerLine = 15;
    private final int maxLines = 3;
    private final Font font = new Font("宋体", Font.PLAIN, 20);

    public TextBubblePanel(String text) {
        this.fullText = truncateText(text);
        this.bg = new ImageIcon(getClass().getResource("/left_msg_nor.9.png")).getImage();

        setLayout(new BorderLayout());
        setOpaque(false);

        label = new JLabel(buildHtmlText(fullText));
        label.setFont(font);
        label.setForeground(Color.white);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));

        add(label, BorderLayout.CENTER);

        // 设置合适大小
        Dimension size = calculatePreferredSize(fullText);
        setPreferredSize(size);
    }

    // 将长文本裁剪为最多 maxLines 行，每行最多 maxCharsPerLine 字符，超出加省略号
    private String truncateText(String text) {
        int maxChars = maxLines * maxCharsPerLine;
        if (text.length() > maxChars) {
            return text.substring(0, maxChars - 1) + "…";
        }
        return text;
    }

    // 生成 HTML 格式内容，添加换行标签
    private String buildHtmlText(String text) {
        StringBuilder sb = new StringBuilder("<html><body style='width:auto;'>");
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if ((i + 1) % maxCharsPerLine == 0 && i != text.length() - 1) {
                sb.append("<br>");
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    // 计算背景尺寸
    private Dimension calculatePreferredSize(String text) {
        FontMetrics fm = getFontMetrics(font);
        int lineHeight = fm.getHeight();
        int lines = (int) Math.ceil(text.length() / (double) maxCharsPerLine);
        lines = Math.min(lines, maxLines);

        int width = fm.charWidth('测') * maxCharsPerLine + patch * 2;
        int height = lineHeight * lines + patch * 2;

        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int iw = bg.getWidth(null);
        int ih = bg.getHeight(null);
        int w = getWidth();
        int h = getHeight();

        // 四角
        g.drawImage(bg, 0, 0, patch, patch, 0, 0, patch, patch, this);
        g.drawImage(bg, w - patch, 0, w, patch, iw - patch, 0, iw, patch, this);
        g.drawImage(bg, 0, h - patch, patch, h, 0, ih - patch, patch, ih, this);
        g.drawImage(bg, w - patch, h - patch, w, h, iw - patch, ih - patch, iw, ih, this);

        // 边
        g.drawImage(bg, patch, 0, w - patch, patch, patch, 0, iw - patch, patch, this);
        g.drawImage(bg, patch, h - patch, w - patch, h, patch, ih - patch, iw - patch, ih, this);
        g.drawImage(bg, 0, patch, patch, h - patch, 0, patch, patch, ih - patch, this);
        g.drawImage(bg, w - patch, patch, w, h - patch, iw - patch, patch, iw, ih - patch, this);

        // 中
        g.drawImage(bg, patch, patch, w - patch, h - patch, patch, patch, iw - patch, ih - patch, this);
    }

    public void setTextContent(String content) {
        this.fullText = truncateText(content);
        label.setText(buildHtmlText(fullText));
        Dimension newSize = calculatePreferredSize(fullText);
        this.setPreferredSize(newSize);
        this.revalidate();  // 通知父容器更新布局
        this.repaint();     // 重新绘制背景
    }

}
