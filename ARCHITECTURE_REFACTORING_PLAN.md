# BeautyStock: Vertical Slice Architecture Refactoring Plan

**Project:** BeautyStock - Beauty Inventory Tracker  
**Branch:** `refactor/vertical-slice-architecture`  
**Date:** May 1, 2026  
**Status:** Refactoring in Progress

---

## 1. Executive Summary

This document outlines the restructuring of the BeautyStock project from a traditional **Layered Architecture** to a **Vertical Slice Architecture**. The refactoring aims to improve:

- **Maintainability**: Each feature is independently organized
- **Modularity**: Clear boundaries between features
- **Scalability**: Easier to add new features without affecting existing ones
- **Team Collaboration**: Features can be developed in parallel

---

## 2. Current Architecture Overview

### 2.1 Layered Architecture (Current)
```
Backend (Spring Boot)
├── controller/ (all endpoints mixed)
├── service/ (all business logic mixed)
├── entity/ (all entities)
├── repository/ (all data access)
├── dto/ (all DTOs)
└── exception/

Frontend (React)
├── components/ (18 components mixed by page)
├── services/ (all API calls)
└── store/ (shared state)

Mobile (Android)
└── Kotlin structure
```

**Problems:**
- Difficult to locate features and dependencies
- Cross-cutting concerns (no clear separation)
- Harder to test individual features
- Difficult feature team assignments

---

## 3. Target Architecture: Vertical Slices

### 3.1 Vertical Slices Identified

The project will be restructured around **4 core feature slices** and **shared infrastructure**:

#### Slice 1: **Authentication**
- User registration
- User login
- OAuth2 (Google) integration
- JWT token management
- User logout
- Refresh token handling

#### Slice 2: **Product Management**
- Create product
- Read/view products
- Update product
- Delete product
- Product search
- Filter products by category
- Upload product image
- Get expiring products list
- Dashboard/analytics

#### Slice 3: **Favorites**
- Add product to favorites
- Remove from favorites
- Check if product is favorite
- View favorites list

#### Slice 4: **User Profile**
- View user profile
- Update profile information
- User settings

#### Slice 5: **Shared Infrastructure** (Cross-cutting)
- Security configuration
- Exception handling
- Utility functions
- Shared components
- Common DTOs and entities

---

## 4. Proposed Directory Structure

### 4.1 Backend Structure

```
backend/
├── src/main/java/com/beautystock/
│   ├── shared/                          # Shared infrastructure
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebConfig.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── CustomExceptions.java
│   │   ├── util/
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── Constants.java
│   │   └── dto/
│   │       └── CommonDTOs.java
│   │
│   ├── features/
│   │   │
│   │   ├── authentication/               # Vertical Slice 1
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── UserRole.java
│   │   │   │   └── RefreshToken.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── RefreshTokenRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginDTO.java
│   │   │   │   ├── RegisterDTO.java
│   │   │   │   ├── AuthResponseDTO.java
│   │   │   │   └── UserProfileDTO.java
│   │   │   └── README.md (feature documentation)
│   │   │
│   │   ├── products/                    # Vertical Slice 2
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java
│   │   │   ├── service/
│   │   │   │   └── ProductService.java
│   │   │   ├── entity/
│   │   │   │   └── Product.java
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── ProductDTO.java
│   │   │   │   └── CreateProductDTO.java
│   │   │   └── README.md
│   │   │
│   │   ├── favorites/                   # Vertical Slice 3
│   │   │   ├── controller/
│   │   │   │   └── FavoriteController.java
│   │   │   ├── service/
│   │   │   │   └── FavoriteService.java
│   │   │   ├── entity/
│   │   │   │   └── Favorite.java
│   │   │   ├── repository/
│   │   │   │   └── FavoriteRepository.java
│   │   │   ├── dto/
│   │   │   │   └── FavoriteDTO.java
│   │   │   └── README.md
│   │   │
│   │   └── profile/                     # Vertical Slice 4
│   │       ├── controller/
│   │       │   └── ProfileController.java
│   │       ├── service/
│   │       │   └── ProfileService.java
│   │       ├── dto/
│   │       │   └── ProfileUpdateDTO.java
│   │       └── README.md
│   │
│   └── BeautyStockApplication.java
│
├── src/test/java/com/beautystock/     # Test structure mirrors features
│   ├── features/
│   │   ├── authentication/
│   │   │   ├── AuthControllerTest.java
│   │   │   ├── AuthServiceTest.java
│   │   │   └── AuthIntegrationTest.java
│   │   ├── products/
│   │   │   ├── ProductControllerTest.java
│   │   │   ├── ProductServiceTest.java
│   │   │   └── ProductIntegrationTest.java
│   │   ├── favorites/
│   │   │   ├── FavoriteControllerTest.java
│   │   │   ├── FavoriteServiceTest.java
│   │   │   └── FavoriteIntegrationTest.java
│   │   └── profile/
│   │       ├── ProfileControllerTest.java
│   │       ├── ProfileServiceTest.java
│   │       └── ProfileIntegrationTest.java
│   └── shared/
│       ├── security/
│       │   └── SecurityConfigTest.java
│       └── exception/
│           └── ExceptionHandlingTest.java
│
└── src/main/resources/
    ├── application.yml
    ├── db/migration/
    │   ├── V1__increase_column_sizes.sql
    │   ├── V2__fix_product_columns.sql
    │   └── V3__fix_favorites_user_id_column.sql
    └── test-data.sql
```

