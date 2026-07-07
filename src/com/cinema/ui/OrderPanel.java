package com.cinema.ui;

import com.cinema.entity.Ticket;
import com.cinema.entity.User;
import com.cinema.service.TicketService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * 订单管理面板
 */
public class OrderPanel extends JPanel {

    private final User user;
    private final TicketService ticketService = new TicketService();
    private final JTable orderTable;
    private final DefaultTableModel tableModel;

    private static final String[] COLUMN_NAMES = {"订单号", "电影名称", "影厅", "放映时间", "数量", "总价", "状态", "下单时间"};

    public OrderPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout());

        // 创建顶部面板
        add(createTopPanel(), BorderLayout.NORTH);

        // 创建表格
        tableModel = new DefaultTableModel(null, COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.setFillsViewportHeight(true);
        orderTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 创建底部面板
        add(createBottomPanel(), BorderLayout.SOUTH);

        // 加载数据
        loadOrderData();
    }

    /** 刷新订单数据（供外部调用） */
    public void refresh() {
        loadOrderData();
    }

    /**
     * 创建顶部面板：刷新按钮 + 标题
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JLabel titleLabel = new JLabel("我的订单");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadOrderData());
        topPanel.add(refreshBtn, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * 创建底部面板：退票按钮
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton refundBtn = new JButton("退票");
        refundBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refundBtn.addActionListener(e -> refundTicket());
        bottomPanel.add(refundBtn);

        return bottomPanel;
    }

    /**
     * 加载订单数据
     */
    private void loadOrderData() {
        tableModel.setRowCount(0);

        List<Ticket> ticketList;
        if ("admin".equals(user.getRole())) {
            // 管理员查看所有订单
            ticketList = ticketService.findAll();
        } else {
            // 普通员工查看自己经手的订单
            ticketList = ticketService.findByUser(user.getId());
        }

        for (Ticket ticket : ticketList) {
            Vector<String> row = new Vector<>();
            row.add(ticket.getOrderNo());
            row.add(ticket.getMovie() != null ? ticket.getMovie().getTitle() : "");
            row.add(ticket.getHall() != null ? ticket.getHall().getName() : "");
            row.add(ticket.getSchedule() != null ? ticket.getSchedule().getShowDateTimeText() : "");
            row.add(String.valueOf(ticket.getQuantity()));
            row.add("¥" + ticket.getTotalPrice());
            row.add(ticket.getStatusText());
            row.add(ticket.getCreateTime() != null ? ticket.getCreateTime().toString().replace("T", " ") : "");
            tableModel.addRow(row);
        }
    }

    /**
     * 退票处理
     */
    private void refundTicket() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要退票的订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取选中行的订单号
        String orderNo = (String) tableModel.getValueAt(selectedRow, 0);
        String statusText = (String) tableModel.getValueAt(selectedRow, 6);

        // 只能退已支付的订单
        if (!"已支付".equals(statusText)) {
            JOptionPane.showMessageDialog(this, "只能退已支付的订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确认对话框
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要退订订单 " + orderNo + " 吗？", "退票确认",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 获取当前数据中的 Ticket 对象
        List<Ticket> ticketList;
        if ("admin".equals(user.getRole())) {
            ticketList = ticketService.findAll();
        } else {
            ticketList = ticketService.findByUser(user.getId());
        }

        Ticket targetTicket = null;
        for (Ticket t : ticketList) {
            if (t.getOrderNo().equals(orderNo)) {
                targetTicket = t;
                break;
            }
        }

        if (targetTicket == null) {
            JOptionPane.showMessageDialog(this, "未找到该订单", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 执行退票
        boolean success = ticketService.refund(targetTicket.getId());
        if (success) {
            JOptionPane.showMessageDialog(this, "退票成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            loadOrderData();
        } else {
            JOptionPane.showMessageDialog(this, "退票失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
