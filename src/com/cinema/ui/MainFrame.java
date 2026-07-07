package com.cinema.ui;

import com.cinema.entity.User;

import javax.swing.*;
import java.awt.*;

/**
 * 系统主界面框架
 */
public class MainFrame extends JFrame {

    private final User user;
    private final JTabbedPane tabbedPane;
    private final OrderPanel orderPanel;

    public MainFrame(User user) {
        this.user = user;

        // 窗口基本设置
        setTitle("影院票务管理系统");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 使用 BorderLayout 布局
        setLayout(new BorderLayout());

        // 顶部面板：系统标题 + 当前用户信息
        add(createTopPanel(), BorderLayout.NORTH);

        // 主体：JTabbedPane
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // 添加标签页
        orderPanel = new OrderPanel(user);
        initTabs();

        // 标签页切换监听，切到订单管理时自动刷新
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                String title = tabbedPane.getTitleAt(selectedIndex);
                if ("订单管理".equals(title)) {
                    orderPanel.refresh();
                }
            }
        });

        // 设置窗口可见
        setVisible(true);
    }

    /**
     * 创建顶部面板，显示系统标题和当前用户信息
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(30, 60, 110));
        topPanel.setPreferredSize(new Dimension(1200, 60));

        // 系统标题
        JLabel titleLabel = new JLabel("影院票务管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 用户信息
        String roleText;
        switch (user.getRole()) {
            case "admin": roleText = "管理员"; break;
            case "staff": roleText = "员工"; break;
            case "customer": roleText = "顾客"; break;
            default: roleText = user.getRole();
        }
        JLabel userLabel = new JLabel("当前用户：" + user.getRealName() + "（" + roleText + "）");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));
        topPanel.add(userLabel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * 初始化标签页
     */
    private void initTabs() {
        tabbedPane.addTab("电影管理", new MoviePanel(user));
        tabbedPane.addTab("放映计划", new SchedulePanel(user));
        tabbedPane.addTab("售票管理", new TicketPanel(user));
        tabbedPane.addTab("订单管理", orderPanel);

        // 仅管理员（及员工）可见的标签页
        if ("admin".equals(user.getRole()) || "staff".equals(user.getRole())) {
            tabbedPane.addTab("影厅管理", new HallPanel(user));
        }
        // 仅管理员可见
        if ("admin".equals(user.getRole())) {
            tabbedPane.addTab("用户管理", new UserPanel(user));
        }
    }
}
