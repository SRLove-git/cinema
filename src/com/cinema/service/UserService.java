package com.cinema.service;

import com.cinema.dao.UserDao;
import com.cinema.entity.User;
import com.cinema.util.Constants;

import java.util.List;

/**
 * 用户业务服务类
 */
public class UserService {
    private final UserDao userDao = new UserDao();

    public User login(String username, String password) {
        return userDao.login(username, password);
    }

    public boolean register(User user) {
        User exist = userDao.findByUsername(user.getUsername());
        if (exist != null) return false;
        user.setRole(Constants.ROLE_CUSTOMER);
        return userDao.insert(user) > 0;
    }

    public List<User> findAll() { return userDao.findAll(); }
    public User findById(int id) { return userDao.findById(id); }
    public User findByUsername(String username) { return userDao.findByUsername(username); }
    public boolean insert(User user) { return userDao.insert(user) > 0; }
    public boolean update(User user) { return userDao.update(user) > 0; }
    public boolean delete(int id) { return userDao.delete(id) > 0; }
}
