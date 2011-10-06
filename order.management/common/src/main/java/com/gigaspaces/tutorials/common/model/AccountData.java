package com.gigaspaces.tutorials.common.model;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

@SpaceClass
@Entity
public class AccountData implements Serializable {
  String userName;
  BigDecimal balance;
  static final long serialVersionUID = 162512L;

  @Column
  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  @SpaceRouting
  @Id
  @SpaceId
  @Column
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("AccountData");
    sb.append("{balance=").append(balance);
    sb.append(", userName='").append(userName).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AccountData)) {
      return false;
    }

    AccountData that = (AccountData) o;

    if (balance != null ? !balance.equals(that.balance) : that.balance != null) {
      return false;
    }
    if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = userName != null ? userName.hashCode() : 0;
    result = 31 * result + (balance != null ? balance.hashCode() : 0);
    return result;
  }
}
