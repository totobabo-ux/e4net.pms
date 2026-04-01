package com.e4net.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "common_code",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_code", "code"}))
@Getter @Setter @NoArgsConstructor
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
}
