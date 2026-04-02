## 18. 테스트 계정 관리 및 curl 통합 테스트 패턴

### BCrypt 해시 생성 (테스트 계정 DB 삽입용)
```bash
# Maven 테스트 클래스로 해시 생성
cat > src/test/java/com/e4net/pms/HashGeneratorTest.java << 'EOF'
package com.e4net.pms;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGeneratorTest {
    @Test void generateHash() {
        System.out.println(new BCryptPasswordEncoder().encode("원하는비밀번호"));
    }
}
EOF
mvn test -Dtest=HashGeneratorTest 2>&1 | grep '^\$2a'
```

### 테스트 계정 DB 삽입 (users 테이블 스키마 기준)
```sql
INSERT INTO users (employee_no, name, email, password)
VALUES ('devtest', '개발테스트', 'dev@test.com', '{bcrypt_hash}')
ON DUPLICATE KEY UPDATE password = '{bcrypt_hash}';
```
> `users` 테이블에 `role` 컬럼 없음 — INSERT 시 포함하면 오류

### curl 세션 기반 통합 테스트 패턴
```bash
# 1) 로그인 (session.txt에 쿠키 저장)
curl -s -c session.txt -b session.txt \
  -X POST http://localhost:8080/login \
  -d "username=devtest&password=test1234" \
  -D headers.txt -o /dev/null
grep "Location" headers.txt   # project-select 로 리다이렉트 되면 로그인 성공

# 2) 프로젝트 선택
curl -s -c session.txt -b session.txt \
  -X POST http://localhost:8080/project-select \
  -d "projectId=1" -o /dev/null

# 3) 기능 테스트
curl -s -c session.txt -b session.txt http://localhost:8080/{resource} -w "HTTP: %{http_code}"

# 엑셀 다운로드
curl -s -c session.txt -b session.txt \
  http://localhost:8080/{resource}/excel/download \
  -o result.xlsx -w "HTTP: %{http_code}, 크기: %{size_download} bytes"

# 엑셀 업로드
curl -s -c session.txt -b session.txt \
  -X POST http://localhost:8080/{resource}/excel/upload \
  -F "excelFile=@result.xlsx" -D - -o /dev/null | grep Location
```

> **중요:** curl 세션 요청 시 `-c session.txt -b session.txt` **둘 다** 지정해야 쿠키가 읽히고 쓰임.
> `-c`만 지정하면 쿠키를 저장하지만 전송하지 않음 → 세션 인식 불가

### 엑셀 파일 유효성 빠른 확인
```bash
file result.xlsx          # "Microsoft Excel 2007+" 확인
unzip -l result.xlsx      # 내부 구조 확인
unzip -p result.xlsx xl/sharedStrings.xml   # 셀 텍스트 내용 확인
```
