package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.dto.EventDTO;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.dto.ParticipantDTO;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.exceptions.ParticipantAlreadyExistsException;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class EventServiceImplMockTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @BeforeEach
    void setUp(){
        //Reset mocks to avoid test interferences
        Mockito.reset(participantRepository);
        Mockito.reset(eventRepository);
        Mockito.reset(logisticsRepository);

    }


    @InjectMocks//Automatically injects mocks
    EventServicesImpl eventServices;

    //Testing the add in the case of success and when the participant inputs are null or if there is already an existing participant
    @Test
    void testAddParticipant_Success(){
        //Arrange
        ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setIdPart(1);

        Participant participant = new Participant();
        participant.setIdPart(1);

        when(participantRepository.save(any(Participant.class))).thenReturn(participant);
        //Act
        Participant result = eventServices.addParticipant(participantDTO);
        // Assert
        assertEquals(participant,result);
        verify(participantRepository,times(1)).save(any(Participant.class));
    }

    @Test
    void testAddParticipant_NullParticipant() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> eventServices.addParticipant(null),"ParticipantDTO must not be null");
    }
//not correct
    @Test
    void testAddParticipant_ParticipantExists() {
        // Arrange
        ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setIdPart(1);

        Participant existingParticipant = new Participant();
        existingParticipant.setIdPart(1);

       when(participantRepository.findById(1)).thenReturn(Optional.of(existingParticipant));

        // Act & Assert
        assertThrows(ParticipantAlreadyExistsException.class, () -> eventServices.addParticipant(participantDTO));

        verify(participantRepository, times(1)).findById(1);

    }



