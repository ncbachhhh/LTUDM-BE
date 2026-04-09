package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Key.MessageDeletionId;
import com.ncbachhhh.LTUDM.entity.MessageDeletion.MessageDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageDeletionRepository extends JpaRepository<MessageDeletion, MessageDeletionId> {
}
