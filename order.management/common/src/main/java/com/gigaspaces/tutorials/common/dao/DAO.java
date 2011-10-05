package com.gigaspaces.tutorials.common.dao;

import com.gigaspaces.tutorials.common.model.BaseEntity;
import org.openspaces.core.GigaSpace;

public interface DAO<T extends BaseEntity> {
  // Utility methods
  void setGigaspace(GigaSpace space);

  // READ METHODS
  T[] readMultiple(T template);

  T[] readMultiple(T template, int count);

  T read(T template);

  T readById(String id);

  T readByQuery(String query, Object... parameters);

  T[] readMultipleByQuery(String query, int count, Object... parameters);

  // TAKE METHODS
  T[] takeMultiple(T template);

  T[] takeMultiple(T template, int count);

  T take(T template);

  T takeById(String id);

  T takeById(String id, long timeout);

  T takeByQuery(String query, Object... parameters);

  T[] takeMultipleByQuery(String query, int count, Object... parameters);

  // MESSAGE METHODS
  T poll(T template, long timeout);

  T pollById(String id, long timeout);

  T peek(T template, long timeout);

  T push(T entry);

  T push(T entry, long timeout);

  // WRITE METHODS
  T write(T entry);

  T write(T entry, long timeout);

  T update(T entry);

  T update(T entry, long timeout);

  int getReads();

  int getTakes();

  int getWrites();

  void reset();

}