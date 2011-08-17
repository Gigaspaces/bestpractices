package com.eauction.model;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import com.gigaspaces.metadata.index.SpaceIndexType;

import java.io.Serializable;

import java.math.BigDecimal;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@SpaceClass
@SuppressWarnings("serial")
public class Auction implements Serializable {
    public static final long DAY = 86400000L;
    public static final long HOUR = 3600000L;
    public static final long MINUTE = 60000L;
    public static final long MAX_LIFETIME = 129600000L; // in milliseconds, so 1 and a half day
    private String id;
    private BigDecimal askingPrice;
    private BigDecimal minBidPrice;
    private Integer bidCount;
    private String title;
    private String ownerId;
    private Integer routingId;
    private Date createdDate;
    private Long lifeTime;
    private AuctionStatus auctionStatus;
    private AuctionType auctionType;
    private Collection<Bid> bids;
    private User owner;

    public Auction(String id) {
        this.id = id;
    }

    public Auction() {
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SpaceIndex(type = SpaceIndexType.EXTENDED)
    public BigDecimal getAskingPrice() {
        return askingPrice;
    }

    public void setAskingPrice(BigDecimal askingPrice) {
        this.askingPrice = askingPrice;
    }

    public BigDecimal getMinBidPrice() {
        return minBidPrice;
    }

    public void setMinBidPrice(BigDecimal minBidPrice) {
        this.minBidPrice = minBidPrice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @SpaceRouting
    public Integer getRoutingId() {
        return routingId;
    }

    public void setRoutingId(Integer routingId) {
        this.routingId = routingId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public void setAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public Collection<Bid> getBids() {
        return bids;
    }

    public void setBids(Collection<Bid> bids) {
        this.bids = bids;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        this.ownerId = (owner != null) ? owner.getId() : null;
        this.routingId = (owner != null) ? owner.getRoutingId() : null;
    }

    public Integer getBidCount() {
        return bidCount;
    }

    public void setBidCount(Integer bidCount) {
        this.bidCount = bidCount;
    }

    public Long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(Long lifeTime) {
        this.lifeTime = lifeTime;
    }
}
