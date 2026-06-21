# Claude.md — AI Workflow for Food Delivery Order Management System

## Overview

This project was built collaboratively between Kartik Mohan (the developer) and Claude Code (claude-sonnet-4-6) across multiple iterative prompt cycles. The developer intervened at every major decision point — clarifying requirements, redirecting the architecture, catching design gaps, and refining scope. This document captures the actual workflow as it evolved.

---

## Prompt Iteration Log

### Iteration 1 — Initial Problem Statement
**Developer prompt:** The full problem statement was shared — multi-restaurant, multi-city food delivery with roles (admin, restaurant owner, customer, delivery partner), lifecycle management, and concurrency requirements.

**Claude output:** Initial high-level entity sketch (User, Restaurant, MenuItem, Order).

**Developer intervention:** Pushed back on the initial flat entity design — pointed out that `City` needed to be a first-class entity (not just a string on Restaurant), and that `DeliveryPartner` should have its own profile entity separate from `User` to support city-scoped availability and vehicle metadata.

---

### Iteration 2 — Concurrency Design
**Developer prompt:** "How will you handle concurrent orders for the same item so stock doesn't go negative? And what about two partners being assigned the same order?"

**Claude output:** Proposed optimistic locking for everything.

**Developer intervention:** Pushed back — said pessimistic locking (`SELECT FOR UPDATE`) is safer for stock because the contention window is narrow and the failure cost (oversell) is high. Agreed to keep optimistic locking only for partner assignment where "try the next partner" is a valid fallback.

**Outcome:** `MenuItemRepository.findByIdWithLock` uses `@Lock(PESSIMISTIC_WRITE)`. `Order` and `DeliveryPartner` use `@Version` for optimistic locking.

---

### Iteration 3 — Order Placement Atomicity
**Developer prompt:** "The requirement says order placement must atomically reflect item stock, order state, and payment. Walk me through how you're doing this."

**Claude output:** Draft service that had payment in a separate transaction.

**Developer intervention:** Flagged this as wrong — if payment is separate, a crash between order creation and payment creates an orphaned order with deducted stock and no payment. Required everything to happen in a single `@Transactional` boundary.

**Outcome:** `OrderService.placeOrder` is a single transaction: lock menu items → deduct stock → persist order → persist payment → all commit together.

---

### Iteration 4 — Async Fan-out Design
**Developer prompt:** "Status updates should fan out asynchronously to customer, restaurant, and delivery partner without blocking the calling flow. How are you implementing this?"

**Claude output:** Initial use of `@Async` alone on event methods.

**Developer intervention:** Pointed out the problem — if the `@Async` method fires before the transaction commits and the transaction then rolls back, notifications go out for an order that never actually existed. Instructed to use `@TransactionalEventListener(phase = AFTER_COMMIT)` to gate on transaction completion.

**Outcome:** `OrderEventListener` uses both `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` and `@Async("notificationExecutor")`. A dedicated `ThreadPoolTaskExecutor` bean (`notificationExecutor`) isolates notification threads from the request thread pool.

---

### Iteration 5 — Order Lifecycle State Machine
**Developer prompt:** "What transitions are valid? Can a restaurant move an order from PLACED directly to PREPARING? Can a customer cancel an ACCEPTED order?"

**Claude output:** Initial permissive transition logic.

**Developer intervention:** Defined the exact valid transition map:
- Restaurant: `PLACED → ACCEPTED`, `PLACED → REJECTED`, `ACCEPTED → PREPARING`, `PREPARING → READY_FOR_PICKUP`
- Partner: `READY_FOR_PICKUP → OUT_FOR_DELIVERY`, `OUT_FOR_DELIVERY → DELIVERED`
- Customer cancel: only when `PLACED`

Also required that stock be restored on `REJECTED` and `CANCELLED`, and payment refunded on `CANCELLED`.

**Outcome:** `validateRestaurantTransition` and `validatePartnerTransition` methods with `switch` expressions. Stock restoration and payment refund wired in the same transaction as the status change.

---

### Iteration 6 — Role-Based Access Control
**Developer prompt:** "How does a restaurant owner update only their own restaurant's orders? What stops a customer from calling the restaurant-status endpoint?"

**Claude output:** `@PreAuthorize` annotations present but ownership check missing.

