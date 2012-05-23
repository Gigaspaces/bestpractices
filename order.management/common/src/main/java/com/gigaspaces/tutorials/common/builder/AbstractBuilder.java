package com.gigaspaces.tutorials.common.builder;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public class AbstractBuilder<T extends Serializable> implements Builder<T> {
  protected Class<T> persistentClass;
  protected T instance;

  @SuppressWarnings({"unchecked"})
  public AbstractBuilder() {
    this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                                           .getGenericSuperclass()).getActualTypeArguments()[0];
    try {
      instance = persistentClass.newInstance();
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  @Override
  public final T build() {
    if (this.instance == null) {
      throw new Error("Reusing builder for " + persistentClass.getName());
    }
    T instance = this.instance;
    this.instance = null;
    return instance;
  }
}
