package com.lee.blogdemo1.repository;

import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    /**
     * 根据id查询
     */
    Optional<Category> findById(Long categoryId);

    /**
     * 查找该用户创建的分类
     */
    List<Category> findByUser(User user);

    /**
     * 根据用户、分类名称查询
     */
    List<Category> findByUserAndName(User user,String name);

    /**
     * 获取用户的Category列表
     */
    void removeAllByUser(User user);

}
