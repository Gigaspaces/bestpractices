package com.gigaspaces.tutorials.common.service;

import com.gigaspaces.tutorials.common.model.AccountData;

public interface AccountDataService {
    boolean accountExists(String userName);

    void save(AccountData data);

    AccountData load(String userName, int timeout);

    AccountData[] getAllAccountData();
}
