package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class JdbcAccountDAO implements AccountDAO {

    private static final String ACCOUNTS_TABLE_FIELDS = "account_id, user_id, balance ";
    private JdbcTemplate jdbcTemplate;
    public JdbcAccountDAO(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @Override
    public Account fetchBalance(int userId) {
        Account result = null;
        String sql = "SELECT " + ACCOUNTS_TABLE_FIELDS + "FROM accounts WHERE user_id = ?;";
        //question mark in above line prevents SQL injection
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId);
        if (rowSet.next()) {
            result = mapRowToAccount(rowSet);
        }
        return result;
    }

    @Override
    public Long fetchAccountNumberByUserId(Long userId) {
        Long result = 0L;
        String sql = "SELECT a.account_id FROM accounts AS a JOIN users AS u ON u.user_id = a.user_id WHERE u.user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId);
        if (rowSet.next()) {
            result = rowSet.getLong("account_id");
        }
        return result;
    }

    private Account mapRowToAccount(SqlRowSet rowSet) {
        Account account = new Account();
        account.setAccountId(rowSet.getInt("account_id"));
        account.setUserId(rowSet.getInt("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));
        return account;
    }

}
