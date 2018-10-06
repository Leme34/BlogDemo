package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.repository.UserRepository;
import com.lee.blogdemo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * 实现UserDetailsService的loadUserByUsername接口 ,为认证用户提供用户信息
 */
@Service
public class UserServiceImpl implements UserService,UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public User saveOrUpateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User registerUser(User user) {
        //加密密码
        user.setEncodePassword(user.getPassword());
        //入库
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    @Override
    public Page<User> listUsersByNameLike(String name, Pageable pageable) {
        // jpa模糊查询一定要加 "%"+参数名+"%"
        name = "%" + name + "%";
        return userRepository.findByNameLike(name,pageable);
    }

    @Override
    public List<User> listUsersByUsernames(Collection<String> usernames) {
        return userRepository.findByUsernameIn(usernames);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }
}
