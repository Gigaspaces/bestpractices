package com.eauction.model;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import java.io.Serializable;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@SpaceClass
@SuppressWarnings("serial")
public class User implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private Integer routingId;
    private Collection<Auction> auctions;
    private Collection<Bid> bids;
    private Collection<Message> messages;

    public User(String id) {
        this.id = id;
    }

    public User() {
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @SpaceRouting
    public Integer getRoutingId() {
        return routingId;
    }

    public void setRoutingId(Integer routingId) {
        this.routingId = routingId;
    }

    public Collection<Auction> getAuctions() {
        return auctions;
    }

    public void setAuctions(Collection<Auction> auctions) {
        this.auctions = auctions;
    }

    public Collection<Bid> getBids() {
        return bids;
    }

    public void setBids(Collection<Bid> bids) {
        this.bids = bids;
    }

    public Collection<Message> getMessages() {
        return messages;
    }

    public void setMessages(Collection<Message> messages) {
        this.messages = messages;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }
}
