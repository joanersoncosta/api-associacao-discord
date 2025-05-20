package com.example.demo.associacao.comunicacao;

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