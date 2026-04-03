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
@Table(name = "common_code",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_code", "code"}))
@Getter @Setter @NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CommonCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;           // 코드 그룹 (GRADE, INPUT_TYPE, MANPOWER_STATUS ...)

    @Column(name = "code", nullable = false, length = 50)
    private String code;                // 코드값

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;            // 코드명 (화면에 표시)

    @Column(name = "sort_order")
    private Integer sortOrder = 0;      // 정렬 순서

    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";         // 사용여부

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
