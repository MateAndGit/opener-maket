### Step 4. High-Concurrency Coupon Issuance & Usage System

#### 1. Overview
This module focuses on building a robust coupon system capable of handling **DDoS-level traffic spikes** while maintaining **strict data consistency**. The design prioritizes preventing "Over-issuance" and managing real-time redemption limits using high-performance caching strategies.

#### 2. Core Requirements & Challenges
* **FCFS (First-Come, First-Served) Issuance**: Strict quantity control at the point of issuance.
* **FCFS Usage & Revocation**: Managing a global usage limit where "issued but unused" coupons must be invalidated once the redemption cap is reached.
* **Duplicate Prevention**: Ensuring a 1-to-1 relationship between a User and a Coupon within a specific event type.
* **System Stability**: Protecting the RDBMS from thundering herd problems during peak event hours.

#### 3. Technical Solutions (Architectural Approach)

| Problem | Solution | Strategy |
| :--- | :--- | :--- |
| **Race Condition** | **Redis Atomic Operations** | Leveraging `DECR` to manage inventory in-memory for $O(1)$ performance. |
| **Write-Through Bottleneck** | **Async Persistence** | Offloading DB writes to an asynchronous worker to reduce API response latency. |
| **Redemption Limit** | **Global Usage Counter** | Tracking real-time redemptions in Redis to trigger lazy revocation. |
| **Eligibility Check** | **Redis Set (SADD)** | Storing User IDs in a Set to achieve instant duplicate check. |



#### 4. Domain Logic & Coupon Strategies
* **`ISSUE_LIMITED`**: Hard-capped issuance quantity.
* **`USE_LIMITED`**: Unlimited issuance, but redemption is capped.
* **`STACKABLE`**: Boolean flag to determine if the coupon can be combined with other discounts.

#### 5. Revocation & Tracking Strategy
* **Status Management**: `READY` (Issued), `USED` (Redeemed), `REVOKED` (Exceeded limit).
* **Consistency Model**:
  * **Phase 1**: Validate eligibility and decrement counter in Redis.
  * **Phase 2**: If successful, log the issuance record in MySQL.
  * **Phase 3**: During payment, re-validate the global usage counter for `USE_LIMITED` types.



---