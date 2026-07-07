package com.cinema.dao;

import com.cinema.entity.Seat;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 座位数据访问类
 */
public class SeatDao {

    /** 查询某场次的所有座位 */
    public List<Seat> findBySchedule(int scheduleId) {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM seat WHERE schedule_id = ? ORDER BY row_num, col_num";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, scheduleId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    /** 批量初始化场次座位 */
    public void initSeats(int scheduleId, int rows, int cols) {
        String sql = "INSERT INTO seat (schedule_id, row_num, col_num, status) VALUES (?, ?, ?, 0)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    stmt.setInt(1, scheduleId);
                    stmt.setInt(2, r);
                    stmt.setInt(3, c);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /** 更新座位状态（事务操作，批量） */
    public boolean batchUpdateStatus(List<Integer> seatIds, int status) {
        String sql = "UPDATE seat SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for (Integer seatId : seatIds) {
                stmt.setInt(1, status);
                stmt.setInt(2, seatId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /** 根据ID列表查询座位 */
    public List<Seat> findByIds(List<Integer> ids) {
        List<Seat> list = new ArrayList<>();
        if (ids.isEmpty()) return list;
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT * FROM seat WHERE id IN (" + placeholders + ") ORDER BY row_num, col_num";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    /** 删除某场次的所有座位 */
    public int deleteBySchedule(int scheduleId) {
        String sql = "DELETE FROM seat WHERE schedule_id=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, scheduleId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt);
        }
        return 0;
    }

    private Seat mapRow(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setId(rs.getInt("id"));
        seat.setScheduleId(rs.getInt("schedule_id"));
        seat.setRowNum(rs.getInt("row_num"));
        seat.setColNum(rs.getInt("col_num"));
        seat.setStatus(rs.getInt("status"));
        if (rs.getTimestamp("created_at") != null) {
            seat.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return seat;
    }
}
