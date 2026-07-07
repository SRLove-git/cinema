package com.cinema.entity;

import java.time.LocalDateTime;

/**
 * 座位实体类
 */
public class Seat {
    private Integer id;
    private Integer scheduleId;
    private Integer rowNum;     // 行号
    private Integer colNum;     // 列号
    private Integer status;     // 0-可选, 1-已售, 2-锁定
    private LocalDateTime createdAt;

    public Seat() {}

    public Seat(Integer scheduleId, Integer rowNum, Integer colNum, Integer status) {
        this.scheduleId = scheduleId;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.status = status;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public Integer getRowNum() { return rowNum; }
    public void setRowNum(Integer rowNum) { this.rowNum = rowNum; }

    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 获取状态文本 */
    public String getStatusText() {
        if (status == 0) return "可选";
        if (status == 1) return "已售";
        return "锁定";
    }

    /** 获取座位显示名，如 A1, B5 */
    public String getSeatLabel() {
        char rowChar = (char) ('A' + rowNum - 1);
        return String.valueOf(rowChar) + colNum;
    }
}
