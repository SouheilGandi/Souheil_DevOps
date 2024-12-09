package tn.esprit.eventsproject.dto;

import lombok.*;
import tn.esprit.eventsproject.entities.Tache;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ParticipantDTO {
    private int idPart;
    private String nom;
    private String prenom;
    private Tache tache;
    private Set<EventDTO> events;

}
