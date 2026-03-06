/*
 * AI Agent的UI界面
 */
package client.ui;

import client.ai.*;
import common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class AgentPanel extends JPanel {
	private final User user;
    private final String agentTitle;
    private final AgentHooks hooks;

	private JTextPane chatPane;      // HTML 氣泡聊天區
	private JTextArea inputArea;     // 多行輸入框
    private JButton sendButton;
    private JLabel statusLabel; // 頂部狀態顯示
    private UseLlm llm;
    ConversationMemory conversationMemory = new ConversationMemory(10); // 只记录10条对话=五轮对话
    private static final String BASE_HTML = """
        <html>
        <head>
        <meta charset="UTF-8">
        <style type="text/css">
        /* Swing HTML 仅支持很少的属性，这里尽量保守 */
        body {
          font-family: 'Microsoft JhengHei UI', sans-serif;
          margin: 0; padding: 12px;
          background: transparent; /* 让下面的浅灰透上来 */
        }
        /* 整条消息容器（用 margin 做行间距） */
        .row { margin: 10px 0; }
        /* 气泡主体：用背景色 + 1px边模拟“气泡” */
        .cell {
          max-width: 78%;
          padding: 8px 10px;
          margin: 2px 8px;
          border: 1px solid #BFC5CD;
          background: #FFFFFF;
          white-space: normal; word-wrap: break-word; font-size: 13.5px; line-height: 1.6;
        }
        /* 我方气泡 */
        .cellme {
          max-width: 78%;
          padding: 8px 10px;
          margin: 2px 8px;
          background: #69cc1eff;
          white-space: normal; word-wrap: break-word; font-size: 13.5px; line-height: 1.6;
        }
        /* 时间行 */
        .meta { font-size: 10px; color: #9AA0A6; margin-top: 4px; }
        table { border-collapse: collapse; }
        td { vertical-align: top; }
        </style>
        </head>
        <body>
          <!-- 统一把所有消息塞进这个容器 -->
          <div id="thread"></div>
        </body>
        </html>
        """;

    // 通用构造函数
    public AgentPanel(User user, String title, AgentHooks hooks) {
        this.user = user;
        this.agentTitle = title != null ? title : "智能助理";
        this.hooks = hooks;
        initUiAndData();
    }
    
    public AgentPanel(User user) {
        this.user = user;
        this.agentTitle = "课务助理";
        this.hooks = new CourseAgentHooks(user); // ← 使用独立出来的 Hook
        initUiAndData();
    }        
    
    private void initUiAndData() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            Font uiFont = new Font("Microsoft JhengHei UI", Font.PLAIN, 13);
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) UIManager.put(key, uiFont);
            }
        } catch (Exception ignore) {}
        
        // LLM 实例
        llm = new UseLlm();
    
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        chatPane.setText(BASE_HTML);

        // 预加载（不同模块可以在 hooks 里拉数据）
        if (hooks != null) hooks.preload();

        appendBubble(false, hooks != null ? hooks.helpText() : CourseSelectionContext.HELP_TEXT);

        // 整个聊天面板的初始 HTML（右键“清空信息”和 /clear 时会用它重置）
            JScrollPane chatScroll = new JScrollPane(chatPane);
            chatScroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
            chatPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // 避免字体失效
            // 统一浅灰底（你可换回 0xF2F4F7，更淡）
            final Color APP_BG = new Color(0xE1E5EA);
            setBackground(APP_BG);
    
            // 关键：三处都要上底色
            chatPane.setOpaque(true);
            chatPane.setBackground(APP_BG);
    
            chatScroll.setOpaque(true);
            chatScroll.setBackground(APP_BG);
            chatScroll.getViewport().setOpaque(true);
            chatScroll.getViewport().setBackground(APP_BG);
    
            
            JPopupMenu chatMenu = new JPopupMenu();
            JMenuItem copyAll = new JMenuItem("复制全部");
            copyAll.addActionListener(e -> {
                chatPane.selectAll();
                chatPane.copy();
                chatPane.select(0,0);
            });
            JMenuItem clearChat = new JMenuItem("清空信息");
            clearChat.addActionListener(e -> { 
                chatPane.setText(BASE_HTML);          // 重置整页
                conversationMemory.clear();           // 清空对话上下文
                inputArea.requestFocusInWindow(); // 清空后聚焦
            });
            chatMenu.add(copyAll);
            chatMenu.add(clearChat);
            chatPane.setComponentPopupMenu(chatMenu);
    
            // 输入框
    //        inputField = new JTextField();
    //        inputField.addActionListener(e -> sendMessage());
            // FIND（从 inputArea = new JTextArea(3, 20); 开始直到键盘绑定 am.put("newline", ...) 结束的整个“输入框初始化”块）
            // REPLACE WITH:
            final String placeholder = "输入信息（Enter 送出、Shift+Enter 換行）";
            inputArea = new JTextArea(3, 20) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (!this.hasFocus() && this.getText().isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(new Color(150,150,150));
                        g2.drawString(placeholder, 10, 18);
                        g2.dispose();
                    }
                }
            };
            inputArea.setLineWrap(true);
            inputArea.setWrapStyleWord(true);
            inputArea.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
    
            // 鍵盤綁定：Enter 送出；Shift+Enter 換行
            InputMap im = inputArea.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap am = inputArea.getActionMap();
            im.put(KeyStroke.getKeyStroke("ENTER"), "send");
            im.put(KeyStroke.getKeyStroke("shift ENTER"), "newline");
            am.put("send", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButton.doClick();
                }
            });
            am.put("newline", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    inputArea.append("\n");
                }
            });
    
            JPopupMenu inputMenu = new JPopupMenu();
            JMenuItem paste = new JMenuItem("貼上");
            paste.addActionListener(e -> inputArea.paste());
            inputMenu.add(paste);
            inputArea.setComponentPopupMenu(inputMenu);
    
            // 送出按钮
            sendButton = new JButton("送出");
            sendButton.setPreferredSize(new Dimension(90, 36)); // 可调
            sendButton.addActionListener(e -> sendMessage());
            sendButton.setFocusPainted(false);
            sendButton.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
            sendButton.setBackground(new Color(0x22C55E)); // 亮绿
            sendButton.setForeground(Color.WHITE);
            sendButton.setOpaque(true);
    
            sendButton.addChangeListener(e -> {
                ButtonModel m = sendButton.getModel();
                if (!sendButton.isEnabled()) {
                    sendButton.setBackground(new Color(0x86EFAC));  // 禁用：浅绿
                } else if (m.isArmed() || m.isPressed()) {
                    sendButton.setBackground(new Color(0x16A34A));  // 按下：深绿
                } else {
                    sendButton.setBackground(new Color(0x22C55E));  // 默认：亮绿
                }
            });
    
            UIDefaults btnDef = new UIDefaults();
            btnDef.put("Button.contentAreaFilled", Boolean.TRUE);
            btnDef.put("Button[Enabled].backgroundPainter", null);
            btnDef.put("Button[Disabled].backgroundPainter", null);
            sendButton.putClientProperty("Nimbus.Overrides", btnDef);
            sendButton.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
    
    
    
            // 下方输入区布局
            JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            
            JScrollPane inputScroll = new JScrollPane(inputArea);
            inputScroll.setBorder(BorderFactory.createEmptyBorder()); // 去默认边
            JPanel inputCard = new JPanel(new BorderLayout());
            inputCard.setBackground(Color.WHITE);
            inputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE6,0xE8,0xEB)),
                BorderFactory.createEmptyBorder(4,8,4,8)
            ));
            inputCard.add(inputScroll, BorderLayout.CENTER);
            inputPanel.add(inputCard, BorderLayout.CENTER); // 用 inputCard 替换原来的 new JScrollPane(inputArea)
    
            
    //        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
    
            // 整体布局
            setLayout(new BorderLayout());
            // 浅灰背景（参考微信）
            setBackground(APP_BG);
            inputPanel.setBackground(APP_BG);
    
            
            JPanel header = new JPanel(new BorderLayout());
            header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            
            
    
            JLabel title = new JLabel(agentTitle);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
    
            JLabel subtitle = new JLabel("您好，" + (user != null ? String.valueOf(user.getUserId()) : "同学"));
            subtitle.setForeground(new Color(120,120,120));
    
            JPanel titleBox = new JPanel(new GridLayout(2,1,0,2));
            titleBox.setOpaque(false);
            titleBox.add(title);
            titleBox.add(subtitle);
    
            statusLabel = new JLabel("就绪");
            statusLabel.setForeground(new Color(120,120,120));
    
            header.add(titleBox, BorderLayout.WEST);
            header.add(statusLabel, BorderLayout.EAST);
    
            add(header, BorderLayout.NORTH);
            
            header.setBackground(new Color(0xF0,0xF3,0xF7));
            header.setOpaque(true);
            header.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0, new Color(0xE0,0xE3,0xE7)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            title.setForeground(new Color(0x1F,0x29,0x37));
            subtitle.setForeground(new Color(0x6B,0x72,0x80));
            statusLabel.setForeground(new Color(0x6B,0x72,0x80));
    
            
            // --- 聊天卡片容器（白底+描边+阴影） ---
            JPanel chatCard = new JPanel(new BorderLayout());
            chatCard.setBackground(APP_BG);
            // 只保留外边距，不要白色卡片和描边，让白色气泡直接“浮”在浅灰上
            chatCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
            chatCard.add(chatScroll, BorderLayout.CENTER);
    
            
            add(chatCard, BorderLayout.CENTER);
            add(inputPanel, BorderLayout.SOUTH);  // 下方输入
           
        }        

        
    
    private Timer typingTimer;
    private int ellipsis = 0;

    private void setSending(boolean sending) {
        // 輸入區與按鈕
        inputArea.setEnabled(!sending);
        inputArea.setFont(inputArea.getFont().deriveFont(14f));
        inputArea.setMargin(new Insets(8,10,8,10)); // 已有 border，可二选一
        
        sendButton.setEnabled(!sending);
        sendButton.setText(sending ? "发送中…" : "送出");

        // 頂部狀態
        if (statusLabel == null) return;
        if (sending) {
            if (typingTimer != null && typingTimer.isRunning()) typingTimer.stop();
            typingTimer = new Timer(400, e -> {
                ellipsis = (ellipsis + 1) % 4;
                statusLabel.setText("AI 正在输入" + ".".repeat(ellipsis));
            });
            typingTimer.start();
        } else {
            if (typingTimer != null) typingTimer.stop();
            statusLabel.setText("就绪");
        }
    }

    private void sendMessage() {
    	String text = inputArea.getText().trim();
        if(text.isEmpty()) return ;

   // 处理本地命令（不调用 LLM）
   if ("/clear".equalsIgnoreCase(text)) {
        appendBubble(true, text);
        inputArea.setText("");
        // 清 UI + 清记忆
        chatPane.setText(BASE_HTML);
        conversationMemory.clear();
        inputArea.requestFocusInWindow(); // 清空后聚焦
       return;
   }
   if ("/help".equalsIgnoreCase(text)) {
        appendBubble(true, text);
        inputArea.setText("");
        appendBubble(false, hooks != null ? hooks.helpText() : CourseSelectionContext.HELP_TEXT);
        return;
   }

        appendBubble(true, text);

//        chatArea.append("你: " + text + "\n");
        inputArea.setText("");
        setSending(true);
        
        // 保存记忆再请求
        conversationMemory.addUserMessage(text);

        // SwingWorker 后台调用API请求，防卡UI
        SwingWorker<String, Void> worker = new SwingWorker<String,Void>() {
        @Override
        protected String doInBackground() throws Exception {
            org.json.JSONArray messages = new org.json.JSONArray();

            // (1) 系統提示（規則/行為）
            messages.put(new org.json.JSONObject()
                    .put("role", "system")
                    .put("content", hooks != null ? hooks.systemPrompt()
                                                : AgentPrompts.courseSelectionSystemPrompt()));


            // (2) 访问数据库数据
            // messages.put(new org.json.JSONObject()
            //         .put("role", "system")
            //         .put("content", CourseSelectionContext.build(courses, lessons, myEnrollments, lessonTimes, enrolledMap)));
           messages.put(new org.json.JSONObject()
                   .put("role", "system")
                   .put("content", hooks != null ? hooks.buildDataset() : ""));            

            // (3) 歷史對話（Sliding Window）
            org.json.JSONArray hist = conversationMemory.getRecentMessages();
            for (int i = 0; i < hist.length(); i++) {
                messages.put(hist.getJSONObject(i));
            }
            // 更可读的调试输出：逐条打印 role + 原始 content（会真正换行）
            for (int i = 0; i < messages.length(); i++) {
                org.json.JSONObject m = messages.getJSONObject(i);
                System.out.println("[" + i + "] role=" + m.getString("role"));
                System.out.println(m.getString("content")); // 这里会显示真实换行
                System.out.println("----");
            }

            return llm.getResponse(messages);
        }

            
            @Override
            protected void done() {
                setSending(false);
                try {
                    String content = get();
                    conversationMemory.addAssistantMessage(content); // 保存 AI 回复
                    appendBubble(false, content);
                } catch(InterruptedException | ExecutionException ex) {
                	appendBubble(false, "发生错误：" + ex.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }
   
    private void appendBubble(boolean fromMe, String text) {
        // 先做轻量规整：去掉 **；把“行首的 -/*”和“句号/冒号后紧跟的 -”都转成换行点列；
        // 同时在“关于…”与“结论：”前断一行，让段落更清晰
        String norm = text
            .replace("**", "")
            // 行首 -/* -> •
            .replaceAll("(?m)^\\s*[-*]\\s+", "• ")
            // 冒号/句号/分号/逗号/右括号 后面的短横列表 -> 换行点列
            .replaceAll("([：。；，\\)])\\s*-\\s+", "$1\n• ")
            // 兜底：任意“ 空格-空格 ”也视作新条目（尽量少伤害句中连字符）
            .replaceAll("\\s+-\\s+", "\n• ")
            // 在“关于…”与“结论：”前断行，形成新段
            .replaceAll("\\s*•\\s*(关于)", "\n\n$1")
            .replaceAll("\\s*(结论：)", "\n$1");

        // 转义 + 压空行
        String safe = norm.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        safe = safe.replace("\r\n", "\n").replaceAll("\\n{3,}", "\n\n").trim();
        // 关键：Swing HTML 不支持 white-space: pre-wrap，需要把换行显式转成 <br/>
        safe = safe.replace("\n", "<br/>");

        String time = java.time.LocalTime.now().withNano(0).toString();
        String snippet;

        if (fromMe) {
            // 我方（右侧）
            snippet = ("""
            <div class="row me">
              <table width="100%%" cellpadding="0" cellspacing="0"><tr>
                <td></td>
                <td align="right"><div class="cellme">%s</div></td>
              </tr></table>
              <div class="meta" align="right">%s</div>
            </div>
            """).formatted(safe, time); // 注意：这里 table 的 100%% 是故意的（给 formatted 转义 %）
        } else {
            // AI（左侧）
            snippet = ("""
            <div class="row ai">
              <table width="100%%" cellpadding="0" cellspacing="0"><tr>
                <td align="left"><div class="cell">%s</div></td>
                <td></td>
              </tr></table>
              <div class="meta">%s</div>
            </div>
            """).formatted(safe, time);
        }

        try {
            String current = chatPane.getText();
            int idx = current.lastIndexOf("</div></body>"); // 先找 thread 结束
            if (idx < 0) idx = current.lastIndexOf("</body>");
            if (idx >= 0) {
                String updated = current.substring(0, idx) + snippet + current.substring(idx);
                chatPane.setText(updated);
                chatPane.setCaretPosition(chatPane.getDocument().getLength());
            }
        } catch (Exception ignore) {}
    }

}

