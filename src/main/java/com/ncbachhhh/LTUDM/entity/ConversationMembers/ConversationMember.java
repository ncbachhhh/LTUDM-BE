package com.ncbachhhh.LTUDM.entity.ConversationMembers;

import com.ncbachhhh.LTUDM.entity.Key.ConversationMemberId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "conversation_members")
public class ConversationMember {
    @EmbeddedId
    private ConversationMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMemberRole role = ConversationMemberRole.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;
}
