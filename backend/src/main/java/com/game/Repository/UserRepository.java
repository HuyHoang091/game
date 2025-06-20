package com.game.Repository;

import com.game.Model.User;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);

    @Transactional
    @Query(value = "SELECT * FROM users " +
                   "WHERE username = :username", nativeQuery = true)
    Optional<User> findByUsernameOptional(@Param("username") String username);
}