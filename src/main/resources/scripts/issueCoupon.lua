-- KEYS: [coupon_stock_key, coupon_user_set, event_integrated_user_set]
-- ARGV: [userId]

-- 1. Check event integrated duplication (Have you received another coupon from this event?)
if redis.call('SISMEMBER', KEYS[3], ARGV[1]) == 1 then
    return -3 -- Error Code: "You already hold a coupon from this event."
end

-- 2. Check stock
local stock = tonumber(redis.call('GET', KEYS[1]) or "0")
if stock <= 0 then
    return -2 -- Error Code: "First-come-first-served ended"
end

-- 3. Process (Atomic execution)
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1]) -- Save user who received this coupon
redis.call('SADD', KEYS[3], ARGV[1]) -- Save as event participant (Lock role)

return 1