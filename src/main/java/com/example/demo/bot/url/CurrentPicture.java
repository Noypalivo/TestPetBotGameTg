package com.example.demo.bot.url;

public class CurrentPicture {
    private Long index;
    private String url;

    // Конструктор класса
    public CurrentPicture(Long index, String url) {
        this.index = index;
        this.url = url;
    }
    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