**Developer intervention:** Pointed out that role guards alone aren't enough — need ownership checks inside the service (e.g., `restaurant.getOwner().getId().equals(ownerId)`). Also clarified endpoint split: restaurant owners use `/restaurant-status`, delivery partners use `/delivery-status`, enforced with role guards.

**Outcome:** Ownership verification inside every service method that touches protected resources. Separate REST endpoints for each actor role.

---

### Iteration 7 — Rating System Design
**Developer prompt:** "Ratings should be possible for both the restaurant and the delivery partner after delivery. Make sure a customer can't rate the same target twice for the same order."

**Claude output:** Single rating per order.

**Developer intervention:** Clarified: a customer can submit two ratings per delivered order (one for the restaurant, one for the delivery partner) but not the same target twice. Required a unique constraint on `(order_id, target)` and idempotency check in the service.

**Outcome:** `Rating` entity has `@UniqueConstraint(columnNames = {"order_id", "target"})`. `RatingService` checks `ratingRepository.existsByOrderIdAndTarget` before saving. `avgRating` on `Restaurant` and `DeliveryPartner` recalculated in-transaction after each rating.

---

### Iteration 8 — Java Version Compatibility Issue
**Developer prompt:** "Fix the compilation errors."

**Claude output:** Initial code used Lombok (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`).

**Developer intervention:** The local machine runs Java 26, which is incompatible with Lombok 1.18.36's use of internal `com.sun.tools.javac.code.TypeTag` APIs. Developer directed: strip Lombok entirely, replace with explicit Java builders, constructors, and accessor methods.

**Outcome:** All Lombok annotations removed from the entire codebase. Every entity, DTO, and service class uses handwritten builders (inner static `Builder` class pattern), explicit constructors, and getters/setters. Zero external annotation processing dependencies.

---

### Iteration 9 — Test Coverage Requirements
**Developer prompt:** "Write unit tests and integration tests for the core flows."

**Claude output:** Initial test stubs.

**Developer intervention:** Required specific scenarios be covered:
- Unit: successful order placement with stock deduction, insufficient stock guard, restaurant-closed guard, unlimited stock (no deduction), cancel-and-restore, invalid cancel on accepted order
- Integration (MockMvc + H2): full lifecycle from placed to ready-for-pickup, closed restaurant guard, customer cancel, role-based access denial

**Outcome:** `OrderServiceTest` (6 tests, Mockito), `RestaurantServiceTest` (4 tests, Mockito), `AuthIntegrationTest` (5 tests, MockMvc), `OrderIntegrationTest` (4 tests, MockMvc + H2). All 19 tests pass.

---

## Key Design Decisions and Who Drove Them

| Decision | Initiated By | Outcome |
|----------|-------------|---------|
| `City` as first-class entity | Developer | Separate `cities` table, referenced by `Restaurant` and `DeliveryPartner` |
| `DeliveryPartner` as separate profile entity | Developer | City-scoped availability, vehicle type, own rating |
| Pessimistic lock for stock, optimistic for partner | Developer | `@Lock(PESSIMISTIC_WRITE)` on MenuItem, `@Version` on Order and DeliveryPartner |
| Single transaction for order+payment | Developer | `placeOrder` is one `@Transactional` method |
| `@TransactionalEventListener(AFTER_COMMIT)` | Developer | Prevents phantom notifications for rolled-back orders |
| Exact lifecycle state machine | Developer | Explicit `switch` validation, separate endpoints per actor |
| `(order_id, target)` unique constraint for ratings | Developer | DB constraint + service-level idempotency check |
| Lombok removal for Java 26 compat | Developer (environment) | All builders/getters/setters written explicitly |

---

## Tech Stack
- **Spring Boot 3.2** — Web, Data JPA, Security, Validation, Events, Async
- **PostgreSQL** (prod) / **H2** (tests)
- **Spring Security + JWT** (JJWT 0.12)
- **JUnit 5 + Mockito + MockMvc** for testing
- **Maven** build tool
- **Java 21** source/target (runtime: Java 26)

## Skills and Tools Used During Development
- Claude Code CLI (claude-sonnet-4-6) for code generation across 9+ prompt iterations
- Developer-directed architecture review at every layer
- Iterative debugging of Java 26 / Lombok incompatibility
- H2 in-memory database for self-contained test execution
