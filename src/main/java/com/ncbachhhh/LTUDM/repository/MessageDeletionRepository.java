package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
import com.ncbachhhh.LTUDM.entity.MessageDeletion.MessageDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MessageDeletionRepository extends JpaRepository<MessageDeletion, MessageDeletionId> {
    @Modifying
    void deleteByIdMessageIdIn(Collection<String> messageIds);
}
