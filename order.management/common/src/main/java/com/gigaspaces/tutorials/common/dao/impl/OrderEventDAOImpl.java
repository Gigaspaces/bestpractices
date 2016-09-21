package com.gigaspaces.tutorials.common.dao.impl;

import com.gigaspaces.tutorials.common.dao.AbstractDAO;
import com.gigaspaces.tutorials.common.dao.OrderEventDAO;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import org.springframework.stereotype.Repository;

@Repository
public class OrderEventDAOImpl extends AbstractDAO<OrderEvent> implements OrderEventDAO {
    @Override
    public OrderEvent write(OrderEvent object) {
        object.setLastUpdateTime(System.currentTimeMillis());
        return super.write(object);
    }
}
