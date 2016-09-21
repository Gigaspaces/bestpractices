package com.gigaspaces.tutorials.common.model;

import com.gigaspaces.annotation.pojo.*;
import com.gigaspaces.metadata.index.SpaceIndexType;

import java.io.Serializable;
import java.math.BigDecimal;

@SpaceClass
public class OrderEvent implements Serializable {
    private String id;
    private Operation operation;
    private Status status;
    private String username;
    private BigDecimal price;
    private Long lastUpdateTime;

    static final long serialVersionUID = 812753L;

    public OrderEvent() {
    }

    @SpaceProperty
    @SpaceIndex(type = SpaceIndexType.BASIC)
    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SpaceProperty
    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @SpaceRouting
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @SpaceProperty
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

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
        sb.append("{id='").append(id).append('\'');
        sb.append(", operation=").append(operation);
        sb.append(", status=").append(status);
        sb.append(", username='").append(username).append('\'');
        sb.append(", price=").append(price);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
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
        if (username != null ? !username.equals(that.username) : that.username != null) {
          return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = operation != null ? operation.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        return result;
    }
}
