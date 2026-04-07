# Design Patterns Refactoring Analysis
## BeautyStock Project

**Date**: April 7, 2026  
**Scope**: React Frontend + Spring Boot Backend + Mobile  
**Goal**: Improve code organization, reusability, maintainability, and scalability

---

## 📊 STEP 1: CODE ANALYSIS & ISSUES IDENTIFIED

### Backend (Spring Boot) Issues

#### 🔴 **Issue 1: Mixed Responsibilities in `AuthService`**
- **File**: `backend/src/main/java/com/beautystock/service/AuthService.java`
- **Problem**:
  - JWT generation mixed with business logic
  - Password validation mixed with registration flow
  - Multiple concerns: token generation, user creation, refresh token management
  - Token generation has imperative logic (Key creation, Date calculations)
- **Impact**: Hard to test, reuse, or extend token generation; rigid and coupled

#### 🔴 **Issue 2: God Object - `ProductService` (Too Many Responsibilities)**
- **File**: `backend/src/main/java/com/beautystock/service/ProductService.java`
- **Problem**:
  - Single service handles CRUD, favorites, dashboard, search, filtering, image upload
  - Over 300 lines with 15+ methods
  - Tight coupling between product and favorite operations
  - Image processing logic intertwined with product update
- **Impact**: Violation of Single Responsibility Principle (SRP); difficult to test; hard to maintain

#### 🔴 **Issue 3: Repetitive Manual DTO Mapping**
- **File**: `backend/src/main/java/com/beautystock/service/ProductService.java:toDto()` method
- **Problem**:
  - Manual conversion from Product → ProductDTO across multiple methods
  - Similar mapping logic potentially repeated in other services
  - No abstraction for transformation logic
- **Impact**: Code duplication; hard to maintain field mappings; error-prone

#### 🔴 **Issue 4: Switch-Like Exception Handling Logic**
- **File**: `backend/src/main/java/com/beautystock/exception/GlobalExceptionHandler.java`
- **Problem**:
  - Multiple if-else statements checking exception message strings
  - Status detection based on error message content (fragile)
  - No type-safe exception hierarchy
- **Impact**: Brittle error handling; coupled to message text; difficult to extend

#### 🔴 **Issue 5: Direct Object Creation with `new` Instead of Dependency Injection**
- **File**: `backend/src/main/java/com/beautystock/service/ProductService.java:uploadImage()`
- **Problem**:
  - `Base64.getEncoder()` instantiated locally instead of injected/configured
  - Tight coupling to concrete implementation
- **Impact**: Hard to mock for testing; difficult to swap implementations

#### 🟡 **Issue 6: Product Service Directly Accesses Security Context**
- **File**: `backend/src/main/java/com/beautystock/service/ProductService.java:getCurrentEmail()`
- **Problem**:
  - Service tightly coupled to Spring Security context
  - Business logic mixed with authentication concerns
- **Impact**: Hard to test in isolation; difficult to reuse in different auth contexts

---

### Frontend (React) Issues

#### 🔴 **Issue 7: Mixed Concerns in Components**
- **File**: `web/src/components/Dashboard.tsx` and others
- **Problem**:
  - Components handle API calls, state management, rendering, business logic
  - Data fetching logic coupled to component lifecycle
  - Repetitive favorite toggle pattern across components
- **Impact**: Hard to test; difficult to reuse logic; testing requires heavy mocking

#### 🔴 **Issue 8: Large, Flat API Service File**
- **File**: `web/src/services/api.ts`
- **Problem**:
  - All API calls in single file (100+ lines)
  - No separation by domain (auth, product, favorite)
  - Error interceptor mixed with HTTP client setup
  - No abstraction for common patterns (list, create, update, delete)
- **Impact**: Difficult to maintain; hard to test interceptors; poor organization

#### 🔴 **Issue 9: Multiple Zustand Stores with Similar Patterns**
- **File**: `web/src/store/auth.ts`
- **Problem**:
  - Repeated store creation pattern: `useAuthStore`, `useWeatherStore`, `useDashboardStore`
  - Copy-paste implementation of loading/error state
  - No factory or common abstraction
