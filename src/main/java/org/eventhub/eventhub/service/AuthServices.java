package org.eventhub.eventhub.service;

import org.eventhub.eventhub.dto.users.UserRegisterRequestDto;

public interface AuthServices {
    String login(String username, String password);

    String register(UserRegisterRequestDto request);

}
