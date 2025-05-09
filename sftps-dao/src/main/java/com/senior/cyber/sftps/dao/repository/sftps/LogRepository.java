package com.senior.cyber.sftps.dao.repository.sftps;

import com.senior.cyber.sftps.dao.entity.sftps.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, String> {

}
