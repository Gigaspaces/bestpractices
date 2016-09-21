package com.gigaspaces.tutorials.common.service.impl;

import com.gigaspaces.tutorials.common.dao.AccountDataDAO;
import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountDataServiceImpl implements AccountDataService {
    @Autowired
    AccountDataDAO dao;

    @Override
    public boolean accountExists(String userName) {
        return dao.readById(userName) != null;
    }

    @Override
    public void save(AccountData data) {
        dao.write(data);
    }

    /**
     * Destructive read for account with matching userName
     *
     * @param userName the username (account id)
     * @param timeout  number of ms to wait
     * @return a matching account, or null if not found
     */
    @Override
    public AccountData load(String userName, int timeout) {
        return dao.takeById(userName, timeout);
    }

    @Override
    public AccountData[] getAllAccountData() {
        return dao.readMultiple("order by username");
    }
}
