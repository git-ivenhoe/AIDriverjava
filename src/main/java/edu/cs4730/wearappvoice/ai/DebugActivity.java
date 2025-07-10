package edu.cs4730.wearappvoice.ai;

import edu.cs4730.wearappvoice.utils.Constants;
import edu.cs4730.wearappvoice.utils.Handler;
import edu.cs4730.wearappvoice.utils.Log;
import edu.cs4730.wearappvoice.utils.Looper;
import edu.cs4730.wearappvoice.voice.Message;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class DebugActivity extends JDialog {

    private DefaultListModel<Message> listModel;
    private JList<Message> messageList;
    private JTextField inputField;
    private JButton sendButton;

    public DebugActivity(Frame parent) {
        super(parent, "调试对话框", false);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 初始化列表
        listModel = new DefaultListModel<>();
        messageList = new JList<>(listModel);
        messageList.setCellRenderer(new MessageCellRenderer());
        JScrollPane scrollPane = new JScrollPane(messageList);
        add(scrollPane, BorderLayout.CENTER);

        // 输入框和按钮
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("发送");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // 加载历史消息
        loadMessages();

        // 发送按钮事件
        sendButton.addActionListener((ActionEvent e) -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(text, true, false, false, null);
                msg.AddAndUpdateList();
                listModel.addElement(msg);
                inputField.setText("");
                Log.i("DebugDialog", "模拟广播: " + text);
            }
        });

        // 模拟消息处理线程
        new Thread(() -> {
            Looper.prepare();
            Constants.debugHandle = new Handler() {
                @Override
                public void handleMessage(edu.cs4730.wearappvoice.utils.Message msg) {
                    switch (msg.what) {
                        case Constants.MESSAGE_UPDATE_MSGLIST:
                            loadMessages();
                            break;
                        case Constants.MESSAGE_SET_PLAY_VOICE:
                            playVoiceMessage(msg.obj.toString());
                            break;
                    }
                }
            };
            Looper.loop();
        }).start();
    }

    // 重新加载对话内容
    private void loadMessages() {
        listModel.clear();
        List<Message> allMessages = Constants.dialogMessage;
        for (Message msg : allMessages) {
            listModel.addElement(msg);
        }
    }

    // 播放语音
    private void playVoiceMessage(String path) {
        try {
            File audioFile = new File(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            pause(true);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    pause(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "播放失败：" + path);
            pause(false);
        }
    }

    private void pause(boolean state) {
        if (Constants.speechService != null) {
            Constants.speechService.setPause(state);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DebugActivity dialog = new DebugActivity(null);
            dialog.setVisible(true);
        });
    }

    // 自定义渲染器
    static class MessageCellRenderer extends JPanel implements ListCellRenderer<Message> {

        private ImageIcon userIcon;
        private ImageIcon aiIcon;
        private Font font;

        public MessageCellRenderer() {
            setOpaque(true);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // 设置统一字体
            font = new Font("宋体", Font.PLAIN, 16);

            // 加载图标
            userIcon = loadIcon("user.png");  // 请确认 user.png 在资源路径中
            aiIcon = loadIcon("robotIcon2.png");      // 请确认 ai.png 在资源路径中
        }

        private ImageIcon loadIcon(String name) {
            java.net.URL imgURL = getClass().getResource("/" + name);
            if (imgURL != null) {
                return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            } else {
                System.err.println("图标加载失败: " + name);
                return null;
            }
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {
            // 清空面板内容
            this.removeAll();

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(msg.isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
            panel.setOpaque(false);

            JLabel iconLabel = new JLabel(msg.isUser ? userIcon : aiIcon);

            JTextArea textArea = new JTextArea(msg.text);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(font);
            textArea.setEditable(false);
            textArea.setOpaque(true);
            textArea.setBackground(msg.isUser ? new Color(220, 255, 220) : new Color(240, 240, 255));
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // 设置最大宽度并自动换行
//            textArea.setMaximumSize(new Dimension(list.getWidth() - 100, Integer.MAX_VALUE));
            // 不使用 JLabel 默认的 wrap 控制，让宽度更大
            textArea.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));
            textArea.setPreferredSize(null);
            textArea.setSize(textArea.getPreferredSize());
            if (msg.isUser) {
                panel.add(textArea);
                panel.add(iconLabel);
            } else {
                panel.add(iconLabel);
                panel.add(textArea);
            }

            this.add(panel, BorderLayout.CENTER);

            return this;
        }
    }
}
