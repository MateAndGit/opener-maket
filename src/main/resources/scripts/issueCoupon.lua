-- KEYS: [쿠폰재고키, 쿠폰별유저셋, 이벤트통합유저셋]
-- ARGV: [userId]

-- 1. 이벤트 통합 중복 확인 (이 이벤트에서 다른 쿠폰 받은 적 있나?)
if redis.call('SISMEMBER', KEYS[3], ARGV[1]) == 1 then
    return -3 -- 에러코드: "이미 이 이벤트의 쿠폰을 하나 보유 중입니다."
end

-- 2. 재고 확인
local stock = tonumber(redis.call('GET', KEYS[1]) or "0")
if stock <= 0 then
    return -2 -- 에러코드: "선착순 마감"
end

-- 3. 처리 (원자적 실행)
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1]) -- 이 쿠폰 받은 유저 저장
redis.call('SADD', KEYS[3], ARGV[1]) -- 이 이벤트 참여자로 저장 (락 역할)

return 1