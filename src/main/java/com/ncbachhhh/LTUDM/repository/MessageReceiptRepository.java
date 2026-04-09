package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Key.MessageReceiptId;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, MessageReceiptId> {
}
