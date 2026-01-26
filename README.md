## Step 2. Enhancing Purchase Functionality

### Implementation Details

- **Robust Purchase Process**: Strengthen the ordering flow with the following mandatory steps:
    - **Balance Deduction**: Deduct the total amount from the buyer's virtual cash.
    - **Inventory Management**: Decrease stock by 1. Unlike Step 1, purchases must fail if stock is zero, and the item status must change to `SOLD_OUT`.
    - **Reward Points**: Credit 2.5% of the payment amount as reward points.
- **Point Usage**:
    - Buyers can choose to spend points during purchase.
    - The specific amount of points to use must be determined by the user (Minimum usage: 1 Point).

### Programming Requirements

- **System Architecture**: Design the sequence of balance deduction, stock updates, and point accrual.
- **Error Recovery**: Define how the system should recover or rollback if a specific step in the process fails.
- **Logical Gap Analysis**: Identify and resolve the logical inconsistency between Step 1 and Step 2 using provided hints. (Hint: Think about concurrency and data integrity).