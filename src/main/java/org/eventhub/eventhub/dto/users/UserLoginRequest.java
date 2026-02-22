package org.eventhub.eventhub.dto.users;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String identifier;
    private String password;
}
