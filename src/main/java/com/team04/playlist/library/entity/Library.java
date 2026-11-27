package com.team04.playlist.library.entity;


import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "library",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_library_user_name",
                columnNames = {"user_id", "name"}
        )
)
public class Library{

    private static final String DEFAULT_NAME = "기본 보관함";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Library(Long userId, String name, boolean isDefault) {
        this.userId = userId;
        this.name = name;
        this.isDefault = isDefault;
    }

    //최초 로그인 때, 기본 보관함 생성
    public static Library createDefault(Long userId){
        return new Library(userId,DEFAULT_NAME ,true);
    }

    //유저가 해당 보관함 주인인지 판단
    public void validateOwner(Long userId) {
        if(!this.userId.equals(userId)){
            throw new BusinessException(ErrorCode.NOT_LIBRARY_OWNER);
        }
    }
}