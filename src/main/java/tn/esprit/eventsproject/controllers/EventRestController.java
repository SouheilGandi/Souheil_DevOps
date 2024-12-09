package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.dto.EventDTO;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.dto.ParticipantDTO;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("event")
@RestController
public class EventRestController {
    private final IEventServices eventServices;

    @PostMapping("/addPart")
    public Participant addParticipant(@RequestBody ParticipantDTO participant){
        return eventServices.addParticipant(participant);
    }
    @PostMapping("/addEvent/{id}")
    public Event addEventPart(@RequestBody EventDTO event, @PathVariable("id") int idPart){
        return eventServices.addAffectEvenParticipant(event,idPart);
    }
    @PostMapping("/addEvent")
    public Event addEvent(@RequestBody EventDTO event){
        return eventServices.addAffectEvenParticipant(event);
    }
    @PutMapping("/addAffectLog/{description}")
    public Logistics addAffectLog(@RequestBody LogisticsDTO logistics, @PathVariable("description") String descriptionEvent){
        return eventServices.addAffectLog(logistics,descriptionEvent);
    }
    @GetMapping("/getLogs/{d1}/{d2}")
    public List<Logistics> getLogistiquesDates (@PathVariable("d1") LocalDate dateDebut, @PathVariable("d2") LocalDate dateFin){
        return eventServices.getLogisticsDates(dateDebut,dateFin);
    }
}
