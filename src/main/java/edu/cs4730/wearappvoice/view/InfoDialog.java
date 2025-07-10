package edu.cs4730.wearappvoice.view;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InfoDialog {

    private JDialog dialog;
    private JFrame parent;

    public InfoDialog(JFrame parent) {
        this.parent = parent;
    }

    public void showInfoDialog(List<InfoItem> infoItemList) {
        dialog = new JDialog(parent, "信息列表", true);
        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(parent);

        // 列表内容
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (InfoItem item : infoItemList) {
            listModel.addElement(item.getTitle() + ": " + item.getDescription());
        }

        JList<String> jList = new JList<>(listModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(jList);

        // 设置布局
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);

        // 添加关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.add(closeButton, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public void dismiss() {
        if (dialog != null && dialog.isVisible()) {
            dialog.dispose();
        }
    }

    // 内部类模拟 InfoAdapter.InfoItem
    public static class InfoItem {
        private String title;
        private String description;

        public InfoItem(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
