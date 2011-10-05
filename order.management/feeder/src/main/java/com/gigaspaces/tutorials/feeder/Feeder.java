package com.gigaspaces.tutorials.feeder;

import com.gigaspaces.tutorials.common.builder.OrderEventBuilder;
import com.gigaspaces.tutorials.common.model.Operation;
import com.gigaspaces.tutorials.common.model.OrderEvent;
import com.gigaspaces.tutorials.common.service.OrderEventService;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A feeder bean starts a scheduled task that writes a new Data objects to the space
 * (in an unprocessed state).
 * <p/>
 * <p>The space is injected into this bean using OpenSpaces support for @GigaSpaceContext
 * annotation.
 * <p/>
 * <p>The scheduling uses the java.util.concurrent Scheduled Executor Service. It
 * is started and stopped based on Spring lifecycle events.
 */
public class Feeder implements InitializingBean, DisposableBean {

  Logger log = Logger.getLogger(this.getClass().getName());

  private ScheduledExecutorService executorService;

  private ScheduledFuture<?> sf;

  private Random randomGen = new Random();

  private long defaultDelay = 1000;
  private Integer numberOfAccounts;
  private Integer missRate;

  public Integer getNumberOfAccounts() {
    return numberOfAccounts;
  }

  public void setNumberOfAccounts(Integer numberOfAccounts) {
    this.numberOfAccounts = numberOfAccounts;
  }

  public Integer getMissRate() {
    return missRate;
  }

  public void setMissRate(Integer missRate) {
    this.missRate = missRate;
  }

  private FeederTask feederTask;

  @Autowired
  protected GigaSpace gigaSpace;
  @Autowired
  OrderEventService service;

  public void setDefaultDelay(long defaultDelay) {
    this.defaultDelay = defaultDelay;
  }

  public void afterPropertiesSet() throws Exception {
    log.info("--- STARTING FEEDER WITH CYCLE [" + defaultDelay + "]");
    executorService = Executors.newScheduledThreadPool(1);
    feederTask = new FeederTask();
    sf = executorService.scheduleAtFixedRate(feederTask, defaultDelay, defaultDelay,
                                            TimeUnit.MILLISECONDS);
  }

  public void destroy() throws Exception {
    sf.cancel(false);
    sf = null;
    executorService.shutdown();
  }

  public class FeederTask implements Runnable {

    private int counter = 0;

    public void run() {
      int accounts = (int) (getNumberOfAccounts() + (getNumberOfAccounts() * (0.1 * getMissRate())));
      try {
        //	Create a new orderEvent with randomized userName , price and
        //	operation divided between buy and sell values.
        OrderEvent orderEvent = new OrderEventBuilder()
                                .id("USER" + randomGen.nextInt(accounts + 1))
                                .price(100)
                                .operation(randomOrderOperation())
                                .build();
        service.post(orderEvent);
        counter++;
        log.info("---[Wrote orderEvent: " + orderEvent + " ]---");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private Operation randomOrderOperation() {
      return Operation.values()[randomGen.nextInt(Operation.values().length)];
    }

    public int getCounter() {
      return counter;
    }
  }

  public int getFeedCount() {
    return feederTask.getCounter();
  }
}