### 4.2 Frontend Structure

```
web/
├── src/
│   ├── App.tsx
│   ├── index.css
│   ├── main.tsx
│   │
│   ├── features/                       # Feature-based organization
│   │   │
│   │   ├── authentication/             # Vertical Slice 1
│   │   │   ├── pages/
│   │   │   │   ├── LoginPage.tsx
│   │   │   │   ├── RegisterPage.tsx
│   │   │   │   └── OAuth2CallbackPage.tsx
│   │   │   ├── components/
│   │   │   │   └── GoogleSignInButton.tsx
│   │   │   ├── services/
│   │   │   │   └── authService.ts
│   │   │   ├── store/
│   │   │   │   └── authStore.ts
│   │   │   ├── types/
│   │   │   │   └── auth.types.ts
│   │   │   └── README.md
│   │   │
│   │   ├── products/                  # Vertical Slice 2
│   │   │   ├── pages/
│   │   │   │   ├── ProductsPage.tsx
│   │   │   │   ├── AddProductPage.tsx
│   │   │   │   └── ProductDetail.tsx
│   │   │   ├── components/
│   │   │   │   ├── ProductCard.tsx
│   │   │   │   ├── ProductImage.tsx
│   │   │   │   └── AddProductForm.tsx
│   │   │   ├── services/
│   │   │   │   └── productService.ts
│   │   │   ├── store/
│   │   │   │   └── productStore.ts
│   │   │   ├── types/
│   │   │   │   └── product.types.ts
│   │   │   └── README.md
│   │   │
│   │   ├── favorites/                 # Vertical Slice 3
│   │   │   ├── pages/
│   │   │   │   └── FavoritesPage.tsx
│   │   │   ├── services/
│   │   │   │   └── favoriteService.ts
│   │   │   ├── store/
│   │   │   │   └── favoriteStore.ts
│   │   │   ├── types/
│   │   │   │   └── favorite.types.ts
│   │   │   └── README.md
│   │   │
│   │   ├── profile/                   # Vertical Slice 4
│   │   │   ├── pages/
│   │   │   │   └── ProfilePage.tsx
│   │   │   ├── services/
│   │   │   │   └── profileService.ts
│   │   │   ├── store/
│   │   │   │   └── profileStore.ts
│   │   │   ├── types/
│   │   │   │   └── profile.types.ts
│   │   │   └── README.md
│   │   │
│   │   └── analytics/                 # Vertical Slice 5
│   │       ├── pages/
│   │       │   └── Dashboard.tsx
│   │       ├── components/
│   │       │   ├── Dashboard2.tsx
│   │       │   ├── SkincareAdvice.tsx
│   │       │   └── WeatherDisplay.tsx
│   │       ├── services/
│   │       │   └── analyticsService.ts
│   │       ├── store/
│   │       │   └── analyticsStore.ts
│   │       └── README.md
│   │
│   ├── shared/                         # Shared infrastructure
│   │   ├── components/
│   │   │   ├── Layout.tsx
│   │   │   └── LocationForm.tsx
│   │   ├── services/
│   │   │   ├── api.ts (HTTP client)
│   │   │   └── utils.ts
│   │   ├── types/
│   │   │   └── common.types.ts
│   │   ├── hooks/
│   │   │   ├── useAuth.ts
│   │   │   └── useApi.ts
│   │   └── constants/
│   │       └── config.ts
│   │
│   └── styles/
│       ├── App.css
│       └── tailwind.config.js
│
├── __tests__/                         # Test structure
│   ├── features/
│   │   ├── authentication/
│   │   │   ├── AuthPage.test.tsx
│   │   │   └── authService.test.ts
│   │   ├── products/
│   │   │   ├── ProductsPage.test.tsx
│   │   │   ├── ProductCard.test.tsx
│   │   │   └── productService.test.ts
│   │   ├── favorites/
│   │   │   ├── FavoritesPage.test.tsx
│   │   │   └── favoriteService.test.ts
│   │   └── profile/
│   │       ├── ProfilePage.test.tsx
│   │       └── profileService.test.ts
│   └── shared/
│       ├── Layout.test.tsx
│       └── api.test.ts
│
└── (config files remain at root)
```

