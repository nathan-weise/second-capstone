package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;

public interface AccountDAO {

    public Account fetchBalance(int id);

    public Long fetchAccountNumberByUserId(Long userId);

}
