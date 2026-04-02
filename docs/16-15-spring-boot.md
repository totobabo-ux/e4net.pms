## 15. Spring Boot 서버 기동 방법
```bash
# 프로젝트 루트에서 실행
mvn spring-boot:run

# 포트 충돌 시 기존 프로세스 종료 후 재기동 (Windows)
netstat -ano | grep :8080       # PID 확인
taskkill /PID <PID> /F
mvn spring-boot:run

# 스테일 클래스 파일로 인한 오류 발생 시
mvn clean spring-boot:run
```
