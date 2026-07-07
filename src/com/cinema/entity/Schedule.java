package com.cinema.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 放映计划实体类
 */
public class Schedule {
    private Integer id;
    private Integer movieId;
    private Integer hallId;
    private LocalDate showDate;      // 放映日期
    private LocalTime showTime;      // 放映时间
    private BigDecimal price;        // 票价
    private Integer status;          // 0-取消, 1-正常
    private LocalDateTime createdAt;

    // 关联对象（用于显示）
    private Movie movie;
    private Hall hall;

    public Schedule() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMovieId() { return movieId; }
    public void setMovieId(Integer movieId) { this.movieId = movieId; }

    public Integer getHallId() { return hallId; }
    public void setHallId(Integer hallId) { this.hallId = hallId; }

    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }

    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }

    /** 获取状态文本 */
    public String getStatusText() {
        return status != null && status == 1 ? "正常" : "已取消";
    }

    /** 获取完整的放映时间字符串 */
    public String getShowDateTimeText() {
        return showDate.toString() + " " + showTime.toString();
    }
}
