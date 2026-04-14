package com.campushub.service;

import com.campushub.dto.BookingResponse;
import com.campushub.dto.EventResponse;
import com.campushub.dto.UserResponse;
import com.campushub.entity.Booking;
import com.campushub.entity.Event;
import com.campushub.entity.User;
import com.campushub.exception.ConflictException;
import com.campushub.exception.ResourceNotFoundException;
import com.campushub.repository.BookingRepository;
import com.campushub.repository.EventRepository;
import com.campushub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public UserResponse promoteToOrganizer(String email) {
        User user = findUserByEmail(email);
        if (user.getRole() == User.Role.ADMIN) {
            throw new ConflictException("Cannot change role of an ADMIN user");
        }
        user.setRole(User.Role.ORGANIZER);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse demoteToStudent(String email) {
        User user = findUserByEmail(email);
        if (user.getRole() == User.Role.ADMIN) {
            throw new ConflictException("Cannot demote an ADMIN user");
        }
        user.setRole(User.Role.STUDENT);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String email) {
        User user = findUserByEmail(email);
        if (user.getRole() == User.Role.ADMIN) {
            throw new ConflictException("Cannot delete an ADMIN user");
        }
        userRepository.delete(user);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream().map(this::toEventResponse).toList();
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        eventRepository.delete(event);
    }


    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::toBookingResponse).toList();
    }

    public List<BookingResponse> getBookingsForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        return bookingRepository.findByEventId(eventId).stream().map(this::toBookingResponse).toList();
    }

    public List<BookingResponse> getBookingsForUser(String email) {
        User user = findUserByEmail(email);
        return bookingRepository.findByUserId(user.getId()).stream().map(this::toBookingResponse).toList();
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }

    private EventResponse toEventResponse(Event e) {
        return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getLocation(),
                e.getEventTime(), e.getCapacity(), e.getAvailableTickets(), e.getOrganizerId(), e.getCreatedAt());
    }

    private BookingResponse toBookingResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getUserId(), b.getEventId(),
                b.getTicketCount(), b.getStatus(), b.getCreatedAt());
    }
}
