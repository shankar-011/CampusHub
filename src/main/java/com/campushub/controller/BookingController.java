package com.campushub.controller;

import com.campushub.dto.BookingResponse;
import com.campushub.dto.CreateBookingRequest;
import com.campushub.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Book tickets and view your reservations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book tickets for an event", description = "Atomically decrements available tickets. Returns 409 if sold out or already booked.")
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest req) {
        Long userId = getAuthenticatedUserId();
        return bookingService.createBooking(req, userId);
    }

    @GetMapping
    @Operation(summary = "Get my bookings")
    public List<BookingResponse> getUserBookings() {
        Long userId = getAuthenticatedUserId();
        return bookingService.getUserBookings(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by ID", description = "Returns 403 if the booking belongs to another user")
    public BookingResponse getBookingById(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        return bookingService.getBookingById(id, userId);
    }

    private Long getAuthenticatedUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(principal);
    }
}
