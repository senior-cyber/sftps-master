package com.senior.cyber.sftps.api.repository;

import com.senior.cyber.sftps.dao.entity.Key;
import com.senior.cyber.sftps.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyRepository extends JpaRepository<Key, Long> {

    List<Key> findByUser(User user);

}
