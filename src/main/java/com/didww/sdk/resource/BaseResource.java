package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Id;

public abstract class BaseResource {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
