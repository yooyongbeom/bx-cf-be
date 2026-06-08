package com.bwg.channel.backend.authcore.service;

import com.bwg.channel.backend.authcore.domain.dto.CustomUserDetails;
import com.bwg.channel.backend.authcore.exception.UserNotFoundException;

public interface CustomUserDetailsService {
    CustomUserDetails loadUserByUsername(String username) throws UserNotFoundException;
}