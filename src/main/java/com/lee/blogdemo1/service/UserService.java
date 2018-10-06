package com.lee.blogdemo1.service;

import com.lee.blogdemo1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * 新增、编辑、保存用户
     */
    User saveOrUpateUser(User user);

    /**
     * 加密密码并注册用户
     */
    User registerUser(User user);

    /**
     * 删除用户
     */
    void removeUser(Long id);

    /**
     * 根据id获取用户
     * Optional 类是一个可以为null的容器对象,很好的解决空指针异常
     */
    Optional<User> getUserById(Long id);

    /**
     * 根据用户名进行分页模糊查询
     */
    Page<User> listUsersByNameLike(String name, Pageable pageable);

    /**
     * 根据用户名集合，查询用户详细信息列表  (在es聚合查询出username后用)
     */
    List<User> listUsersByUsernames(Collection<String> usernames);

}
