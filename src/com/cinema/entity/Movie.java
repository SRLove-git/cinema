package com.cinema.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电影实体类
 */
public class Movie {
    private Integer id;
    private String title;
    private String genre;        // 类型
    private Integer duration;    // 时长（分钟）
    private String language;     // 语言
    private LocalDate releaseDate; // 上映日期
    private String description;
    private String poster;
    private Integer status;      // 0-下架, 1-在映
    private LocalDateTime createdAt;

    public Movie() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 获取状态文本 */
    public String getStatusText() {
        return status != null && status == 1 ? "在映" : "下架";
    }

    /** 获取类型显示文本 */
    public String getGenreText() {
        return genre != null ? genre : "未分类";
    }

    @Override
    public String toString() {
        return title;
    }
}
