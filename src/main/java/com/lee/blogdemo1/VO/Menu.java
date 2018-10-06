package com.lee.blogdemo1.VO;

/**
 * 后台管理的菜单，admin/index前端显示一个它的list
 */
public class Menu {

    private String name; // 菜单名称

    private String url; // 菜单 URL

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Menu(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
