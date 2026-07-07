package com.cinema.service;

import com.cinema.dao.SeatDao;
import com.cinema.dao.TicketDao;
import com.cinema.entity.Seat;
import com.cinema.entity.Ticket;
import com.cinema.util.Constants;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 售票业务服务类
 */
public class TicketService {
    private final TicketDao ticketDao = new TicketDao();
    private final SeatDao seatDao = new SeatDao();

    public List<Ticket> findAll() { return ticketDao.findAll(); }
    public List<Ticket> findByUser(int userId) { return ticketDao.findByUser(userId); }
    public Ticket findById(int id) { return ticketDao.findById(id); }

    /** 购票：创建订单并锁定座位 */
    public boolean purchase(Ticket ticket, List<Integer> seatIds) {
        // 1. 生成订单号
        ticket.setOrderNo(generateOrderNo());
        ticket.setStatus(Constants.ORDER_PAID);

        // 2. 更新座位状态为已售
        boolean seatOk = seatDao.batchUpdateStatus(seatIds, Constants.SEAT_SOLD);
        if (!seatOk) return false;

        // 3. 保存订单
        return ticketDao.insert(ticket) > 0;
    }

    /** 退票 */
    public boolean refund(int ticketId) {
        Ticket ticket = ticketDao.findById(ticketId);
        if (ticket == null || Constants.ORDER_REFUNDED.equals(ticket.getStatus())) {
            return false;
        }

        // 1. 还原座位状态
        List<Integer> seatIds = Arrays.stream(ticket.getSeatIds().split(","))
                .map(Integer::parseInt).collect(Collectors.toList());
        seatDao.batchUpdateStatus(seatIds, Constants.SEAT_AVAILABLE);

        // 2. 更新订单状态
        return ticketDao.updateStatus(ticketId, Constants.ORDER_REFUNDED) > 0;
    }

    /** 获取某场次的可用座位 */
    public List<Seat> getAvailableSeats(int scheduleId) {
        return seatDao.findBySchedule(scheduleId).stream()
                .filter(s -> s.getStatus() == Constants.SEAT_AVAILABLE)
                .collect(Collectors.toList());
    }

    /** 获取某场次所有座位 */
    public List<Seat> getAllSeats(int scheduleId) {
        return seatDao.findBySchedule(scheduleId);
    }

    /** 生成订单号 */
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9999);
        return "CK" + datePart + String.format("%04d", random);
    }
}
