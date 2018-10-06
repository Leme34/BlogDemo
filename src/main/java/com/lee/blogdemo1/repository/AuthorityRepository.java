package com.lee.blogdemo1.repository;

import com.lee.blogdemo1.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority,Long>{
}
