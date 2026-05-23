package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.User.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.active = true
              AND u.id <> :currentUserId
              AND (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%'))
                 OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :name, '%'))
              )
              AND EXISTS (
                    SELECT f
                    FROM Friendship f
                    WHERE f.status = com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus.ACCEPTED
                      AND ((f.requesterId = :currentUserId AND f.addresseeId = u.id)
                        OR (f.addresseeId = :currentUserId AND f.requesterId = u.id))
              )
            ORDER BY u.displayName ASC, u.username ASC
            """)
    List<User> searchAcceptedFriendsByName(
            @Param("currentUserId") String currentUserId,
            @Param("name") String name,
            Pageable pageable);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.active = true
              AND u.id <> :currentUserId
              AND (
                    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY u.displayName ASC, u.username ASC
            """)
    List<User> searchUsersForFriendRequest(
            @Param("currentUserId") String currentUserId,
            @Param("keyword") String keyword,
            Pageable pageable);

}
