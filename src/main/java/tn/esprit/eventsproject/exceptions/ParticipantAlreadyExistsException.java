package tn.esprit.eventsproject.exceptions;

public class ParticipantAlreadyExistsException extends  RuntimeException{

    // Default constructor
    public ParticipantAlreadyExistsException() {
        super("Participant already exists");
    }

}
