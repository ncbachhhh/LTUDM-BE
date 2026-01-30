package com.ncbachhhh.LTUDM.entity.ConversationMembers;

import com.ncbachhhh.LTUDM.entity.Key.ConversationMemberId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

@Table(name = "conversation_members")
public class ConversationMember {
    @EmbeddedId
    private ConversationMemberId id;

    @Enumerated(EnumType.STRING)
    private ConversationMemberRole role; // Vai trò của thành viên trong cuộc trò chuyện
}
