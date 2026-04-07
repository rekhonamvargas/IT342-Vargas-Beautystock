# Design Patterns Refactoring - Git Commit Plan

## Branch Setup

```bash
# Create feature branch
git checkout -b feature/design-patterns-refactor

# Confirm branch creation
git branch -v
```

---

## Commit Strategy

We'll organize commits by pattern domain for clear, logical history.

### Phase 1: Backend Exception Handling & Type Safety
```bash
git add backend/src/main/java/com/beautystock/exception/

# Commit 1: Foundation
git commit -m "refactor(exceptions): create custom exception hierarchy with type-safe base class

- Add BeautyStockException abstract base class with HttpStatus and error codes
- Improves type-safe exception handling vs string-based routing
- Foundation for Strategy Pattern implementation"

# Commit 2: Strategy Pattern
git commit -m "refactor(exceptions): implement Strategy Pattern for exception handling

- Create ExceptionHandlingStrategy interface with 4 concrete implementations
- Replaces if-else chains in GlobalExceptionHandler with strategy delegation
- Each exception type has dedicated handler; easy to extend with new strategies  
- Improves maintainability and testability of error handling"
```

### Phase 2: Backend Token Generation
```bash
git add backend/src/main/java/com/beautystock/service/auth/JwtTokenBuilder.java

git commit -m "refactor(auth): implement Builder Pattern for JWT token generation

- Extract JWT creation logic from AuthService into reusable JwtTokenBuilder
- Provides fluent API for flexible token configuration
- Enables independent testing of token generation
- Separates token concerns from authentication business logic"
```

### Phase 3: Backend DTO Mapping
```bash
git add backend/src/main/java/com/beautystock/mapper/

git commit -m "refactor(mapper): implement Factory Pattern for DTO mapping

- Create EntityMapper<E, D> generic interface for entity-to-DTO transformations
- Add ProductMapper implementation centralizing all Product mapping logic
- Eliminates 8+ repetitive mapping calls throughout codebase
- Single source of truth for product transformation rules"
```

### Phase 4: Backend User Context Abstraction
```bash
git add backend/src/main/java/com/beautystock/service/auth/CurrentUserProvider*

git commit -m "refactor(auth): decouple services from Spring Security context

- Create CurrentUserProvider interface abstracting SecurityContext dependency
- Add SecurityContextUserProvider concrete implementation
- Services now depend on interface, not framework classes
- Improves testability - can mock user provider without Spring Security setup"
```

### Phase 5: Backend Image Processing Strategy
```bash
git add backend/src/main/java/com/beautystock/service/product/ImageProcessing*

git commit -m "refactor(product): implement Strategy Pattern for image processing

- Create ImageProcessingStrategy interface for pluggable processing
- Implement Base64ImageProcessingStrategy for current functionality
- Extract 40 lines of image handling from ProductService
- Enables future cloud storage implementations without code changes"
```

### Phase 6: Backend Service Decomposition
```bash
git add backend/src/main/java/com/beautystock/service/product/

git commit -m "refactor(product): apply Facade Pattern to decompose ProductService

- Extract FavoriteService (single responsibility: manage favorites)
- Extract DashboardService (single responsibility: aggregate statistics)
- Extract ProductImageService (single responsibility: handle images)
- Create ProductFacade coordinating all services with clean interface
- Original ProductService reduced from 300+ lines with 15+ methods
- Each service now testable and reusable independently"
```

### Phase 7: Frontend API Organization  
```bash
git add web/src/services/apiClients/
git add web/src/services/ApiClientFactory.ts

git commit -m "refactor(api): implement Factory Pattern for API client organization

- Create domain-specific API clients: AuthApiClient, ProductApiClient, etc.
- Centralize HTTP configuration in ApiClientFactory
- Replace single 100+ line api.ts with organized domain files
- Improves code navigation and maintainability
- Maintains backward compatibility with exported convenience functions"
```

### Phase 8: Frontend Store Factory
```bash
git add web/src/store/createAsyncStore.ts

git commit -m "refactor(store): implement Factory Pattern for Zustand store creation

- Create createAsyncStore generic factory for consistent async stores
- Implements standard pattern: data/isLoading/error + setters
- Replaces 3 duplicate store implementations with 1 factory
- All async stores now automatically have same behavior
- 50+ lines of duplication eliminated"
```

### Phase 9: Frontend Error Handling
```bash
git add web/src/services/ErrorHandler.ts

git commit -m "refactor(error): implement Strategy Pattern for centralized error handling

- Create ErrorHandler utility with ErrorType enumeration
- Classify errors by type and return formatted error info
- Provides consistent error messages across all components
- Eliminates repeated error handling logic in each component
- Single source of truth for error handling strategy"
```

