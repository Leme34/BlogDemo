package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.repository.CategoryRepository;
import com.lee.blogdemo1.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    @Override
    public void removeCategoryById(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    public List<Category> listCategory(User user) {
        return categoryRepository.findByUser(user);
    }

    @Transactional
    @Override
    public void removeAllByUser(User user) {
        categoryRepository.removeAllByUser(user);
    }

}
