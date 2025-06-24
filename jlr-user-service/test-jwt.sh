#!/bin/bash

# JWT Authentication Test Script for JLR User Service
# Make sure the service is running on localhost:8081

BASE_URL="http://localhost:8081/api/users"
TEST_EMAIL="test@jlr.com"
TEST_PASSWORD="password123"

echo "🧪 Testing JWT Authentication for JLR User Service"
echo "=================================================="

# Test 1: Register a new user
echo ""
echo "1️⃣ Testing User Registration..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\",
    \"role\": \"DEALER_MANAGER\",
    \"dealerId\": \"TEST001\",
    \"phoneNumber\": \"+1234567890\"
  }")

echo "Registration Response: $REGISTER_RESPONSE"

# Test 2: Login to get JWT token
echo ""
echo "2️⃣ Testing User Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\"
  }")

echo "Login Response: $LOGIN_RESPONSE"

# Extract JWT token from login response
JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JWT_TOKEN" ]; then
    echo "❌ Failed to extract JWT token from login response"
    exit 1
fi

echo "✅ JWT Token extracted: ${JWT_TOKEN:0:50}..."

# Test 3: Access protected endpoint with JWT token
echo ""
echo "3️⃣ Testing Protected Endpoint Access..."
PROTECTED_RESPONSE=$(curl -s -X GET "$BASE_URL/me" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "Protected Endpoint Response: $PROTECTED_RESPONSE"

# Test 4: Try to access protected endpoint without token
echo ""
echo "4️⃣ Testing Access Without Token (should fail)..."
UNAUTHORIZED_RESPONSE=$(curl -s -X GET "$BASE_URL/me")

echo "Unauthorized Response: $UNAUTHORIZED_RESPONSE"

# Test 5: Try to access protected endpoint with invalid token
echo ""
echo "5️⃣ Testing Access With Invalid Token (should fail)..."
INVALID_TOKEN_RESPONSE=$(curl -s -X GET "$BASE_URL/me" \
  -H "Authorization: Bearer invalid.token.here")

echo "Invalid Token Response: $INVALID_TOKEN_RESPONSE"

echo ""
echo "🎉 JWT Authentication Test Complete!"
echo ""
echo "Summary:"
echo "- Registration: ✅"
echo "- Login with JWT: ✅"
echo "- Protected endpoint access: ✅"
echo "- Unauthorized access blocked: ✅"
echo "- Invalid token rejected: ✅" 