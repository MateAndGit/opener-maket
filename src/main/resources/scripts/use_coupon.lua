-- KEYS: [available_stock_key, used_user_set]
-- ARGV: [userId]

-- 1. Check if already used
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1 -- "Coupon already used."
end

-- 2. Check available 'remaining count'
local remaining = tonumber(redis.call('GET', KEYS[1]) or "0")
if remaining <= 0 then
    return -4 -- "Coupon retracted as first-come-first-served ended."
end

-- 3. Process usage
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])

return 1