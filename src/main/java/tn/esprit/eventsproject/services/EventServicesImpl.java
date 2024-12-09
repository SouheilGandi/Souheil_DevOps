package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.eventsproject.dto.EventDTO;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.dto.ParticipantDTO;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.exceptions.ParticipantAlreadyExistsException;
import tn.esprit.eventsproject.exceptions.ParticipantNotFoundException;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Slf4j
@RequiredArgsConstructor
@Service
public class EventServicesImpl implements IEventServices{

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final LogisticsRepository logisticsRepository;
    private static final String NOTFOUND="Not Found";

    @Override
    public Participant addParticipant(ParticipantDTO participantDTO) {
        if (participantDTO == null) {
            throw new IllegalArgumentException("ParticipantDTO must not be null");
        }
        // Convert DTO to entity
        Participant participant = new Participant();
        participant.setNom(participantDTO.getNom());
        participant.setPrenom(participantDTO.getPrenom());
        participant.setTache(participantDTO.getTache());
        participant.setIdPart(participantDTO.getIdPart());

        // Check if the participant already exists
        if (participantRepository.findById(participant.getIdPart()).isPresent()) {
            throw new ParticipantAlreadyExistsException();
        }
        return participantRepository.save(participant);
    }

    @Override
    public Event addAffectEvenParticipant(EventDTO eventDTO) {
        // Convert DTO to entity
        Event event = new Event();
        event.setDescription(eventDTO.getDescription());
        event.setDateDebut(eventDTO.getDateDebut());
        event.setDateFin(eventDTO.getDateFin());
        event.setCout(eventDTO.getCout());
        event.setIdEvent(eventDTO.getIdEvent());

        Set<Participant> participants = new HashSet<>();
        for (ParticipantDTO participantDTO : eventDTO.getParticipants()) {
            Participant participant = participantRepository.findById(participantDTO.getIdPart()).orElse(null);
            if (participant == null) {
                throw new ParticipantNotFoundException("Participant with ID " + participantDTO.getIdPart() + NOTFOUND);
            }
            participants.add(participant);
        }
        event.setParticipants(participants);

        Set<Logistics> logistics = new HashSet<>();
        for (LogisticsDTO logisticsDTO : eventDTO.getLogistics()) {
            Logistics log = new Logistics();
            log.setDescription(logisticsDTO.getDescription());
            log.setReserve(logisticsDTO.isReserve());
            log.setPrixUnit(logisticsDTO.getPrixUnit());
            log.setQuantite(logisticsDTO.getQuantite());
            log.setIdLog(logisticsDTO.getIdLog());
            logistics.add(log);
        }
        event.setLogistics(logistics);


        return eventRepository.save(event);
    }

    @Override
    public Event addAffectEvenParticipant(EventDTO eventDTO, int idParticipant) {
        if (eventDTO == null) {
            throw new NullPointerException("EventDTO cannot be null");
        }
        //Convert DTO to entity
        Event event = new Event();
        event.setDescription(eventDTO.getDescription());
        event.setDateDebut(eventDTO.getDateDebut());
        event.setDateFin(eventDTO.getDateFin());
        event.setCout(eventDTO.getCout());
        event.setIdEvent(eventDTO.getIdEvent());

        // Find the participant by ID
        Participant participant = participantRepository.findById(idParticipant).orElse(null);
        if (participant == null) {
            throw new ParticipantNotFoundException("Participant with ID " + idParticipant + NOTFOUND);
        }

        // Add the participant to the event
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        participant.getEvents().add(event);

        // Handling logistics
        Set<Logistics> logistics = new HashSet<>();
        if (eventDTO.getLogistics() != null) {
            for (LogisticsDTO logisticsDTO : eventDTO.getLogistics()) {
                Logistics log = new Logistics();
                log.setDescription(logisticsDTO.getDescription());
                log.setReserve(logisticsDTO.isReserve());
                log.setPrixUnit(logisticsDTO.getPrixUnit());
                log.setQuantite(logisticsDTO.getQuantite());
                log.setIdLog(logisticsDTO.getIdLog());
                logistics.add(log);
            }
        }
        event.setLogistics(logistics);
        participantRepository.save(participant);

        return eventRepository.save(event);
    }


    @Override
    public Logistics addAffectLog(LogisticsDTO logisticsDTO, String descriptionEvent) {
        Event event = eventRepository.findByDescription(descriptionEvent);
        if (event == null) {
            throw new IllegalArgumentException("Event with description " + descriptionEvent + NOTFOUND);
        }
        //Convert DTO to entity
        Logistics logistics = new Logistics();
        logistics.setDescription(logisticsDTO.getDescription());
        logistics.setReserve(logisticsDTO.isReserve());
        logistics.setPrixUnit(logisticsDTO.getPrixUnit());
        logistics.setQuantite(logisticsDTO.getQuantite());

        //Add logistics to event
        Set<Logistics> logisticsSet = event.getLogistics();
        if (logisticsSet == null) {
            logisticsSet = new HashSet<>();
            event.setLogistics(logisticsSet);
        }
        logisticsSet.add(logistics);

        return logisticsRepository.save(logistics);
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate dateDebut, LocalDate dateFin) {
        List<Event> events = eventRepository.findByDateDebutBetween(dateDebut, dateFin);
        List<Logistics> logisticsList = new ArrayList<>();

        for (Event event : events) {
            if (event.getLogistics().isEmpty()) {
                continue;
            }

            Set<Logistics> logisticsSet = event.getLogistics();
            for (Logistics logistics : logisticsSet) {
                if (logistics.isReserve()) {
                    logisticsList.add(logistics);
                }
            }
        }
        return logisticsList;
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Override
    public void calculCout() {
        List<Event> events = eventRepository.findByParticipantsNomAndParticipantsPrenomAndParticipantsTache("Tounsi", "Ahmed", Tache.ORGANISATEUR);
        for (Event event : events) {
            log.info(event.getDescription());

            // Reset sum for each event
            float sum = 0f;

            Set<Logistics> logisticsSet = event.getLogistics();
            for (Logistics logistics : logisticsSet) {
                if (logistics.isReserve()) {
                    sum += logistics.getPrixUnit() * logistics.getQuantite();
                }
            }
            event.setCout(sum);
            eventRepository.save(event);
            log.info("Cout de l'Event " + event.getDescription() + " est " + sum);
        }
    }

}
