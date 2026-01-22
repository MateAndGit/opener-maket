package mateandgit.opener_maket.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("A")

public class Album extends Item {

    private String artist;
    private String etc;

}
