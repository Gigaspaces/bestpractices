package com.gigaspaces.tutorials.feeder;

import com.gigaspaces.tutorials.common.dao.gigaspaces.AccountDataDAO;
import com.gigaspaces.tutorials.common.builder.AccountDataBuilder;
import com.gigaspaces.tutorials.common.model.AccountData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class PreloadAccountsBean {
  @Autowired
  AccountDataDAO dao;

  @PostConstruct
  public void loadAccounts() {
    for (int i = 0; i < 1000; i++) {
      AccountData account = new AccountDataBuilder()
                            .id("USER " + i)
                            .balance(1000)
                            .build();
      dao.write(account);
    }
  }
}
