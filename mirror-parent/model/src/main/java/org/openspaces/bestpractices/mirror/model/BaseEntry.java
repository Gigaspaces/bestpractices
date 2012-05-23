package org.openspaces.bestpractices.mirror.model;

import com.gigaspaces.annotation.pojo.SpaceId;
import org.springframework.data.annotation.Id;

public class BaseEntry {
    @Id
    String id;

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
