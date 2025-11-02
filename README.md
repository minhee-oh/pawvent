# Pawvent - 반려견 안전 산책 지도 서비스

반려견과 함께하는 안전한 산책을 위한 스마트 지도 서비스입니다.

## 🐕 프로젝트 소개

Pawvent는 반려견 소유자들이 안전하고 즐거운 산책을 할 수 있도록 돕는 웹 애플리케이션입니다. 실시간 위험 스팟 정보 공유, 산책 루트 저장 및 공유, 응급상황 대응 등의 기능을 제공합니다.

## ✨ 주요 기능 (MVP)

### 1. 실시간 지도표시 및 산책 루트 저장 서비스
- 카카오맵 API를 활용한 정확한 지도 서비스
- 산책 경로 그리기 및 저장
- 저장된 루트 관리 (수정, 삭제, 공유)

### 2. 스팟 등록 서비스 (좌표, 카테고리, 설명, 사진 등 첨부)
- 다양한 카테고리의 스팟 등록
- GPS 좌표 기반 정확한 위치 정보
- 사진 및 상세 설명 추가 가능

### 3. 위험 스팟 근처에서 우회 일림 서비스
- 실시간 위험 지역 알림
- 안전한 대체 경로 제안
- 사용자 제보 기반 위험 정보 업데이트

### 4. 위험 상황시 대응 서비스 제공
- 응급상황별 맞춤형 대응 가이드
- 근처 위험 요소 실시간 확인
- 안전 지역으로의 경로 안내

### 5. 반응형 웹페이지 서비스
- 모바일 및 데스크톱 최적화
- 직관적이고 사용하기 쉬운 UI/UX

### 6. 로그인 서비스 (카카오 OAuth)
- 간편한 카카오 로그인
- 사용자별 맞춤형 서비스 제공

## 🔧 기술 스택

### Backend
- **Spring Boot 3.5.6** - 메인 프레임워크
- **Spring Security** - 인증 및 보안
- **Spring Data JPA** - ORM 및 데이터 접근
- **PostgreSQL** - 메인 데이터베이스
- **PostGIS** - 공간 데이터 처리
- **JWT** - 토큰 기반 인증

### Frontend
- **Thymeleaf** - 서버사이드 템플릿 엔진
- **HTML5/CSS3/JavaScript** - 기본 웹 기술
- **카카오맵 API** - 지도 서비스

### 인증
- **카카오 OAuth 2.0** - 소셜 로그인

## 🏗️ 프로젝트 구조

```
pawvent/
├── src/
│   ├── main/
│   │   ├── java/com/pawvent/pawventserver/
│   │   │   ├── config/          # 설정 클래스
│   │   │   ├── controller/      # REST API 컨트롤러
│   │   │   ├── domain/          # 엔티티 클래스
│   │   │   ├── dto/             # 데이터 전송 객체
│   │   │   ├── repository/      # 데이터 접근 계층
│   │   │   └── service/         # 비즈니스 로직
│   │   └── resources/
│   │       ├── templates/       # Thymeleaf 템플릿
│   │       └── application.properties
│   └── test/
├── build.gradle
└── README.md
```

## 📋 설치 및 실행 가이드

### 1. 사전 요구사항
- Java 17 이상
- PostgreSQL 14 이상 (PostGIS 확장 포함)
- Gradle 7.0 이상

### 2. 데이터베이스 설정
```sql
-- PostgreSQL에 PostGIS 확장 추가
CREATE EXTENSION postgis;

-- 데이터베이스 생성
CREATE DATABASE pawvent_db;
```

### 3. 설정 파일 수정
`src/main/resources/application.properties` 파일에서 다음 설정을 수정하세요:

```properties
# 데이터베이스 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/pawvent_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# 카카오 OAuth 설정
spring.security.oauth2.client.registration.kakao.client-id=YOUR_KAKAO_CLIENT_ID
spring.security.oauth2.client.registration.kakao.client-secret=YOUR_KAKAO_CLIENT_SECRET

# 카카오맵 API 키
kakao.map.api-key=YOUR_KAKAO_MAP_API_KEY
```

### 4. 카카오 개발자 등록
1. [카카오 개발자 사이트](https://developers.kakao.com/)에서 애플리케이션 등록
2. OAuth 리다이렉트 URI 설정: `http://localhost:8081/login/oauth2/code/kakao`
3. 카카오맵 API 키 발급

### 5. 애플리케이션 실행
```bash
./gradlew bootRun
```

애플리케이션이 시작되면 `http://localhost:8081`에서 접근할 수 있습니다.

## 🔍 주요 API 엔드포인트

### 인증
- `GET /oauth2/authorization/kakao` - 카카오 로그인

### 산책 루트
- `POST /api/routes` - 루트 저장
- `GET /api/routes/my` - 내 루트 조회
- `GET /api/routes/shared` - 공유 루트 조회
- `PUT /api/routes/{id}` - 루트 수정
- `DELETE /api/routes/{id}` - 루트 삭제

### 위험 스팟
- `POST /api/hazards/report` - 위험 스팟 신고
- `GET /api/hazards/nearby` - 주변 위험 스팟 조회
- `GET /api/hazards/category/{category}` - 카테고리별 조회

### 응급상황
- `POST /api/emergency/report` - 응급상황 신고
- `GET /api/emergency/safe-route` - 안전 경로 조회

## 🗄️ 데이터베이스 스키마

### 주요 테이블
- **users**: 사용자 정보
- **pets**: 반려동물 정보  
- **walk_routes**: 산책 루트 (PostGIS LineString)
- **hazards**: 위험 스팟 (PostGIS Point)
- **bookmarks**: 즐겨찾기
- **challenges**: 챌린지 정보

## 🚀 확장 기능 (향후 개발 예정)

### 1. 커뮤니티 CRUD 서비스
- 게시글 작성, 수정, 삭제
- 댓글 시스템
- 좋아요 및 북마크 기능

### 2. 통계 서비스
- 오늘 총 산책 시간, 총 칼로리 소모량 등
- 주/월/년 단위 통계

### 3. 주변 추천 스팟 표시 서비스
- AI 기반 맞춤형 장소 추천
- 애견 동반 카페, 병원 등 정보

### 4. 이달의 챌린지와 랭킹 서비스
- 월간 산책 챌린지
- 사용자 랭킹 시스템
- 리워드 시스템

---

**Pawvent** - 반려견과 함께하는 안전한 산책의 시작 🐕💙

