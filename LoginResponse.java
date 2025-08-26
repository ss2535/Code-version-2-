package com.scheduleNetwork_version2.scheduleNetwork_version2.dto;

import java.util.List;

public record LoginResponse(String token, String username, List<String> roles) {}
