package com.lee.blogdemo1.service;

import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    /**
     * 保存Category
     */
    Category saveCategory(Category category);

    /**
     * 根据id删除Category
     */
    void removeCategoryById(Long categoryId);

    /**
     * 根据id获取Category
     */
    Optional<Category> getCategoryById(Long categoryId);


    /**
     * 获取用户的Category列表
     */
    List<Category> listCategory(User user);

    /**
     * 删除此用户的所有Category
     */
    void removeAllByUser(User user);


}
