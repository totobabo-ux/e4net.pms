package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class CommunityDto {

    private Long id;

    private Long projectId;          // 선택된 사업 ID (null 허용)
    private String projectName;      // 화면 표시용

    private String communityType;   // 공지사항 / 자료실

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String writer;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate postDate;
    private String content;

    private List<AttachFileDto> attachments;
}
