# BeautyStock: Comprehensive Software Test Plan

**Project:** BeautyStock - Beauty Inventory Tracker  
**Version:** 1.0  
**Date:** May 1, 2026  
**Branch:** `refactor/vertical-slice-architecture`  
**Status:** Test Plan - Ready for Implementation

---

## 1. Executive Summary

This Test Plan outlines the comprehensive testing strategy for the BeautyStock application after restructuring to Vertical Slice Architecture. The plan ensures all functional requirements continue to work correctly after refactoring and maintains or improves code quality.

**Test Coverage Targets:**
- Unit Tests: >= 80% code coverage
- Integration Tests: >= 90% feature coverage
- End-to-End Tests: 100% of critical workflows

---

## 2. Scope

### 2.1 In Scope
- Authentication feature (register, login, logout, OAuth2, profile)
- Products feature (CRUD operations, search, filtering, image upload)
- Favorites feature (add, remove, check status)
- User profile management
- Dashboard and analytics
- All REST API endpoints
- Error handling and exception scenarios
- Database interactions
- Security and authorization
- Cross-feature integrations

### 2.2 Out of Scope
- Mobile application testing (separate scope)
- Performance testing (load testing, stress testing)
- Security penetration testing
- Third-party services (email delivery, Google OAuth provider)
- External weather API integration

---

## 3. Test Environment

### 3.1 Environment Configuration

**Backend:**
- Spring Boot 3.2.2
- Java 17
- PostgreSQL (test database)
- TestContainers for database isolation
- H2 in-memory database (for unit tests)

**Frontend:**
- React 18 with TypeScript
- Vitest testing framework
- React Testing Library
- Axios mock adapter

**Tools:**
- Maven for Java build and test execution
- JUnit 5 for Java unit testing
- Mockito for mocking
- npm/yarn for JavaScript dependencies

---

## 4. Test Strategy

### 4.1 Testing Levels

#### **Level 1: Unit Tests**
Test individual classes and methods in isolation.

**Scope:**
- Services (AuthService, ProductService, etc.)
- Utilities (JwtTokenProvider, etc.)
- DTOs (validation)
- Repository queries (basic methods)

**Tools:** JUnit 5, Mockito  
**Coverage Target:** >= 80% per class

---

#### **Level 2: Integration Tests**
Test feature components working together.

**Scope:**
- Controller + Service + Repository interactions
- Database transactions
- Service-to-service communication
- Exception handling in realistic scenarios

**Tools:** Spring Boot Test, TestContainers, @DataJpaTest  
**Coverage Target:** >= 90% of feature workflows

---

#### **Level 3: API Tests**
Test REST endpoints end-to-end with authentication.

**Scope:**
- All HTTP methods (GET, POST, PUT, DELETE)
- Request/response validation
- Authentication token validation
- Authorization enforcement
- Error responses

**Tools:** MockMvc, REST Assured  
**Coverage Target:** 100% of endpoints

---

#### **Level 4: Frontend Component Tests**
Test React components in isolation.

**Scope:**
- Component rendering
- User interactions
- Props and state management
- Conditional rendering

**Tools:** React Testing Library, Vitest  
**Coverage Target:** >= 80% of components

---

#### **Level 5: End-to-End Tests (Optional)**
Test complete workflows from UI to API.

**Scope:**
- Registration workflow
- Login workflow
- Product CRUD workflows
- Favorites management

**Tools:** Playwright, Selenium, Cypress  
**Coverage Target:** Critical workflows only

---

### 4.2 Test Types

#### **Functional Tests**
Verify each feature works as specified.

#### **Regression Tests**
Verify existing functionality remains unchanged after refactoring.

#### **Security Tests**
Verify authentication and authorization work correctly.

#### **Error Handling Tests**
Verify proper error responses for invalid inputs.

#### **Database Tests**
Verify data persistence and integrity.

---

## 5. Test Cases by Feature

### 5.1 Authentication Feature

#### Test Suite: Registration
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| AUT-001 | Register with valid data | POST /v1/auth/register with valid credentials | User created, JWT returned | ☐ |
| AUT-002 | Register with duplicate email | POST /v1/auth/register with existing email | 409 Conflict error | ☐ |
| AUT-003 | Register with weak password | POST /v1/auth/register with < 8 chars | 400 Bad Request | ☐ |
| AUT-004 | Register with mismatched passwords | POST /v1/auth/register with different confirm password | 400 Bad Request | ☐ |
| AUT-005 | Register and verify email sent | Complete registration | Welcome email sent | ☐ |

