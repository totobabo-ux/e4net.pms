package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;          // 사번 (회원 ID, 유니크)

    @Column(name = "password", nullable = false, length = 255)
    private String password;            // 비밀번호 (BCrypt 해시)

    @Column(name = "name", nullable = false, length = 50)
    private String name;                // 이름

    @Column(name = "company", length = 100)
    private String company;             // 소속회사

    @Column(name = "department", length = 100)
    private String department;          // 소속부서

    @Column(name = "position", length = 50)
    private String position;            // 직위

    @Column(name = "phone", length = 20)
    private String phone;               // 연락처

    @Column(name = "email", length = 100)
    private String email;               // 이메일

    @Column(name = "reg_id", length = 50, updatable = false)
    private String regId;               // 등록자 ID

    @Column(name = "upd_id", length = 50)
    private String updId;               // 수정자 ID

    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt;        // 등록일시

    @LastModifiedDate
    @Column(name = "upd_dt")
    private LocalDateTime updDt;        // 수정일시
}
