package com.gigaspaces.tutorials.common.service;

import com.gigaspaces.tutorials.common.builder.AccountDataBuilder;
import com.gigaspaces.tutorials.common.model.AccountData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration
public class AccountServicesTest extends AbstractTestNGSpringContextTests {
  @Autowired
  AccountDataService service;

  @Test
  public void testAccountService() {
    AccountData data = new AccountDataBuilder()
                       .id("1234")
                       .balance("123")
                       .build();
    service.save(data);
    assertTrue(service.accountExists("1234"));
    AccountData d = service.load("1234", 1000);
    assertEquals(d, data);
  }
}