- **Impact**: Code duplication; hard to maintain consistency; error-prone updates

#### 🔴 **Issue 10: Repetitive Error Handling in Components**
- **File**: `web/src/components/Dashboard.tsx`, other components
- **Problem**:
  - Each component implements its own error handling
  - Loading states managed manually
  - Try-catch blocks with hardcoded messages
- **Impact**: Inconsistent UX; redundant code; hard to centralize error strategy

---

## 🧩 STEP 2: DESIGN PATTERNS RECOMMENDATIONS

### Backend Patterns

#### **Pattern 1: Builder Pattern** → JWT Token Generation
- **Where**: `AuthService.generateJWT()`
- **Problem Being Solved**: Complex JWT creation with multiple parameters
- **Implementation**:
  - Create `JwtTokenBuilder` class
  - Fluent API for configurable token creation
  - Separates token generation from business logic
- **Benefits**:
  - ✅ Easier to test in isolation
  - ✅ Reusable JWT construction
  - ✅ Readable, expressive code
  - ✅ Easy to extend (new claims, algorithms)

#### **Pattern 2: Factory Pattern** → DTO Mapping
- **Where**: Create `DtoMapperFactory` or `EntityMapper<T, D>`
- **Problem Being Solved**: Repetitive manual DTO conversions
- **Implementation**:
  - Interface: `EntityMapper<Entity, DTO>`
  - Concrete: `ProductMapper implements EntityMapper<Product, ProductDTO>`
  - Factory collects all mappers; services request via interface
- **Benefits**:
  - ✅ Centralized mapping logic
  - ✅ Easy to test mappers independently
  - ✅ Reusable across services
  - ✅ Single place to update mappings

#### **Pattern 3: Strategy Pattern** → Exception Handling
- **Where**: Create custom exception hierarchy + `ExceptionStrategy`
- **Problem Being Solved**: String-based exception routing
- **Implementation**:
  - Custom exceptions: `ProductNotFoundException`, `InvalidCredentialsException`
  - `ExceptionStrategy` interface with implementations
  - `GlobalExceptionHandler` uses strategy to handle exceptions
- **Benefits**:
  - ✅ Type-safe error handling
  - ✅ Easy to extend with new exception types
  - ✅ Better separation of concerns
  - ✅ Testable exception handling

#### **Pattern 4: Facade Pattern** → ProductService Simplification
- **Where**: Break `ProductService` into smaller services + create `ProductFacade`
- **Problem Being Solved**: God Object with too many responsibilities
- **Implementation**:
  - Create focused services: `ProductCrudService`, `FavoriteService`, `DashboardService`, `ImageUploadService`
  - `ProductFacade` coordinates calls to specialized services
  - Controllers use facade for simplified interface
- **Benefits**:
  - ✅ Each service has single responsibility
  - ✅ Easier to test individual services
  - ✅ Better code organization
  - ✅ More flexible for feature additions

#### **Pattern 5: Strategy Pattern** → Image Upload Processing
- **Where**: Create `ImageProcessingStrategy` interface
- **Problem Being Solved**: Image processing logic mixed with domain logic
- **Implementation**:
  - `ImageProcessingStrategy` interface with `Base64Strategy`, `FileStorageStrategy`
  - Injected into service (dependency injection)
  - Can switch strategies without code changes
- **Benefits**:
  - ✅ Decoupled from concrete implementation
  - ✅ Easy to mock for testing
  - ✅ Extensible for new strategies (cloud storage, compression)

#### **Pattern 6: Repository Pattern** → Auth Context Abstraction
- **Where**: Create `CurrentUserProvider` interface
- **Problem Being Solved**: Service tightly coupled to Security context
- **Implementation**:
  - Interface: `CurrentUserProvider { getCurrentEmail(): String }`
  - Implementation: `SecurityContextUserProvider implements CurrentUserProvider`
  - Services depend on interface, not Spring Security directly
- **Benefits**:
  - ✅ Services testable without security context
  - ✅ Easy to mock for unit tests
  - ✅ Can swap implementations for different auth systems

---

### Frontend Patterns

