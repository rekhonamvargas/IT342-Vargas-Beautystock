# BeautyStock - Comprehensive Test Plan

**Project:** BeautyStock - Beauty Inventory Tracker  
**Date:** May 2, 2026  
**Version:** 1.0  
**Architecture:** Vertical Slice Architecture (VSA)

---

## 1. OVERVIEW

This Test Plan provides comprehensive coverage of all functional requirements for the BeautyStock application across all platforms (Backend API, Web Frontend, Mobile Application).

---

## 2. SCOPE OF TESTING

### 2.1 Features Covered
- **Authentication:** Email registration, email login, Google OAuth 2.0, role selection
- **Products:** Create, read, update, delete products; search and filter
- **Favorites:** Add/remove favorites, view favorites list
- **Profile:** View user profile, update user information
- **Dashboard:** View inventory overview, analytics, skincare recommendations
- **Email Notifications:** Welcome emails, login notifications with custom messages

### 2.2 Platforms
- Backend API (Spring Boot 3.2.2 on Java 17+)
- Web Frontend (React 18 with TypeScript, Vite)
- Mobile Application (Android/Kotlin with Gradle)

### 2.3 Out of Scope
- Load testing
- Performance testing
- Security penetration testing

---

## 3. TEST STRATEGY

### 3.1 Testing Levels
1. **Unit Testing** - Individual components/methods
2. **Integration Testing** - Component interactions, API endpoints
3. **End-to-End Testing** - Full user workflows
4. **Regression Testing** - All features after refactoring

### 3.2 Test Types
- Functional Testing
- UI/UX Testing
- Database Testing
- API Testing

---

## 4. FUNCTIONAL REQUIREMENTS & TEST CASES

### 4.1 AUTHENTICATION FEATURE

#### 4.1.1 Email Registration
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| AUTH-REG-001 | Register with valid email and password | User created, redirected to login | ❌ |
| AUTH-REG-002 | Register with duplicate email | Error message displayed | ❌ |
| AUTH-REG-003 | Register with invalid email format | Validation error shown | ❌ |
| AUTH-REG-004 | Register with weak password | Password requirements message | ❌ |
| AUTH-REG-005 | Register with empty fields | Required field errors | ❌ |
| AUTH-REG-006 | Welcome email sent on registration | Email received with custom Victorian message | ❌ |

#### 4.1.2 Email Login
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| AUTH-LOGIN-001 | Login with valid credentials | Token stored, redirected to dashboard | ❌ |
| AUTH-LOGIN-002 | Login with incorrect password | "Invalid credentials" error | ❌ |
| AUTH-LOGIN-003 | Login with non-existent email | "Invalid credentials" error | ❌ |
| AUTH-LOGIN-004 | Login with empty email | Validation error | ❌ |
| AUTH-LOGIN-005 | Login with empty password | Validation error | ❌ |
| AUTH-LOGIN-006 | Session maintains across page refresh | User still logged in | ❌ |

#### 4.1.3 Google OAuth 2.0
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| AUTH-OAUTH-001 | Sign in with Google (new user) | New user created, redirected to role selection | ❌ |
| AUTH-OAUTH-002 | Sign in with Google (existing user) | Token generated, redirected to dashboard | ❌ |
| AUTH-OAUTH-003 | Google user data mapped correctly | Email, name, profile picture saved | ❌ |
| AUTH-OAUTH-004 | Invalid OAuth credentials | Error message displayed | ❌ |
| AUTH-OAUTH-005 | Google redirect URI working | OAuth flow completes successfully | ❌ |

#### 4.1.4 Role Selection
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| AUTH-ROLE-001 | New Google user sees role selection | Two role buttons displayed (Youth/Adult) | ❌ |
| AUTH-ROLE-002 | Select Youth role | Role updated to ROLE_YOUTH | ❌ |
| AUTH-ROLE-003 | Select Adult role | Role updated to ROLE_ADULT | ❌ |
| AUTH-ROLE-004 | Redirect to dashboard after role selection | User lands on dashboard | ❌ |
| AUTH-ROLE-005 | Existing users skip role selection | Direct login to dashboard | ❌ |

#### 4.1.5 Email Notifications
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| EMAIL-001 | Welcome email sent on registration | Victorian message received | ❌ |
| EMAIL-002 | Login notification on Google OAuth | Email notifies user of login | ❌ |
| EMAIL-003 | Email contains custom message | "Dearest Esteemed User..." message present | ❌ |
| EMAIL-004 | Email deliverability | Email reaches inbox, not spam | ❌ |

#### 4.1.6 Logout
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| AUTH-LOGOUT-001 | Click logout button | Token cleared, redirected to login | ❌ |
| AUTH-LOGOUT-002 | Session cleared after logout | Cannot access protected routes | ❌ |

---

### 4.2 PRODUCTS FEATURE

#### 4.2.1 View Products
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PROD-VIEW-001 | Load products list | All products displayed | ❌ |
| PROD-VIEW-002 | Search products by name | Filtered results shown | ❌ |
| PROD-VIEW-003 | Filter by category | Only category products shown | ❌ |
| PROD-VIEW-004 | Sort by date added | Products in correct order | ❌ |
| PROD-VIEW-005 | Load product detail | Full product info displayed | ❌ |

#### 4.2.2 Add Product
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PROD-ADD-001 | Add product with all fields | Product created successfully | ❌ |
| PROD-ADD-002 | Add product without image | Product created without image | ❌ |
| PROD-ADD-003 | Add product with invalid data | Validation error shown | ❌ |
| PROD-ADD-004 | Product appears in list | New product visible immediately | ❌ |

