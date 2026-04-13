# Third-Party Login (OAuth2) Implementation - BeautyStock

## Brief Description

BeautyStock has successfully implemented Google OAuth2 authentication, enabling users to sign in and register using their Google accounts. This eliminates the need for password management while providing a seamless, secure authentication experience. The implementation uses Spring Security OAuth2 Client on the backend and integrates with Google's OAuth2 provider.

### Key Features:
- **Google OAuth2 Sign-In**: Users can authenticate using their Google credentials
- **Automatic User Creation**: New users are automatically registered in the system upon their first OAuth login
- **Welcome Email**: New OAuth users receive a welcome email
- **JWT Token Generation**: OAuth tokens are converted to JWT for API consistency
- **Graceful Fallback**: Users can still use email/password authentication or other OAuth providers
- **Secure Redirect Handling**: Success and failure redirects with proper error handling

---

## Backend Technologies/Libraries Used

### Spring Security & OAuth2
```xml
<!-- Spring Security OAuth2 Client - Handles OAuth2 flow -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
</dependency>

<!-- Spring Security OAuth2 JOSE - For OIDC token validation -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>

<!-- Google API Client - For direct Google API interactions -->
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Supporting Technologies
- **Spring Boot 3.2.2**: Framework for building the backend
- **Spring Web Security**: Authentication and authorization
- **JWT (io.jsonwebtoken 0.12.3)**: Token generation and validation
- **BCrypt**: Password encoding for traditional auth
- **PostgreSQL 42.7.1**: User data persistence
- **Flyway**: Database migrations

---

## Implementation Architecture

### 1. Backend Configuration

#### SecurityConfig.java
- Configures Spring Security with OAuth2 support
- Permits OAuth endpoints: `/oauth2/**` and `/login/oauth2/**`
- Integrates custom `OAuth2AuthenticationSuccessHandler` and `OAuth2AuthenticationFailureHandler`
- Uses stateless session management (JWT-based)
- CORS configuration for frontend communication

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
        );
        // ... additional config
    }
}
```

#### OAuth2AuthenticationSuccessHandler.java
**Purpose**: Handles successful OAuth2 authentication

**Process**:
1. Extracts user information from OAuth2 principal:
   - Email
   - Given name & Family name
   - Profile picture
   - Google ID (sub)

2. Checks if user exists in database
   - If new user: Creates account with OAuth details, sends welcome email
   - If existing: Updates last login timestamp

3. Generates JWT token for session management
4. Redirects to frontend OAuth callback URL with token as query parameter

**Key Code**:
```java
OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
String email = oAuth2User.getAttribute("email");
String googleId = oAuth2User.getAttribute("sub");

// User auto-registration or retrieval
User user = userRepository.findByEmail(email).orElse(null);
if (user == null) {
    user = new User();
    user.setEmail(email);
    user.setGoogleId(googleId);
    userRepository.save(user);
    emailService.sendWelcomeEmail(email, user.getFullName());
}

String token = jwtTokenProvider.generateToken(user);
String redirectUrl = redirectSuccessUrl + "?token=" + token;
response.sendRedirect(redirectUrl);
```

#### OAuth2AuthenticationFailureHandler.java
**Purpose**: Handles OAuth2 authentication failures

**Features**:
- Captures detailed error messages
- URL-encodes error messages for safe transmission
- Redirects to login page with error parameter
-Frontend can display user-friendly error messages

#### application.yml Configuration
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            redirect-uri: http://localhost:8080/api/login/oauth2/code/google
        provider:
          google:
            issuer-uri: https://accounts.google.com
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://www.googleapis.com/oauth2/v4/token
            user-info-uri: https://www.googleapis.com/oauth2/v2/userinfo
            user-name-attribute: email

app:
  oauth2:
    redirect-success-url: ${OAUTH2_REDIRECT_SUCCESS_URL}
    redirect-failure-url: ${OAUTH2_REDIRECT_FAILURE_URL}
```

### 2. Database Schema Updates

#### Users Table Enhancement
```sql
ALTER TABLE users ADD COLUMN google_id VARCHAR(255);
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(2048);
```

**User Entity Updates**:
```java
@Entity
@Table(name = "users")
public class User {
    @Column(name = "google_id")
    private String googleId;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    // Both can be used interchangeably
    public String getPassword() { return passwordHash; }
    public void setPassword(String password) { this.passwordHash = password; }
}
```

### 3. Frontend Integration

#### GoogleSignInButton.tsx
Reusable component for OAuth2 sign-in:

```typescript
export function GoogleSignInButton({ text = 'continue_with' }) {
  const handleGoogleSignIn = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault()
    window.location.href = '/api/oauth2/authorization/google'
  }
  
  return (
    <button onClick={handleGoogleSignIn} className="...">
      {/* Google logo SVG */}
      Continue with Google
    </button>
  )
}
```

**Features**:
- Official Google logo (SVG)
- Customizable button text (signin_with, signup_with, continue_with)
- Navigates to Spring Security OAuth2 authorization endpoint

#### OAuth2CallbackPage.tsx
Handles OAuth redirect and token processing:

```typescript
useEffect(() => {
  const token = new URLSearchParams(location.search).get('token')
  const error = new URLSearchParams(location.search).get('error')
  
  if (token) {
    localStorage.setItem('token', token)
    setUser(decodedUser)
    navigate('/dashboard')
  } else if (error) {
    setError(`Authentication failed: ${error}`)
  }
}, [location.search])
```

#### API Service Integration
```typescript
// services/api.ts
export const authApi = {
  googleAuth: (token: string) => 
    post('/auth/google', { token }),
}

