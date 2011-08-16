package com.eauction.model;

import org.junit.Ignore;

import org.openspaces.core.GigaSpace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations = "classpath:/META-INF/spring/pu-test.xml")
@Ignore
public class BaseTest {
    @Autowired
    @Qualifier(value = "gigaSpace")
    GigaSpace gigaSpace;
    @Autowired
    @Qualifier(value = "gigaSpacePartition1")
    GigaSpace gigaSpacePartition1;
    @Autowired
    @Qualifier(value = "gigaSpacePartition2")
    GigaSpace gigaSpacePartition2;
    @Autowired
    @Qualifier(value = "gigaSpacePartitionClustered")
    GigaSpace gigaSpacePartitionClustered;
}
