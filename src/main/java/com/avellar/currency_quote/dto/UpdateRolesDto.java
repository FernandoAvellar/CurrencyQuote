package com.avellar.currency_quote.dto;

import java.util.List;

public record UpdateRolesDto(String username, List<String> roles) {
}