#### **Pattern 7: Factory Pattern** → API Client Organization
- **Where**: Create `ApiClientFactory` with endpoint factories
- **Problem Being Solved**: Large flat api.ts file; poor organization
- **Implementation**:
  - `AuthApiClient` class with auth endpoints
  - `ProductApiClient` class with product endpoints
  - `FavoriteApiClient` class with favorite endpoints
  - Factory method to instantiate clients with shared axios instance
- **Benefits**:
  - ✅ Better code organization
  - ✅ Each domain has endpoint grouped
  - ✅ Easy to locate and maintain endpoints
  - ✅ Reusable across components

#### **Pattern 8: Observer Pattern** → Zustand Store Factory
- **Where**: Create generic store factory function
- **Problem Being Solved**: Repeated store creation pattern
- **Implementation**:
  - Generic function: `createAsyncStore<T>()` with loading/error state
  - Reusable for auth, weather, dashboard stores
  - Consistent handling of loading and error states
- **Benefits**:
  - ✅ No code duplication
  - ✅ Consistent store behavior
  - ✅ Easy to add new stores with same pattern
  - ✅ Maintainable state management

#### **Pattern 9: Strategy Pattern** → Error Handling in Components
- **Where**: Create `ErrorHandler` class/utility
- **Problem Being Solved**: Repeated error handling logic in components
- **Implementation**:
  - `HttpErrorStrategy` interface with implementations
  - `ErrorHandler` class uses strategy to handle different error types
  - Components call `ErrorHandler.handle()` instead of duplicating logic
- **Benefits**:
  - ✅ Consistent error presentation
  - ✅ Reduced component code
  - ✅ Centralized error handling logic
  - ✅ Easy to test error scenarios

#### **Pattern 10: Custom Hook Pattern** → API Data Fetching
- **Where**: Create `useFetch()` and `useFavoriteToggle()` hooks
- **Problem Being Solved**: Mixed API calls and UI logic in components
- **Implementation**:
  - `useAsyncData<T>()` hook handles fetching + loading + error
  - `useFavoriteToggle()` hook handles favorite add/remove with optimistic updates
  - Components use hooks to abstract data layer
- **Benefits**:
  - ✅ Cleaner component code
  - ✅ Reusable data fetching logic
  - ✅ Better testability
  - ✅ Separation of concerns

---

## 📋 SUMMARY TABLE

| Issue | Pattern | Benefit | Priority |
|-------|---------|---------|----------|
| JWT token mixed logic | Builder | Testable, reusable | 🔴 High |
| Repetitive DTO mapping | Factory | DRY, maintainable | 🔴 High |
| String-based exceptions | Strategy + Custom Exceptions | Type-safe, extensible | 🔴 High |
| God Object Service | Facade | SRP, organized | 🔴 High |
| Image processing coupled | Strategy | Decoupled, mockable | 🟡 Medium |
| Security context tightly coupled | Repository | Testable, isolated | 🟡 Medium |
| Large flat API file | Factory | Organized, findable | 🔴 High |
| Repeated store patterns | Factory | DRY, consistent | 🔴 High |
| Component error handling | Strategy | Consistent, reusable | 🟡 Medium |
| Mixed component concerns | Custom Hooks | Clean, testable | 🔴 High |

---

## 🎯 NEXT STEPS

1. **Refactor Backend** (Priority: High)
   - Implement Builder Pattern for JWT
   - Create Factory Pattern for DTO mapping
   - Break down ProductService with Facade
   - Create custom exception hierarchy

2. **Refactor Frontend** (Priority: High)
   - Organize API client with Factory Pattern
   - Create Zustand store factory
   - Build custom hooks for data fetching
   - Implement error handler strategy

3. **Create Tests** (Priority: High)
   - Unit tests for isolated services
   - Component tests with mocked services
   - Integration tests for happy paths

4. **Update Documentation** (Priority: Medium)
   - Document new patterns
   - Update architecture diagrams
   - Create migration guide

---

**Total Issues Identified**: 10  
**Patterns Recommended**: 10  
**Estimated Refactoring Time**: 4-6 hours  
**Estimated Testing Time**: 2-3 hours  
**Total Effort**: 2-3 days (distributed implementation)
