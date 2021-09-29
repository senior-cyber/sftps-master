package com.senior.cyber.sftps.api.repository;

import com.senior.cyber.sftps.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    Optional<User> findByEmailAddress(String emailAddress);

}
