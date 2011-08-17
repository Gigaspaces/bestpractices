package com.eauction.gigaspaces.util;

import com.eauction.model.AuctionType;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Component;

import java.util.Properties;

import javax.annotation.PostConstruct;


@Component
public class AuctionTitleGenerator {
    private Properties propertyMap;

    @PostConstruct
    public void init() throws Exception {
        try {
            Resource resource = new DefaultResourceLoader().getResource(
                    "auctionTitles.properties");
            propertyMap = new Properties();
            propertyMap.load(resource.getInputStream());
        } catch (Exception e) {
            throw new Exception(
                "Could not find or load auctionTitles.properties.");
        }
    }

    public String getTitle(AuctionType auctionType) {
        return propertyMap.getProperty(auctionType.name() +
            (int) (9 * Math.random()));
    }
}
