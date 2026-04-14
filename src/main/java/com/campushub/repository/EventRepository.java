package com.campushub.repository;

import com.campushub.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Modifying
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets - :count " +
           "WHERE e.id = :id AND e.availableTickets >= :count")
    int atomicDecrement(@Param("id") Long id, @Param("count") int count);
}
