package ru.zaets.home.research.criteriaapi.two;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaSpecificationExecutor<Device>, JpaRepository<Device, UUID> {
}