package com.ncbachhhh.LTUDM.entity.Block;

import com.ncbachhhh.LTUDM.entity.Key.BlockId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "blocks")
public class Block {
    @EmbeddedId
    private BlockId id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
