package com.gigaspaces.tutorials.common.dao;

import java.io.Serializable;

public interface DAO<T extends Serializable> {
  T write(T object);

  T readById(String id);

  T takeById(String id, long timeout);

  T[] readMultiple(T template);

  T[] readMultiple(String query);

  T read(T template, int timeout);
}