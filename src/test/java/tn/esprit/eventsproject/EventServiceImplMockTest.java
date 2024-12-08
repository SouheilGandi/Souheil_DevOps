package tn.esprit.eventsproject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.exceptions.ParticipantAlreadyExistsException;
import tn.esprit.eventsproject.exceptions.ParticipantNotFoundException;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplMockTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;


    @InjectMocks//Automatically injects mocks
    EventServicesImpl eventServices;

    //Testing the add in the case of success and when the participant inputs are null or if there is already an existing participant
    @Test
    void testAddParticipant(){
        //Arrange
        Participant participant = new Participant();
        participant.setIdPart(1);

        when(participantRepository.save(participant)).thenReturn(participant);
        //Act
        Participant result = eventServices.addParticipant(participant);
        // Assert
        assertEquals(participant,result);
        verify(participantRepository,times(1)).save(participant);
    }

    @Test
    void testAddParticipant_NullParticipant() {
        // Arrange
        Participant participant = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> eventServices.addParticipant(participant));
    }

    @Test
    void testAddParticipant_ParticipantExists() {
        // Arrange
        Participant existingParticipant = new Participant();
        existingParticipant.setIdPart(1);

        when(participantRepository.findById(1)).thenReturn(Optional.of(existingParticipant));

        // Act & Assert
        assertThrows(ParticipantAlreadyExistsException.class, () -> eventServices.addParticipant(existingParticipant));
    }



    //Testing if there is no events to affect or there are already existing events or there no participant to begin with
    @Test
    void testAddAffectEvenParticipant_NoEvents(){
        //Arrange
        int idParticipant=1;

        Participant participant = new Participant();
        participant.setIdPart(idParticipant);

        Event event= new Event();
        event.setIdEvent(101);

        // Mock the behavior of participantRepository and eventRepository
        when(participantRepository.findById(idParticipant)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        Event result = eventServices.addAffectEvenParticipant(event,idParticipant);

        //Assert
        assertEquals(event,result);
        assertNotNull(participant.getEvents());
        assertTrue(participant.getEvents().contains(event));

        //Verify interactions
        verify(participantRepository,times(1)).findById(idParticipant);
        verify(eventRepository,times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipant_WithExistingEvents() {
        // Arrange
        int idParticipant = 2;

        Event existingEvent = new Event();
        existingEvent.setIdEvent(100); // Existing event

        Participant participant = new Participant();
        participant.setIdPart(idParticipant);
        participant.setEvents(new HashSet<>(Collections.singletonList(existingEvent))); // Participant with one event

        Event newEvent = new Event();
        newEvent.setIdEvent(101); // New event to add

        // Mock the behavior
        when(participantRepository.findById(idParticipant)).thenReturn(Optional.of(participant));
        when(eventRepository.save(newEvent)).thenReturn(newEvent);

        // Act
        Event result = eventServices.addAffectEvenParticipant(newEvent, idParticipant);

        // Assert
        assertEquals(newEvent, result); // Ensure the returned event matches the added event
        assertEquals(2, participant.getEvents().size()); // Verify participant now has two events
        assertTrue(participant.getEvents().contains(newEvent)); // Verify the new event is added

        // Verify interactions
        verify(participantRepository, times(1)).findById(idParticipant);
        verify(eventRepository, times(1)).save(newEvent);
    }

    @Test
    void testAddAffectEvenParticipant_ParticipantNotFound() {
        // Prepare the necessary mock data
        Event event = new Event();
        Participant participant=new Participant();
        participant.setIdPart(50);

        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        // Mock the behavior of participantRepository.findById to return an empty Optional
        when(participantRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Call the method and expect an exception
        assertThrows(ParticipantNotFoundException.class,() -> eventServices.addAffectEvenParticipant(event));

        // Verify that the eventRepository.save method was never called since the participant was not found
        verify(eventRepository, never()).save(any(Event.class));

    }
    //all participants exist in the database and the event are successfully added to their events.
    @Test
    void testAddAffectEvenParticipant_AllParticipantsExist() {
        // Arrange
        Event event = new Event();
        event.setIdEvent(101); // New event

        Participant participant1 = new Participant();
        participant1.setIdPart(1); // Existing participant
        participant1.setEvents(new HashSet<>()); // Empty set of events

        Participant participant2 = new Participant();
        participant2.setIdPart(2); // Existing participant
        participant2.setEvents(new HashSet<>()); // Empty set of events

        Set<Participant> participants = new HashSet<>();
        participants.add(participant1);
        participants.add(participant2);
        event.setParticipants(participants);

        // Mock the behavior
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant1));
        when(participantRepository.findById(2)).thenReturn(Optional.of(participant2));
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        Event result = eventServices.addAffectEvenParticipant(event);

        // Assert
        assertEquals(event, result); // Ensure the event is returned correctly
        assertTrue(participant1.getEvents().contains(event)); // Participant 1's events should contain the new event
        assertTrue(participant2.getEvents().contains(event)); // Participant 2's events should contain the new event

        // Verify interactions
        verify(participantRepository, times(1)).findById(1);
        verify(participantRepository, times(1)).findById(2);
        verify(eventRepository, times(1)).save(event);
    }
    //When a null event is passed
    @Test
    void testAddAffectEvenParticipant_NullEvent() {
        // Arrange
        int idParticipant = 1;

        // Mock the behavior
        lenient().when(participantRepository.findById(idParticipant)).thenReturn(Optional.of(new Participant()));

        // Call the method and expect an exception
        assertThrows(NullPointerException.class, () -> eventServices.addAffectEvenParticipant(null, idParticipant));
    }

    //Testing for affect logistics for a certain event picked by its description
    @Test
    void testAddAffectLog_NewEventWithNoLogistics(){
        String descriptionEvent="Annual conference";
        Logistics logistics = new Logistics();
        Event event= new Event();
        event.setDescription(descriptionEvent);
        event.setLogistics(null);

        when(eventRepository.findByDescription(descriptionEvent)).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result= eventServices.addAffectLog(logistics,descriptionEvent);

        assertEquals(logistics, result);
        verify(eventRepository).findByDescription(descriptionEvent);
        verify(eventRepository).save(event);
        verify(logisticsRepository).save(logistics);

        assertNotNull(event.getLogistics());
        assertTrue(event.getLogistics().contains(logistics));
    }

    @Test
    void testAddAffectLog_EventWithExistingLogistics() {
        // Arrange
        String descriptionEvent = "Annual Conference";
        Logistics logistics = new Logistics(); // Populate logistics as needed.
        Event event = new Event();
        event.setDescription(descriptionEvent);
        Set<Logistics> existingLogistics = new HashSet<>();
        Logistics existingLogistic = new Logistics(); // Existing logistics.
        existingLogistics.add(existingLogistic);
        event.setLogistics(existingLogistics);

        when(eventRepository.findByDescription(descriptionEvent)).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        // Act
        Logistics result = eventServices.addAffectLog(logistics, descriptionEvent);

        // Assert
        assertEquals(logistics, result);
        verify(eventRepository).findByDescription(descriptionEvent);
        verify(logisticsRepository).save(logistics);

        assertEquals(2, event.getLogistics().size());
        assertTrue(event.getLogistics().contains(logistics));
    }
    @Test
    void testAddAffectLog_EventNotFound() {
        // Arrange
        String descriptionEvent = "Non-Existent Event";
        Logistics logistics = new Logistics();

        when(eventRepository.findByDescription(descriptionEvent)).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> eventServices.addAffectLog(logistics, descriptionEvent));

        verify(eventRepository).findByDescription(descriptionEvent);
        verify(logisticsRepository, never()).save(any());
    }

    //Testing for the getting logistics from dateDebut to dateFin

    @Test
    public void testGetLogisticsDates_WithValidDatesAndReservedLogistics() {
        // Arrange
        LocalDate dateDebut = LocalDate.of(2023, 1, 1);
        LocalDate dateFin = LocalDate.of(2023, 1, 31);

        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);

        Logistics logistics2 = new Logistics();
        logistics2.setReserve(false);

        Event event1 = new Event();
        event1.setLogistics(new HashSet<>(Arrays.asList(logistics1, logistics2)));

        Event event2 = new Event();
        event2.setLogistics(new HashSet<>(Collections.singletonList(logistics1)));

        List<Event> mockEvents = Arrays.asList(event1, event2);

        Mockito.when(eventRepository.findByDateDebutBetween(dateDebut, dateFin))
                .thenReturn(mockEvents);

        // Act
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(logistics1));
        assertFalse(result.contains(logistics2));
    }

    @Test
    public void testGetLogisticsDates_WithValidDatesAndNoLogistics() {
        // Arrange
        LocalDate dateDebut = LocalDate.of(2023, 1, 1);
        LocalDate dateFin = LocalDate.of(2023, 1, 31);

        Event event1 = new Event();
        event1.setLogistics(new HashSet<>()); // No logistics for this event

        List<Event> mockEvents = Collections.singletonList(event1);

        Mockito.when(eventRepository.findByDateDebutBetween(dateDebut, dateFin))
                .thenReturn(mockEvents);

        // Act
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Assert
        assertNull(result); // Since the logistics list is empty
    }
    @Test
    public void testGetLogisticsDates_WithNoEvents() {
        // Arrange
        LocalDate dateDebut = LocalDate.of(2023, 1, 1);
        LocalDate dateFin = LocalDate.of(2023, 1, 31);

        Mockito.when(eventRepository.findByDateDebutBetween(dateDebut, dateFin))
                .thenReturn(Collections.emptyList());

        // Act
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetLogisticsDates_WithNullDates() {
        // Arrange
        LocalDate dateDebut = null;
        LocalDate dateFin = null;

        Mockito.when(eventRepository.findByDateDebutBetween(dateDebut, dateFin))
                .thenReturn(Collections.emptyList());

        // Act
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    //Testing the calculcout service
    @Test
    public void testCalculCout_WithReservedLogistics() {
        // Arrange
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        logistics1.setPrixUnit(100f);
        logistics1.setQuantite(2);

        Logistics logistics2 = new Logistics();
        logistics2.setReserve(false); // Not reserved, should not be included

        Event event1 = new Event();
        event1.setDescription("Event 1");
        event1.setLogistics(new HashSet<>(Arrays.asList(logistics1, logistics2)));
        event1.setCout(0f);

        List<Event> mockEvents = Collections.singletonList(event1);

        Mockito.when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(mockEvents);

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.times(1)).save(Mockito.any(Event.class));
        assertEquals(200f, event1.getCout()); // 100 * 2 for reserved logistics
    }

    @Test
    public void testCalculCout_WithNoReservedLogistics() {
        // Arrange
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(false); // Not reserved

        Event event1 = new Event();
        event1.setDescription("Event 2");
        event1.setLogistics(new HashSet<>(Collections.singletonList(logistics1)));
        event1.setCout(0f);

        List<Event> mockEvents = Collections.singletonList(event1);

        Mockito.when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(mockEvents);

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.times(1)).save(Mockito.any(Event.class));
        assertEquals(0f, event1.getCout()); // No reserved logistics, cost should remain 0
    }

    @Test
    public void testCalculCout_WithMultipleEvents() {
        // Arrange
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        logistics1.setPrixUnit(50f);
        logistics1.setQuantite(1);

        Logistics logistics2 = new Logistics();
        logistics2.setReserve(true);
        logistics2.setPrixUnit(200f);
        logistics2.setQuantite(3);

        Event event1 = new Event();
        event1.setDescription("Event 3");
        event1.setLogistics(new HashSet<>(Collections.singletonList(logistics1)));
        event1.setCout(0f);

        Event event2 = new Event();
        event2.setDescription("Event 4");
        event2.setLogistics(new HashSet<>(Collections.singletonList(logistics2)));
        event2.setCout(0f);

        List<Event> mockEvents = Arrays.asList(event1, event2);

        Mockito.when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(mockEvents);

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.times(2)).save(Mockito.any(Event.class));
        assertEquals(50.0, event1.getCout()); // 50 * 1
        assertEquals(600.0, event2.getCout()); // 200 * 3
    }

    @Test
    public void testCalculCout_WithNoEvents() {
        // Arrange
        Mockito.when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.emptyList());

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any(Event.class));
    }
}
