package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.entity.Authority;
import com.lee.blogdemo1.repository.AuthorityRepository;
import com.lee.blogdemo1.service.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityServiceImpl implements AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public Authority getAuthorityById(Long id) {
        return authorityRepository.findById(id).get();
    }

}
