package com.cinema.service;

import com.cinema.dao.MovieDao;
import com.cinema.entity.Movie;

import java.util.List;

/**
 * 电影业务服务类
 */
public class MovieService {
    private final MovieDao movieDao = new MovieDao();

    public List<Movie> findAll() { return movieDao.findAll(); }
    public List<Movie> findShowing() { return movieDao.findShowing(); }
    public Movie findById(int id) { return movieDao.findById(id); }
    public List<Movie> search(String keyword) { return movieDao.search(keyword); }
    public int add(Movie movie) { return movieDao.insert(movie); }
    public int update(Movie movie) { return movieDao.update(movie); }
    public int delete(int id) { return movieDao.delete(id); }
}
