package com.campushub.controller;

import com.campushub.dto.CreateEventRequest;
import com.campushub.dto.EventResponse;
import com.campushub.dto.UpdateEventRequest;
import com.campushub.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Browse and manage campus events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "List all events")
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @Operation(summary = "Create an event", description = "Requires ORGANIZER or ADMIN role")
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest req) {
        Long organizerId = getAuthenticatedUserId();
        return eventService.createEvent(req, organizerId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @Operation(summary = "Update an event", description = "Only the event owner or ADMIN can update. Available tickets adjust automatically with capacity changes.")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest req) {
        Long requesterId = getAuthenticatedUserId();
        return eventService.updateEvent(id, req, requesterId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an event", description = "Only the event owner or ADMIN can delete")
    public void deleteEvent(@PathVariable Long id) {
        Long requesterId = getAuthenticatedUserId();
        eventService.deleteEvent(id, requesterId);
    }

    private Long getAuthenticatedUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(principal);
    }
}
