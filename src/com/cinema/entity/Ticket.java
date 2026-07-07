package com.cinema.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单/票务实体类
 */
public class Ticket {
    private Integer id;
    private String orderNo;         // 订单号
    private Integer userId;
    private Integer scheduleId;
    private String seatIds;         // 座位ID列表，逗号分隔
    private Integer quantity;       // 数量
    private BigDecimal totalPrice;  // 总价
    private String status;          // paid-已支付, refunded-已退款
    private LocalDateTime createTime;

    // 关联对象（用于显示）
    private User user;
    private Schedule schedule;
    private Movie movie;
    private Hall hall;

    public Ticket() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public String getSeatIds() { return seatIds; }
    public void setSeatIds(String seatIds) { this.seatIds = seatIds; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }

    /** 获取状态文本 */
    public String getStatusText() {
        return "paid".equals(status) ? "已支付" : "已退款";
    }
}
