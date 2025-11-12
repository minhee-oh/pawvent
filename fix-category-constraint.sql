-- community_post 테이블의 category CHECK 제약조건 수정
-- 기존 제약조건 삭제 후 새로운 제약조건 추가

-- 1. 기존 제약조건 삭제
ALTER TABLE community_post DROP CONSTRAINT IF EXISTS community_post_category_check;

-- 2. 새로운 제약조건 추가 (WALK_CERTIFICATION, FREE, SAFETY 허용)
ALTER TABLE community_post 
ADD CONSTRAINT community_post_category_check 
CHECK (category IN ('WALK_CERTIFICATION', 'FREE', 'SAFETY'));


