package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatRequestDto {
    @NotBlank(message = "Application field 'app' not specified")
    private String app;
    @NotBlank(message = "Uri field 'uri' not specified")
    private String uri;
    @NotBlank(message = "IP address field 'ip' not specified")
    private String ip;
    @NotBlank(message = "Timestamp field 'timestamp' not specified")
    private String timestamp;
}
