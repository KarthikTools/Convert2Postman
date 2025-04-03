package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ReadyAPI interface (REST service).
 */
public class ReadyApiInterface {
    private String id;
    private String name;
    private String description;
    private List<ReadyApiResource> resources;

    public ReadyApiInterface() {
        this.resources = new ArrayList<>();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void addResource(ReadyApiResource resource) {
        this.resources.add(resource);
    }

    public List<ReadyApiResource> getResources() {
        return resources;
    }

    @Override
    public String toString() {
        return "ReadyApiInterface{" +
                "name='" + name + '\'' +
                ", resources=" + resources.size() +
                '}';
    }
} 