//FAILED
@Test
void testAddAffectEvenParticipant_WithExistingEvents() {
    // Arrange
    int idParticipant = 2;
    EventDTO eventDTO = new EventDTO();
    eventDTO.setIdEvent(101); // Event ID to be added

    Event existingEvent = new Event();
    existingEvent.setIdEvent(100); // Existing event in participant's events

    ParticipantDTO participantDTO = new ParticipantDTO();
    participantDTO.setIdPart(idParticipant);

    Participant participant = new Participant();
    participant.setIdPart(idParticipant);
    participant.setEvents(new HashSet<>(Collections.singletonList(existingEvent))); // Participant with one event

    // Mock the behavior
    when(participantRepository.findById(idParticipant)).thenReturn(Optional.of(participant));

    Event newEvent = new Event();
    newEvent.setIdEvent(eventDTO.getIdEvent()); // ID 101 as expected in DTO

    // Mock event save to return the new event
    when(eventRepository.save(any(Event.class))).thenReturn(newEvent);

    // Act
    Event result = eventServices.addAffectEvenParticipant(eventDTO, idParticipant);

    // Assert
    assertEquals(eventDTO.getIdEvent(), result.getIdEvent()); // Ensure the returned event matches the added event
    assertEquals(2, participant.getEvents().size()); // Verify participant now has two events

    // Verify interactions
    verify(participantRepository, times(1)).findById(idParticipant);
    verify(eventRepository, times(1)).save(any(Event.class));
}


    @Test
    void testAddAffectEvenParticipant_ParticipantNotFound() {
        // Arrange
        int idParticipant = 50;
        EventDTO eventDTO = new EventDTO();
        eventDTO.setDescription("Event for participant");

        lenient().when(participantRepository.findById(idParticipant)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullPointerException.class, () -> eventServices.addAffectEvenParticipant(eventDTO));
        verify(eventRepository, never()).save(any(Event.class));

    }



    //When a null event is passed
    @Test
    void testAddAffectEvenParticipant_NullEvent() {
        // Arrange
        int idParticipant = 1;

        // Mock the repository behavior
        lenient().when(participantRepository.findById(idParticipant)).thenReturn(Optional.of(new Participant()));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> eventServices.addAffectEvenParticipant(null,idParticipant));

        // Verify no interactions with the repositories since the input is null
        verify(participantRepository, never()).findById(anyInt());
        verify(eventRepository, never()).save(any(Event.class));
    }


    @Test
    void testAddAffectLog_WhenEventExists_ShouldAddLogistics() {
        // Arrange
        String descriptionEvent = "Annual conference";
        LogisticsDTO logisticsDTO = new LogisticsDTO();
        logisticsDTO.setDescription("New Logistics");
        logisticsDTO.setReserve(true);
        logisticsDTO.setPrixUnit(100f);
        logisticsDTO.setQuantite(2);

        Event event = new Event();
        event.setDescription(descriptionEvent);
        event.setLogistics(new HashSet<>()); // Initialize logistics set

        Logistics logistics = new Logistics();
        logistics.setDescription(logisticsDTO.getDescription());
        logistics.setReserve(logisticsDTO.isReserve());
        logistics.setPrixUnit(logisticsDTO.getPrixUnit());
        logistics.setQuantite(logisticsDTO.getQuantite());

        // Mock repository behavior
        when(eventRepository.findByDescription(descriptionEvent)).thenReturn(event);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);

        // Act
        Logistics result = eventServices.addAffectLog(logisticsDTO, descriptionEvent);

        // Assert
        assertNotNull(result); // Ensure the result is not null
        assertEquals(logistics.getDescription(), result.getDescription());  // Assert that the logistics were correctly added
        assertEquals(logistics.isReserve(), result.isReserve());
        assertEquals(logistics.getPrixUnit(), result.getPrixUnit(), 0.01); // Allow small tolerance for floating point values
        assertEquals(logistics.getQuantite(), result.getQuantite());

        // ArgumentCaptor to capture the argument passed to logisticsRepository.save()
        ArgumentCaptor<Logistics> logisticsCaptor = ArgumentCaptor.forClass(Logistics.class);
        verify(logisticsRepository).save(logisticsCaptor.capture()); // Capture the argument passed to save()

        Logistics capturedLogistics = logisticsCaptor.getValue();
        assertNotNull(capturedLogistics);
        assertEquals(logistics.getDescription(), capturedLogistics.getDescription());
        assertEquals(logistics.isReserve(), capturedLogistics.isReserve());
        assertEquals(logistics.getPrixUnit(), capturedLogistics.getPrixUnit(), 0.01);
        assertEquals(logistics.getQuantite(), capturedLogistics.getQuantite());

        // Verify that the methods were called as expected
        verify(eventRepository).findByDescription(descriptionEvent); // Check that findByDescription was called
    }

    @Test
    void testAddAffectLog_EventNotFound() {
        // Arrange
        String descriptionEvent = "Non-Existent Event";
        LogisticsDTO logisticsDTO = new LogisticsDTO(); // Use LogisticsDTO instead of Logistics

        when(eventRepository.findByDescription(descriptionEvent)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> eventServices.addAffectLog(logisticsDTO, descriptionEvent));

        // Verify interactions
        verify(eventRepository).findByDescription(descriptionEvent);
        verify(logisticsRepository, never()).save(any(Logistics.class));
    }

    //Testing for the getting logistics from dateDebut to dateFin


    @Test
     void testGetLogisticsDates_WithValidDatesAndNoLogistics() {
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
        assertNotNull(result); // The result should not be null, even if no logistics are present
        assertTrue(result.isEmpty()); // The result list should be empty since no logistics exist
    }

    @Test
     void testGetLogisticsDates_WithNoEvents() {
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
     void testGetLogisticsDates_WithNullDates() {
        // Arrange


        Mockito.when(eventRepository.findByDateDebutBetween(null, null))
                .thenReturn(Collections.emptyList());

        // Act
        List<Logistics> result = eventServices.getLogisticsDates(null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    //Testing the calculcout service
    @Test
     void testCalculCout_WithReservedLogistics() {
        // Arrange
        // Create logistics entities (not DTOs)
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        logistics1.setPrixUnit(100f);
        logistics1.setQuantite(2);

        Logistics logistics2 = new Logistics();
        logistics2.setReserve(false); // Not reserved, should not be included

        // Create event entity (not DTO)
        Event event1 = new Event();
        event1.setDescription("Event 1");
        event1.setLogistics(new HashSet<>(Arrays.asList(logistics1, logistics2)));
        event1.setCout(0f);

        List<Event> mockEvents = Collections.singletonList(event1);

        // Mock the repository to return the list of Event entities (not EventDTO)
        Mockito.when(eventRepository.findByParticipantsNomAndParticipantsPrenomAndParticipantsTache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(mockEvents);

        // Mock save method for Event entity
        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        // Convert Event entities to EventDTOs before passing them to the service if needed
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.times(1)).save(Mockito.any(Event.class));
        assertEquals(200f, event1.getCout()); // 100 * 2 for reserved logistics (from Event entity)
    }

    @Test
     void testCalculCout_WithNoReservedLogistics() {
        // Arrange
        // Create logistics entity (not DTO)
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(false); // Not reserved

        // Create event entity (not DTO)
        Event event1 = new Event();
        event1.setDescription("Event 2");
        event1.setLogistics(new HashSet<>(Collections.singletonList(logistics1)));
        event1.setCout(0f);

        List<Event> mockEvents = Collections.singletonList(event1);

        // Mock the repository to return the list of Event entities (not EventDTO)
        Mockito.when(eventRepository.findByParticipantsNomAndParticipantsPrenomAndParticipantsTache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(mockEvents);

        // Mock save method for Event entity
        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.times(1)).save(Mockito.any(Event.class));
        assertEquals(0f, event1.getCout()); // No reserved logistics, cost should remain 0
    }


    @Test
     void testCalculCout_WithMultipleEvents() {
        // Arrange
        // Create logistics entities (not DTOs)
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        logistics1.setPrixUnit(50f);
        logistics1.setQuantite(1);

        Logistics logistics2 = new Logistics();
        logistics2.setReserve(true);
        logistics2.setPrixUnit(200f);
        logistics2.setQuantite(3);

        // Create event entities (not DTOs)
        Event event1 = new Event();
        event1.setDescription("Event 3");
        event1.setLogistics(new HashSet<>(Collections.singletonList(logistics1)));
        event1.setCout(0f);

        Event event2 = new Event();
        event2.setDescription("Event 4");
        event2.setLogistics(new HashSet<>(Collections.singletonList(logistics2)));
        event2.setCout(0f);

        List<Event> mockEvents = Arrays.asList(event1, event2);

        // Mock the repository to return the list of Event entities (not EventDTO)
        Mockito.when(eventRepository.findByParticipantsNomAndParticipantsPrenomAndParticipantsTache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(mockEvents);

        // Mock save method for Event entity
        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.times(2)).save(Mockito.any(Event.class));
        assertEquals(50.0, event1.getCout()); // 50 * 1 for the first event
        assertEquals(600.0, event2.getCout()); // 200 * 3 for the second event
    }


    @Test
     void testCalculCout_WithNoEvents() {
        // Arrange
        Mockito.when(eventRepository.findByParticipantsNomAndParticipantsPrenomAndParticipantsTache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.emptyList());

        // Act
        eventServices.calculCout();

        // Assert
        Mockito.verify(eventRepository, Mockito.never()).save(Mockito.any(Event.class));
    }

}
