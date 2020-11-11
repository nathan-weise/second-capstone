package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.controller.AccountController;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDAO implements TransferDAO{


    //USER ACCOUNT ID INSTEAD OF USER ID

    private static final String ACCOUNTS_TABLE_FIELDS = "account_id, user_id, balance ";
    private static final String TRANSFERS_TABLE_FIELDS = "transfer_id,transfer_type_id,transfer_status_id,account_from,account_to,amount";
    private JdbcTemplate jdbcTemplate;
    private int accountIdNumber;
    private UserDAO userDAO;
    private JdbcAccountDAO jdbcAccountDAO;

    public JdbcTransferDAO(JdbcTemplate jdbcTemplate, UserDAO userDAO, JdbcAccountDAO jdbcAccountDAO) {
        this.jdbcAccountDAO = jdbcAccountDAO;
        this.jdbcTemplate = jdbcTemplate;
        this.userDAO = userDAO;
    }


    @ResponseStatus
    @Override
    public void transferTEBucks(Long accountTo, BigDecimal amount, Long userId) {
        Long accountId = jdbcAccountDAO.fetchAccountNumberByUserId(userId);
        if (validReceivingAccount(accountTo) && hasSufficientFunds(amount, userId)) {
            handleTransfer(userId, accountTo, amount);
        } else if (!validReceivingAccount(accountTo) && !hasSufficientFunds(amount, userId)){
            throw new RuntimeException("Account not found & you have insufficient funds.");
        } else if (!validReceivingAccount(accountTo)) {
            throw new RuntimeException("Account not found.");
        } else {
            throw new RuntimeException("You do not have sufficient funds.");
        }

    }

    //validate receiving account, search by user ID
    private boolean validReceivingAccount(Long userId) {
        boolean isValid = false;
        String sql = "SELECT " + ACCOUNTS_TABLE_FIELDS + "FROM accounts WHERE user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,userId);
        if (rowSet.next()) {
            isValid = true;
        }
        return isValid;
    }

    //validate user has enough funds available to send
    private boolean hasSufficientFunds(BigDecimal amount, Long userId) {
        boolean sufficientFunds = false;
        String sql = "SELECT " + ACCOUNTS_TABLE_FIELDS + "FROM accounts WHERE user_id = " + userId + ";";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
        if (rowSet.next()) {
            BigDecimal currentBalance = rowSet.getBigDecimal("balance");
            if (currentBalance.compareTo(amount) >= 0)
                sufficientFunds = true;
        }
        return sufficientFunds;
    }

    public void handleTransfer(Long userIdFrom, Long userIdTo, BigDecimal amount) {
        addBalance(userIdTo, amount);
        subtractBalance(userIdFrom, amount);
        Transfer transfer = new Transfer();
        transfer.setAmount(amount);
        transfer.setAccountTo(jdbcAccountDAO.fetchAccountNumberByUserId(userIdTo));
        transfer.setAccountFrom(jdbcAccountDAO.fetchAccountNumberByUserId(userIdFrom));
        createTransfer(transfer);
    }

    public void createTransfer(Transfer transfer) {
        String sql = "INSERT INTO transfers (transfer_type_id,transfer_status_id,account_from,account_to,amount)" +
                "VALUES (?,?,?,?,?) RETURNING transfer_id;"; //wrap in transaction. consolidate SQL.
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,transfer.getTransferTypeId(),transfer.getTransferStatusId(),
                                                        transfer.getAccountFrom(),transfer.getAccountTo(),transfer.getAmount());
        if(rowSet.next()) {
            transfer.setTransferId(rowSet.getInt("transfer_id"));
        }
    }

    public void addBalance(Long userId, BigDecimal amount) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?;";
        jdbcTemplate.update(sql, amount, userId);

    }

    public void subtractBalance(Long userId, BigDecimal amount) {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?;";
        jdbcTemplate.update(sql, amount, userId);
    }

    @Override
    public Long fetchUsersAccountId(Long userId) {
        return 0L;
    }

    @Override
    public List<Transfer> listAllMyTransfers(Principal principal) {
        int accountNumber = fetchAccoundId(principal);
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT " + TRANSFERS_TABLE_FIELDS + " FROM transfers WHERE account_from = ? OR account_to = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,accountNumber,accountNumber);
        if (!rowSet.next()) {
            throw new RuntimeException("You have no transfers.");
        }
        while (rowSet.next()) {
            Transfer transfer = mapRowToTransfer2(rowSet);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public List<Transfer> listSpecificTransfers(Principal principal, int transferId) {
        int accountNumber = fetchAccoundId(principal);
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT " + TRANSFERS_TABLE_FIELDS + " FROM transfers WHERE (account_from = ? OR account_to = ?) AND transfer_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,accountNumber,accountNumber, transferId);
        if (!rowSet.next()) {
            throw new RuntimeException("No transfers by that ID.");
        }
        while (rowSet.next()) {
            Transfer transfer = mapRowToTransfer2(rowSet);
            transfers.add(transfer);
        }
        return transfers;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setAccountFrom(rowSet.getLong("account_from"));
        transfer.setAccountTo(rowSet.getLong("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        return transfer;
    }

    private Transfer mapRowToTransfer2(SqlRowSet rowSet) {
        Transfer transfer = new Transfer();
        transfer.setAccountFrom(rowSet.getLong("account_from"));
        transfer.setAccountTo(rowSet.getLong("account_to"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        transfer.setTransferTypeId(rowSet.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rowSet.getInt("transfer_status_id"));
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        return transfer;
    }
    private int fetchAccoundId(Principal principal) {
        String sql = "SELECT a.account_id FROM accounts AS a JOIN users AS u ON u.user_id = a.user_id WHERE u.username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, principal.getName());
        if (rowSet.next()) {
            accountIdNumber = rowSet.getInt("account_id");
        }
        return accountIdNumber;
    }

}
