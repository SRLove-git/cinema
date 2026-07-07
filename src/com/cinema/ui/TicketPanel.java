package com.cinema.ui;

import com.cinema.entity.Movie;
import com.cinema.entity.Schedule;
import com.cinema.entity.Seat;
import com.cinema.entity.Ticket;
import com.cinema.entity.User;
import com.cinema.service.MovieService;
import com.cinema.service.ScheduleService;
import com.cinema.service.TicketService;
import com.cinema.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 售票管理面板
 * 选电影 → 选场次 → 选座位 → 购票
 */
public class TicketPanel extends JPanel {

    private final User currentUser;
    private final MovieService movieService = new MovieService();
    private final ScheduleService scheduleService = new ScheduleService();
    private final TicketService ticketService = new TicketService();

    // UI 组件
    private final DefaultListModel<Movie> movieListModel = new DefaultListModel<>();
    private final JList<Movie> movieList = new JList<>(movieListModel);
    private final DefaultListModel<Schedule> scheduleListModel = new DefaultListModel<>();
    private final JList<Schedule> scheduleList = new JList<>(scheduleListModel);
    private final JPanel seatGridPanel = new JPanel();
    private final JLabel selectedInfoLabel = new JLabel("已选: 0 座 | 总价: ¥0.00");
    private final JButton purchaseButton = new JButton("购票");
    private final JButton refreshButton = new JButton("刷新");

    // 状态
    private Schedule selectedSchedule;
    private List<Seat> allSeats;
    private final Map<Integer, JToggleButton> seatButtonMap = new HashMap<>();
    private final Set<Integer> selectedSeatIdSet = new HashSet<>();

