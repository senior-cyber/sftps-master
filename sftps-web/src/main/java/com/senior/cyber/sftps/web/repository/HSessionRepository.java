package com.senior.cyber.sftps.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HSessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionId(String sessionId);

}
