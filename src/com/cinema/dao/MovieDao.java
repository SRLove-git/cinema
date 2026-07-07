package com.cinema.dao;

import com.cinema.entity.Movie;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 电影数据访问类
 */
public class MovieDao {

    /** 查询所有电影 */
    public List<Movie> findAll() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movie ORDER BY status DESC, release_date DESC";
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

    /** 查询所有在映电影 */
    public List<Movie> findShowing() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movie WHERE status = 1 ORDER BY release_date DESC";
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

    /** 根据ID查询 */
    public Movie findById(int id) {
        String sql = "SELECT * FROM movie WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    /** 模糊搜索 */
    public List<Movie> search(String keyword) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movie WHERE title LIKE ? OR genre LIKE ? ORDER BY status DESC, release_date DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
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

    /** 插入 */
    public int insert(Movie movie) {
        String sql = "INSERT INTO movie (title, genre, duration, language, release_date, description, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getLanguage());
            stmt.setDate(5, movie.getReleaseDate() != null ? Date.valueOf(movie.getReleaseDate()) : null);
            stmt.setString(6, movie.getDescription());
            stmt.setInt(7, movie.getStatus() != null ? movie.getStatus() : 1);
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
    public int update(Movie movie) {
        String sql = "UPDATE movie SET title=?, genre=?, duration=?, language=?, release_date=?, description=?, status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getLanguage());
            stmt.setDate(5, movie.getReleaseDate() != null ? Date.valueOf(movie.getReleaseDate()) : null);
            stmt.setString(6, movie.getDescription());
            stmt.setInt(7, movie.getStatus() != null ? movie.getStatus() : 1);
            stmt.setInt(8, movie.getId());
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
        String sql = "DELETE FROM movie WHERE id=?";
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

    private Movie mapRow(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setGenre(rs.getString("genre"));
        movie.setDuration(rs.getInt("duration"));
        movie.setLanguage(rs.getString("language"));
        Date rd = rs.getDate("release_date");
        if (rd != null) movie.setReleaseDate(rd.toLocalDate());
        movie.setDescription(rs.getString("description"));
        movie.setPoster(rs.getString("poster"));
        movie.setStatus(rs.getInt("status"));
        if (rs.getTimestamp("created_at") != null) {
            movie.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return movie;
    }
}
