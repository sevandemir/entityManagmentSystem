package org.eventhub.eventhub.dto.users;

import lombok.Data;
import org.eventhub.eventhub.enums.Role;

@Data
public class UserRegisterRequestDto {
    private String userName;
    private String password;
    private String email;
    private String phone;
    private Role role;

}
