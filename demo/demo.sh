#!/bin/bash
# ─────────────────────────────────────────────────────────────
#  LIVE DEMO SCRIPT — run this ON camera, step by step
#  Each step is a separate command. Press Enter between each.
#  Tokens are hardcoded below — paste from setup.sh output.
# ─────────────────────────────────────────────────────────────

BASE="http://localhost:8080"

# ── PASTE YOUR TOKENS FROM setup.sh OUTPUT HERE ───────────────
ADMIN_TOKEN=""
OWNER_TOKEN=""
PARTNER_TOKEN=""
REST_ID=1
ITEM1_ID=1   # Chicken Biryani
ITEM2_ID=2   # Garlic Naan
# ──────────────────────────────────────────────────────────────

STEP=0
next() {
  STEP=$((STEP+1))
  echo ""
  echo "══════════════════════════════════════════════"
  echo "  STEP $STEP: $1"
  echo "══════════════════════════════════════════════"
  read -p "  Press Enter to run..." _
}

# ── STEP 1: Register as Customer & get token ───────────────────
next "Register as Customer — JWT returned in response"

CUST_RESP=$(curl -s -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Kartik Customer","email":"kartik@demo.com","phone":"9000000099","password":"demo123","role":"CUSTOMER"}')
CUST_TOKEN=$(echo $CUST_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo $CUST_RESP | python3 -m json.tool

# ── STEP 2: Browse the menu ────────────────────────────────────
next "Browse Restaurant Menu"

curl -s "$BASE/api/restaurants/$REST_ID/menu" | python3 -m json.tool

# ── STEP 3: Place Order ────────────────────────────────────────
next "Place Order — atomic: stock deducted + payment created in one transaction"

ORDER_RESP=$(curl -s -X POST $BASE/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUST_TOKEN" \
  -d "{
    \"restaurantId\": $REST_ID,
    \"deliveryAddress\": \"HSR Layout, Bangalore\",
    \"paymentMethod\": \"UPI\",
    \"items\": [
      {\"menuItemId\": $ITEM1_ID, \"quantity\": 2},
      {\"menuItemId\": $ITEM2_ID, \"quantity\": 1}
    ]
  }")
ORDER_ID=$(echo $ORDER_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
ORDER_NUM=$(echo $ORDER_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['orderNumber'])")
echo $ORDER_RESP | python3 -m json.tool
echo ""
echo "  ORDER ID: $ORDER_ID  |  ORDER NUMBER: $ORDER_NUM"

# ── STEP 4: Check stock decreased ─────────────────────────────
next "Verify Stock Deducted (Chicken Biryani: was 20, ordered 2, should be 18)"

curl -s "$BASE/api/restaurants/$REST_ID/menu/full" \
  -H "Authorization: Bearer $OWNER_TOKEN" | python3 -m json.tool

# ── STEP 5: Restaurant Accepts ────────────────────────────────
next "Restaurant ACCEPTS order — delivery partner auto-assigned"

curl -s -X PATCH "$BASE/api/orders/$ORDER_ID/restaurant-status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{"status":"ACCEPTED","note":"Order confirmed, preparing soon"}' | python3 -m json.tool

# ── STEP 6: Restaurant Preparing ──────────────────────────────
next "Restaurant marks order PREPARING"

curl -s -X PATCH "$BASE/api/orders/$ORDER_ID/restaurant-status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{"status":"PREPARING"}' \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print('  Status:', d['data']['status'])"

# ── STEP 7: Restaurant — Ready for Pickup ─────────────────────
next "Restaurant marks order READY_FOR_PICKUP"

curl -s -X PATCH "$BASE/api/orders/$ORDER_ID/restaurant-status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{"status":"READY_FOR_PICKUP"}' \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print('  Status:', d['data']['status'])"

# ── STEP 8: Partner picks up ──────────────────────────────────
next "Delivery Partner marks OUT_FOR_DELIVERY"

curl -s -X PATCH "$BASE/api/orders/$ORDER_ID/delivery-status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $PARTNER_TOKEN" \
  -d '{"status":"OUT_FOR_DELIVERY"}' \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print('  Status:', d['data']['status'])"

# ── STEP 9: Delivered ──────────────────────────────────────────
next "Delivery Partner marks DELIVERED"

curl -s -X PATCH "$BASE/api/orders/$ORDER_ID/delivery-status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $PARTNER_TOKEN" \
  -d '{"status":"DELIVERED"}' | python3 -m json.tool

# ── STEP 10: Rate Restaurant ───────────────────────────────────
next "Customer rates the Restaurant (5 stars)"

curl -s -X POST "$BASE/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUST_TOKEN" \
  -d "{\"orderId\":$ORDER_ID,\"target\":\"RESTAURANT\",\"rating\":5,\"review\":\"Best biryani in Bangalore!\"}" \
  | python3 -m json.tool

# ── STEP 11: Rate Delivery Partner ────────────────────────────
next "Customer rates the Delivery Partner (4 stars)"

curl -s -X POST "$BASE/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUST_TOKEN" \
  -d "{\"orderId\":$ORDER_ID,\"target\":\"DELIVERY_PARTNER\",\"rating\":4,\"review\":\"Fast and friendly!\"}" \
  | python3 -m json.tool

# ── STEP 12: Duplicate rating blocked ─────────────────────────
next "Try to rate restaurant again — should be BLOCKED"

curl -s -X POST "$BASE/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUST_TOKEN" \
  -d "{\"orderId\":$ORDER_ID,\"target\":\"RESTAURANT\",\"rating\":1,\"review\":\"Changed my mind\"}" \
  | python3 -m json.tool

# ── STEP 13: Check updated restaurant rating ───────────────────
next "Check Restaurant — avgRating updated in-transaction"

curl -s "$BASE/api/restaurants/$REST_ID" | python3 -m json.tool

echo ""
echo "══════════════════════════════════════════════"
echo "  DEMO COMPLETE"
echo "══════════════════════════════════════════════"
