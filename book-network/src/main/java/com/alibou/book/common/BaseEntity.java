package com.alibou.book.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @CreatedDate
    @Column(updatable = false,nullable = false)
    private LocalDateTime createdDate;

    @Column(insertable = false)
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    @CreatedBy
    @Column(updatable = false,nullable = false)
    private Integer createdBy;   //user id

    @LastModifiedBy
    @Column(insertable = false)
    private Integer lastModifiedBy;
}