#### Test Suite: Login
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| AUT-006 | Login with valid credentials | POST /v1/auth/login with correct email/password | JWT token returned | ☐ |
| AUT-007 | Login with invalid password | POST /v1/auth/login with wrong password | 401 Unauthorized | ☐ |
| AUT-008 | Login with non-existent user | POST /v1/auth/login with unknown email | 401 Unauthorized | ☐ |
| AUT-009 | Login updates last_login timestamp | Complete login | last_login_at updated | ☐ |

#### Test Suite: OAuth2 (Google)
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| AUT-010 | OAuth2 Google login - new user | OAuth2 success callback | New user created, redirected with token | ☐ |
| AUT-011 | OAuth2 Google login - existing user | OAuth2 success callback | User logged in, JWT token returned | ☐ |
| AUT-012 | OAuth2 Google failure handling | OAuth2 failure callback | Redirect to login with error | ☐ |

#### Test Suite: Authorization
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| AUT-013 | Logout revokes refresh tokens | POST /v1/auth/logout | Refresh tokens deleted | ☐ |
| AUT-014 | Get current user profile | GET /v1/auth/me with valid token | User profile returned | ☐ |
| AUT-015 | Access protected endpoint without token | GET /products without token | 401 Unauthorized | ☐ |
| AUT-016 | Access protected endpoint with invalid token | GET /products with expired token | 401 Unauthorized | ☐ |

---

### 5.2 Products Feature

#### Test Suite: Product CRUD
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| PRD-001 | Create product with valid data | POST /products with all required fields | Product created, ID returned | ☐ |
| PRD-002 | Create product with missing name | POST /products without name | 400 Bad Request | ☐ |
| PRD-003 | Get all products | GET /products | List of user's products returned | ☐ |
| PRD-004 | Get product by ID | GET /products/{id} | Product details returned | ☐ |
| PRD-005 | Get product owned by another user | GET /products/{id} (different owner) | 404 Not Found | ☐ |
| PRD-006 | Update product | PUT /products/{id} with updated data | Product updated | ☐ |
| PRD-007 | Delete product | DELETE /products/{id} | Product deleted | ☐ |
| PRD-008 | Delete also removes favorites | DELETE /products/{id} | Product and associated favorites deleted | ☐ |

#### Test Suite: Product Image Upload
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| PRD-009 | Upload image successfully | POST /products/{id}/upload-image with image file | Image stored as base64, URL returned | ☐ |
| PRD-010 | Upload image - file too large | POST /products/{id}/upload-image with large file | 413 Payload Too Large | ☐ |
| PRD-011 | Upload image - invalid format | POST /products/{id}/upload-image with non-image file | 400 Bad Request | ☐ |

#### Test Suite: Search and Filter
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| PRD-012 | Search products by name | GET /products/search?query=name | Matching products returned | ☐ |
| PRD-013 | Search products by brand | GET /products/search?query=brand | Matching products returned | ☐ |
| PRD-014 | Filter by category | GET /products/category/{category} | Products in category returned | ☐ |
| PRD-015 | Search with no results | GET /products/search?query=xyz | Empty list returned | ☐ |

#### Test Suite: Dashboard
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| PRD-016 | Get dashboard metrics | GET /products/dashboard | Dashboard data (totals, expiring, etc.) returned | ☐ |
| PRD-017 | Get expiring products | GET /products/expiring | Products expiring within 15 days returned | ☐ |
| PRD-018 | Dashboard counts accuracy | Create products, verify counts | Metrics match actual data | ☐ |

---

### 5.3 Favorites Feature

#### Test Suite: Favorites Management
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| FAV-001 | Add product to favorites | POST /favorites/{productId} | Favorite created | ☐ |
| FAV-002 | Add duplicate favorite | POST /favorites/{productId} twice | Duplicate not created | ☐ |
| FAV-003 | Add non-existent product to favorites | POST /favorites/{invalidId} | 404 Not Found | ☐ |
| FAV-004 | Remove from favorites | DELETE /favorites/{productId} | Favorite deleted | ☐ |
| FAV-005 | Check if product is favorite | GET /favorites/{productId}/is-favorite | true/false returned | ☐ |
| FAV-006 | Verify favorite in product DTO | Get product details | isFavorite field accurate | ☐ |

---

### 5.4 Profile Feature

#### Test Suite: User Profile
| ID | Test Case | Steps | Expected Result | Status |
|----|-----------|-------|-----------------|--------|
| PRF-001 | Get user profile | GET /profile | User profile returned | ☐ |
| PRF-002 | Update user profile | PUT /profile with updated data | Profile updated | ☐ |
| PRF-003 | Update full name | PUT /profile with new fullName | fullName updated | ☐ |
| PRF-004 | Update city | PUT /profile with new city | city updated | ☐ |
| PRF-005 | Verify profile persists | Update profile, verify in database | Changes persisted | ☐ |

