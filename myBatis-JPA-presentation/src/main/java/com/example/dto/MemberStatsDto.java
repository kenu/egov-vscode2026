package com.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberStatsDto {
    private Long id;
    private String name;
    private int totalLoginCount;
    private String lastAccessDate;
    private Boolean isDeleted;
}
