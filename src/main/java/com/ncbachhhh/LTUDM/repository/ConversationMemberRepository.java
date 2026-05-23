package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.Key.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    List<ConversationMember> findByIdConversationId(String conversationId);

    List<ConversationMember> findByIdUserId(String userId);

    boolean existsByIdConversationIdAndIdUserId(String conversationId, String userId);

    @Modifying
    void deleteByIdConversationId(String conversationId);
}
