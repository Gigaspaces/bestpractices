package com.gigaspaces.tutorials.common.builder;

import com.gigaspaces.tutorials.common.model.BaseEntity;

public interface Builder<T extends BaseEntity> {
  T build();
}
