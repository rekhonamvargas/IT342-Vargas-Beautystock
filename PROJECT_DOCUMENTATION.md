# BeautyStock - Project Documentation

## 📋 Short Summary

**BeautyStock** is a full-stack beauty product management application that allows users to create, browse, and manage their beauty product inventory. The platform provides a web interface for desktop users and a mobile app for on-the-go access. Users can upload product images, mark favorites, receive skincare advice, and manage their product collection with a clean, modern interface.

**Status:** Phase 3 – Web Main Feature Completed ✅

---

## 🎯 Main Feature Description

### Product Management & Favorites System

The core functionality of BeautyStock revolves around three interconnected features:

#### **1. Product Management**
- **Create Products:** Users can add new beauty products with details like name, category, description, ingredients, and product images
- **Browse Products:** View all products in a grid layout with filtering and sorting capabilities
- **Product Details:** Detailed product view with full information, images, and related recommendations
- **Edit/Delete Products:** Manage personal product inventory

#### **2. Favorites System**
- Users can mark products as favorites with a heart button
- Favorites are saved to the database and persisted across sessions
- Dedicated Favorites page displays all bookmarked products
- Visual feedback with filled (❤️) and empty (♡) heart states

#### **3. Dashboard Analytics**
- **Total Products:** Count of all products in user's inventory (🧴 emoji)
- **Total Favorites:** Count of bookmarked products (❤️ emoji)
- **Expiring Soon:** Products approaching expiration date (⚠️ emoji)
- Advanced dashboard provides skincare advice and weather-based recommendations

---

## 📝 Inputs and Validations Used

### Frontend Validations

#### **Product Form Inputs:**
```
- Product Name: Required, string, 1-255 characters
- Category: Required, selected from predefined categories
- Description: Text area, optional, up to 1000 characters
- Ingredients: Comma-separated list, optional
- Price: Optional, numeric input
- Product Image: File upload, Base64 encoded, max 5MB
- Expiration Date: Date picker, optional
```

#### **Authentication Inputs:**
```
- Email: Required, valid email format (RFC 5322)
- Password: Required, minimum 6 characters
- Password Confirmation: Match validation for registration
```

#### **Form Validations:**
- Empty field checks
- Email format validation
- File type validation (image only)
- Image size limits
- Duplicate product prevention

### Backend Validations

```java
// Product validation constraints
@NotBlank(message = "Product name is required")
private String productName;

@NotBlank(message = "Category is required")
private String category;

@Column(name = "image_url", columnDefinition = "TEXT")
private String imageUrl; // Supports large Base64 encoded images

@Column(name = "owner_email", nullable = false)
private String ownerEmail; // Associates products with users

// Favorite validation constraints
@NotNull(message = "Product ID is required")
private Long productId;

@NotNull(message = "User must be logged in")
private UUID userId;
```

---

## ⚙️ How the Feature Works

### **Product Addition & Storage Flow**

```
1. User selects "Add Product" button
2. Form opens with product input fields
3. User fills in product details and selects image
4. Image is converted to Base64 data URI
5. Form submits to backend API
6. Backend validates all inputs
7. Product is saved to PostgreSQL database
8. User gets success notification
9. Product appears in dashboard and products grid
```

### **Favorite Toggle Flow**

```
1. User hovers over product card
2. Heart button appears (♡ empty or ❤️ filled)
3. User clicks heart button
4. Frontend sends PATCH request to backend
5. Backend checks if favorite already exists
6. If not exists: Creates new Favorite record with:
   - Product ID
   - User ID (from authenticated session)
   - Owner Email
   - Creation timestamp (auto-generated)
7. Heart button updates in real-time (optimistic update)
8. Favorites count updates instantly
9. FavoritesPage refreshes when navigated to
```

### **Database Constraint Resolution**

The application handles critical data integrity:

```
- created_at: Auto-populated using @CreationTimestamp
- user_id: Fetched from UserRepository and set during favorite creation
- product_id: Foreign key with NOT NULL constraint
- owner_email: Required to associate favorites with specific user
```

### **Image Upload Handling**

