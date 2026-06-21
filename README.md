# Food Delivery Order Management System

A production-grade REST API for managing food delivery operations across multiple cities and restaurants, built with Spring Boot.

## Tech Stack

- **Framework**: Spring Boot 3.2 (Java 21)
- **Database**: PostgreSQL (H2 for tests)
- **Security**: Spring Security + JWT (JJWT 0.12)
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Bean Validation (Jakarta)
- **Testing**: JUnit 5, Mockito, MockMvc
- **Build**: Maven

## Architecture Decisions & Assumptions

### Concurrency Safety
- **Overselling prevention**: `MenuItemRepository.findByIdWithLock` uses `@Lock(PESSIMISTIC_WRITE)` — a database-level `SELECT FOR UPDATE` that ensures only one transaction can decrement stock at a time for the same item.
- **Partner assignment contention**: `DeliveryPartner` has `@Version` for optimistic locking. When multiple orders try to assign the same partner simultaneously, the one that loses the race gets a `ObjectOptimisticLockingFailureException`; the system tries the next available partner.
- **Order state**: `Order` entity carries `@Version` so concurrent status transitions fail fast instead of silently overwriting each other.

### Atomic Order Placement
Order placement is a single `@Transactional` operation that:
1. Acquires pessimistic write locks on all requested `MenuItem` rows
2. Validates stock and deducts it
3. Persists the `Order` + `OrderItem` rows
4. Creates a `Payment` record (simulated — `COMPLETED` immediately)
5. Only after the transaction commits does `@TransactionalEventListener(AFTER_COMMIT)` fire the `OrderPlacedEvent`

This means: if payment simulation fails, the entire order + stock deduction rolls back atomically.

### Async Fan-out Notifications
`OrderEventListener` uses both `@Async("notificationExecutor")` and `@TransactionalEventListener(phase = AFTER_COMMIT)`:
- **AFTER_COMMIT**: notification only fires after the DB transaction is durable, eliminating phantom notifications for rolled-back orders.
- **@Async**: notification delivery (email/SMS in production, log in this implementation) runs on a separate thread pool and never blocks the API response.

### Order Lifecycle
```
PLACED → ACCEPTED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
       ↘ REJECTED
PLACED → CANCELLED (customer only)
```
Each transition is validated in the service layer. An `OrderStatusHistory` row is recorded at every transition.

### Delivery Partner Assignment
When a restaurant `ACCEPTS` an order, the system automatically scans for `AVAILABLE` partners in the restaurant's city and assigns the first one (marking them `BUSY`). This is a best-effort auto-assignment. If no partner is available, the order remains unassigned until a partner manually picks it up via the `/delivery-status` endpoint.

### Payment
Payment is simulated within the same transaction as order placement. The `transactionId` is a UUID reference. In production, replace `PaymentService` logic with a real gateway (Razorpay/Stripe) call with webhook confirmation.

### Roles & Access Control
| Role | Capabilities |
|------|-------------|
| `ADMIN` | Manage cities, deactivate restaurants, register delivery partners, view all partners |
| `RESTAURANT_OWNER` | Manage own restaurants (create/update/open/close), manage menu, accept/reject/update orders |
| `CUSTOMER` | Browse restaurants & menus, place orders, track orders, cancel (when PLACED), rate after delivery |
| `DELIVERY_PARTNER` | Update own availability/city, view assigned orders, update delivery status |

### Stock Management
- `stockQuantity = null` means **unlimited stock** (no deduction occurs).
- `stockQuantity > 0` enforces hard limits with pessimistic locking.
- On order cancellation or rejection, stock is restored within the same transaction.
- Soft delete for menu items (marked `available=false`) preserves referential integrity for order history.

### Ratings
- Customers can rate both the restaurant and the delivery partner for a single delivered order.
- One rating per `(order_id, target)` pair (enforced by DB unique constraint).
- `Restaurant.avgRating` and `DeliveryPartner.avgRating` are updated in-transaction after each new rating.

