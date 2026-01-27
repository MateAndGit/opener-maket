## Step 3: Product Search & Review System Implementation

### 1. Requirements
- **Search Functionality**:
  - Minimum keyword length: 2 characters.
  - Search target: Product names containing the keyword (Partial match).
- **Sorting Criteria**:
  - Most Purchased (Sales Volume)
  - Highest Rating (Review Score)
  - Price: Low to High / High to Low
  - Latest (Newest arrival)
- **Review System**:
  - Rating scale: 1 to 5 stars.
  - Restriction: Only users who have purchased the specific product can write a review.

### 2. Performance Optimization Goals
The core focus of this step is "Scalability." As data grows, naive implementations lead to significant performance degradation.

- **Data Seeding**: Generate 10,000 to 100,000 dummy records for products and reviews.
- **Benchmark Comparison**:
  - Compare **Naive Implementation** (Full Table Scan, real-time AVG calculation) vs. **Optimized Implementation** (Indexing, Denormalization).
  - Measure response times for each sorting criteria under heavy data load.

### 3. Key Technical Considerations
- **Indexing Strategy**: How to apply indexes to multi-column sorting (Composite Indexes).
- **Denormalization**: Maintaining an `average_rating` or `review_count` field in the `SellItem` entity to avoid expensive JOIN and AGGREGATE operations during search.
- **Query DSL/Criteria API**: Implementing dynamic sorting and filtering efficiently in Java.