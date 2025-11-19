package com.example.hmrback.api.controller;

import com.example.hmrback.auth.service.JwtService;
import com.example.hmrback.persistence.entity.*;
import com.example.hmrback.persistence.repository.*;
import com.example.hmrback.utils.test.EntityTestUtils;
import com.example.hmrback.utils.test.IntegrationTestUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.example.hmrback.utils.test.TestConstants.SHOULD_BE_INITIALIZED_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("localtest")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecipeBaseIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(RecipeBaseIntegrationTest.class);

    @Autowired
    public MockMvc mockMvc;

    public static RoleEntity roleUser;
    public static RoleEntity roleAdmin;

    public static UserEntity savedUser;
    public static UserEntity savedAdmin;
    public static UserEntity savedOtherUser;

    public static String adminToken;
    public static String userToken;
    public static String otherToken;

    public static RecipeEntity savedRecipe;
    public static RecipeEntity savedOtherRecipe;

    public static List<ProductEntity> savedProducts;

    /**
     * Base setup for Integration tests
     *
     * @param context the application context
     */
    @BeforeAll
    static void setup(
        @Autowired
        ApplicationContext context) {
        LOG.info("[Integration Tests] Base Setup");

        // Role setup
        roleSetup(context);

        // User setup
        userSetup(context);

        // Token setup
        tokenSetup(context);

        // Recipe setup
        recipeSetup(context);

        // Step setup
        stepSetup(context);

        // Product setup
        productSetup(context);

        // Ingredient setup
        ingredientSetup(context);
    }

    @Test
    @Order(0)
    void contextLoads() {
        assertNotNull(roleUser, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Role USER"));
        assertNotNull(roleAdmin, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Role ADMIN"));

        assertNotNull(savedUser, SHOULD_BE_INITIALIZED_MESSAGE.formatted("User"));
        assertNotNull(savedAdmin, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Admin"));
        assertNotNull(savedOtherUser, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Other User"));

        assertNotNull(adminToken, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Admin token"));
        assertNotNull(userToken, SHOULD_BE_INITIALIZED_MESSAGE.formatted("User token"));
        assertNotNull(otherToken, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Other user token"));

        assertNotNull(savedRecipe, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Saved Recipe"));
        assertNotNull(savedOtherRecipe, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Saved other Recipe"));

        assertNotNull(savedProducts, SHOULD_BE_INITIALIZED_MESSAGE.formatted("Saved products"));
    }

    public static void roleSetup(ApplicationContext context) {
        RoleRepository roleRepository = context.getBean(RoleRepository.class);

        roleUser = roleRepository.save(EntityTestUtils.buildRoleEntity(false));
        roleAdmin = roleRepository.save(EntityTestUtils.buildRoleEntity(true));
    }

    public static void userSetup(ApplicationContext context) {
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        UserRepository userRepository = context.getBean(UserRepository.class);

        UserEntity user = EntityTestUtils.buildUserEntity(1L, true);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Set.of(roleUser));
        savedUser = userRepository.save(user);

        UserEntity admin = EntityTestUtils.buildUserEntity(2L, true);
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRoles(Set.of(roleAdmin, roleUser));
        savedAdmin = userRepository.save(admin);

        UserEntity otherUser = EntityTestUtils.buildUserEntity(3L, true);
        otherUser.setUsername("otherUser");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRoles(Set.of(roleUser));
        savedOtherUser = userRepository.save(otherUser);
    }

    public static void tokenSetup(ApplicationContext context) {
        JwtService jwtService = context.getBean(JwtService.class);
        adminToken = jwtService.generateToken(savedAdmin);
        userToken = jwtService.generateToken(savedUser);
        otherToken = jwtService.generateToken(savedOtherUser);

    }

    public static void recipeSetup(ApplicationContext context) {
        RecipeRepository recipeRepository = context.getBean(RecipeRepository.class);

        RecipeEntity recipeEntity = EntityTestUtils.buildRecipeEntity(1L, true);
        recipeEntity.setAuthor(savedUser);
        savedRecipe = recipeRepository.save(recipeEntity);

        RecipeEntity otherRecipe = EntityTestUtils.buildRecipeEntityIT();
        otherRecipe.setAuthor(savedOtherUser);
        savedOtherRecipe = recipeRepository.save(otherRecipe);
    }

    public static void stepSetup(ApplicationContext context) {
        StepRepository stepRepository = context.getBean(StepRepository.class);

        List<StepEntity> stepList = EntityTestUtils.buildStepEntityList(5, true).stream()
            .peek(step -> step.setRecipe(savedRecipe))
            .toList();
        stepRepository.saveAll(stepList);
    }

    public static void productSetup(ApplicationContext context) {
        ProductRepository productRepository = context.getBean(ProductRepository.class);

        savedProducts = productRepository.saveAll(IntegrationTestUtils.buildProductEntityList());
    }

    public static void ingredientSetup(ApplicationContext context) {
        IngredientRepository ingredientRepository = context.getBean(IngredientRepository.class);

        List<IngredientEntity> ingredientEntityList = IntegrationTestUtils.buildIngredientEntityList(savedRecipe, savedProducts);
        ingredientRepository.saveAll(ingredientEntityList);
    }

}
