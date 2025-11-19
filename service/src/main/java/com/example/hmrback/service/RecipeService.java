package com.example.hmrback.service;

import com.example.hmrback.mapper.RecipeMapperImpl;
import com.example.hmrback.model.Recipe;
import com.example.hmrback.model.request.RecipeFilter;
import com.example.hmrback.persistence.entity.RecipeEntity;
import com.example.hmrback.persistence.entity.UserEntity;
import com.example.hmrback.persistence.repository.RecipeRepository;
import com.example.hmrback.persistence.repository.UserRepository;
import com.example.hmrback.predicate.factory.RecipePredicateFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.hmrback.exception.util.ExceptionMessageConstants.RECIPE_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
public class RecipeService {

    private static final Logger LOG = LoggerFactory.getLogger(RecipeService.class);

    // Repo
    private final RecipeRepository recipeRepository;

    // Mapper
    private final RecipeMapperImpl recipeMapper;
    private final UserRepository userRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository, RecipeMapperImpl recipeMapper, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.userRepository = userRepository;
    }

    public Recipe createRecipe(Recipe recipe, String username) {
        UserEntity author = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException(
            "L'auteur %s est introuvable".formatted(username)));

        LOG.info("Création d'une recette par l'utilisateur {}", username);

        RecipeEntity recipeEntity = recipeMapper.toEntity(recipe);
        recipeEntity.setAuthor(author);

        return recipeMapper.toModel(recipeRepository.save(recipeEntity));
    }

    public Page<Recipe> searchRecipes(RecipeFilter filter, Pageable pageable) {

        LOG.info("Recherche de recettes avec filtres {}", filter);

        if (filter != null) {
            return recipeRepository.findAll(RecipePredicateFactory.fromFilters(filter), pageable).map(recipeMapper::toModel);
        }
        return Page.empty();
    }

    @Transactional
    public Recipe updateRecipe(
        @NotNull
        Long recipeId,
        @Valid
        Recipe recipe) {

        LOG.info("Update de la recette {}", recipeId);

        return recipeMapper.toModel(recipeRepository.saveAndFlush(recipeMapper.toEntity(recipe)));
    }

    @Transactional
    public void deleteRecipe(
        @NotNull
        Long id) {

        LOG.info("Suppression de la recette {}", id);

        Optional<RecipeEntity> recipeEntity = recipeRepository.findById(id);
        recipeEntity.ifPresentOrElse(recipeRepository::delete, () -> {
            throw new EntityNotFoundException(RECIPE_NOT_FOUND_EXCEPTION_MESSAGE.formatted(id));
        });
    }
}
