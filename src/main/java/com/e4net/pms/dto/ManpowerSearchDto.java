package com.e4net.pms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ManpowerSearchDto {
    private String projectName;  // 사업명
    private String userName;     // 참여자명
    private String company;      // 소속회사
    private String status;       // 상태
}
