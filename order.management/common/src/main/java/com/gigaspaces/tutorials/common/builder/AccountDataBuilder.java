package com.gigaspaces.tutorials.common.builder;

import com.gigaspaces.tutorials.common.model.AccountData;

import java.math.BigDecimal;

public class AccountDataBuilder extends AbstractBuilder<AccountData> {
  public AccountDataBuilder username(String username) {
    instance.setUserName(username);
    return this;
  }

  public AccountDataBuilder balance(String balance) {
    instance.setBalance(new BigDecimal(balance));
    return this;
  }

  public AccountDataBuilder balance(double balance) {
    instance.setBalance(new BigDecimal(balance));
    return this;
  }

  AccountDataBuilder balance(int balance) {
    instance.setBalance(new BigDecimal(balance));
    return this;
  }

  AccountDataBuilder balance(BigDecimal balance) {
    instance.setBalance(balance);
    return this;
  }
}
