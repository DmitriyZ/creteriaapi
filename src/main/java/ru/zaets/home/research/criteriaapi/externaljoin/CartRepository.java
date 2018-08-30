package ru.zaets.home.research.criteriaapi.externaljoin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.zaets.home.research.criteriaapi.externaljoin.Cart;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaSpecificationExecutor<Cart>, JpaRepository<Cart, UUID> {
}