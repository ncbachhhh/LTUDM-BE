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

    User findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.active = true
              AND u.id <> :currentUserId
              AND (
                    LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY u.displayName ASC, u.username ASC
            """)
    List<User> searchOtherActiveUsers(
            @Param("currentUserId") String currentUserId,
            @Param("query") String query,
            Pageable pageable);

}
