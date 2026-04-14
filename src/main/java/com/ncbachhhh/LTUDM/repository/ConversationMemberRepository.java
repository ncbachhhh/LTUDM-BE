package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.Key.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    List<ConversationMember> findByIdConversationId(String conversationId);

    List<ConversationMember> findByIdConversationIdIn(Collection<String> conversationIds);

    List<ConversationMember> findByIdUserId(String userId);

    boolean existsByIdConversationIdAndIdUserId(String conversationId, String userId);

    long countByIdConversationId(String conversationId);

    @Modifying
    void deleteByIdConversationId(String conversationId);

    @Query("""
            SELECT COUNT(cm)
            FROM ConversationMember cm
            WHERE cm.id.conversationId = :conversationId
              AND cm.id.userId = :userId
            """)
    long countMemberInConversation(@Param("conversationId") String conversationId, @Param("userId") String userId);
}
