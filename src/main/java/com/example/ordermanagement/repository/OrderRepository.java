package com.example.ordermanagement.repository;

import com.example.ordermanagement.domain.Order;
import com.example.ordermanagement.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserid(Long userId);

    List<Order> findByStatus(OrderStatus status);

    @Query("select o from Order o where o.user.id = :userId and o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Query("select count(o) from Order o where o.createdAt >= :from")
    long countOrdersSince(@Param("from")LocalDateTime from);
}
