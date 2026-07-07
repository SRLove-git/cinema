package com.cinema.ui;

import com.cinema.entity.User;
import com.cinema.service.UserService;

import javax.swing.*;
import java.awt.*;

/**
 * 用户登录界面
 */
public class LoginFrame extends JFrame {

    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("影院管理系统 - 登录");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("影院管理系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 122, 183));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("密  码:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.addActionListener(e -> doLogin());

        JButton registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.addActionListener(e -> doRegister());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 回车键触发登录
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * 执行登录操作
     */
    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名和密码", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userService.login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "登录成功！欢迎 " + user.getRealName(), "提示", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); // 关闭登录窗口
            EventQueue.invokeLater(() -> new MainFrame(user).setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 弹出注册对话框
     */
    private void doRegister() {
        JDialog dialog = new JDialog(this, "用户注册", true);
        dialog.setSize(380, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // 注册表单字段
        JTextField regUsernameField = new JTextField(15);
        JPasswordField regPasswordField = new JPasswordField(15);
        JTextField regRealNameField = new JTextField(15);
        JTextField regPhoneField = new JTextField(15);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(regUsernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(regPasswordField, gbc);

        // 真实姓名
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("真实姓名:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(regRealNameField, gbc);

        // 手机号
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("手机号:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(regPhoneField, gbc);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton saveButton = new JButton("注册");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String regUsername = regUsernameField.getText().trim();
            String regPassword = new String(regPasswordField.getPassword()).trim();
            String regRealName = regRealNameField.getText().trim();
            String regPhone = regPhoneField.getText().trim();

            if (regUsername.isEmpty() || regPassword.isEmpty() || regRealName.isEmpty() || regPhone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写所有字段", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User newUser = new User();
            newUser.setUsername(regUsername);
            newUser.setPassword(regPassword);
            newUser.setRealName(regRealName);
            newUser.setPhone(regPhone);

            boolean success = userService.register(newUser);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "注册成功，请登录", "提示", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "用户名已存在", "注册失败", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveButton);
        btnPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
