package org.openspaces.wan.demo.service.impl;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openspaces.core.GigaSpace;
import org.openspaces.wan.demo.model.WANDemoEntry;
import org.openspaces.wan.demo.service.UpdateDemoEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public class UpdateDemoEntryServiceImpl implements UpdateDemoEntryService {
	@Autowired
	GigaSpace space;
	@Autowired
	PlatformTransactionManager ptm;

	String root;
	private ScheduledExecutorService executorService;
	ScheduledFuture<?> future;
	final Random random = new Random();

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public GigaSpace getSpace() {
		return space;
	}

	public void setSpace(GigaSpace space) {
		this.space = space;
	}

	public PlatformTransactionManager getPtm() {
		return ptm;
	}

	public void setPtm(PlatformTransactionManager ptm) {
		this.ptm = ptm;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		executorService = Executors.newScheduledThreadPool(1);
		System.out
				.println("UpdateDemoEntryServiceImpl post-properties running");
		System.out.printf("%s %s%n", space.toString(), ptm.toString());
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("In runnable");
				try {
					StringBuilder sb = new StringBuilder();
					WANDemoEntry template = new WANDemoEntry();
					template.setName(root + " " + random.nextInt(10));
					WANDemoEntry entry = space.takeIfExists(template);
					if (entry == null) {
						entry = template;
						entry.setValue(1000.0);
						sb.append("created ").append(entry);
					} else {
						sb.append("loaded ").append(entry);
						entry.setValue(entry.getValue()
								+ (2 * random.nextDouble() - 1) * 5);
					}
					sb.append(", writing ");
					sb.append(entry.toString());
					space.write(entry);
					System.out.println(sb.toString());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
		future = executorService.scheduleAtFixedRate(runnable, 0, 1,
				TimeUnit.SECONDS);
	}

	public void destroy() throws Exception {
		future.cancel(false);
		future = null;
		executorService.shutdown();
	}

}
