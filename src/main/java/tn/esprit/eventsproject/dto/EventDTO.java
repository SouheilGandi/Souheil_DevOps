package tn.esprit.eventsproject.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    private int idEvent;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private float cout;
    private Set<ParticipantDTO> participants;
    private Set<LogisticsDTO> logistics;
    public Set<LogisticsDTO> getLogistics() {
        if (logistics == null) {
            logistics = new HashSet<>();  // Ensures logistics is never null
        }
        return logistics;
    }


}