---

## 6. Data Setup and Teardown

### 6.1 Test Data

**User Accounts:**
```sql
INSERT INTO users (id, email, password_hash, full_name, role, email_verified, created_at, updated_at)
VALUES
  ('550e8400-e29b-41d4-a716-446655440001', 'test1@example.com', 'hashed_password', 'Test User 1', 'ROLE_ADULT', true, now(), now()),
  ('550e8400-e29b-41d4-a716-446655440002', 'test2@example.com', 'hashed_password', 'Test User 2', 'ROLE_YOUTH', true, now(), now());
```

**Test Products:**
```sql
INSERT INTO products (owner_email, user_id, name, brand, category, description, price, purchased_location, expiration_date, created_at, updated_at)
VALUES
  ('test1@example.com', 1, 'Test Product 1', 'Brand A', 'Skincare', 'Description', 29.99, 'Store', '2026-12-31', now(), now()),
  ('test1@example.com', 1, 'Test Product 2', 'Brand B', 'Makeup', 'Description', 49.99, 'Store', '2026-06-30', now(), now());
```

### 6.2 Setup and Teardown Strategy

**Unit Tests:**
- Use mock objects - no real database
- Setup: Create mock repositories in `@BeforeEach`
- Teardown: Clear mocks in `@AfterEach`

**Integration Tests:**
- Use TestContainers with PostgreSQL
- Setup: `@DirtiesContext` for test isolation
- Teardown: Container automatically cleaned up

**API Tests:**
- Use `@SpringBootTest` with MockMvc
- Setup: Load test database with SQL scripts
- Teardown: Rollback transactions after each test

---

## 7. Test Execution Plan

### 7.1 Test Sequence

1. **Phase 1: Unit Tests** (Backend Services)
   - Duration: 2-3 hours
   - Commands: `mvn test -Dgroups=unit`

2. **Phase 2: Integration Tests** (Database + Services)
   - Duration: 3-4 hours
   - Commands: `mvn test -Dgroups=integration`

3. **Phase 3: API Tests** (REST Endpoints)
   - Duration: 2-3 hours
   - Commands: `mvn test -Dgroups=api`

4. **Phase 4: Frontend Component Tests**
   - Duration: 2-3 hours
   - Commands: `npm test`

5. **Phase 5: Regression Testing** (Manual)
   - Duration: 4-6 hours
   - Verify all workflows work end-to-end

### 7.2 Success Criteria

✅ All unit tests pass  
✅ All integration tests pass  
✅ All API tests pass  
✅ Frontend component tests pass  
✅ Code coverage >= 80%  
✅ No regression issues found  
✅ Build succeeds without warnings  

---

## 8. Known Issues and Risks

### Potential Issues

1. **Cross-Package Dependencies**
   - Products feature depends on Favorites and Auth repositories
   - Mitigation: Use dependency injection properly

2. **Database Migration Scripts**
   - Flyway scripts reference old schema
   - Mitigation: Verify schema still works with new code

3. **Test Database Setup**
   - TestContainers may be slow on first run
   - Mitigation: Cache Docker images

4. **OAuth2 Testing**
   - Can't test real Google OAuth in unit tests
   - Mitigation: Mock OAuth2User and use test tokens

---

## 9. Test Deliverables

- [ ] Test source code in `src/test/java`
- [ ] Test coverage report (Jacoco)
- [ ] Test execution log
- [ ] Regression test results
- [ ] Test summary report
- [ ] Bug report (if any)

---

## 10. Appendix: Sample JUnit5 Test Class

```java
@SpringBootTest
@DataJpaTest
@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    private RegisterDTO validRegisterDTO;

    @BeforeEach
    void setUp() {
        validRegisterDTO = new RegisterDTO();
        validRegisterDTO.setFullName("Test User");
        validRegisterDTO.setEmail("test@example.com");
        validRegisterDTO.setPassword("Password123");
        validRegisterDTO.setConfirmPassword("Password123");
        validRegisterDTO.setAgeRange("ADULT");
    }

    @Test
    @DisplayName("Successfully register new user")
    void testRegisterSuccess() {
        // Arrange
        // Act
        AuthResponseDTO response = authService.register(validRegisterDTO);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        
        verify(emailService, times(1)).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Registration fails with duplicate email")
    void testRegisterDuplicateEmail() {
        // Arrange
        authService.register(validRegisterDTO);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            authService.register(validRegisterDTO);
        });
    }
}
```

---

**Test Plan Version:** 1.0  
**Last Updated:** May 1, 2026  
**Status:** Ready for Test Implementation
