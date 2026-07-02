package com.bwg.channel.backend.securitycommon.service;

import com.bwg.channel.backend.securitycommon.domain.dto.CustomUserDetails;
import com.bwg.channel.backend.securitycommon.exception.UserNotFoundException;

public interface CustomUserDetailsService {
    CustomUserDetails loadUserByUsername(String username) throws UserNotFoundException;
}
