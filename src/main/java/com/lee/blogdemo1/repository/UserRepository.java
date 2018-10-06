package com.lee.blogdemo1.repository;

import com.lee.blogdemo1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{
    /**
     * 根据名称分页查询用户列表
     * @param name
     * @param pageable
     * @return
     */
    Page<User> findByNameLike(String name, Pageable pageable);

    /**
     * 根据用户名(账号)查询
     * @param username
     * @return
     */
    User findByUsername(String username);

    /**
     * 根据用户名(账号)列表查询
     * @param usernames
     * @return
     */
    List<User> findByUsernameIn(Collection<String> usernames);

    Optional<User> findById(Long id);

}
