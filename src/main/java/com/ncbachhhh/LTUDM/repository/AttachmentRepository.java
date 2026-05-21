package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Attachment.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    Optional<Attachment> findByMessageId(String messageId);

    List<Attachment> findByMessageIdIn(Collection<String> messageIds);
}
