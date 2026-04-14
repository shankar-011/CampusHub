package com.campushub.service;

import com.campushub.dto.BookingResponse;
import com.campushub.dto.CreateBookingRequest;
import com.campushub.entity.Booking;
import com.campushub.entity.Event;
import com.campushub.exception.*;
import com.campushub.repository.BookingRepository;
import com.campushub.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public BookingService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest req, Long userId) {
        Event event = eventRepository.findById(req.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + req.eventId()));

        if (!event.getEventTime().isAfter(OffsetDateTime.now())) {
            throw new BookingExpiredException("Cannot book tickets for a past event");
        }

        int affected = eventRepository.atomicDecrement(req.eventId(), req.ticketCount());
        if (affected == 0) {
            throw new ConflictException("Not enough tickets available");
        }
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEventId(req.eventId());
        booking.setTicketCount(req.ticketCount());
        booking.setStatus("CONFIRMED");
        try {
            return toResponse(bookingRepository.saveAndFlush(booking));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException("You have already booked this event");
        }
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        List<BookingResponse> list = bookingRepository.findByUserId(userId).stream().map(this::toResponse).toList();
        return list;
    }


    public BookingResponse getBookingById(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not own this booking");
        }
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getUserId(), b.getEventId(),
                b.getTicketCount(), b.getStatus(), b.getCreatedAt());
    }
}
