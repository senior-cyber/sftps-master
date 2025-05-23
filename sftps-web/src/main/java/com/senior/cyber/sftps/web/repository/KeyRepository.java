package com.senior.cyber.sftps.web.repository;

import com.senior.cyber.sftps.dao.entity.sftps.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyRepository extends JpaRepository<Key, Long> {

}