```
1. User selects image file from device
2. Frontend FileReader converts to Base64 string
3. Base64 string prefixed with "data:image/[type];base64," 
4. Full data URI sent to backend as JSON
5. Stored in TEXT column (supports large images)
6. Displayed directly in <img src> tags (no additional encoding needed)
```

---

## 🔌 API Endpoints Used

### **Product Management Endpoints**

#### **GET** `/api/products`
- **Purpose:** Retrieve all products by owner email
- **Parameters:** `ownerEmail` (query param)
- **Response:** Array of ProductDTO objects
- **Status:** 200 OK

#### **GET** `/api/products/{id}`
- **Purpose:** Get single product details
- **Parameters:** `id` (path param)
- **Response:** ProductDTO object with full details
- **Status:** 200 OK

#### **POST** `/api/products`
- **Purpose:** Create new product
- **Body:** CreateProductDTO
  ```json
  {
    "productName": "Hydrating Moisturizer",
    "category": "Moisturizer",
    "description": "Face moisturizer",
    "ingredients": "Water, Glycerin, Vitamin E",
    "imageUrl": "data:image/jpeg;base64,/9j/4AAQ...",
    "expirationDate": "2025-12-31",
    "ownerEmail": "user@example.com"
  }
  ```
- **Response:** 201 Created with product ID
- **Status:** 201

#### **PUT** `/api/products/{id}`
- **Purpose:** Update existing product
- **Parameters:** `id` (path param)
- **Body:** CreateProductDTO (updated fields)
- **Response:** Updated ProductDTO
- **Status:** 200 OK

#### **DELETE** `/api/products/{id}`
- **Purpose:** Delete product
- **Parameters:** `id` (path param)
- **Response:** 204 No Content
- **Status:** 204

---

### **Favorites Endpoints**

#### **GET** `/api/favorites/{id}`
- **Purpose:** Check if product is favorited
- **Parameters:** `id` (product ID)
- **Query Params:** `ownerEmail`
- **Response:** Boolean or Favorite object
- **Status:** 200 OK

#### **POST** `/api/favorites/{id}`
- **Purpose:** Add product to favorites
- **Parameters:** `id` (product ID)
- **Query Params:** `ownerEmail`
- **Response:** 201 Created
- **Status:** 201

#### **PATCH** `/api/favorites/{id}`
- **Purpose:** Toggle favorite status
- **Parameters:** `id` (product ID)
- **Query Params:** `ownerEmail`
- **Response:** Updated favorite status
- **Status:** 200 OK

#### **DELETE** `/api/favorites/{id}`
- **Purpose:** Remove from favorites
- **Parameters:** `id` (product ID)
- **Query Params:** `ownerEmail`
- **Response:** 204 No Content
- **Status:** 204

#### **GET** `/api/favorites`
- **Purpose:** Get all favorites for user
- **Query Params:** `ownerEmail`
- **Response:** Array of Favorite objects
- **Status:** 200 OK

---

### **Authentication Endpoints**

#### **POST** `/api/v1/auth/login`
- **Purpose:** User login
- **Body:** `{ "email": "user@example.com", "password": "password123" }`
- **Response:** JWT token or session credentials
- **Status:** 200 OK / 401 Unauthorized

#### **POST** `/api/v1/auth/register`
- **Purpose:** User registration
- **Body:** `{ "email": "user@example.com", "password": "password123" }`
- **Response:** 201 Created / User object
- **Status:** 201 / 409 Conflict (if user exists)

---

## 🗄️ Database Tables Involved

