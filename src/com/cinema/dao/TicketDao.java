package com.cinema.dao;

import com.cinema.entity.*;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据访问类
 */
public class TicketDao {

    private final MovieDao movieDao = new MovieDao();
    private final HallDao hallDao = new HallDao();
    private final ScheduleDao scheduleDao = new ScheduleDao();
    private final UserDao userDao = new UserDao();

    /** 查询所有订单 */
    public List<Ticket> findAll() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM ticket ORDER BY create_time DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowWithRelation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    /** 根据用户ID查询订单 */
    public List<Ticket> findByUser(int userId) {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM ticket WHERE user_id = ? ORDER BY create_time DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowWithRelation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    /** 根据ID查询 */
    public Ticket findById(int id) {
        String sql = "SELECT * FROM ticket WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) return mapRowWithRelation(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    /** 插入订单 */
    public int insert(Ticket ticket) {
        String sql = "INSERT INTO ticket (order_no, user_id, schedule_id, seat_ids, quantity, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, ticket.getOrderNo());
            stmt.setInt(2, ticket.getUserId());
            stmt.setInt(3, ticket.getScheduleId());
            stmt.setString(4, ticket.getSeatIds());
            stmt.setInt(5, ticket.getQuantity());
            stmt.setBigDecimal(6, ticket.getTotalPrice());
            stmt.setString(7, ticket.getStatus());
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return -1;
    }

    /** 更新订单状态（退款） */
    public int updateStatus(int id, String status) {
        String sql = "UPDATE ticket SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt);
        }
        return 0;
    }

    /** 删除订单 */
    public int delete(int id) {
        String sql = "DELETE FROM ticket WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt);
        }
        return 0;
    }

    private Ticket mapRowWithRelation(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getInt("id"));
        t.setOrderNo(rs.getString("order_no"));
        t.setUserId(rs.getInt("user_id"));
        t.setScheduleId(rs.getInt("schedule_id"));
        t.setSeatIds(rs.getString("seat_ids"));
        t.setQuantity(rs.getInt("quantity"));
        t.setTotalPrice(rs.getBigDecimal("total_price"));
        t.setStatus(rs.getString("status"));
        if (rs.getTimestamp("create_time") != null) {
            t.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }

        // 加载关联对象
        t.setUser(userDao.findById(t.getUserId()));
        Schedule schedule = scheduleDao.findById(t.getScheduleId());
        t.setSchedule(schedule);
        if (schedule != null) {
            t.setMovie(schedule.getMovie());
            t.setHall(schedule.getHall());
        }
        return t;
    }
}
