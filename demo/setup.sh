#!/bin/bash
# Run this BEFORE you start recording.
# It sets up users, city, restaurant, menu, and delivery partner.
# At the end it prints the tokens and IDs you'll need for demo.sh.

BASE="http://localhost:8080"

echo "=============================="
echo "  FOOD DELIVERY — DEMO SETUP  "
echo "=============================="

# ── 1. Register Admin ──────────────────────────────────────────────────────────
echo -e "\n[1/7] Registering Admin..."
ADMIN_RESP=$(curl -s -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Kartik Admin","email":"admin@demo.com","phone":"9000000001","password":"admin123","role":"ADMIN"}')
ADMIN_TOKEN=$(echo $ADMIN_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "  Admin token saved."

# ── 2. Register Restaurant Owner ───────────────────────────────────────────────
echo -e "\n[2/7] Registering Restaurant Owner..."
OWNER_RESP=$(curl -s -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Rajesh Kumar","email":"rajesh@demo.com","phone":"9000000002","password":"owner123","role":"RESTAURANT_OWNER"}')
OWNER_TOKEN=$(echo $OWNER_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "  Owner token saved."

# ── 3. Register Delivery Partner ───────────────────────────────────────────────
echo -e "\n[3/7] Registering Delivery Partner..."
PARTNER_RESP=$(curl -s -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Raju Bhai","email":"raju@demo.com","phone":"9000000003","password":"partner123","role":"DELIVERY_PARTNER"}')
PARTNER_USER_ID=$(echo $PARTNER_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['userId'])")
PARTNER_TOKEN=$(echo $PARTNER_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "  Partner user ID: $PARTNER_USER_ID — token saved."

# ── 4. Create City ─────────────────────────────────────────────────────────────
echo -e "\n[4/7] Creating city: Bangalore..."
curl -s -X POST $BASE/api/cities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"name":"Bangalore","state":"Karnataka"}' > /dev/null
echo "  City created."

# ── 5. Create Restaurant & Open It ────────────────────────────────────────────
echo -e "\n[5/7] Creating restaurant: Biryani Palace..."
REST_RESP=$(curl -s -X POST $BASE/api/restaurants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{"name":"Biryani Palace","address":"MG Road, Bangalore","phone":"9111111111","cityId":1,"cuisineType":"Indian"}')
REST_ID=$(echo $REST_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
curl -s -X PATCH "$BASE/api/restaurants/$REST_ID/open" \
  -H "Authorization: Bearer $OWNER_TOKEN" > /dev/null
echo "  Restaurant created (ID: $REST_ID) and opened."

# ── 6. Add Menu Items ──────────────────────────────────────────────────────────
echo -e "\n[6/7] Adding menu items..."
ITEM1=$(curl -s -X POST "$BASE/api/restaurants/$REST_ID/menu" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{"name":"Chicken Biryani","description":"Aromatic basmati rice with chicken","price":299.00,"stockQuantity":20,"available":true}')
ITEM1_ID=$(echo $ITEM1 | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "  Chicken Biryani added (ID: $ITEM1_ID, stock: 20)."

ITEM2=$(curl -s -X POST "$BASE/api/restaurants/$REST_ID/menu" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{"name":"Garlic Naan","description":"Soft naan with garlic butter","price":49.00,"available":true}')
ITEM2_ID=$(echo $ITEM2 | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "  Garlic Naan added (ID: $ITEM2_ID, stock: unlimited)."

# ── 7. Register Delivery Partner Profile & Set Available ───────────────────────
echo -e "\n[7/7] Setting up delivery partner..."
curl -s -X POST $BASE/api/delivery-partners \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"userId\":$PARTNER_USER_ID,\"cityId\":1,\"vehicleType\":\"BIKE\",\"vehicleNumber\":\"KA01AB1234\"}" > /dev/null
curl -s -X PATCH "$BASE/api/delivery-partners/me/availability?status=AVAILABLE" \
  -H "Authorization: Bearer $PARTNER_TOKEN" > /dev/null
echo "  Delivery partner registered and set AVAILABLE."

# ── Print summary ───────────────────────────────────────────────────────────────
echo ""
echo "=============================="
echo "  SETUP COMPLETE — COPY THESE "
echo "=============================="
echo ""
echo "ADMIN_TOKEN=$ADMIN_TOKEN"
echo ""
echo "OWNER_TOKEN=$OWNER_TOKEN"
echo ""
echo "PARTNER_TOKEN=$PARTNER_TOKEN"
echo ""
echo "REST_ID=$REST_ID"
echo "ITEM1_ID=$ITEM1_ID (Chicken Biryani)"
echo "ITEM2_ID=$ITEM2_ID (Garlic Naan)"
echo ""
echo "Now register your demo customer and start the app recording."
echo "Run: bash demo.sh"
