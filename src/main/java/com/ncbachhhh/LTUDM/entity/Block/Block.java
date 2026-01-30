package com.ncbachhhh.LTUDM.entity.Block;

import com.ncbachhhh.LTUDM.entity.Key.BlockId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter

@Table(name = "blocks")
public class Block {
    @EmbeddedId
    private BlockId id;
    private LocalDate created_at; // Thời gian chặn người dùng
}
