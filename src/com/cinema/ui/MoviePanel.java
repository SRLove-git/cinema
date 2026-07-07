package com.cinema.ui;

import com.cinema.entity.Movie;
import com.cinema.entity.User;
import com.cinema.service.MovieService;
import com.cinema.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 电影管理面板
 */
public class MoviePanel extends JPanel {

    private final User user;
    private final MovieService movieService = new MovieService();

    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton deleteButton;

    private static final String[] COLUMN_NAMES = {"ID", "电影名称", "类型", "时长(分钟)", "语言", "上映日期", "状态"};

    public MoviePanel(User user) {
        this.user = user;
        initUI();
        loadMovieData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 顶部面板：搜索框 + 搜索按钮 + 添加按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        JButton addButton = new JButton("添加电影");

        topPanel.add(new JLabel("搜索:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        // 中间：JTable 显示电影列表
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接编辑
            }
        };
        movieTable = new JTable(tableModel);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getTableHeader().setReorderingAllowed(false);
        // 双击行编辑（顾客不启用）
        movieTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && !Constants.ROLE_CUSTOMER.equals(user.getRole())) {
                    int selectedRow = movieTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        editMovie(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(movieTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部面板：编辑按钮 + 删除按钮 + 刷新按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        JButton refreshButton = new JButton("刷新");

        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 权限控制：顾客只能查看
        if (Constants.ROLE_CUSTOMER.equals(user.getRole())) {
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        } else if (Constants.ROLE_STAFF.equals(user.getRole())) {
            deleteButton.setVisible(false);
        }

        // 事件绑定
        searchButton.addActionListener(e -> searchMovie());
        addButton.addActionListener(e -> addMovie());
        editButton.addActionListener(e -> {
            int selectedRow = movieTable.getSelectedRow();
            if (selectedRow >= 0) {
                editMovie(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的电影", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteMovie());
        refreshButton.addActionListener(e -> loadMovieData());
    }

    /**
     * 加载所有电影数据到表格
     */
    private void loadMovieData() {
        List<Movie> movieList = movieService.findAll();
        refreshTable(movieList);
    }

    /**
     * 搜索电影
     */
    private void searchMovie() {
        String keyword = searchField.getText().trim();
        List<Movie> movieList;
        if (keyword.isEmpty()) {
            movieList = movieService.findAll();
        } else {
            movieList = movieService.search(keyword);
        }
        refreshTable(movieList);
    }

    /**
     * 刷新表格数据
     */
    private void refreshTable(List<Movie> movieList) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Movie movie : movieList) {
            Object[] row = new Object[7];
            row[0] = movie.getId();
            row[1] = movie.getTitle();
            row[2] = movie.getGenreText();
            row[3] = movie.getDuration();
            row[4] = movie.getLanguage();
            row[5] = movie.getReleaseDate() != null ? movie.getReleaseDate().format(formatter) : "";
            row[6] = movie.getStatusText();
            tableModel.addRow(row);
        }
    }

    /**
     * 添加电影
     */
    private void addMovie() {
        MovieDialog dialog = new MovieDialog((Frame) null, "添加电影");
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Movie movie = dialog.getMovie();
            int result = movieService.add(movie);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadMovieData();
            } else {
                JOptionPane.showMessageDialog(this, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 编辑电影（根据表格行号）
     */
    private void editMovie(int row) {
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        Movie movie = movieService.findById(id);
        if (movie == null) {
            JOptionPane.showMessageDialog(this, "未找到该电影", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        MovieDialog dialog = new MovieDialog(movie, "编辑电影");
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Movie updatedMovie = dialog.getMovie();
            updatedMovie.setId(movie.getId());
            int result = movieService.update(updatedMovie);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "修改成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadMovieData();
            } else {
                JOptionPane.showMessageDialog(this, "修改失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 删除电影
     */
    private void deleteMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的电影", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int option = JOptionPane.showConfirmDialog(this, "确定要删除该电影吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
            int result = movieService.delete(id);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadMovieData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 电影编辑对话框
     */
    private static class MovieDialog extends JDialog {

        private boolean confirmed = false;
        private Movie movie;

        private JTextField titleField;
        private JTextField genreField;
        private JTextField durationField;
        private JTextField languageField;
        private JTextField releaseDateField;
        private JTextArea descriptionArea;
        private JComboBox<String> statusCombo;

        /**
         * 添加模式构造函数
         */
        MovieDialog(Frame owner, String title) {
            super(owner, title, true);
            this.movie = new Movie();
            initForm();
            setLocationRelativeTo(owner);
        }

        /**
         * 编辑模式构造函数
         */
        MovieDialog(Movie movie, String title) {
            super((Frame) null, title, true);
            this.movie = movie;
            initForm();
            populateForm();
            setLocationRelativeTo(null);
        }

        private void initForm() {
            setLayout(new BorderLayout(10, 10));
            setSize(450, 400);
            setResizable(false);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new EmptyBorder(15, 15, 5, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // 电影名称
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            formPanel.add(new JLabel("电影名称:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            titleField = new JTextField(20);
            formPanel.add(titleField, gbc);

            // 类型
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            formPanel.add(new JLabel("类型:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            genreField = new JTextField(20);
            formPanel.add(genreField, gbc);

            // 时长
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0;
            formPanel.add(new JLabel("时长(分钟):"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            durationField = new JTextField(20);
            formPanel.add(durationField, gbc);

            // 语言
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.weightx = 0;
            formPanel.add(new JLabel("语言:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            languageField = new JTextField(20);
            formPanel.add(languageField, gbc);

            // 上映日期
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.weightx = 0;
            formPanel.add(new JLabel("上映日期:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            releaseDateField = new JTextField(20);
            formPanel.add(releaseDateField, gbc);
            JLabel dateHint = new JLabel("(格式: yyyy-MM-dd)");
            dateHint.setFont(dateHint.getFont().deriveFont(Font.PLAIN, 11));
            dateHint.setForeground(Color.GRAY);
            gbc.gridx = 2;
            gbc.weightx = 0;
            formPanel.add(dateHint, gbc);

            // 描述
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.NORTH;
            formPanel.add(new JLabel("描述:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            descriptionArea = new JTextArea(4, 20);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            JScrollPane descScroll = new JScrollPane(descriptionArea);
            formPanel.add(descScroll, gbc);

            // 状态
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(new JLabel("状态:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            statusCombo = new JComboBox<>(new String[]{"在映", "下架"});
            formPanel.add(statusCombo, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
            JButton confirmButton = new JButton("确定");
            JButton cancelButton = new JButton("取消");
            buttonPanel.add(confirmButton);
            buttonPanel.add(cancelButton);

            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            confirmButton.addActionListener(e -> {
                if (validateForm()) {
                    buildMovie();
                    confirmed = true;
                    dispose();
                }
            });
            cancelButton.addActionListener(e -> dispose());

            getRootPane().setDefaultButton(confirmButton);
        }

        /**
         * 编辑模式下填充表单数据
         */
        private void populateForm() {
            titleField.setText(movie.getTitle() != null ? movie.getTitle() : "");
            genreField.setText(movie.getGenre() != null ? movie.getGenre() : "");
            durationField.setText(movie.getDuration() != null ? String.valueOf(movie.getDuration()) : "");
            languageField.setText(movie.getLanguage() != null ? movie.getLanguage() : "");
            releaseDateField.setText(movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : "");
            descriptionArea.setText(movie.getDescription() != null ? movie.getDescription() : "");
            statusCombo.setSelectedIndex(movie.getStatus() != null && movie.getStatus() == Constants.MOVIE_SHOWING ? 0 : 1);
        }

        /**
         * 校验表单
         */
        private boolean validateForm() {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入电影名称", "提示", JOptionPane.WARNING_MESSAGE);
                titleField.requestFocus();
                return false;
            }
            if (genreField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入电影类型", "提示", JOptionPane.WARNING_MESSAGE);
                genreField.requestFocus();
                return false;
            }
            String durationText = durationField.getText().trim();
            if (!durationText.isEmpty()) {
                try {
                    int d = Integer.parseInt(durationText);
                    if (d <= 0) {
                        JOptionPane.showMessageDialog(this, "时长必须大于0", "提示", JOptionPane.WARNING_MESSAGE);
                        durationField.requestFocus();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "时长请输入整数", "提示", JOptionPane.WARNING_MESSAGE);
                    durationField.requestFocus();
                    return false;
                }
            }
            String dateText = releaseDateField.getText().trim();
            if (!dateText.isEmpty()) {
                try {
                    LocalDate.parse(dateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "日期格式错误，请使用 yyyy-MM-dd 格式", "提示", JOptionPane.WARNING_MESSAGE);
                    releaseDateField.requestFocus();
                    return false;
                }
            }
            return true;
        }

        /**
         * 从表单构建 Movie 对象
         */
        private void buildMovie() {
            movie.setTitle(titleField.getText().trim());
            movie.setGenre(genreField.getText().trim());
            String durationText = durationField.getText().trim();
            if (!durationText.isEmpty()) {
                movie.setDuration(Integer.parseInt(durationText));
            }
            movie.setLanguage(languageField.getText().trim());
            String dateText = releaseDateField.getText().trim();
            if (!dateText.isEmpty()) {
                movie.setReleaseDate(LocalDate.parse(dateText, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            movie.setDescription(descriptionArea.getText().trim());
            movie.setStatus(statusCombo.getSelectedIndex() == 0 ? Constants.MOVIE_SHOWING : Constants.MOVIE_DOWN);
        }

        boolean isConfirmed() {
            return confirmed;
        }

        Movie getMovie() {
            return movie;
        }
    }
}
