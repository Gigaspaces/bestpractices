package com.gigaspaces.rest.loader;

import com.gigaspaces.rest.model.Person;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.springframework.beans.factory.InitializingBean;

public class Initializer implements InitializingBean {
  @GigaSpaceContext
  GigaSpace space;
  int count = 10;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    for (int i = 0; i < count; i++) {
      Person p = new Person();
      p.setId("" + i);
      p.setFirstName("John " + i);
      p.setLastName("Doe");
      p.setAge(30+(int)(Math.random()*20));

      System.out.println("writing "+p);
      space.write(p);
    }
  }
}
