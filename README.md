## Step 1. Seller and Buyer Basic Setup

### Implementation Details

- **Trading Environment**: Establish a foundational environment for both sellers and buyers.
- **Cash System**: Since real-world currency transactions are not supported, implement a "Virtual Cash" charging system to enable purchases.
- **Selling Features**:
    - Sellers can list items for sale.
    - Each listing must include basic product information (Name, Description, Price) and the available stock quantity.
    - *Note: For this step, we assume purchases are possible even if the stock quantity becomes negative.*
- **Revenue & Commission**:
    - A **5% platform commission** is deducted from the total sales revenue before being paid out to the seller.
    - **Payout Escrow**: Funds are not released to the seller until the buyer explicitly clicks the "Confirm Purchase" button.
- **Buying Features**:
    - Buyers can purchase items using their recharged virtual cash balance.
    - Upon purchase, the order status changes to `Payment Completed`.
    - **Refund Policy**: Refunds are permitted until the order status changes to `Dispatched` (Shipping Started).
