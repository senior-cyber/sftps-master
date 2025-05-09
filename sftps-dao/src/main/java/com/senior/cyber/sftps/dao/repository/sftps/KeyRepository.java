package com.senior.cyber.sftps.dao.repository.sftps;

import com.senior.cyber.sftps.dao.entity.rbac.User;
import com.senior.cyber.sftps.dao.entity.sftps.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyRepository extends JpaRepository<Key, String> {

    List<Key> findByUser(User user);

}