#### 4.2.3 Edit Product
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PROD-EDIT-001 | Edit product name | Change saved | ❌ |
| PROD-EDIT-002 | Edit product category | Change saved | ❌ |
| PROD-EDIT-003 | Edit product details | All changes persisted | ❌ |

#### 4.2.4 Delete Product
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PROD-DEL-001 | Delete product | Product removed from list | ❌ |
| PROD-DEL-002 | Confirm delete | Confirmation dialog shown | ❌ |
| PROD-DEL-003 | Delete cascades | Favorites deleted if product deleted | ❌ |

---

### 4.3 FAVORITES FEATURE

#### 4.3.1 Add to Favorites
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| FAV-ADD-001 | Add product to favorites | Heart icon filled | ❌ |
| FAV-ADD-002 | Product appears in favorites | Visible in favorites page | ❌ |
| FAV-ADD-003 | Add duplicate | Not added twice | ❌ |

#### 4.3.2 Remove from Favorites
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| FAV-REM-001 | Remove from favorites | Heart icon unfilled | ❌ |
| FAV-REM-002 | Product removed from list | No longer in favorites | ❌ |

#### 4.3.3 View Favorites
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| FAV-VIEW-001 | View empty favorites | "No favorites" message | ❌ |
| FAV-VIEW-002 | View favorites list | All favorited products shown | ❌ |

---

### 4.4 PROFILE FEATURE

#### 4.4.1 View Profile
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PROF-VIEW-001 | Load profile page | User info displayed | ❌ |
| PROF-VIEW-002 | Display role | Current role shown | ❌ |

#### 4.4.2 Update Profile
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PROF-UPD-001 | Update name | Change saved | ❌ |
| PROF-UPD-002 | Update email | Change saved | ❌ |
| PROF-UPD-003 | Invalid data rejected | Validation error shown | ❌ |

---

### 4.5 DASHBOARD FEATURE

#### 4.5.1 Dashboard Overview
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| DASH-VIEW-001 | Load dashboard | Overview displayed | ❌ |
| DASH-VIEW-002 | Show recent products | Latest additions shown | ❌ |
| DASH-VIEW-003 | Show inventory stats | Count accurate | ❌ |

#### 4.5.2 Skincare Advice
| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| ADVICE-001 | Get recommendations | Advice shown for role | ❌ |
| ADVICE-002 | Weather integration | Recommendations based on weather | ❌ |

---

## 5. TEST EXECUTION PLAN

### 5.1 Test Execution Schedule
1. **Unit Tests** - Daily during development
2. **Integration Tests** - After feature completion
3. **Regression Tests** - Before release
4. **E2E Tests** - Weekly

### 5.2 Test Environment
- **Backend:** http://localhost:8080/api
- **Frontend:** http://localhost:3001
- **Database:** PostgreSQL (Neon Cloud)
- **Email:** Gmail SMTP (configured with app password)

### 5.3 Test Data
- Test user 1: sarah.martinez@example.com (password: SecurePass123!)
- Test user 2: rekhona.vargas@gmail.com (Google account)
- Test products: Beauty supplies in various categories

---

## 6. REGRESSION TEST CHECKLIST

### 6.1 Core Features
- [ ] Email registration works
- [ ] Email login works  
- [ ] Google OAuth works
- [ ] Role selection works
- [ ] Welcome email sent (new users)
- [ ] Login notification email sent (returning users)
- [ ] Email contains custom Victorian message
- [ ] Products can be created
- [ ] Products can be viewed
- [ ] Products can be edited
- [ ] Products can be deleted
- [ ] Products can be searched
- [ ] Favorites can be added
- [ ] Favorites can be removed
- [ ] Favorites can be viewed
- [ ] Profile can be viewed
- [ ] Profile can be updated
- [ ] Dashboard displays correctly
- [ ] Skincare advice shown
- [ ] Logout works

### 6.2 API Endpoints
- [ ] POST /api/v1/auth/register
- [ ] POST /api/v1/auth/login
- [ ] GET /api/v1/auth/login/oauth2/code/google
- [ ] PATCH /api/v1/auth/me/role
- [ ] GET /api/v1/auth/me
- [ ] POST /api/v1/auth/logout
- [ ] GET /api/v1/products
- [ ] POST /api/v1/products
- [ ] GET /api/v1/products/{id}
- [ ] PUT /api/v1/products/{id}
- [ ] DELETE /api/v1/products/{id}
- [ ] POST /api/v1/favorites
- [ ] DELETE /api/v1/favorites/{id}
- [ ] GET /api/v1/favorites
- [ ] GET /api/v1/profile
- [ ] PATCH /api/v1/profile

### 6.3 Database
- [ ] User records created correctly
- [ ] Product records created correctly
- [ ] Favorite relationships maintained
- [ ] Data persists after logout/login
- [ ] Cascading deletes work

---

## 7. BUG TRACKING

| Bug ID | Description | Severity | Status |
|--------|-------------|----------|--------|
| BUG-001 | [To be filled during testing] | - | PENDING |

---

## 8. TEST RESULTS SUMMARY

| Feature | Total Tests | Passed | Failed | Pass Rate |
|---------|-------------|--------|--------|-----------|
| Authentication | 15 | 0 | 0 | 0% |
| Products | 10 | 0 | 0 | 0% |
| Favorites | 5 | 0 | 0 | 0% |
| Profile | 5 | 0 | 0 | 0% |
| Dashboard | 5 | 0 | 0 | 0% |
| **TOTAL** | **40** | **0** | **0** | **0%** |

---

## 9. SIGN-OFF

- **Test Plan Created By:** Rekhona Vargas
- **Date Created:** May 2, 2026
- **Test Execution Date:** [To be updated]
- **Test Completion Date:** [To be updated]

