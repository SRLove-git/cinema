package com.cinema.service;

import com.cinema.dao.ScheduleDao;
import com.cinema.dao.SeatDao;
import com.cinema.entity.Hall;
import com.cinema.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

/**
 * 放映计划业务服务类
 */
public class ScheduleService {
    private final ScheduleDao scheduleDao = new ScheduleDao();
    private final SeatDao seatDao = new SeatDao();

    public List<Schedule> findAll() { return scheduleDao.findAll(); }
    public List<Schedule> findByMovie(int movieId) { return scheduleDao.findByMovie(movieId); }
    public List<Schedule> findByDate(LocalDate date) { return scheduleDao.findByDate(date); }
    public Schedule findById(int id) { return scheduleDao.findById(id); }

    public int add(Schedule schedule, int rows, int cols) {
        int id = scheduleDao.insert(schedule);
        if (id > 0) {
            seatDao.initSeats(id, rows, cols);
        }
        return id;
    }

    public int update(Schedule schedule) { return scheduleDao.update(schedule); }

    public int delete(int id) {
        seatDao.deleteBySchedule(id);
        return scheduleDao.delete(id);
    }
}
