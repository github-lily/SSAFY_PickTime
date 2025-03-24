package com.b101.pickTime.db.repository;

import com.b101.pickTime.api.user.response.UserInfoDto;
import com.b101.pickTime.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username); // 이미 존재하는 이메일인지 확인
    Optional<User> findByUsername(String username);

    @Query("SELECT new com.b101.pickTime.api.user.response.UserInfoDto(" +
            "u.username, u.name, u.level) " +
            "FROM User u " +
            "WHERE u.userId = :userId")
    UserInfoDto getUserByUserId(@Param("userId") int userId);
}