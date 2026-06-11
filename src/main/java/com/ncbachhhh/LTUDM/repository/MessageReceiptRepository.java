package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, MessageReceiptId> {
    List<MessageReceipt> findByIdMessageId(String messageId);

    List<MessageReceipt> findByIdMessageIdIn(Collection<String> messageIds);

    @Modifying
    void deleteByIdMessageIdIn(Collection<String> messageIds);
}
