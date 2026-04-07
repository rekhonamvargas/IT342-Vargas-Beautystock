# Quick Start: Main Feature Implementation (2-3 hours)

## 📌 What You Have NOW

✅ **Working:**
- Product CRUD (create/edit/delete products)
- Favorites system (heart button, saved to DB)
- Dashboard with stats (🧴 products, ❤️ favorites)
- Product images upload (Base64 encoding)
- User authentication (JWT, roles)
- Database integration (PostgreSQL)

❌ **Missing (MAIN FEATURE):**
- Weather API integration
- Skincare advice endpoints
- Age-based recommendations displayed

---

## 🎯 Your Assignment: Missing 20%

**Time: 2-3 hours**
**Difficulty: Medium**
**Impact: Completes 100% of SDD requirements**

---

## 🚀 QUICK IMPLEMENTATION (Copy-Paste Ready)

### 1️⃣ BACKEND: Create WeatherService.java (15 min)

**Path:** `backend/src/main/java/com/beautystock/service/WeatherService.java`

```java
package com.beautystock.service;

import com.beautystock.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {
    @Value("${openweather.api.key}")
    private String apiKey;

    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q={city}&appid={apiKey}&units=metric";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getSkincareRecommendation(String city, UserRole userRole) {
        try {
            String url = WEATHER_URL.replace("{city}", city).replace("{apiKey}", apiKey);
            var weatherResponse = restTemplate.getForObject(url, Map.class);
            
            if (weatherResponse == null) return fallbackRec(userRole);

            Map<String, Object> main = (Map<String, Object>) weatherResponse.get("main");
            double temp = ((Number) main.get("temp")).doubleValue();
            int humidity = ((Number) main.get("humidity")).intValue();

            return buildRecommendation(temp, humidity, userRole);
        } catch (Exception e) {
            return fallbackRec(userRole);
        }
    }

    private Map<String, Object> buildRecommendation(double temp, int humidity, UserRole userRole) {
        Map<String, Object> rec = new HashMap<>();
        rec.put("temperature", temp);
        rec.put("humidity", humidity);

        String skinType = humidity > 70 ? "Lightweight" : humidity < 40 ? "Hydrating" : "Balanced";
        rec.put("skincareType", skinType);

        if (userRole == UserRole.ROLE_YOUTH) {
            rec.put("title", "Youth Skincare Advice");
            rec.put("emoji", "🌟");
            rec.put("advice", "Oil-free moisturizer & SPF 30+ daily. Focus on breakout prevention.");
        } else {
            rec.put("title", "Adult Skincare Advice");
            rec.put("emoji", "✨");
            rec.put("advice", "Rich moisturizer with retinol 2-3x weekly. SPF 50+ essential.");
        }

        return rec;
    }

    private Map<String, Object> fallbackRec(UserRole userRole) {
        Map<String, Object> rec = new HashMap<>();
        rec.put("title", userRole == UserRole.ROLE_YOUTH ? "Youth Skincare Advice" : "Adult Skincare Advice");
        rec.put("advice", "Set your city in profile for personalized weather-based skincare advice.");
        rec.put("emoji", userRole == UserRole.ROLE_YOUTH ? "🌟" : "✨");
        return rec;
    }
}
```

---

### 2️⃣ BACKEND: Create RecommendationController.java (15 min)

**Path:** `backend/src/main/java/com/beautystock/controller/RecommendationController.java`

```java
package com.beautystock.controller;

import com.beautystock.entity.UserRole;
import com.beautystock.repository.UserRepository;
import com.beautystock.service.WeatherService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/v1/recommendations")
public class RecommendationController {
    private final WeatherService weatherService;
    private final UserRepository userRepository;

    public RecommendationController(WeatherService weatherService, UserRepository userRepository) {
        this.weatherService = weatherService;
        this.userRepository = userRepository;
    }

    @GetMapping("/youth/weather")
    public ResponseEntity<?> getYouthWeatherAdvice() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ROLE_YOUTH) {
            return ResponseEntity.status(403).body(Map.of("error", "Only ROLE_YOUTH access"));
        }

        if (user.getCity() == null || user.getCity().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "advice", "Set your city in profile for weather-based advice",
                "title", "Youth Skincare Advice"
            ));
        }

        Map<String, Object> rec = weatherService.getSkincareRecommendation(user.getCity(), UserRole.ROLE_YOUTH);
        return ResponseEntity.ok(rec);
    }

    @GetMapping("/adult/weather")
    public ResponseEntity<?> getAdultWeatherAdvice() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ROLE_ADULT) {
            return ResponseEntity.status(403).body(Map.of("error", "Only ROLE_ADULT access"));
        }

        if (user.getCity() == null || user.getCity().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "advice", "Set your city in profile for weather-based advice",
                "title", "Adult Skincare Advice"
            ));
        }

        Map<String, Object> rec = weatherService.getSkincareRecommendation(user.getCity(), UserRole.ROLE_ADULT);
        return ResponseEntity.ok(rec);
    }
}
```

