package com.gigaspaces.tutorials.common.dao;

import com.gigaspaces.query.IdQuery;
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
}
