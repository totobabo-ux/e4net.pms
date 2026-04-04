package com.e4net.pms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class AppMenuDto {

    private Long    id;
    private Long    parentId;
    private String  parentName;
    private Integer depth;
    private Integer sortOrder;
    private String  menuCode;
    private String  menuName;
    private String  contextPath;
    private String  icon;
    private String  fixedYn;
    private String  useYn;
}
