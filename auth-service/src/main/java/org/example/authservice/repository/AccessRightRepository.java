package org.example.authservice.repository;

import org.example.authservice.entity.AccessRight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRightRepository extends JpaRepository<AccessRight, Long> {
}