### Phase 10: Frontend Custom Hooks
```bash
git add web/src/hooks/useAsyncData.ts
git add web/src/hooks/useFavoriteToggle.ts

git commit -m "refactor(hooks): create custom hooks for data fetching and state management

- Add useAsyncData<T> hook: handles fetching with loading/error states
- Add useFavoriteToggle hook: manages favorite toggling with optimistic updates
- Reduces component code 40-50% by extracting data layer logic
- Hooks are independently testable with React Testing Library
- Promotes reusability across multiple components"
```

### Phase 11: Documentation & Summary
```bash
git add DESIGN_PATTERNS_ANALYSIS.md
git add BACKEND_REFACTORING_REPORT.md
git add FRONTEND_REFACTORING_REPORT.md
git add REFACTORING_MIGRATION_GUIDE.md

git commit -m "docs: add comprehensive design patterns refactoring documentation

- DESIGN_PATTERNS_ANALYSIS: Initial analysis + 10 pattern recommendations
- BACKEND_REFACTORING_REPORT: Before/after examples for 6 backend patterns
- FRONTEND_REFACTORING_REPORT: Before/after examples for 4 frontend patterns
- REFACTORING_MIGRATION_GUIDE: Step-by-step guide for team implementation
- Includes benefits, architectural changes, and migration notes"
```

---

## Final Integration Commit

```bash
# After all refactoring is validated and working
git commit --allow-empty -m "refactor: complete design patterns migration phase

BREAKING CHANGES: NONE (backward compatible implementation)

Refactoring Summary:
- Backend: 15 new pattern implementation classes
- Frontend: 8 new files with organized domain logic
- Total Improvements:
  * Exception Handling: String-based → Strategy Pattern (Type-safe)
  * Token Generation: Mixed logic → Builder Pattern (Isolated)
  * DTO Mapping: Repetitive → Factory Pattern (DRY)
  * User Context: Tightly coupled → Repository Pattern (Testable)
  * Image Processing: Mixed → Strategy Pattern (Extensible)
  * Services: God Object → Facade Pattern (SRP)
  * API Clients: Single file → Factory Pattern (Organized)
  * Store Creation: Repetitive → Factory Pattern (Consistent)
  * Error Handling: Scattered → Strategy Pattern (Centralized)
  * Data Fetching: Mixed logic → Custom Hooks (Clean)

All patterns follow SOLID principles and real-world best practices.
See DESIGN_PATTERNS_ANALYSIS.md for complete rationale."
```

---

## Branch Merge

```bash
# After all commits, prepare for merge
git log --oneline  # Review commit history

# Merge into main (after code review)
git checkout main
git merge --no-ff feature/design-patterns-refactor

# Create release tag
git tag -a v1.1.0-refactored -m "Version 1.1.0: Design Patterns Refactoring"
git push origin main feature/design-patterns-refactor v1.1.0-refactored
```

---

## Commit Message Convention

**Format:**
```
type(scope): subject line (50 chars max)

body (optional): explanation of changes (wrap at 72 chars)

footer (optional): breaking changes, issue references, etc.
```

**Types:**
- `refactor`: Code restructuring without functional changes
- `feat`: New features
- `fix`: Bug fixes
- `docs`: Documentation changes
- `test`: Test additions/modifications
- `chore`: Build, dependency, tooling changes

**Scopes:**
- `exceptions`: Exception handling
- `auth`: Authentication & JWT  
- `mapper`: DTO mapping
- `product`: Product service
- `api`: API clients
- `store`: Zustand stores
- `error`: Error handling
- `hooks`: Custom React hooks

**Examples:**
```
refactor(exceptions): implement Strategy Pattern for exception handling
refactor(mapper): extract ProductMapper from ProductService
refactor(product): apply Facade Pattern to decompose service
refactor(hooks): create useAsyncData hook for data fetching
```

---

## Validation Steps

Before pushing to main:

1. **Compile/Build**
   ```bash
   # Backend
   cd backend && mvn clean compile
   
   # Frontend  
   cd web && npm run build
   ```

2. **Existing Tests Pass**
   ```bash
   # Backend tests
   cd backend && mvn test
   
   # Frontend tests
   cd web && npm test
   ```

3. **No Breaking Changes**
   - Old imports still work (backward compatibility maintained)
   - No API route changes
   - No database schema changes

4. **Code Review Checklist**
   - [ ] Patterns correctly implemented
   - [ ] SOLID principles followed
   - [ ] Backward compatible
   - [ ] Documentation updated
   - [ ] No hardcoded values
   - [ ] Proper error handling
   - [ ] Follows naming conventions

---

## Alternative: Cherry-Pick Commits

If you want to implement patterns incrementally:

```bash
# Cherry-pick individual pattern commits to other branches
git cherry-pick feature/design-patterns-refactor~10  # Auth pattern
git cherry-pick feature/design-patterns-refactor~5   # Service decomposition

# This allows rolling out patterns independently
```

---

## Rollback Plan (if needed)

```bash
# Revert entire refactor branch
git revert -m 1 <merge-commit-hash>

# Or revert specific commits
git revert <commit-hash>

# This maintains history while undoing changes
```

