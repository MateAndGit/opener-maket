-- KEYS: [사용가능잔여수량키, 사용완료유저셋]
-- ARGV: [userId]

-- 1. 이미 사용했는지 확인
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1 -- "이미 사용된 쿠폰입니다."
end

-- 2. 사용 가능한 '잔여 횟수' 확인
local remaining = tonumber(redis.call('GET', KEYS[1]) or "0")
if remaining <= 0 then
    return -4 -- "사용 선착순이 마감되어 쿠폰이 회수되었습니다."
end

-- 3. 사용 처리
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])

return 1