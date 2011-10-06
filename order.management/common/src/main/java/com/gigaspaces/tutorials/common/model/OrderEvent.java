package com.gigaspaces.tutorials.common.model;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

@SpaceClass
public class OrderEvent implements Serializable {
  private String id;
  private Operation operation;
  private Status status;
  private String userName;
  private BigDecimal price;

  static final long serialVersionUID = 812753L;

  public OrderEvent() {
  }

  @SpaceId
  @Id
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Enumerated(value = EnumType.STRING)
  @SpaceProperty
  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  @SpaceRouting
  @Column
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Column
  @SpaceProperty
  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  @Enumerated(value = EnumType.STRING)
  @SpaceProperty
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("OrderEvent");
    sb.append("{operation=").append(operation);
    sb.append(", status=").append(status);
    sb.append(", id='").append(id).append('\'');
    sb.append(", userName='").append(userName).append('\'');
    sb.append(", price=").append(price);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrderEvent)) {
      return false;
    }

    OrderEvent that = (OrderEvent) o;

    if (operation != that.operation) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (price != null ? !price.equals(that.price) : that.price != null) {
      return false;
    }
    if (status != that.status) {
      return false;
    }
    if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = operation != null ? operation.hashCode() : 0;
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (userName != null ? userName.hashCode() : 0);
    result = 31 * result + (price != null ? price.hashCode() : 0);
    return result;
  }
}