## API Summary

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register (CUSTOMER/RESTAURANT_OWNER/DELIVERY_PARTNER) |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Cities (Admin)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/cities` | Public | List active cities |
| POST | `/api/cities` | ADMIN | Create city |
| PATCH | `/api/cities/{id}/activate` | ADMIN | Activate city |
| PATCH | `/api/cities/{id}/deactivate` | ADMIN | Deactivate city |

### Restaurants
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/restaurants/city/{cityId}?openOnly=true` | Public | Browse restaurants in city |
| GET | `/api/restaurants/city/{cityId}/search?name=` | Public | Search by name |
| GET | `/api/restaurants/{id}` | Public | Get restaurant details |
| POST | `/api/restaurants` | RESTAURANT_OWNER | Create restaurant |
| PUT | `/api/restaurants/{id}` | RESTAURANT_OWNER | Update restaurant |
| PATCH | `/api/restaurants/{id}/open` | RESTAURANT_OWNER | Open for orders |
| PATCH | `/api/restaurants/{id}/close` | RESTAURANT_OWNER | Close for orders |
| GET | `/api/restaurants/my` | RESTAURANT_OWNER | My restaurants |
| DELETE | `/api/restaurants/{id}` | ADMIN | Deactivate restaurant |

### Menu
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/restaurants/{id}/menu` | Public | Available menu items |
| GET | `/api/restaurants/{id}/menu/full` | RESTAURANT_OWNER | All items (incl. unavailable) |
| POST | `/api/restaurants/{id}/menu` | RESTAURANT_OWNER | Add menu item |
| PUT | `/api/restaurants/{id}/menu/{itemId}` | RESTAURANT_OWNER | Update menu item |
| DELETE | `/api/restaurants/{id}/menu/{itemId}` | RESTAURANT_OWNER | Remove (soft delete) |

### Orders
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders` | CUSTOMER | Place order (atomic: stock + payment) |
| GET | `/api/orders/{id}` | Any (own) | Get order details |
| GET | `/api/orders/my` | CUSTOMER | My order history |
| GET | `/api/orders/restaurant/{restaurantId}?status=` | RESTAURANT_OWNER/ADMIN | Restaurant orders |
| PATCH | `/api/orders/{id}/restaurant-status` | RESTAURANT_OWNER | Accept/Reject/Preparing/Ready |
| PATCH | `/api/orders/{id}/delivery-status` | DELIVERY_PARTNER | Out-for-delivery / Delivered |
| PATCH | `/api/orders/{id}/cancel` | CUSTOMER | Cancel (only when PLACED) |

### Delivery Partners
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/delivery-partners` | ADMIN | Register delivery partner |
| GET | `/api/delivery-partners/me` | DELIVERY_PARTNER | My profile |
| PATCH | `/api/delivery-partners/me/availability?status=` | DELIVERY_PARTNER | Set AVAILABLE/BUSY/OFFLINE |
| GET | `/api/delivery-partners/me/orders` | DELIVERY_PARTNER | My active orders |
| PATCH | `/api/delivery-partners/me/city/{cityId}` | DELIVERY_PARTNER | Update operating city |
| GET | `/api/delivery-partners/city/{cityId}` | ADMIN | Partners in a city |

### Ratings
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/ratings` | CUSTOMER | Rate restaurant or delivery partner |
| GET | `/api/ratings/restaurant/{id}` | Public | Restaurant ratings |
| GET | `/api/ratings/partner/{id}` | Public | Partner ratings |

## Running the Application

### Prerequisites
- Java 21+
- PostgreSQL 14+
- Maven 3.9+

### Database Setup
```sql
CREATE DATABASE food_delivery;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE food_delivery TO postgres;
```

### Run
```bash
cd food-delivery
mvn spring-boot:run
```
Application starts on `http://localhost:8080`

### Run Tests
```bash
mvn test
```
Tests use H2 in-memory database (no PostgreSQL required).

## Example Requests

### Register + Login
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","phone":"9876543210","password":"pass123","role":"CUSTOMER"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"pass123"}'
```

### Place an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"restaurantId":1,"deliveryAddress":"123 Main St","paymentMethod":"UPI","items":[{"menuItemId":1,"quantity":2}]}'
```
