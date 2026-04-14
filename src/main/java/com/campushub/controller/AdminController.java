package com.campushub.controller;

import com.campushub.dto.BookingResponse;
import com.campushub.dto.EventResponse;
import com.campushub.dto.UserResponse;
import com.campushub.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "User, event and booking management — ADMIN only")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/search")
    @Operation(summary = "Find user by email")
    public UserResponse getUserByEmail(@RequestParam String email) {
        return adminService.getUserByEmail(email);
    }

    @PatchMapping("/users/promote-organizer")
    @Operation(summary = "Promote user to ORGANIZER")
    public UserResponse promoteToOrganizer(@RequestParam String email) {
        return adminService.promoteToOrganizer(email);
    }

    @PatchMapping("/users/demote-student")
    @Operation(summary = "Demote user back to STUDENT")
    public UserResponse demoteToStudent(@RequestParam String email) {
        return adminService.demoteToStudent(email);
    }

    @DeleteMapping("/users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user", description = "Cannot delete ADMIN accounts")
    public void deleteUser(@RequestParam String email) {
        adminService.deleteUser(email);
    }


    @GetMapping("/events")
    @Operation(summary = "List all events")
    public List<EventResponse> getAllEvents() {
        return adminService.getAllEvents();
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete any event")
    public void deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
    }

    @GetMapping("/bookings")
    @Operation(summary = "List all bookings")
    public List<BookingResponse> getAllBookings() {
        return adminService.getAllBookings();
    }

    @GetMapping("/bookings/event/{eventId}")
    @Operation(summary = "Get all bookings for a specific event")
    public List<BookingResponse> getBookingsForEvent(@PathVariable Long eventId) {
        return adminService.getBookingsForEvent(eventId);
    }

    @GetMapping("/bookings/user")
    @Operation(summary = "Get all bookings for a specific user")
    public List<BookingResponse> getBookingsForUser(@RequestParam String email) {
        return adminService.getBookingsForUser(email);
    }
}
