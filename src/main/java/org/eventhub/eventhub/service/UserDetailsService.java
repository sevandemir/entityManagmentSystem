package org.eventhub.eventhub.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface UserDetailsService {
    UserDetails loadUser(String identifier) throws UsernameNotFoundException;
}