// Auth store handles token validation and user state
const useAuthStore = create((set) => ({
  setUser: (user) => set({ user, isAuthenticated: true }),
}))
```

#### Login Page Integration
```typescript
export function LoginPage() {
  return (
    <form onSubmit={handleSubmit}>
      <GoogleSignInButton text="signin_with" />
      
      {/* Traditional email/password form also available */}
      <input type="email" name="email" />
      <input type="password" name="password" />
    </form>
  )
}
```

---

## Frontend Integration Summary

### 1. **Sign-In Flow**
```
User clicks "Sign in with Google"
  ↓
Navigate to /api/oauth2/authorization/google
  ↓
Spring Security redirects to Google's OAuth2 endpoint
  ↓
User logs in with Google credentials
  ↓
Google redirects back to /api/login/oauth2/code/google (localhost:8080)
  ↓
OAuth2AuthenticationSuccessHandler processes user
  ↓
Redirect to http://localhost:3004/oauth2/callback?token=JWT_TOKEN
  ↓
OAuth2CallbackPage extracts token from URL
  ↓
Store token in localStorage and update auth state
  ↓
Redirect to dashboard
```

### 2. **User Registration**
- First-time OAuth users are automatically registered
- Welcome email is sent to new users
- Default role is assigned (ROLE_ADULT)
- No password required for OAuth users

### 3. **Session Management**
- JWT tokens are issued for both OAuth and traditional logins
- Same session handling for both authentication methods
- Token includes user info, expiration, and signatures

### 4. **Error Handling**
- OAuth2 failures redirect to login with error message
- Frontend displays user-friendly error notifications
- Console logs for debugging

---

## Proof of Implementation

### Backend Files Created/Modified

**New Files Created:**
1. ✅ `backend/src/main/java/com/beautystock/config/OAuth2AuthenticationSuccessHandler.java` - Handles OAuth success
2. ✅ `backend/src/main/java/com/beautystock/config/OAuth2AuthenticationFailureHandler.java` - Handles OAuth failures
3. ✅ `backend/src/main/java/com/beautystock/service/EmailService.java` - Sends welcome emails
4. ✅ `backend/src/main/java/com/beautystock/service/JwtTokenProvider.java` - JWT token management

**Modified Files:**
1. ✅ `backend/pom.xml` - Added OAuth2 and Google API dependencies
2. ✅ `backend/src/main/java/com/beautystock/config/SecurityConfig.java` - OAuth2 configuration
3. ✅ `backend/src/main/resources/application.yml` - OAuth2 properties
4. ✅ `backend/src/main/java/com/beautystock/service/AuthService.java` - Save email service integration
5. ✅ `backend/src/main/java/com/beautystock/entity/User.java` - Added googleId and profileImageUrl fields

### Frontend Files Created/Modified

**New Files Created:**
1. ✅ `web/src/components/OAuth2CallbackPage.tsx` - OAuth callback handler

**Modified Files:**
1. ✅ `web/src/components/GoogleSignInButton.tsx` - OAuth sign-in button
2. ✅ `web/src/components/LoginPage.tsx` - Integrated Google sign-in
3. ✅ `web/src/lib/auth.ts` - Auth utilities
4. ✅ `web/src/services/api.ts` - API calls
5. ✅ `web/src/store/auth.ts` - Auth state management
6. ✅ `web/src/App.tsx` - Routing configuration

### Database Changes
- Added `google_id` field to users table
- Added `profile_image_url` field to users table
- Migration files in `backend/src/main/resources/db/migration/`

---

## Security Considerations

### 1. **Credential Handling**
- Google Client ID and Secret are stored in environment variables
- Never hardcoded in source code
- Configuration externalized via `application.yml`

### 2. **Token Security**
- JWT tokens include expiration (24 hours)
- Refresh tokens for extended sessions (7 days)
- Tokens stored securely in localStorage
- HTTPS recommended for production

### 3. **CORS Configuration**
- Limited to localhost for development
- Must be updated for production domains
- Credentials allowed for OAuth flow

### 4. **User Data**
- Google ID stored separately from authentication
- Email verified flag set to true for OAuth users
- Profile images stored as URLs, not in database

### 5. **Redirect Validation**
- Redirect URLs configured in SecurityConfig
- Validated against registered OAuth apps in Google Console

---

## Configuration Requirements

### Local Development Setup

1. **Google OAuth2 Credentials**
   - Create project in Google Cloud Console
   - Generate OAuth2 credentials (Web application)
   - Add `http://localhost:8080/api/login/oauth2/code/google` as redirect URI

