package com.senior.cyber.sftps.dao.repository.rbac;

import com.senior.cyber.sftps.dao.entity.rbac.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    void deleteByUserIdAndRoleId(String userId, String roleId);

}
