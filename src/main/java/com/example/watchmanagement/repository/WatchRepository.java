package com.example.watchmanagement.repository;

import com.example.watchmanagement.model.Watch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchRepository extends JpaRepository<Watch, Long> {
}
