package com.cinema;

import com.cinema.ui.LoginFrame;

import javax.swing.*;

/**
 * 电影院售票管理系统 - 入口类
 *
 * 开发语言：Java (JDK 23+)
 * GUI技术：Java Swing
 * 数据库：MySQL + JDBC
 */
public class Main {
    public static void main(String[] args) {
        // 设置 Swing 外观为系统风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在事件调度线程中启动 GUI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
