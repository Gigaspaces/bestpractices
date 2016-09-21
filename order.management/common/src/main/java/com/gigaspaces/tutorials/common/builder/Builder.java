package com.gigaspaces.tutorials.common.builder;

import java.io.Serializable;

public interface Builder<T extends Serializable> {
    T build();
}