### **1. Users Table**
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);
```
**Purpose:** Store user account information and authentication credentials

---

### **2. Products Table**
```sql
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  product_name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  description TEXT,
  ingredients TEXT,
  image_url TEXT,  -- Stores Base64 encoded images
  price DECIMAL(10, 2),
  expiration_date DATE,
  owner_email VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE
);
```
**Purpose:** Store beauty product information and metadata
**Key Fields:**
- `image_url` is TEXT type (converted from VARCHAR) to support large Base64-encoded images
- `owner_email` links products to specific users
- `created_at` auto-populated for tracking

---

### **3. Favorites Table**
```sql
CREATE TABLE favorites (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  owner_email VARCHAR(255) NOT NULL,
  user_id UUID NOT NULL,  -- Converted from BIGINT to UUID (V3 Migration)
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE(product_id, owner_email, user_id)
);
```
**Purpose:** Track which products are marked as favorites by users
**Key Features:**
- Composite unique constraint prevents duplicate favorites
- `created_at` auto-timestamp tracks when favorite was added
- `user_id` UUID type links to users table
- Cascade delete maintains referential integrity

---

### **Migration History**

#### **V1: Increase Column Sizes**
```sql
ALTER TABLE products ALTER COLUMN product_name TYPE VARCHAR(500);
ALTER TABLE products ALTER COLUMN description TYPE TEXT;
```
**Reason:** Support longer product names and descriptions

#### **V2: Fix Product Columns**
```sql
ALTER TABLE products ALTER COLUMN image_url TYPE TEXT;
```
**Reason:** Support large Base64-encoded image data (previously limited to 255 characters)

#### **V3: Fix Favorites User ID Column**
```sql
ALTER TABLE favorites ALTER COLUMN user_id TYPE uuid USING user_id::text::uuid;
```
**Reason:** Convert user_id to UUID type for proper relationship with users table

---

## 🎨 Frontend Components Architecture

### **Key Components:**

| Component | Purpose | Features |
|-----------|---------|----------|
| **Dashboard** | Main landing page | Statistics cards (products, favorites, expiring), weather display, skincare advice |
| **ProductsPage** | Product grid display | Filter by category, sort by name/date, add product button |
| **FavoritesPage** | Favorites collection | Display bookmarked products, auto-reload on navigation |
| **AddProductPage** | Create new product | Form with image upload, validation, success notification |
| **ProductDetail** | Single product view | Full details, image preview, favorite toggle, related products |
| **ProductCard** | Reusable product display | Name, image, heart button, price, category |
| **ProductImage** | Image rendering | Base64 display, responsive sizing, loading states |

---

## 🚀 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Frontend** | React 18 + TypeScript | Latest |
| **Build Tool** | Vite | 5.4.21 |
| **Styling** | Tailwind CSS | Latest |
| **HTTP Client** | Axios | Latest |
| **Router** | React Router v6 | Latest |
| **Backend** | Spring Boot | 3.2.2 |
| **Database** | PostgreSQL | 17.8 (Neon) |
| **Migration Tool** | Flyway | 10.0.0 |
| **Mobile** | Android (Kotlin/Gradle) | Latest |

---

## 📦 Current Status

✅ **Phase 3 – Web Main Feature Completed**

### Completed Features:
- ✅ User authentication (login/register)
- ✅ Product creation with image upload
- ✅ Product browsing and filtering
- ✅ Favorites system with persistence
- ✅ Dashboard with statistics
- ✅ Responsive web UI with Tailwind CSS
- ✅ Decorative fonts (Playfair Display) for headings
- ✅ Emoji-enhanced UI (🧴 products, ❤️ favorites, ⚠️ expiring)
- ✅ Database migrations (V1-V3)
- ✅ API endpoints functional
- ✅ GitHub repository pushed

### Access Points:
- **Frontend:** http://localhost:3015
- **Backend API:** http://localhost:8080
- **GitHub:** https://github.com/rekhonamvargas/IT342-Vargas-Beautystock.git

---

## 🔐 Security Considerations

1. **Authentication:** Credentials validated on backend before granting access
2. **Data Validation:** All inputs validated both client and server-side
3. **Database Constraints:** NOT NULL, UNIQUE, FOREIGN KEY constraints enforce data integrity
4. **Image Storage:** Base64-encoded images stored as text, protected with owner_email association
5. **Cascade Delete:** Proper cleanup when users or products are deleted

---

## 📝 Notes for Future Development

- Consider implementing refresh tokens for extended sessions
- Add pagination for large product lists
- Implement product search functionality
- Add more detailed product analytics
- Consider caching frequently accessed products
- Implement user profile customization
- Add product recommendations algorithm
- Consider implementing product reviews/ratings

---

**Documentation Last Updated:** April 6, 2026  
**Project Lead:** User (VARGAS)  
**Repository:** IT342 Beautystock Project
