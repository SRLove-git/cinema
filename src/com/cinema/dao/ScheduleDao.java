package com.cinema.dao;

import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Schedule;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 放映计划数据访问类
 */
public class ScheduleDao {

    private final MovieDao movieDao = new MovieDao();
    private final HallDao hallDao = new HallDao();

    /** 查询所有场次（含关联对象） */
    public List<Schedule> findAll() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM schedule ORDER BY show_date DESC, show_time DESC";
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

    /** 查询某部电影的所有正常场次 */
    public List<Schedule> findByMovie(int movieId) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM schedule WHERE movie_id = ? AND status = 1 AND show_date >= ? ORDER BY show_date, show_time";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, movieId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
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

    /** 查询某天的所有正常场次 */
    public List<Schedule> findByDate(LocalDate date) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM schedule WHERE show_date = ? AND status = 1 ORDER BY show_time";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(date));
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
    public Schedule findById(int id) {
        String sql = "SELECT * FROM schedule WHERE id = ?";
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

    /** 插入 */
    public int insert(Schedule schedule) {
        String sql = "INSERT INTO schedule (movie_id, hall_id, show_date, show_time, price, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, schedule.getMovieId());
            stmt.setInt(2, schedule.getHallId());
            stmt.setDate(3, Date.valueOf(schedule.getShowDate()));
            stmt.setTime(4, Time.valueOf(schedule.getShowTime()));
            stmt.setBigDecimal(5, schedule.getPrice());
            stmt.setInt(6, schedule.getStatus() != null ? schedule.getStatus() : 1);
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

    /** 更新 */
    public int update(Schedule schedule) {
        String sql = "UPDATE schedule SET movie_id=?, hall_id=?, show_date=?, show_time=?, price=?, status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, schedule.getMovieId());
            stmt.setInt(2, schedule.getHallId());
            stmt.setDate(3, Date.valueOf(schedule.getShowDate()));
            stmt.setTime(4, Time.valueOf(schedule.getShowTime()));
            stmt.setBigDecimal(5, schedule.getPrice());
            stmt.setInt(6, schedule.getStatus());
            stmt.setInt(7, schedule.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt);
        }
        return 0;
    }

    /** 删除 */
    public int delete(int id) {
        String sql = "DELETE FROM schedule WHERE id=?";
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

    private Schedule mapRowWithRelation(ResultSet rs) throws SQLException {
        Schedule s = mapRow(rs);
        // 加载关联对象
        s.setMovie(movieDao.findById(s.getMovieId()));
        s.setHall(hallDao.findById(s.getHallId()));
        return s;
    }

    private Schedule mapRow(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setId(rs.getInt("id"));
        s.setMovieId(rs.getInt("movie_id"));
        s.setHallId(rs.getInt("hall_id"));
        s.setShowDate(rs.getDate("show_date").toLocalDate());
        s.setShowTime(rs.getTime("show_time").toLocalTime());
        s.setPrice(rs.getBigDecimal("price"));
        s.setStatus(rs.getInt("status"));
        if (rs.getTimestamp("created_at") != null) {
            s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return s;
    }
}
