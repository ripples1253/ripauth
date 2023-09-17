package icu.ripley.fusionauthtest1minecraft.model;

import lombok.Data;

import java.util.UUID;

@Data
public class User {
    private UUID uuid;
    private String username;
    private boolean authenticationRequired;
    private boolean authenticated;
}
