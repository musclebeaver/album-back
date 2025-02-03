# Database Schema

## 1. users 테이블
사용자 정보를 저장하는 테이블입니다.

| 컬럼명    | 데이터 타입   | 설명                                   |
|-----------|--------------|----------------------------------------|
| id        | BIGINT       | 사용자의 고유 식별자 (기본 키)         |
| username  | VARCHAR(255) | 사용자명 (고유)                        |
| email     | VARCHAR(255) | 이메일 주소 (고유)                      |
| password  | VARCHAR(255) | 비밀번호                               |
| is_approved | BOOLEAN    | 관리자 승인 여부 (기본값: false)         |

## 2. folder 테이블
사용자가 생성한 폴더 정보를 저장하는 테이블입니다.

| 컬럼명  | 데이터 타입   | 설명                                    |
|---------|--------------|-----------------------------------------|
| id      | BIGINT       | 폴더의 고유 식별자 (기본 키)             |
| name    | VARCHAR(100) | 폴더 이름                               |
| user_id | BIGINT       | 폴더를 생성한 사용자의 ID (외래 키)      |

> **외래 키**: `user_id`는 `users` 테이블의 `id`를 참조합니다.

## 3. photo 테이블
폴더에 저장된 사진 정보를 저장하는 테이블입니다.

| 컬럼명      | 데이터 타입   | 설명                                  |
|------------|--------------|---------------------------------------|
| id         | BIGINT       | 사진의 고유 식별자 (기본 키)         |
| title      | VARCHAR(100) | 사진 제목                            |
| description| VARCHAR(500) | 사진 설명                            |
| image_url  | TEXT         | 사진 파일의 URL 또는 경로             |
| folder_id  | BIGINT       | 사진이 속한 폴더의 ID (외래 키)        |

> **외래 키**: `folder_id`는 `folder` 테이블의 `id`를 참조합니다.

## 테이블 관계
- `users` ↔ `folder`: 한 명의 사용자(`users`)는 여러 개의 폴더(`folder`)를 가질 수 있습니다. (`folder.user_id` → `users.id`)
- `folder` ↔ `photo`: 하나의 폴더(`folder`)는 여러 개의 사진(`photo`)을 가질 수 있습니다. (`photo.folder_id` → `folder.id`)

## ERD (Entity-Relationship Diagram)
```
+----------------+       +----------------+       +----------------+
| users         |       | folder         |       | photo          |
+----------------+       +----------------+       +----------------+
| id (PK)       |<------>| id (PK)        |<------>| id (PK)        |
| username      |       | name           |       | title          |
| email         |       | user_id (FK)   |       | description    |
| password      |       +----------------+       | image_url      |
| is_approved   |                                | folder_id (FK) |
+----------------+                                +----------------+
```

## SQL 테이블 생성 스크립트
```sql
-- users 테이블 생성
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE
);

-- folder 테이블 생성
CREATE TABLE folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- photo 테이블 생성
CREATE TABLE photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image_url TEXT NOT NULL,
    folder_id BIGINT NOT NULL,
    FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE CASCADE
);
```

