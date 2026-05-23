package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Block.Block;
import com.ncbachhhh.LTUDM.entity.Key.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, BlockId> {
    @Query("""
            SELECT b
            FROM Block b
            WHERE b.id.blockerId = :blockerId
            """)
    List<Block> findByBlockerId(@Param("blockerId") String blockerId);

    @Query("""
            SELECT COUNT(b) > 0
            FROM Block b
            WHERE b.id.blockerId = :blockerId
              AND b.id.blockedId = :blockedId
            """)
    boolean existsByBlockerIdAndBlockedId(
            @Param("blockerId") String blockerId,
            @Param("blockedId") String blockedId);

    @Modifying
    @Query("""
            DELETE FROM Block b
            WHERE b.id.blockerId = :blockerId
              AND b.id.blockedId = :blockedId
            """)
    void deleteByBlockerIdAndBlockedId(
            @Param("blockerId") String blockerId,
            @Param("blockedId") String blockedId);
}
