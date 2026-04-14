package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, MessageReceiptId> {
    @Modifying
    void deleteByIdMessageIdIn(Collection<String> messageIds);
}
