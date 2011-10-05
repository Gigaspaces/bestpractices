package com.gigaspaces.tutorials.common.dao;

import com.gigaspaces.query.IdQuery;
import com.gigaspaces.tutorials.common.model.BaseEntity;
import com.google.common.collect.MapMaker;
import com.j_spaces.core.client.SQLQuery;
import net.jini.core.lease.Lease;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AbstractDAO<T extends BaseEntity> implements DAO<T> {
  interface Actor<T extends BaseEntity> {
    T[] execute(SQLQuery<T> query, int count);
  }

  protected AtomicInteger reads = new AtomicInteger();
  protected AtomicInteger writes = new AtomicInteger();
  protected AtomicInteger takes = new AtomicInteger();
  protected Class<T> persistentClass;
  //protected Logger log = Logger.getLogger(this.getClass().getName());

  protected Map<String, Pair<Lock, SQLQuery<T>>> queries = new MapMaker()
                                                           .concurrencyLevel(32)
                                                           .expireAfterAccess(60, TimeUnit.SECONDS)
                                                           .makeMap();
  private Actor<T> readMultipleActor = new Actor<T>() {
    @Override
    public T[] execute(SQLQuery<T> sqlQuery, int count) {
      return getGigaspace().readMultiple(sqlQuery, count);
    }
  };
  private Actor<T> takeMultipleActor = new Actor<T>() {
    @Override
    public T[] execute(SQLQuery<T> sqlQuery, int count) {
      return getGigaspace().takeMultiple(sqlQuery, count);
    }
  };

  public GigaSpace getGigaspace() {
    return gigaspace;
  }

  public void setGigaspace(GigaSpace gigaspace) {
    this.gigaspace = gigaspace;
  }

  @Autowired
  protected GigaSpace gigaspace;

  public AbstractDAO() {
    //noinspection unchecked
    this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                                           .getGenericSuperclass()).getActualTypeArguments()[0];
  }

  protected boolean hasId(T entry) {
    return (entry.getId() != null);
  }

  @Override
  public T[] readMultiple(T template) {
    return readMultiple(template, Integer.MAX_VALUE);
  }

  @Override
  public T[] readMultiple(T template, int count) {
    reads.incrementAndGet();
    return getGigaspace().readMultiple(template, count);
  }

  @Override
  public T read(T template) {
    if (hasId(template)) {
      return readById(template.getId());
    }
    reads.incrementAndGet();
    return getGigaspace().read(template);
  }

  @Override
  public T readById(String id) {
    reads.incrementAndGet();
    return getGigaspace().readById(persistentClass, id);
  }

  @Override
  public T readByQuery(String query, Object... parameters) {
    T[] array = readMultipleByQuery(query, 1, parameters);
    if (array.length > 0) {
      return array[0];
    } else {
      return null;
    }
  }

  @Override
  public T[] readMultipleByQuery(String query, int count, Object... parameters) {
    return runQuery(query, count, parameters, readMultipleActor);
  }

  private T[] runQuery(String query, int count, Object[] parameters, Actor<T> actor) {
    if (query.trim().toLowerCase().startsWith("where")) {
      query = query.trim().substring(6);
    }
    if (!queries.containsKey(query)) {
      SQLQuery<T> queryObject = new SQLQuery<T>(persistentClass, query);
      queries.put(query, new Pair<Lock, SQLQuery<T>>(new ReentrantLock(), queryObject));
    }
    Pair<Lock, SQLQuery<T>> pair = queries.get(query);
    pair.getK().lock();
    try {
      pair.getV().setParameters(parameters);
      return actor.execute(pair.getV(), count);
    } finally {
      pair.getK().unlock();
    }
  }

  @Override
  public T[] takeMultiple(T template) {
    return takeMultiple(template, Integer.MAX_VALUE);
  }

  @Override
  public T[] takeMultiple(T template, int count) {
    takes.incrementAndGet();
    return getGigaspace().takeMultiple(template, count);
  }

  @Override
  public T take(T template) {
    takes.incrementAndGet();
    if (hasId(template)) {
      return getGigaspace().takeById(persistentClass, template.getId());
    }
    return gigaspace.take(template);
  }

  @Override
  public T takeById(String id) {
    return gigaspace.takeById(new IdQuery<T>(persistentClass, id));
  }

  @Override
  public T takeById(String id, long timeout) {
    return gigaspace.takeById(new IdQuery<T>(persistentClass, id), timeout);
  }

  @Override
  public T takeByQuery(String query, Object... parameters) {
    T[] array = takeMultipleByQuery(query, 1, parameters);
    if (array.length > 0) {
      return array[0];
    } else {
      return null;
    }
  }

  @Override
  public T[] takeMultipleByQuery(String query, int count, Object... parameters) {
    return runQuery(query, count, parameters, takeMultipleActor);
  }

  @Override
  public T poll(T template, long timeout) {
    takes.incrementAndGet();
    return gigaspace.take(template, timeout);
  }

  @Override
  public T pollById(String id, long timeout) {
    takes.incrementAndGet();
    return takeById(id, timeout);
  }

  @Override
  public T peek(T template, long timeout) {
    reads.incrementAndGet();
    return gigaspace.read(template, timeout);
  }

  @Override
  public T push(T entry) {
    return push(entry, Lease.FOREVER);
  }

  @Override
  public T push(T entry, long timeout) {
    return write(entry, timeout);
  }

  @Override
  public T write(T entry) {
    return write(entry, Lease.FOREVER);
  }

  @Override
  public T write(T entry, long timeout) {
    writes.incrementAndGet();
    if (hasId(entry)) {
      gigaspace.takeById(persistentClass, entry.getId());
    } else {
      entry.setId(UUID.randomUUID().toString());
      entry.setCreateTime(System.currentTimeMillis());
    }
    entry.setUpdateTime(System.currentTimeMillis());
    gigaspace.write(entry, timeout);
    return entry;
  }

  @Override
  public T update(T entry) {
    return update(entry, Lease.FOREVER);
  }

  @Override
  public T update(T entry, long timeout) {
    if (hasId(entry)) {
      write(entry, timeout);
    } else {
      writes.incrementAndGet();
      take(entry);
      write(entry);
    }
    return entry;
  }

  @Override
  public int getReads() {
    return reads.intValue();
  }

  @Override
  public int getTakes() {
    return takes.intValue();
  }

  @Override
  public int getWrites() {
    return writes.intValue();
  }

  @Override
  public void reset() {
    reads.set(0);
    takes.set(0);
    writes.set(0);
  }
}
