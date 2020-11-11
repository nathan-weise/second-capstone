package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.security.UserNotActivatedException;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;


@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {

    private TransferDAO transferDAO;
    private JdbcTransferDAO jdbcTransferDAO;
    private JdbcAccountDAO jdbcAccountDAO;

    public TransferController(TransferDAO transferDAO, JdbcTransferDAO jdbcTransferDAO, JdbcAccountDAO jdbcAccountDAO) {
        this.transferDAO = transferDAO;
        this.jdbcTransferDAO = jdbcTransferDAO;
        this.jdbcAccountDAO = jdbcAccountDAO;
    }


    @PostMapping("user/transfer")
    public void transfer(@RequestBody TransferDTO transferDTO) {
        jdbcTransferDAO.transferTEBucks(transferDTO.getUserTo(), transferDTO.getAmount(), transferDTO.getUserFrom()); //make principal
    }

    @GetMapping("user/mytransfers")
    public List<Transfer> allMyTransfers(Principal principal) {
        return jdbcTransferDAO.listAllMyTransfers(principal);
    }

    @GetMapping("user/transfer/{transferId}")
    public List<Transfer> requestTransfers(Principal principal,@PathVariable int transferId) {
        return jdbcTransferDAO.listSpecificTransfers(principal, transferId);
    }

}
