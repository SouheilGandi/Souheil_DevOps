package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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

    @Override
    public Participant addParticipant(Participant participant) {
        //Check if the input is not null
        if (participant == null){
            throw new IllegalArgumentException("Participant cannot be null");
        }
        //Check if the participant doesn't exist
        if(participantRepository.findById(participant.getIdPart()).isPresent()){
            throw new ParticipantAlreadyExistsException();
        }
        return participantRepository.save(participant);
    }

    @Override
    public Event addAffectEvenParticipant(Event event, int idParticipant) {
        if (event == null) {
            throw new NullPointerException("Event cannot be null");
        }

        Participant participant = participantRepository.findById(idParticipant).orElse(null);
            if (participant == null) {
            throw new ParticipantNotFoundException("Participant with ID " + idParticipant + " not found");}

            if(participant.getEvents() == null){
            Set<Event> events = new HashSet<>();
            events.add(event);
            participant.setEvents(events);
        }else {
            participant.getEvents().add(event);
        }
        return eventRepository.save(event);
    }

    @Override
    public Event addAffectEvenParticipant(Event event) {
        if (event == null) {
            throw new NullPointerException("Event cannot be null");
        }
        Set<Participant> participants = event.getParticipants();
        for(Participant aParticipant:participants){
            Participant participant = participantRepository.findById(aParticipant.getIdPart()).orElse(null);

            //Check when the participant is not found
            if(participant == null){
                throw new ParticipantNotFoundException("Participant with ID " + aParticipant.getIdPart() + " not found");
            }

            if(participant.getEvents() == null){
                Set<Event> events = new HashSet<>();
                events.add(event);
                participant.setEvents(events);
            }else {
                participant.getEvents().add(event);
            }
        }
        return eventRepository.save(event);
    }

    @Override
    public Logistics addAffectLog(Logistics logistics, String descriptionEvent) {
      Event event = eventRepository.findByDescription(descriptionEvent);
      if(event.getLogistics() == null){
          Set<Logistics> logisticsSet = new HashSet<>();
          logisticsSet.add(logistics);
          event.setLogistics(logisticsSet);
          eventRepository.save(event);
      }
      else{
          event.getLogistics().add(logistics);
      }
        return logisticsRepository.save(logistics);
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate date_debut, LocalDate date_fin) {
        List<Event> events = eventRepository.findByDateDebutBetween(date_debut, date_fin);

        List<Logistics> logisticsList = new ArrayList<>();
        for (Event event:events){
            if(event.getLogistics().isEmpty()){

                return null;
            }

            else {
                Set<Logistics> logisticsSet = event.getLogistics();
                for (Logistics logistics:logisticsSet){
                    if(logistics.isReserve())
                        logisticsList.add(logistics);
                }
            }
        }
        return logisticsList;
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Override
    public void calculCout() {
        List<Event> events = eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi","Ahmed", Tache.ORGANISATEUR);
    // eventRepository.findAll();
        for(Event event:events){
            log.info(event.getDescription());

            // Reset somme for each event
            float somme = 0f;

            Set<Logistics> logisticsSet = event.getLogistics();
            for (Logistics logistics:logisticsSet){
                if(logistics.isReserve())
                    somme+=logistics.getPrixUnit()*logistics.getQuantite();
            }
            event.setCout(somme);
            eventRepository.save(event);
            log.info("Cout de l'Event "+event.getDescription()+" est "+ somme);

        }

    }

}
