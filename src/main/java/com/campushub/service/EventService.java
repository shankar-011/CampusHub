package com.campushub.service;

import com.campushub.dto.CreateEventRequest;
import com.campushub.dto.EventResponse;
import com.campushub.dto.UpdateEventRequest;
import com.campushub.entity.Event;
import com.campushub.exception.ConflictException;
import com.campushub.exception.ForbiddenException;
import com.campushub.exception.ResourceNotFoundException;
import com.campushub.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> getAllEvents() {
        List<EventResponse> list = eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return list;
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return toResponse(event);
    }

    public EventResponse createEvent(CreateEventRequest req, Long organizerId) {
        Event event = new Event();
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setLocation(req.location());
        event.setEventTime(req.eventTime());
        event.setCapacity(req.capacity());
        event.setAvailableTickets(req.capacity());
        event.setOrganizerId(organizerId);
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public EventResponse updateEvent(Long id, UpdateEventRequest req, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        if (!event.getOrganizerId().equals(requesterId)) {
            throw new ForbiddenException("You do not own this event");
        }
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setLocation(req.location());
        event.setEventTime(req.eventTime());

        int capacityDelta = req.capacity() - event.getCapacity();
        int newAvailable = Math.max(0, event.getAvailableTickets() + capacityDelta);
        int booked = event.getCapacity() - event.getAvailableTickets();
        if (req.capacity() < booked) {
            throw new ConflictException(
                    "Cannot reduce capacity below already booked count (" + booked + ")");
        }
        event.setCapacity(req.capacity());
        event.setAvailableTickets(newAvailable);

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public void deleteEvent(Long id, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        if (!event.getOrganizerId().equals(requesterId)) {
            throw new ForbiddenException("You do not own this event");
        }
        eventRepository.delete(event);
    }

    private EventResponse toResponse(Event e) {
          return new EventResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getLocation(),
                e.getEventTime(),
                e.getCapacity(),
                e.getAvailableTickets(),
                e.getOrganizerId(),
                e.getCreatedAt()
        );
    }
}
