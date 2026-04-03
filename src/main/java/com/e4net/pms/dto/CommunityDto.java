package com.e4net.pms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class CommunityDto {

    private Long id;

    private String communityType;   // 공지사항 / 자료실

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String writer;
    private LocalDate postDate;
    private String content;

    private List<AttachFileDto> attachments;
}