---

## 5. Refactoring Strategy

### 5.1 Phase 1: Backend Restructuring
1. Create new directory structure under `src/main/java/com/beautystock/features/`
2. Move authentication-related files to `features/authentication/`
3. Move product-related files to `features/products/`
4. Move favorites-related files to `features/favorites/`
5. Create profile feature (extracted from existing functionality)
6. Move shared infrastructure to `shared/`
7. Update all imports across the application
8. Verify build succeeds

### 5.2 Phase 2: Frontend Restructuring
1. Create new directory structure under `src/features/`
2. Move authentication components and services
3. Move product components and services
4. Move favorites components and services
5. Move profile components and services
6. Move analytics/dashboard components
7. Consolidate shared utilities
8. Update all import paths
9. Verify build succeeds

### 5.3 Phase 3: Database and Configuration
1. Review Flyway migrations (no changes needed to schema)
2. Update environment configuration if necessary
3. Ensure connection pooling is optimal

---

## 6. Benefits of This Architecture

| Aspect | Before (Layered) | After (Vertical Slice) |
|--------|-----------------|------------------------|
| **Feature Location** | Scattered across layers | All in one directory |
| **Adding New Feature** | Touch multiple layers | Add new slice directory |
| **Team Assignment** | Shared layer ownership | Clear slice ownership |
| **Testing** | Global test structure | Feature-specific tests |
| **Dependency Management** | Circular dependencies possible | Clear feature boundaries |
| **Deployment** | Deploy entire application | Can deploy slices independently |
| **Maintainability** | Hard to find related code | Easy to find feature code |

---

## 7. Implementation Guidelines

### 7.1 Feature Package Structure
Each feature (slice) should contain:
- **controller/** - REST endpoints
- **service/** - Business logic
- **entity/** - Domain models
- **repository/** - Data access
- **dto/** - Data transfer objects
- **README.md** - Feature documentation

### 7.2 Naming Conventions
- Feature directories: `lowercase-with-hyphens` or `camelCase`
- Classes: PascalCase (e.g., `ProductController`)
- Methods: camelCase (e.g., `createProduct()`)
- DTOs: `[Action][Entity]DTO` (e.g., `CreateProductDTO`)

### 7.3 Dependency Flow
```
Controller → Service → Repository → Database
              ↑        ↑
              └── DTO/Entity

Features should NOT depend on other features'
implementation details. Use shared DTOs for
inter-feature communication.
```

### 7.4 Cross-Cutting Concerns
Handled in `shared/`:
- Security & Authentication
- Exception handling
- Logging
- Validation
- Caching
- Configuration

---

## 8. Testing Strategy

### 8.1 Test Types by Feature
1. **Unit Tests** - Test individual classes in isolation
2. **Service Tests** - Test business logic
3. **Integration Tests** - Test feature end-to-end
4. **API Tests** - Test REST endpoints

### 8.2 Test Coverage Goals
- Unit Tests: >= 80% coverage
- Service Tests: >= 85% coverage
- Integration Tests: >= 90% coverage

### 8.3 Test Data Management
- Use `test-data.sql` for database setup
- Use TestContainers for database in tests
- Mock external dependencies (Google OAuth, email, etc.)

---

## 9. Migration Checklist

- [ ] Create new directory structure
- [ ] Move backend classes (no code changes)
- [ ] Update all Java imports
- [ ] Build and test backend
- [ ] Move frontend components (no logic changes)
- [ ] Update all TypeScript imports
- [ ] Build and test frontend
- [ ] Verify all endpoints work
- [ ] Verify all UI pages work
- [ ] Create comprehensive test plan
- [ ] Implement automated tests
- [ ] Run full regression tests
- [ ] Document findings
- [ ] Prepare for merge to main

---

## 10. Success Criteria

✓ All existing features work exactly as before  
✓ Project structure is clearly organized by feature  
✓ Each feature is independently deployable (potential)  
✓ Tests have >= 80% code coverage  
✓ Build succeeds without warnings  
✓ All endpoints respond correctly  
✓ No functional regressions detected  
✓ Clear documentation for each feature  

---

## 11. Post-Refactoring Improvements

After this refactoring is complete, future improvements could include:
- Modularity as separate libraries/microservices
- Feature flags for gradual rollout
- Independent feature testing pipelines
- Feature-based CI/CD workflows
- Separate deployment of slices

---

## 12. Appendix: Key Files to Move

### Backend
- Controllers: 3 files
- Services: 5 files
- Entities: 4 files
- DTOs: ~8 files
- Repositories: 4+ files

### Frontend
- Components: 18 files
- Services: 1 (will split)
- Store: 1 (will split)

**Total affected files:** ~50+ files

---

**Document Version:** 1.0  
**Last Updated:** May 1, 2026  
**Status:** In Execution
