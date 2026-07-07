package com.cinema.ui;

import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Schedule;
import com.cinema.entity.User;
import com.cinema.service.HallService;
import com.cinema.service.MovieService;
import com.cinema.service.ScheduleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

/**
 * 放映计划管理面板
 */
public class SchedulePanel extends JPanel {

    private final User user;
    private final ScheduleService scheduleService = new ScheduleService();
    private final MovieService movieService = new MovieService();
    private final HallService hallService = new HallService();

    private JComboBox<Movie> movieCombo;
    private JComboBox<String> dateFilterCombo;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton cancelButton;
    private JButton queryButton;
    private JButton refreshButton;

    private static final String[] COLUMNS = {"ID", "电影名称", "放映日期", "放映时间", "影厅", "票价", "状态"};

    public SchedulePanel(User user) {
        this.user = user;
        initComponents();
        initLayout();
        initListeners();
        loadScheduleData();
    }

    private void initComponents() {
        // 电影下拉框
        movieCombo = new JComboBox<>();
        movieCombo.addItem(null); // 表示全部
        List<Movie> movies = movieService.findShowing();
        for (Movie m : movies) {
            movieCombo.addItem(m);
        }

        // 日期筛选下拉框
        dateFilterCombo = new JComboBox<>(new String[]{"全部", "今天", "本周", "本月"});

        queryButton = new JButton("查询");
        addButton = new JButton("添加场次");

        // 表格
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.getTableHeader().setReorderingAllowed(false);

        editButton = new JButton("编辑");
        cancelButton = new JButton("取消场次");
        refreshButton = new JButton("刷新");

        // 权限控制
        if ("admin".equals(user.getRole())) {
            // 管理员全部可用
        } else if ("staff".equals(user.getRole())) {
            // 普通员工不能取消场次
            cancelButton.setEnabled(false);
        } else {
            // 顾客只能查看
            addButton.setVisible(false);
            editButton.setVisible(false);
            cancelButton.setVisible(false);
        }
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));

        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("电影："));
        topPanel.add(movieCombo);
        topPanel.add(new JLabel("日期："));
        topPanel.add(dateFilterCombo);
        topPanel.add(queryButton);
        topPanel.add(addButton);

        // 中间表格
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("放映计划列表"));

        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.add(editButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        queryButton.addActionListener(e -> querySchedules());
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        cancelButton.addActionListener(e -> cancelSchedule());
        refreshButton.addActionListener(e -> loadScheduleData());
    }

    /**
     * 加载所有放映计划数据
     */
    private void loadScheduleData() {
        List<Schedule> list = scheduleService.findAll();
        refreshTable(list);
    }

    /**
     * 按条件查询放映计划
     */
    private void querySchedules() {
        Movie selectedMovie = (Movie) movieCombo.getSelectedItem();
        String dateFilter = (String) dateFilterCombo.getSelectedItem();

        List<Schedule> all = scheduleService.findAll();

        // 按电影筛选（在全部数据中按movieId过滤，保留所有状态的场次）
        if (selectedMovie != null) {
            all = all.stream()
                    .filter(s -> s.getMovieId().equals(selectedMovie.getId()))
                    .toList();
        }

        // 按日期筛选
        LocalDate today = LocalDate.now();
        List<Schedule> filtered;
        if ("今天".equals(dateFilter)) {
            filtered = all.stream()
                    .filter(s -> s.getShowDate().equals(today))
                    .toList();
        } else if ("本周".equals(dateFilter)) {
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);
            filtered = all.stream()
                    .filter(s -> !s.getShowDate().isBefore(weekStart) && !s.getShowDate().isAfter(weekEnd))
                    .toList();
        } else if ("本月".equals(dateFilter)) {
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            filtered = all.stream()
                    .filter(s -> !s.getShowDate().isBefore(monthStart) && !s.getShowDate().isAfter(monthEnd))
                    .toList();
        } else {
            filtered = all;
        }

        refreshTable(filtered);
    }

    /**
     * 刷新表格数据
     */
    private void refreshTable(List<Schedule> list) {
        tableModel.setRowCount(0);
        for (Schedule s : list) {
            Vector<Object> row = new Vector<>();
            row.add(s.getId());
            row.add(s.getMovie() != null ? s.getMovie().getTitle() : "");
            row.add(s.getShowDate() != null ? s.getShowDate().toString() : "");
            row.add(s.getShowTime() != null ? s.getShowTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
            row.add(s.getHall() != null ? s.getHall().getName() : "");
            row.add(s.getPrice() != null ? s.getPrice().toString() : "");
            row.add(s.getStatusText());
            tableModel.addRow(row);
        }
    }

    /**
     * 显示添加场次对话框
     */
    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "添加场次", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 电影下拉框
        JComboBox<Movie> movieCombo = new JComboBox<>();
        List<Movie> movies = movieService.findShowing();
        for (Movie m : movies) {
            movieCombo.addItem(m);
        }

        // 影厅下拉框
        JComboBox<Hall> hallCombo = new JComboBox<>();
        List<Hall> halls = hallService.findAll();
        for (Hall h : halls) {
            hallCombo.addItem(h);
        }

        // 日期选择
        JTextField dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        JTextField timeField = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        JTextField priceField = new JTextField("30.00");

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("选择电影："), gbc);
        gbc.gridx = 1;
        formPanel.add(movieCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("选择影厅："), gbc);
        gbc.gridx = 1;
        formPanel.add(hallCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("放映日期："), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("放映时间："), gbc);
        gbc.gridx = 1;
        formPanel.add(timeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("票价："), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            Movie selectedMovie = (Movie) movieCombo.getSelectedItem();
            Hall selectedHall = (Hall) hallCombo.getSelectedItem();
            String dateStr = dateField.getText().trim();
            String timeStr = timeField.getText().trim();
            String priceStr = priceField.getText().trim();

            if (selectedMovie == null) {
                JOptionPane.showMessageDialog(dialog, "请选择电影");
                return;
            }
            if (selectedHall == null) {
                JOptionPane.showMessageDialog(dialog, "请选择影厅");
                return;
            }
            if (dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入放映日期");
                return;
            }
            if (timeStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入放映时间");
                return;
            }

            LocalDate showDate;
            LocalTime showTime;
            BigDecimal price;
            try {
                showDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                showTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(dialog, "票价必须大于0");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "日期、时间或票价格式不正确");
                return;
            }

            Schedule schedule = new Schedule();
            schedule.setMovieId(selectedMovie.getId());
            schedule.setHallId(selectedHall.getId());
            schedule.setShowDate(showDate);
            schedule.setShowTime(showTime);
            schedule.setPrice(price);
            schedule.setStatus(1);

            int result = scheduleService.add(schedule, selectedHall.getRows(), selectedHall.getCols());
            if (result > 0) {
                JOptionPane.showMessageDialog(dialog, "添加成功");
                dialog.dispose();
                loadScheduleData();
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 显示编辑场次对话框
     */
    private void showEditDialog() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的场次");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        Schedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            JOptionPane.showMessageDialog(this, "未找到该场次信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "编辑场次", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 电影下拉框
        JComboBox<Movie> movieCombo = new JComboBox<>();
        List<Movie> movies = movieService.findShowing();
        int movieIndex = -1;
        for (int i = 0; i < movies.size(); i++) {
            movieCombo.addItem(movies.get(i));
            if (movies.get(i).getId().equals(schedule.getMovieId())) {
                movieIndex = i;
            }
        }
        if (movieIndex >= 0) {
            movieCombo.setSelectedIndex(movieIndex);
        }

        // 影厅下拉框
        JComboBox<Hall> hallCombo = new JComboBox<>();
        List<Hall> halls = hallService.findAll();
        int hallIndex = -1;
        for (int i = 0; i < halls.size(); i++) {
            hallCombo.addItem(halls.get(i));
            if (halls.get(i).getId().equals(schedule.getHallId())) {
                hallIndex = i;
            }
        }
        if (hallIndex >= 0) {
            hallCombo.setSelectedIndex(hallIndex);
        }

        JTextField dateField = new JTextField(schedule.getShowDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        JTextField timeField = new JTextField(schedule.getShowTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        JTextField priceField = new JTextField(schedule.getPrice().toString());

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("选择电影："), gbc);
        gbc.gridx = 1;
        formPanel.add(movieCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("选择影厅："), gbc);
        gbc.gridx = 1;
        formPanel.add(hallCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("放映日期："), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("放映时间："), gbc);
        gbc.gridx = 1;
        formPanel.add(timeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("票价："), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            Movie selectedMovie = (Movie) movieCombo.getSelectedItem();
            Hall selectedHall = (Hall) hallCombo.getSelectedItem();
            String dateStr = dateField.getText().trim();
            String timeStr = timeField.getText().trim();
            String priceStr = priceField.getText().trim();

            if (selectedMovie == null || selectedHall == null) {
                JOptionPane.showMessageDialog(dialog, "请选择电影和影厅");
                return;
            }

            LocalDate showDate;
            LocalTime showTime;
            BigDecimal price;
            try {
                showDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                showTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(dialog, "票价必须大于0");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "日期、时间或票价格式不正确");
                return;
            }

            schedule.setMovieId(selectedMovie.getId());
            schedule.setHallId(selectedHall.getId());
            schedule.setShowDate(showDate);
            schedule.setShowTime(showTime);
            schedule.setPrice(price);

            int result = scheduleService.update(schedule);
            if (result > 0) {
                JOptionPane.showMessageDialog(dialog, "更新成功");
                dialog.dispose();
                loadScheduleData();
            } else {
                JOptionPane.showMessageDialog(dialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 取消场次（仅管理员可操作）
     */
    private void cancelSchedule() {
        if (!"admin".equals(user.getRole())) {
            JOptionPane.showMessageDialog(this, "您没有权限取消场次");
            return;
        }

        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要取消的场次");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        Schedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            JOptionPane.showMessageDialog(this, "未找到该场次信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (schedule.getStatus() != null && schedule.getStatus() == 0) {
            JOptionPane.showMessageDialog(this, "该场次已取消");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "确定要取消场次 [" + schedule.getMovie().getTitle() + " " + schedule.getShowDateTimeText() + "] 吗？",
                "确认取消", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            schedule.setStatus(0);
            int result = scheduleService.update(schedule);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "场次已取消");
                loadScheduleData();
            } else {
                JOptionPane.showMessageDialog(this, "取消失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