2. **Environment Variables** (or application.yml)
   ```
   GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=your-client-secret
   OAUTH2_REDIRECT_SUCCESS_URL=http://localhost:3004/oauth2/callback
   OAUTH2_REDIRECT_FAILURE_URL=http://localhost:3004/login
   JWT_SECRET=your-secret-key-at-least-32-characters
   MAIL_PASSWORD=your-gmail-app-password
   ```

3. **Backend Port**
   - Runs on `http://localhost:8080`
   - API context path: `/api`
   - OAuth endpoint: `http://localhost:8080/api/oauth2/authorization/google`

4. **Frontend Port**
   - Runs on `http://localhost:3004` (or configurable)
   - OAuth callback: `http://localhost:3004/oauth2/callback`

---

## Testing Checklist

- [x] Google OAuth flow works from login page
- [x] New users are auto-registered
- [x] Welcome emails sent to new users
- [x] JWT token generated and stored
- [x] Dashboard accessible after OAuth login
- [x] Token refresh works correctly
- [x] Error handling displays proper messages
- [x] Users can still use email/password login
- [x] Profile data (name, picture) used correctly
- [x] Mobile/Android compatible UI

---

## Future Enhancements

1. **Additional OAuth Providers**
   - Facebook OAuth2
   - GitHub OAuth2
   - Apple OAuth2

2. **Enhanced User Profile**
   - Profile picture from OAuth provider
   - Social media links
   - Provider-specific data

3. **Account Linking**
   - Link multiple OAuth providers to one account
   - Migrate from email to OAuth

4. **2FA Support**
   - Two-factor authentication with OAuth2
   - Recovery codes

5. **PKCE Flow**
   - Enhanced security for mobile apps
   - Native app support

---

## Troubleshooting

### Issue: Redirect URI mismatch
**Solution**: Ensure redirect URI in Google Console matches exactly with `application.yml`

### Issue: Token not appearing in callback
**Solution**: Check Google Console for active credentials and correct scope permissions

### Issue: CORS errors
**Solution**: Verify CORS configuration in SecurityConfig and frontend origin

### Issue: New user creation fails
**Solution**: Ensure User entity has nullable fields for OAuth-specific data

---

## References

- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 Setup](https://developers.google.com/identity/protocols/oauth2)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
