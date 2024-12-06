package io.proj3ct.VictorinyOOPbot.service;

import io.proj3ct.VictorinyOOPbot.model.Category;
import io.proj3ct.VictorinyOOPbot.model.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        // Добавляем категорию из API
        Category apiCategory = new Category();
        apiCategory.setId(-1L); // ID для API категории
        apiCategory.setName("Python Interview Questions (API)");
        categories.add(apiCategory);

        return categories;
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}