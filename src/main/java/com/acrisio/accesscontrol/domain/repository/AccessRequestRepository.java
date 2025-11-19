package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRequestRepository  extends JpaRepository<AccessRequest, Long> {
}
