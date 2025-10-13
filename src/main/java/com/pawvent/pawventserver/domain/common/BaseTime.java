package com.pawvent.pawventserver.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

//공통 날짜 설정
@MappedSuperclass
public abstract class BaseTime {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    protected OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected OffsetDateTime updatedAt;

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
