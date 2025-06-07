package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.DataAlreadyInUseException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории {}", newCategoryDto);
        checkExists(newCategoryDto.getName());
        Category newCategory = categoryMapper.toCategory(newCategoryDto);
        Category created = categoryRepository.save(newCategory);
        return categoryMapper.toCategoryDto(created);
    }

    @Override
    public void deleteCategory(long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с ID " + catId + " не найдена.");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(long catId, NewCategoryDto categoryDto) {
        Category toUpdate = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория с ID = " + catId + " не найдена."));
        if (toUpdate.getName().equals(categoryDto.getName())) {
            return categoryMapper.toCategoryDto(toUpdate);
        }
        checkExists(categoryDto.getName());
        toUpdate.setName(categoryDto.getName());
        return categoryMapper.toCategoryDto(toUpdate);
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        List<Category> categories = categoryRepository.findAll(PageRequest.of(from, size)).getContent();
        return categoryMapper.toCategoryDtoList(categories);
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        log.info("Поиск категории по id {}", catId);
        Category finded = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория с ID = " + catId + " не найдена."));
        return categoryMapper.toCategoryDto(finded);
    }

    private void checkExists(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DataAlreadyInUseException("Категория с таким именем уже существует.");
        }
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с ID = " + id + " не найдена."));
    }
}