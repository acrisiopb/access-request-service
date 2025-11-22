package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface AccessRequestRepository  extends JpaRepository<AccessRequest, Long> , JpaSpecificationExecutor<AccessRequest> {
    List<AccessRequest> findByUser(User user);
    boolean existsByUserAndStatusAndModulesContaining(User user, RequestStatus status, Module module);
}
