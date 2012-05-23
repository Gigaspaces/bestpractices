package com.gigaspaces.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OrderManagementServiceController {
  @Autowired
  OrderManagementService service;

  @RequestMapping(value = "/orders/*")
  public ModelAndView getAllOrders() {
    return new ModelAndView("orders", "orderEvents", service.getOrderEvents());
  }

  @RequestMapping(value = "/accounts/*")
  public ModelAndView getAllAccounts() {
    return new ModelAndView("accounts", "accountData", service.getAccountData());
  }
}
