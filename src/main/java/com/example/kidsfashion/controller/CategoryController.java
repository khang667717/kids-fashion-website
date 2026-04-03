package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.CategoryDTO;
import com.example.kidsfashion.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/{id}")
    public String viewCategory(@PathVariable("id") Long id, Model model) {
        CategoryDTO category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "category-detail";
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public List<CategoryDTO> getCategoriesApi() {
        return categoryService.getAllCategories();
    }
}