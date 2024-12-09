package tn.esprit.eventsproject.dto;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsDTO {
    private int idLog;
    private String description;
    private boolean reserve;
    private float prixUnit;
    private int quantite;
}
