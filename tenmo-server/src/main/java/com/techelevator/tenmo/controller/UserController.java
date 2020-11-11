package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.JdbcUserDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class UserController {

    private UserDAO userDAO;
    private JdbcUserDAO jdbcUserDAO;

    public UserController(UserDAO userDAO, JdbcUserDAO jdbcUserDAO) {
        this.userDAO = userDAO;
        this.jdbcUserDAO = jdbcUserDAO;
    }

    @GetMapping("user/list")
    public List<User> displayAllUsers() {
        return jdbcUserDAO.listAllUsers();
    }
}
