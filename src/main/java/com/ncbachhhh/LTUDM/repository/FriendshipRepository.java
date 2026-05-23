package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Friendship.Friendship;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    List<Friendship> findByRequesterId(String requesterId);

    List<Friendship> findByAddresseeId(String addresseeId);

    List<Friendship> findByRequesterIdAndStatus(String requesterId, FriendshipStatus status);

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE f.status = :status
              AND (f.requesterId = :userId OR f.addresseeId = :userId)
            """)
    List<Friendship> findByUserIdAndStatus(
            @Param("userId") String userId,
            @Param("status") FriendshipStatus status);

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE (f.requesterId = :firstUserId AND f.addresseeId = :secondUserId)
               OR (f.requesterId = :secondUserId AND f.addresseeId = :firstUserId)
            """)
    List<Friendship> findAllBetweenUsers(
            @Param("firstUserId") String firstUserId,
            @Param("secondUserId") String secondUserId);

    default Optional<Friendship> findBetweenUsers(String firstUserId, String secondUserId) {
        return findAllBetweenUsers(firstUserId, secondUserId).stream().findFirst();
    }

    @Query("""
            SELECT COUNT(f) > 0
            FROM Friendship f
            WHERE f.status = :status
              AND ((f.requesterId = :firstUserId AND f.addresseeId = :secondUserId)
                OR (f.requesterId = :secondUserId AND f.addresseeId = :firstUserId))
            """)
    boolean existsBetweenUsersByStatus(
            @Param("firstUserId") String firstUserId,
            @Param("secondUserId") String secondUserId,
            @Param("status") FriendshipStatus status);
}
