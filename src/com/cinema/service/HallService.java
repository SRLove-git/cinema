package com.cinema.service;

import com.cinema.dao.HallDao;
import com.cinema.entity.Hall;

import java.util.List;

/**
 * 影厅业务服务类
 */
public class HallService {
    private final HallDao hallDao = new HallDao();

    public List<Hall> findAll() { return hallDao.findAll(); }
    public Hall findById(int id) { return hallDao.findById(id); }
    public int add(Hall hall) { return hallDao.insert(hall); }
    public int update(Hall hall) { return hallDao.update(hall); }
    public int delete(int id) { return hallDao.delete(id); }
}
