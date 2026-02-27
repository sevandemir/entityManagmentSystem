package org.eventhub.eventhub.dto.users;

import lombok.Data;

@Data
public class UserLoginRequestDto {
    private String identifier;
    private String password;
}
