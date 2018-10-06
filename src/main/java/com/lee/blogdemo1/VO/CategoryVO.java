package com.lee.blogdemo1.VO;

import com.lee.blogdemo1.entity.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryVO {

    private String username;
    private Category category;

    public CategoryVO() {
    }

}