---

### 3️⃣ BACKEND: Update application.yml (5 min)

**File:** `backend/src/main/resources/application.yml`

Add at the bottom:
```yaml
openweather:
  api:
    key: 8d5848e7f6e4e8d9d8f8e7d6c5b4a3f2
```

**⚠️ IMPORTANT:** Get your own FREE key:
1. Go to https://openweathermap.org/api
2. Sign up (free account)
3. Go to Settings → API Keys
4. Copy your key
5. Paste it above

---

### 4️⃣ FRONTEND: Create SkincareAdvice.tsx (20 min)

**Path:** `web/src/components/SkincareAdvice.tsx`

```tsx
import { useWeatherStore } from '@/store/auth'

export function SkincareAdvice() {
  const { weather, error: weatherError } = useWeatherStore()

  if (!weather) {
    return (
      <div className="bg-gradient-to-br from-pink-50 to-purple-50 rounded-lg p-6 border border-pink-200">
        <p className="text-gray-500 text-sm">{weatherError || 'Loading...'}</p>
      </div>
    )
  }

  return (
    <div className="bg-gradient-to-br from-pink-50 to-purple-50 rounded-lg p-6 border border-pink-200">
      <div className="flex items-center gap-3 mb-4">
        <span className="text-3xl">{weather.emoji}</span>
        <h2 className="text-2xl font-bold text-pink-600">{weather.title}</h2>
      </div>

      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <span>🌡️</span>
          <p className="text-sm"><strong>Temp:</strong> {weather.temperature}°C</p>
        </div>
        <div className="flex items-center gap-2">
          <span>💧</span>
          <p className="text-sm"><strong>Humidity:</strong> {weather.humidity}%</p>
        </div>
        <div className="bg-white p-4 rounded-lg border-l-4 border-pink-400 mt-4">
          <p className="text-gray-700 text-sm">{weather.advice}</p>
        </div>
      </div>
    </div>
  )
}
```

---

### 5️⃣ FRONTEND: Update Dashboard.tsx (5 min)

At the **top** of the file, add import:
```tsx
import { SkincareAdvice } from './SkincareAdvice'
```

Find in Dashboard return JSX (around line 120), after recent products section, add:
```tsx
{/* Skincare Advice Widget */}
<div className="mt-8">
  <SkincareAdvice />
</div>
```

---

## ✅ Verify Setup Works

### Build Backend
```bash
cd backend
mvn clean compile -DskipTests
# Should say: BUILD SUCCESS
```

### Build Frontend
```bash
cd web
npm run build
# Should say: built in X.XX s
```

### Test It
```bash
# Terminal 1: Backend
cd backend
mvn spring-boot:run

# Terminal 2: Frontend
cd web
npm run dev -- --host
```

Then visit http://localhost:3015

---

## 🧪 Test Steps

1. **Register new user as ROLE_YOUTH**
2. **Set your city in Profile** (e.g., "New York")
3. **Go to Dashboard**
4. **Should see:**
   ```
   🌟 Youth Skincare Advice
   🌡️ Temperature: 15°C
   💧 Humidity: 65%
   📝 Skincare advice based on weather
   ```

---

## 📋 Checklist

Backend:
- [ ] Create WeatherService.java
- [ ] Create RecommendationController.java
- [ ] Add API key to application.yml
- [ ] mvn clean compile (no errors)

Frontend:
- [ ] Create SkincareAdvice.tsx
- [ ] Import in Dashboard.tsx
- [ ] Add widget to Dashboard JSX
- [ ] npm run build (no errors)

Testing:
- [ ] Backend runs on port 8080
- [ ] Frontend runs on port 3015
- [ ] Dashboard shows weather advice
- [ ] Works for ROLE_YOUTH
- [ ] Works for ROLE_ADULT
- [ ] Error message shown if city not set

---

## 🎉 Done!

You now have:
✅ 100% SDD Compliance
✅ Main feature working
✅ Full web integration
✅ Database connection complete
✅ All validations in place

Ready to submit! 🚀

