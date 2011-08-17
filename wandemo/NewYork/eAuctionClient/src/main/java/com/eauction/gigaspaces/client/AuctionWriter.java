package com.eauction.gigaspaces.client;

import com.eauction.gigaspaces.util.AuctionTitleGenerator;

import com.eauction.model.Auction;
import com.eauction.model.AuctionStatus;
import com.eauction.model.AuctionType;
import com.eauction.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceInterruptedException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Component
public class AuctionWriter {
    private final Log log = LogFactory.getLog(AuctionWriter.class);
    private long numberOfItems = Integer.MAX_VALUE;
    private long defaultDelay = 1000;
    @Autowired
    private GigaSpace gigaSpace;
    @Autowired
    private AuctionTitleGenerator auctionTitleGenerator;
    private User[] users;

    //
    // Code needed for the scheduling bit of the feeder
    //
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> sf;
    private AuctionWriterTask auctionWriterTask;

    @PostConstruct
    public void construct() throws Exception {
        log.info("Starting AuctionWriter");

        this.users = gigaSpace.readMultiple(new User(), Integer.MAX_VALUE);

        if ((users == null) || (users.length == 0)) {
            log.info("Could not find users, did you write any?");
        }

        executorService = Executors.newScheduledThreadPool(1);
        auctionWriterTask = new AuctionWriterTask();

        sf = executorService.scheduleAtFixedRate(auctionWriterTask,
                defaultDelay, defaultDelay, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() throws Exception {
        log.info("Stopping AuctionWriter");
        sf.cancel(false);
        sf = null;
        executorService.shutdown();
    }

    /**
     * Sets the number of items that will be written.
     *
     * @param numberOfItems the number of items that will be written.
     */
    public void setNumberOfItems(long numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    /**
     * The delay in milliseconds that the feeder will pause after each write
     *
     * @param defaultDelay the delay after each write
     */
    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    /**
     * Returns the number of times the feeder has written a data-object.
     *
     * @return the number of times the feeder has written a data-object.
     */
    public class AuctionWriterTask implements Runnable {
        private int counter = 1;

        public void run() {
            try {
                if (users == null) {
                    users = gigaSpace.readMultiple(new User(), Integer.MAX_VALUE);
                }

                // Pick a random user
                User user = ((users != null) && (users.length > 0))
                    ? users[(int) ((users.length - 1) * Math.random())] : null;

                // Generate a new dummy auction
                Auction auction = new Auction();
                auction.setAuctionStatus(AuctionStatus.NEW);
                auction.setAuctionType(AuctionType.values()[(int) ((AuctionType.values().length -
                    1) * Math.random())]);

                auction.setAskingPrice(new BigDecimal(10 * Math.random()).setScale(
                        2, RoundingMode.HALF_EVEN));
                auction.setMinBidPrice(BigDecimal.ZERO);
                auction.setTitle(auctionTitleGenerator.getTitle(
                        auction.getAuctionType()));
                auction.setOwner(user);
                auction.setCreatedDate(new Date());
                auction.setLifeTime(Auction.MAX_LIFETIME);
					 if(user==null)auction.setRoutingId(1);

                // Write the auction object
                gigaSpace.write(auction);

                log.info("AuctionWriter wrote [" + counter + "] auctions");

                // Increase an internal counter containing the number of objects written
                counter++;

                // If we've written enough auctions, we simply stop
                if (counter > numberOfItems) {
                    destroy();
                }
            } catch (SpaceInterruptedException e) {
                // ignore, we are being shutdown
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
