package com.readyapi.converter.postman;

public class PostmanBodyOption {
    private String raw;
    private String language;

    public PostmanBodyOption() {
        this.language = "json";
    }

    public PostmanBodyOption(String raw) {
        this();
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
} 