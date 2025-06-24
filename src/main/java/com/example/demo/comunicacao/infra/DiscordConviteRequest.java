package com.example.demo.comunicacao.infra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordConviteRequest {
    private int max_uses;
    private boolean unique;
    private int max_age;
} 