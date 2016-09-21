package com.gigaspaces.tutorials.feeder;

import com.gigaspaces.tutorials.common.builder.AccountDataBuilder;
import com.gigaspaces.tutorials.common.model.AccountData;
import com.gigaspaces.tutorials.common.service.AccountDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PreloadAccountsBean {
    @Autowired
    AccountDataService service;

    @PostConstruct
    public void loadAccounts() {
        for (int i = 0; i < 1000; i++) {
            if (!service.accountExists("USER " + i)) {
                AccountData account = new AccountDataBuilder()
                              .username("USER " + i)
                              .balance(100)
                              .build();
                service.save(account);
            }
        }
    }
}
