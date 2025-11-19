package com.acrisio.accesscontrol.domain.repository;

import com.acrisio.accesscontrol.domain.model.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
}
