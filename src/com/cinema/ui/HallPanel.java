package com.cinema.ui;

import com.cinema.entity.Hall;
import com.cinema.entity.User;
import com.cinema.service.HallService;
import com.cinema.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class HallPanel extends JPanel {

    private final HallService hallService = new HallService();
    private final User currentUser;
    private final JTable hallTable;
    private final DefaultTableModel tableModel;

    public HallPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // 顶部按钮面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("添加影厅");
        JButton refreshBtn = new JButton("刷新");
        topPanel.add(addBtn);
        topPanel.add(refreshBtn);
        add(topPanel, BorderLayout.NORTH);

        // 中间表格
        String[] columns = {"ID", "影厅名称", "行数", "列数", "总座位数", "描述"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        hallTable = new JTable(tableModel);
        hallTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hallTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(hallTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton editBtn = new JButton("编辑");
        JButton deleteBtn = new JButton("删除");
        bottomPanel.add(editBtn);
        bottomPanel.add(deleteBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // 加载数据
        refreshTable();

        // 事件监听
        addBtn.addActionListener(e -> showAddDialog());
        refreshBtn.addActionListener(e -> refreshTable());
        editBtn.addActionListener(e -> showEditDialog());
        deleteBtn.addActionListener(e -> deleteHall());
    }

    /** 刷新表格数据 */
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Hall> halls = hallService.findAll();
        for (Hall hall : halls) {
            Vector<Object> row = new Vector<>();
            row.add(hall.getId());
            row.add(hall.getName());
            row.add(hall.getRows());
            row.add(hall.getCols());
            row.add(hall.getCapacity());
            row.add(hall.getDescription());
            tableModel.addRow(row);
        }
    }

    /** 获取当前选中行对应的影厅 ID */
    private Integer getSelectedHallId() {
        int row = hallTable.getSelectedRow();
        if (row == -1) {
            return null;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    /** 显示添加对话框 */
    private void showAddDialog() {
        Hall hall = showHallDialog(null, "添加影厅");
        if (hall != null) {
            hallService.add(hall);
            refreshTable();
        }
    }

    /** 显示编辑对话框 */
    private void showEditDialog() {
        Integer id = getSelectedHallId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的影厅", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Hall existing = hallService.findById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "影厅数据不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Hall hall = showHallDialog(existing, "编辑影厅");
        if (hall != null) {
            hall.setId(id);
            hallService.update(hall);
            refreshTable();
        }
    }

    /** 删除影厅（仅管理员） */
    private void deleteHall() {
        Integer id = getSelectedHallId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的影厅", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Constants.ROLE_ADMIN.equals(currentUser.getRole())) {
            JOptionPane.showMessageDialog(this, "只有管理员可以删除影厅", "权限不足", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除该影厅吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            hallService.delete(id);
            refreshTable();
        }
    }

    /**
     * 通用添加/编辑对话框
     *
     * @param existing 编辑时传入已有影厅，添加时为 null
     * @param title    对话框标题
     * @return 如果用户保存返回 Hall 对象，否则返回 null
     */
    private Hall showHallDialog(Hall existing, String title) {
        JTextField nameField = new JTextField(15);
        JTextField rowsField = new JTextField(15);
        JTextField colsField = new JTextField(15);
        JTextField descField = new JTextField(15);

        if (existing != null) {
            nameField.setText(existing.getName());
            rowsField.setText(String.valueOf(existing.getRows()));
            colsField.setText(String.valueOf(existing.getCols()));
            descField.setText(existing.getDescription());
        }

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("影厅名称："), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("行数："), gbc);
        gbc.gridx = 1;
        formPanel.add(rowsField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("列数："), gbc);
        gbc.gridx = 1;
        formPanel.add(colsField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("描述："), gbc);
        gbc.gridx = 1;
        formPanel.add(descField, gbc);

        int result = JOptionPane.showConfirmDialog(this, formPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String rowsStr = rowsField.getText().trim();
            String colsStr = colsField.getText().trim();
            String description = descField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "影厅名称不能为空", "输入错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            int rows, cols;
            try {
                rows = Integer.parseInt(rowsStr);
                cols = Integer.parseInt(colsStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "行数和列数必须为整数", "输入错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (rows <= 0 || cols <= 0) {
                JOptionPane.showMessageDialog(this, "行数和列数必须大于 0", "输入错误", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            Hall hall = new Hall();
            hall.setName(name);
            hall.setRows(rows);
            hall.setCols(cols);
            hall.setDescription(description);
            return hall;
        }
        return null;
    }
}
