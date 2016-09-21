package com.gigaspaces.tutorials.common.model;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import java.io.Serializable;
import java.math.BigDecimal;

@SpaceClass
public class AccountData implements Serializable {
    String username;
    BigDecimal balance;
    static final long serialVersionUID = 162512L;

    @SpaceProperty
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @SpaceRouting
    @SpaceId
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AccountData");
        sb.append("{balance=").append(balance);
        sb.append(", username='").append(username).append('\'');
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
        if (username != null ? !username.equals(that.username) : that.username != null) {
          return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        return result;
    }
}
