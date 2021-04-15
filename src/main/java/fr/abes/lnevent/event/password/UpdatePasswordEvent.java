package fr.abes.lnevent.event.password;

import fr.abes.lnevent.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordEvent extends Event {

    public UpdatePasswordEvent(Object source, String siren, String newpassword) {
        super(source);
        this.siren = siren;
        this.newpasswordHash = newpassword;
    }

    private String siren;
    private String newpasswordHash;

}
