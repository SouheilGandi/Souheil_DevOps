package tn.esprit.eventsproject.services;

import tn.esprit.eventsproject.dto.EventDTO;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.dto.ParticipantDTO;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;

import java.time.LocalDate;
import java.util.List;

public interface IEventServices {
     Participant addParticipant(ParticipantDTO participant);
     Event addAffectEvenParticipant(EventDTO event);
     Event addAffectEvenParticipant(EventDTO event,int idParticipant) ;
     Logistics addAffectLog(LogisticsDTO logistics, String descriptionEvent);
     List<Logistics> getLogisticsDates(LocalDate dateDebut, LocalDate dateFin);
     void calculCout();
}