    public TicketPanel(User user) {
        this.currentUser = user;
        initUI();
        loadMovies();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ========== 左侧面板 ==========
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        // 电影列表
        movieList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMovieSelected();
            }
        });
        JScrollPane movieScroll = new JScrollPane(movieList);
        movieScroll.setBorder(new TitledBorder("在映电影"));
        leftPanel.add(movieScroll);

        // 场次列表
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleList.setCellRenderer(new ScheduleListCellRenderer());
        scheduleList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onScheduleSelected();
            }
        });
        JScrollPane scheduleScroll = new JScrollPane(scheduleList);
        scheduleScroll.setBorder(new TitledBorder("放映场次"));
        leftPanel.add(scheduleScroll);

        // ========== 右侧座位面板 ==========
        seatGridPanel.setBorder(new TitledBorder("座位图"));
        JScrollPane seatScroll = new JScrollPane(seatGridPanel);

        // ========== 底部信息栏 ==========
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        purchaseButton.setEnabled(false);
        purchaseButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        purchaseButton.addActionListener(e -> purchase());

        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshButton.addActionListener(e -> refresh());

        bottomPanel.add(refreshButton);
        bottomPanel.add(selectedInfoLabel);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(purchaseButton);

        // ========== 分割布局 ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, seatScroll);
        splitPane.setResizeWeight(0.25);
        splitPane.setDividerSize(5);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** 加载在映电影列表 */
    private void loadMovies() {
        List<Movie> movies = movieService.findShowing();
        movieListModel.clear();
        for (Movie m : movies) {
            movieListModel.addElement(m);
        }
    }

    /** 选中电影后加载其场次 */
    private void onMovieSelected() {
        Movie movie = movieList.getSelectedValue();
        scheduleListModel.clear();
        selectedSchedule = null;
        clearSeatDisplay();
        if (movie != null) {
            List<Schedule> schedules = scheduleService.findByMovie(movie.getId());
            for (Schedule s : schedules) {
                scheduleListModel.addElement(s);
            }
        }
    }

    /** 选中场次后加载座位图 */
    private void onScheduleSelected() {
        selectedSchedule = scheduleList.getSelectedValue();
        clearSeatDisplay();
        purchaseButton.setEnabled(false);
        if (selectedSchedule != null) {
            loadSeats(selectedSchedule.getId());
        }
    }

    /** 清空座位显示 */
    private void clearSeatDisplay() {
        seatGridPanel.removeAll();
        seatGridPanel.revalidate();
        seatGridPanel.repaint();
        seatButtonMap.clear();
        selectedSeatIdSet.clear();
        updateSelectedInfo();
    }

    /** 加载某场次的所有座位 */
    private void loadSeats(int scheduleId) {
        // 清除旧数据
        seatGridPanel.removeAll();
        seatButtonMap.clear();
        selectedSeatIdSet.clear();

        allSeats = ticketService.getAllSeats(scheduleId);
        if (allSeats == null || allSeats.isEmpty()) {
            seatGridPanel.setLayout(new BorderLayout());
            seatGridPanel.add(new JLabel(" 暂无座位数据", SwingConstants.CENTER), BorderLayout.CENTER);
            seatGridPanel.revalidate();
            seatGridPanel.repaint();
            return;
        }

        int maxRow = allSeats.stream().mapToInt(Seat::getRowNum).max().orElse(0);
        int maxCol = allSeats.stream().mapToInt(Seat::getColNum).max().orElse(0);

        // 构建座位映射
        Map<String, Seat> seatMap = new HashMap<>();
        for (Seat s : allSeats) {
            seatMap.put(s.getRowNum() + "," + s.getColNum(), s);
        }

        seatGridPanel.setLayout(new GridLayout(maxRow, maxCol, 4, 4));
        seatGridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int r = 1; r <= maxRow; r++) {
            for (int c = 1; c <= maxCol; c++) {
                String key = r + "," + c;
                Seat seat = seatMap.get(key);
                if (seat != null) {
                    JToggleButton btn = new JToggleButton(seat.getSeatLabel());
                    btn.putClientProperty("seatId", seat.getId());
                    btn.setOpaque(true);
                    btn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                    seatButtonMap.put(seat.getId(), btn);

                    if (seat.getStatus() == Constants.SEAT_SOLD) {
                        // 已售座位：灰色禁用
                        btn.setEnabled(false);
                        btn.setBackground(Color.GRAY);
                    } else {
                        // 可选座位：绿色，可点击切换选中
                        btn.setBackground(Color.GREEN);
                        btn.addActionListener(e -> onSeatToggled(seat.getId()));
                    }
                    seatGridPanel.add(btn);
                } else {
                    // 空位占位
                    seatGridPanel.add(new JLabel());
                }
            }
        }

        seatGridPanel.revalidate();
        seatGridPanel.repaint();
        updateSelectedInfo();
    }

    /** 点击座位切换选中状态 */
    private void onSeatToggled(Integer seatId) {
        JToggleButton btn = seatButtonMap.get(seatId);
        if (btn == null) return;

        if (btn.isSelected()) {
            selectedSeatIdSet.add(seatId);
            btn.setBackground(Color.BLUE);
        } else {
            selectedSeatIdSet.remove(seatId);
            btn.setBackground(Color.GREEN);
        }
        updateSelectedInfo();
    }

    /** 更新底部选中信息 */
    private void updateSelectedInfo() {
        int count = selectedSeatIdSet.size();
        if (count > 0 && selectedSchedule != null && selectedSchedule.getPrice() != null) {
            BigDecimal total = selectedSchedule.getPrice().multiply(BigDecimal.valueOf(count));
            total = total.setScale(2, RoundingMode.HALF_UP);
            selectedInfoLabel.setText("已选: " + count + " 座 | 总价: ¥" + total.toPlainString());
            purchaseButton.setEnabled(true);
        } else {
            selectedInfoLabel.setText("已选: 0 座 | 总价: ¥0.00");
            purchaseButton.setEnabled(false);
        }
    }

    /** 执行购票操作 */
    private void purchase() {
        if (selectedSchedule == null || selectedSeatIdSet.isEmpty()) {
            return;
        }

        int quantity = selectedSeatIdSet.size();
        BigDecimal totalPrice = selectedSchedule.getPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        String seatIdsStr = selectedSeatIdSet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Ticket ticket = new Ticket();
        ticket.setUserId(currentUser.getId());
        ticket.setScheduleId(selectedSchedule.getId());
        ticket.setQuantity(quantity);
        ticket.setTotalPrice(totalPrice);
        ticket.setSeatIds(seatIdsStr);

        List<Integer> seatIdList = new ArrayList<>(selectedSeatIdSet);

        boolean success = ticketService.purchase(ticket, seatIdList);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "购票成功！\n订单号: " + ticket.getOrderNo(),
                    "购票成功",
                    JOptionPane.INFORMATION_MESSAGE);
            // 刷新座位图
            loadSeats(selectedSchedule.getId());
        } else {
            JOptionPane.showMessageDialog(this,
                    "购票失败，请重试",
                    "购票失败",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 刷新：重新加载电影列表和当前场次座位 */
    private void refresh() {
        // 记住当前选中的场次和电影
        Movie selectedMovie = movieList.getSelectedValue();
        int scheduleId = selectedSchedule != null ? selectedSchedule.getId() : -1;

        loadMovies();

        // 恢复选中状态
        if (selectedMovie != null) {
            for (int i = 0; i < movieListModel.getSize(); i++) {
                if (movieListModel.getElementAt(i).getId().equals(selectedMovie.getId())) {
                    movieList.setSelectedIndex(i);
                    break;
                }
            }
        }

        // 刷新座位
        if (scheduleId > 0) {
            loadSeats(scheduleId);
        }
    }

    /** 场次列表自定义渲染器 */
    private static class ScheduleListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Schedule s = (Schedule) value;
            String hallName = s.getHall() != null ? s.getHall().getName() : "未知影厅";
            String price = s.getPrice() != null
                    ? "¥" + s.getPrice().setScale(2, RoundingMode.HALF_UP).toPlainString()
                    : "¥0.00";
            String text = s.getShowDate() + "  " + s.getShowTime()
                    + "  |  " + hallName
                    + "  |  " + price;
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
}
