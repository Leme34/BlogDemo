package com.lee.blogdemo1.service;

import com.lee.blogdemo1.entity.Authority;

/**
 * 权限
 */
public interface AuthorityService {

    /**
     * 根据id获取 Authority
    */
    Authority getAuthorityById(Long id);

}
