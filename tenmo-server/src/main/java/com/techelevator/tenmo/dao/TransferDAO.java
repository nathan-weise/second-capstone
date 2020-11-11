package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Component
public interface TransferDAO {

    public List<Transfer> listAllMyTransfers(Principal principal);

    public List<Transfer> listSpecificTransfers(Principal principal, int transferId);

    public void transferTEBucks(Long accountTo, BigDecimal amount, Long userId);

    public Long fetchUsersAccountId(Long userId);


}
