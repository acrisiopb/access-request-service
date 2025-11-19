package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AccessRequestRepository  extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByUser(User user);
}
