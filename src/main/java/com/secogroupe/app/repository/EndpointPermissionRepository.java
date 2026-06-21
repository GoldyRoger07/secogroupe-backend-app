package com.secogroupe.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secogroupe.app.entity.EndpointPermission;

public interface EndpointPermissionRepository extends JpaRepository<EndpointPermission, Long> {

    List<EndpointPermission> findByEnabledTrue();

    @Query("""
            SELECT e FROM EndpointPermission e WHERE
            LOWER(e.pathPattern)     LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(e.permissionName)  LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(e.httpMethod)      LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(e.description)     LIKE LOWER(CONCAT('%', :f, '%'))
            """)
    Page<EndpointPermission> search(@Param("f") String filter, Pageable pageable);
}
