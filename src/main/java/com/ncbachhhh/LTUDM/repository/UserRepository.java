package com.ncbachhhh.LTUDM.repository;

import com.ncbachhhh.LTUDM.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
}
