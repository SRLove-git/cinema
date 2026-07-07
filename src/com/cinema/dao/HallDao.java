package com.cinema.dao;

import com.cinema.entity.Hall;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 影厅数据访问类
 */
public class HallDao {

    public List<Hall> findAll() {
        List<Hall> list = new ArrayList<>();
        String sql = "SELECT * FROM hall ORDER BY id";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
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

    public Hall findById(int id) {
        String sql = "SELECT * FROM hall WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    public int insert(Hall hall) {
        String sql = "INSERT INTO hall (name, rows, cols, description) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, hall.getName());
            stmt.setInt(2, hall.getRows());
            stmt.setInt(3, hall.getCols());
            stmt.setString(4, hall.getDescription());
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

    public int update(Hall hall) {
        String sql = "UPDATE hall SET name=?, rows=?, cols=?, description=? WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, hall.getName());
            stmt.setInt(2, hall.getRows());
            stmt.setInt(3, hall.getCols());
            stmt.setString(4, hall.getDescription());
            stmt.setInt(5, hall.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt);
        }
        return 0;
    }

    public int delete(int id) {
        String sql = "DELETE FROM hall WHERE id=?";
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

    private Hall mapRow(ResultSet rs) throws SQLException {
        Hall hall = new Hall();
        hall.setId(rs.getInt("id"));
        hall.setName(rs.getString("name"));
        hall.setRows(rs.getInt("rows"));
        hall.setCols(rs.getInt("cols"));
        hall.setDescription(rs.getString("description"));
        if (rs.getTimestamp("created_at") != null) {
            hall.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return hall;
    }
}
