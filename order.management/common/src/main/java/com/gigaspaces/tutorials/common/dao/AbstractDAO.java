package com.gigaspaces.tutorials.common.dao;

import com.gigaspaces.query.IdQuery;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public class AbstractDAO<T extends Serializable> implements DAO<T> {
  @Autowired
  GigaSpace space;
  private Class<T> persistentClass;

  @SuppressWarnings({"unchecked"})
  public AbstractDAO() {
    this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                                           .getGenericSuperclass()).getActualTypeArguments()[0];
  }

  @Override
  public T write(T object) {
    space.write(object);
    return object;
  }

  @Override
  public T readById(String id) {
    return space.readById(persistentClass, id);
  }

  @Override
  public T takeById(String id, long timeout) {
    return space.takeById(new IdQuery<T>(persistentClass, id), timeout);
  }

  @Override
  public T[] readMultiple(T template) {
    SQLQuery<T> query = new SQLQuery<T>(persistentClass, "");
    return space.readMultiple(query, Integer.MAX_VALUE);
  }

  @Override
  public T[] readMultiple(String query) {
    SQLQuery<T> sqlQuery = new SQLQuery<T>(persistentClass, query);
    return space.readMultiple(sqlQuery);
  }

  @Override
  public T read(T template, int timeout) {
    return space.read(template, timeout);
  }
}
