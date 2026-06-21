# Video Script — Food Delivery Order Management System

Total target: 8–10 minutes. Sections have time budgets.

---

## INTRO (30 sec)

> "Hi, I'm Kartik. I'm going to walk you through a food delivery order management
> system I built using Spring Boot 3.2 and Java.
> I'll cover the architecture, how I used AI tooling, the core technical design,
> and a live demo. Let's get into it."

**Show:** Nothing yet. Just face cam or title slide.

---

## SECTION 1 — Problem & Scope (1.5 min)

**Show:** Open `README.md` in IntelliJ or VS Code

> "The system handles multiple cities, each with their own restaurants and delivery
> partners. It supports the full order lifecycle — from placed, to accepted,
> preparing, ready for pickup, out for delivery, and finally delivered."

**Scroll to roles section in README**

> "There are four roles — Admin manages cities and partners. Restaurant owners manage
> their menus and respond to incoming orders. Customers place orders and leave ratings.
> Delivery partners get auto-assigned and update delivery status."

> "The three hard requirements I focused on were:
> One — concurrent stock protection, no overselling.
> Two — atomic order placement — stock, order state, and payment all or nothing.
> Three — async notification fan-out that doesn't block the caller."

---

## SECTION 2 — AI Workflow (2.5 min) ← MOST IMPORTANT SECTION

**Show:** Switch to browser — open GitHub commit history

> "I used Claude Code as my AI pair programmer. What I want to show you is that I
> didn't just prompt and accept — I directed the architecture at every step."

**Point at the 8 commits**

> "You can see the commits are layered — foundation, domain model, repositories,
> security, DTOs, services, controllers, tests. Each layer was a deliberate
> checkpoint, not one big dump."

**Switch to CLAUDE.md in browser**

> "This file documents exactly where I intervened."

**Read iteration 2**

> "The AI initially proposed optimistic locking for stock deduction. I pushed back —
> I said pessimistic locking is the right call here because the contention window is
> narrow and the cost of overselling is high. We can't retry our way out of
> a negative stock balance."

**Read iteration 3**

> "The AI had payment creation in a separate transaction. I caught that immediately —
> if payment is separate and something crashes between order creation and payment,
> you get an orphaned order with deducted stock and no payment. I required a single
> transaction boundary for all three."

**Read iteration 4**

> "The AI used @Async alone on the event listener. I pointed out the problem — if
> the notification fires before the transaction commits and the transaction then
> rolls back, you've just sent a confirmation for an order that doesn't exist.
> I required @TransactionalEventListener with AFTER_COMMIT phase."

> "That's the kind of reasoning I was applying throughout — the AI generated the
> structure, I validated the correctness."

---

## SECTION 3 — Architecture Walkthrough (2 min)

**Show:** IntelliJ — project folder structure

> "Standard layered architecture. Controller, service, repository. Entities never
> leave the service layer raw — everything goes through DTOs. This means I can
> change a column name without breaking the API contract."

**Open `OrderService.java`, scroll to `placeOrder()`**

> "This is the most critical method in the system. Everything happens inside one
> @Transactional boundary."

**Point at `findByIdWithLock`**

> "For each item in the order, we acquire a pessimistic write lock — that's a
> SELECT FOR UPDATE at the database level. Two concurrent orders for the same item
> will queue here. The second one sees the updated stock after the first commits."

**Point at `eventPublisher.publishEvent`**

> "After the transaction commits, this event fires — not before. The caller gets
> their response immediately. Notifications go out on a separate thread pool."

**Open `OrderEventListener.java`**

> "Two annotations working together. @TransactionalEventListener with AFTER_COMMIT
> means it only fires on a successful commit. @Async means it runs on the
> notificationExecutor thread pool — completely non-blocking."

---

## SECTION 4 — Live Demo (2.5 min)

**Show:** Terminal — app should already be running

> "The app runs on H2 in-memory — no external database needed, starts in seconds."

**Run demo.sh step by step — say this for each step:**

### Step 1 — Register
> "Registering as a customer. The response includes a signed JWT — role is baked
> into the token. The server never stores this token."

### Step 2 — Browse Menu
> "Browsing the restaurant menu. Chicken Biryani has a stock of 20."

### Step 3 — Place Order
> "Placing an order for 2 biryanis and 1 naan. Watch the response — payment status
> is COMPLETED and order status is PLACED. All in one transaction."

### Step 4 — Check Stock
> "Stock dropped from 20 to 18. That happened inside the same transaction as the
> order creation."

### Step 5 — Accept Order
> "Restaurant owner accepts the order. Notice — deliveryPartnerId is automatically
> populated. The system found an available partner in the same city and assigned
> them using optimistic locking."

### Steps 6–7 — PREPARING → READY_FOR_PICKUP
> "Restaurant moves it through the state machine. Invalid transitions are rejected —
> you can't jump from PLACED to DELIVERED."

### Steps 8–9 — OUT_FOR_DELIVERY → DELIVERED
> "Delivery partner picks up and delivers. DeliveredAt timestamp is set."

### Steps 10–11 — Rate
> "Customer rates both the restaurant and the delivery partner. Two separate ratings
> per order — both allowed."

### Step 12 — Duplicate blocked
> "Trying to rate the restaurant again — blocked. There's a unique constraint on
> order ID plus rating target."

### Step 13 — avgRating updated
> "Restaurant's avgRating updated in the same transaction as the rating save."

---

## SECTION 5 — Tests (1 min)

**Show:** IntelliJ — open `OrderServiceTest.java` briefly

> "Unit tests with Mockito isolate the service logic — stock deduction, insufficient
> stock, closed restaurant, cancel and restore."

**Switch to terminal — run `mvn test`**

> "Integration tests use MockMvc and H2 — no mocks, full request through the
> security filter and into the database."

**Wait for results to show**

> "19 tests, zero failures."

---

## OUTRO (15 sec)

> "That covers the architecture, the AI workflow, and a full end-to-end demo.
> The repo is on GitHub if you want to dig deeper. Thanks."

---

## BEFORE YOU HIT RECORD — CHECKLIST

- [ ] App is running (`mvn spring-boot:run`)
- [ ] `setup.sh` has been run — tokens are pasted into `demo.sh`
- [ ] IntelliJ has these files open in tabs: README.md, OrderService.java, OrderEventListener.java, OrderServiceTest.java
- [ ] Browser has these tabs open: GitHub commit history, CLAUDE.md
- [ ] Terminal is clean and visible
- [ ] `demo.sh` is ready to run in a second terminal tab
- [ ] Loom is recording screen + face cam
