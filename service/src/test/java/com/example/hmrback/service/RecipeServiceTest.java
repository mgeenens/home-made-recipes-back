package com.example.hmrback.service;

import com.example.hmrback.BaseTU;
import com.example.hmrback.mapper.RecipeMapperImpl;
import com.example.hmrback.model.Recipe;
import com.example.hmrback.model.request.RecipeFilter;
import com.example.hmrback.persistence.entity.RecipeEntity;
import com.example.hmrback.persistence.repository.RecipeRepository;
import com.example.hmrback.utils.test.CommonTestUtils;
import com.example.hmrback.utils.test.EntityTestUtils;
import com.example.hmrback.utils.test.ModelTestUtils;
import com.example.hmrback.utils.test.RecipeFilterEnum;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static com.example.hmrback.utils.test.TestConstants.NUMBER_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RecipeService.class)
class RecipeServiceTest extends BaseTU {

    @Autowired
    private RecipeService service;

    // Repo
    @MockitoBean
    private RecipeRepository repository;

    // Mapper
    @MockitoBean
    private RecipeMapperImpl mapper;

    private static Recipe recipe;
    private static RecipeEntity recipeEntity;
    private static List<RecipeEntity> recipeEntityList;
    private static RecipeFilter recipeFilter;

    @BeforeAll
    static void setUp() {
        recipe = ModelTestUtils.buildRecipe(NUMBER_1, true);
        recipeEntity = EntityTestUtils.buildRecipeEntity(NUMBER_1);
        recipeEntityList = EntityTestUtils.buildRecipeEntityList(3);
        recipeFilter = CommonTestUtils.buildRecipeFilter(RecipeFilterEnum.TITLE, true);
    }

    @Test
    @Order(1)
    void shouldCreateRecipe() {
        when(repository.save(any())).thenReturn(recipeEntity);
        when(mapper.toEntity(any())).thenReturn(recipeEntity);
        when(mapper.toModel(any())).thenReturn(recipe);

        Recipe result = service.createRecipe(recipe);

        assertNotNull(result);

        verify(repository, times(1)).save(any(RecipeEntity.class));
        verify(mapper, times(1)).toEntity(any(Recipe.class));
        verify(mapper, times(1)).toModel(any(RecipeEntity.class));
    }

    @Test
    @Order(2)
    void shouldSearchRecipe() {
        when(repository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(new PageImpl<>(recipeEntityList));
        when(mapper.toModel(any())).thenReturn(recipe);

        Page<Recipe> result = service.searchRecipes(recipeFilter, PageRequest.of(0, 10));

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(3, result.getTotalElements());

        verify(repository, times(1)).findAll(any(Predicate.class), any(Pageable.class));
        verify(mapper, times(3)).toModel(any(RecipeEntity.class));
    }

    @Test
    @Order(3)
    void shouldSearchRecipe_whenFiltersIsNull_thenReturnEmptyList() {
        when(repository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<Recipe> result = service.searchRecipes(recipeFilter, PageRequest.of(0, 10));

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(0, result.getTotalElements());

        verify(repository, times(1)).findAll(any(Predicate.class), any(Pageable.class));
        verify(mapper, times(0)).toModel(recipeEntity);
    }

    @Test
    @Order(4)
    void shouldUpdateRecipe() {
        when(repository.saveAndFlush(any())).thenReturn(recipeEntity);
        when(mapper.toEntity(any())).thenReturn(recipeEntity);
        when(mapper.toModel(any())).thenReturn(recipe);

        Recipe result = service.updateRecipe(NUMBER_1, recipe);

        assertNotNull(result);

        verify(repository, times(1)).saveAndFlush(any(RecipeEntity.class));
        verify(mapper, times(1)).toEntity(any(Recipe.class));
        verify(mapper, times(1)).toModel(any(RecipeEntity.class));
    }

    @Test
    @Order(5)
    void shouldDeleteRecipe() {
        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(recipeEntity));
        doNothing().when(repository).delete(any(RecipeEntity.class));

        service.deleteRecipe(NUMBER_1);

        verify(repository, times(1)).findById(NUMBER_1);
        verify(repository, times(1)).delete(any(RecipeEntity.class));
    }

    @Test
    @Order(5)
    void deleteRecipe_whenRecipeNotFound_thenThrowsException() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.deleteRecipe(NUMBER_1));

        assertNotNull(ex);
        assertEquals("Recipe with id %s not found".formatted(NUMBER_1), ex.getMessage());

        verify(repository, times(1)).findById(NUMBER_1);
        verify(repository, times(0)).delete(any(RecipeEntity.class));
    }

}
