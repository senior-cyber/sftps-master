package com.senior.cyber.sftps.dao.repository.rbac;

import com.senior.cyber.sftps.dao.entity.rbac.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, String> {

    void deleteByGroupIdAndUserId(String groupId, String userId);

}
