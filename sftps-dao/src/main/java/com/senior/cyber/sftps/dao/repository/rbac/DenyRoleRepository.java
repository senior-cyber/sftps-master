package com.senior.cyber.sftps.dao.repository.rbac;

import com.senior.cyber.sftps.dao.entity.rbac.DenyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenyRoleRepository extends JpaRepository<DenyRole, String> {

    void deleteByRoleIdAndUserId(String roleId, String userId);

}
