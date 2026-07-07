package com.cinema.util;

/**
 * 系统常量配置类
 */
public class Constants {

    /** 应用名称 */
    public static final String APP_TITLE = "电影院售票管理系统";

    /** 窗口大小 */
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    /** 角色: 管理员 */
    public static final String ROLE_ADMIN = "admin";
    /** 角色: 普通员工 */
    public static final String ROLE_STAFF = "staff";
    /** 角色: 顾客 */
    public static final String ROLE_CUSTOMER = "customer";

    /** 座位状态: 可选 */
    public static final int SEAT_AVAILABLE = 0;
    /** 座位状态: 已售 */
    public static final int SEAT_SOLD = 1;
    /** 座位状态: 锁定 */
    public static final int SEAT_LOCKED = 2;

    /** 订单状态: 已支付 */
    public static final String ORDER_PAID = "paid";
    /** 订单状态: 已退款 */
    public static final String ORDER_REFUNDED = "refunded";

    /** 电影状态: 在映 */
    public static final int MOVIE_SHOWING = 1;
    /** 电影状态: 下架 */
    public static final int MOVIE_DOWN = 0;

    /** 场次状态: 正常 */
    public static final int SCHEDULE_NORMAL = 1;
    /** 场次状态: 取消 */
    public static final int SCHEDULE_CANCELED = 0;
}
