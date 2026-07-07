package com.cinema.entity;

import java.time.LocalDateTime;

/**
 * 影厅实体类
 */
public class Hall {
    private Integer id;
    private String name;
    private Integer rows;      // 行数
    private Integer cols;      // 列数
    private String description;
    private LocalDateTime createdAt;

    public Hall() {}

    public Hall(Integer id, String name, Integer rows, Integer cols, String description) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.description = description;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 获取总座位数 */
    public int getCapacity() {
        return rows * cols;
    }

    @Override
    public String toString() {
        return name + "（" + getCapacity() + "座）";
    }
}
