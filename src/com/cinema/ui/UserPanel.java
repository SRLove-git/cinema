package com.cinema.ui;

import com.cinema.entity.User;
import com.cinema.service.UserService;
import com.cinema.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户管理面板
 */
public class UserPanel extends JPanel {

    private final User currentUser;
    private final UserService userService = new UserService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JTable userTable;
    private DefaultTableModel tableModel;

    private static final String[] COLUMN_NAMES = {"ID", "用户名", "真实姓名", "电话", "角色", "创建时间"};

    public UserPanel(User user) {
        this.currentUser = user;
        initUI();
        loadUserData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ========== 顶部：添加 + 刷新 ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnAdd = new JButton("添加用户");
        JButton btnRefresh = new JButton("刷新");
        topPanel.add(btnAdd);
        topPanel.add(btnRefresh);

        // ========== 中间：表格 ==========
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // ========== 底部：编辑 + 删除 ==========
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnEdit = new JButton("编辑");
        JButton btnDelete = new JButton("删除");
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);

        // 组装
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ========== 事件绑定 ==========
        btnAdd.addActionListener(e -> showAddDialog());
        btnRefresh.addActionListener(e -> loadUserData());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteUser());
    }

    /** 加载用户数据到表格 */
    private void loadUserData() {
        tableModel.setRowCount(0);
        List<User> list = userService.findAll();
        for (User u : list) {
            String roleDisplay;
            switch (u.getRole()) {
                case Constants.ROLE_ADMIN:
                    roleDisplay = "管理员";
                    break;
                case Constants.ROLE_STAFF:
                    roleDisplay = "员工";
                    break;
                case Constants.ROLE_CUSTOMER:
                    roleDisplay = "顾客";
                    break;
                default:
                    roleDisplay = u.getRole();
            }
            String createdAt = u.getCreatedAt() != null ? u.getCreatedAt().format(dtf) : "";
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getRealName(),
                    u.getPhone(),
                    roleDisplay,
                    createdAt
            });
        }
    }

    /** 获取当前选中的用户，若未选中返回 null */
    private User getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个用户", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        return userService.findById(id);
    }

    // ==================== 添加 ====================

    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "添加用户", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField tfUsername = new JTextField(18);
        JPasswordField pfPassword = new JPasswordField(18);
        JTextField tfRealName = new JTextField(18);
        JTextField tfPhone = new JTextField(18);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"员工", "管理员", "顾客"});

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("用户名："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(tfUsername, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("密码："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(pfPassword, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("真实姓名："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(tfRealName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("电话："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(tfPhone, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("角色："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(cbRole, gbc);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        btnSave.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = new String(pfPassword.getPassword()).trim();
            String realName = tfRealName.getText().trim();
            String phone = tfPhone.getText().trim();
            String selectedRole = (String) cbRole.getSelectedItem();
            String role;
            if ("管理员".equals(selectedRole)) {
                role = Constants.ROLE_ADMIN;
            } else if ("员工".equals(selectedRole)) {
                role = Constants.ROLE_STAFF;
            } else {
                role = Constants.ROLE_CUSTOMER;
            }

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "用户名和密码不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userService.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(dialog, "用户名已存在", "添加失败", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRealName(realName);
            newUser.setPhone(phone);
            newUser.setRole(role);

            if (userService.insert(newUser)) {
                loadUserData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== 编辑 ====================

    private void showEditDialog() {
        User selected = getSelectedUser();
        if (selected == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "编辑用户", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField tfUsername = new JTextField(selected.getUsername(), 18);
        JPasswordField pfPassword = new JPasswordField(18);
        JTextField tfRealName = new JTextField(selected.getRealName(), 18);
        JTextField tfPhone = new JTextField(selected.getPhone(), 18);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"员工", "管理员", "顾客"});
        if (Constants.ROLE_ADMIN.equals(selected.getRole())) {
            cbRole.setSelectedItem("管理员");
        } else if (Constants.ROLE_STAFF.equals(selected.getRole())) {
            cbRole.setSelectedItem("员工");
        } else {
            cbRole.setSelectedItem("顾客");
        }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("用户名："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(tfUsername, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("密码（留空不修改）："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(pfPassword, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("真实姓名："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(tfRealName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("电话："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(tfPhone, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("角色："), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formPanel.add(cbRole, gbc);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        btnSave.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = new String(pfPassword.getPassword()).trim();
            String realName = tfRealName.getText().trim();
            String phone = tfPhone.getText().trim();
            String selectedRole = (String) cbRole.getSelectedItem();
            String role;
            if ("管理员".equals(selectedRole)) {
                role = Constants.ROLE_ADMIN;
            } else if ("员工".equals(selectedRole)) {
                role = Constants.ROLE_STAFF;
            } else {
                role = Constants.ROLE_CUSTOMER;
            }

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "用户名不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            selected.setUsername(username);
            if (!password.isEmpty()) {
                selected.setPassword(password);
            }
            selected.setRealName(realName);
            selected.setPhone(phone);
            selected.setRole(role);

            if (userService.update(selected)) {
                loadUserData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== 删除 ====================

    private void deleteUser() {
        User selected = getSelectedUser();
        if (selected == null) return;

        // 不能删除自己
        if (selected.getId().equals(currentUser.getId())) {
            JOptionPane.showMessageDialog(this, "不能删除当前登录用户", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除用户 \"" + selected.getUsername() + "\" 吗？",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.delete(selected.getId())) {
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
