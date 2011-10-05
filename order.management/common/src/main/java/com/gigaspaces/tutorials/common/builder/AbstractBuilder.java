package com.gigaspaces.tutorials.common.builder;

import com.gigaspaces.tutorials.common.model.BaseEntity;

import java.lang.reflect.ParameterizedType;

public class AbstractBuilder<T extends BaseEntity> implements Builder<T> {
  protected Class<T> persistentClass;
  protected T instance;

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
