-- PostgreSQL + PostGIS 데이터베이스 초기화 스크립트

-- 1. PostGIS 확장 설치 (이미 설치되어 있지 않은 경우)
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. 샘플 데이터 삽입 (테스트용)

-- 사용자 데이터 (카카오 로그인 후 자동 생성됨)
-- INSERT INTO users (kakao_id, email, nickname, profile_image_url, role, created_at, updated_at) 
-- VALUES (12345678, 'test@example.com', '테스트유저', 'http://example.com/image.jpg', 'USER', NOW(), NOW());

-- 위험 카테고리 확인을 위한 샘플 데이터
-- 실제 운영에서는 사용자가 직접 신고하여 데이터가 생성됩니다.

-- 서울 주요 지역의 샘플 위험 스팟 (테스트용)
/*
INSERT INTO hazard (category, description, location, user_id, created_at, updated_at) VALUES
('BROKEN_ROAD', '보도블록이 깨져있어 위험합니다', ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326), 1, NOW(), NOW()),
('TRAFFIC_DANGER', '차량 통행량이 많아 주의가 필요합니다', ST_SetSRID(ST_MakePoint(126.9800, 37.5670), 4326), 1, NOW(), NOW()),
('CONSTRUCTION', '공사 중으로 우회가 필요합니다', ST_SetSRID(ST_MakePoint(126.9790, 37.5660), 4326), 1, NOW(), NOW());
*/

-- 공간 인덱스 생성 (성능 향상을 위해)
CREATE INDEX IF NOT EXISTS idx_hazard_location ON hazard USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_walk_route_route_data ON walk_route USING GIST (route_data);

-- 데이터베이스 설정 확인
SELECT version();
SELECT PostGIS_version();

-- 테이블 구조 확인
\d users
\d hazard
\d walk_route

