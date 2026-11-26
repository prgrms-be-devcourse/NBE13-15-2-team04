# NBE13-15-2-team04

2차 프로젝트 4팀

## 로컬 개발 환경 설정

### 1. 데이터베이스 초기화 (MySQL)

1. MySQL에 프로젝트용 데이터베이스를 생성합니다. (문자셋은 `utf8mb4` 권장)
2. 생성한 데이터베이스에서 [`src/main/resources/sql/schema.sql`](src/main/resources/sql/schema.sql)을 실행해 테이블을 만듭니다.
   ```bash
   mysql -u <사용자명> -p <데이터베이스명> < src/main/resources/sql/schema.sql
   ```
   IntelliJ Database 탭이나 MySQL Workbench에서 파일을 열어 실행해도 됩니다.

### 2. 환경 변수 설정 (`.env`)

DB 접속 정보는 프로젝트 루트의 `.env` 파일에서 읽어옵니다. `.env`는 Git에 올라가지 않으므로 각자 만들어 주세요.

```properties
DB_URL=jdbc:mysql://localhost:3306/<데이터베이스명>
DB_USERNAME=<사용자명>
DB_PASSWORD=<비밀번호>

# 선택: 없으면 application.yaml의 로컬 개발용 기본값을 사용
JWT_SECRET=<32자 이상 임의 문자열>
CORS_ORIGIN=http://127.0.0.1:5173
```

- `JWT_SECRET`은 32바이트보다 짧으면 실행 시 오류가 발생합니다. 운영 환경에서는 반드시 별도 값으로 설정해 주세요.

### 3. 실행

```bash
./gradlew bootRun
```
