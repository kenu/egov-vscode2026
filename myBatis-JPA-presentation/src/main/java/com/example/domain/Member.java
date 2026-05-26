package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted; // 회원 탈퇴 여부

    private Member(String name, Boolean isDeleted) {
        this.name = name;
        this.isDeleted = isDeleted;
    }
    public static Member createNewMember(String name) {
        return new Member(name, false); 
    }

    public void changeName(String name) {
        this.name = name;
    }
}
