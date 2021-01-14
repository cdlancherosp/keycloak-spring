package com.globant.chatbot.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/fluentlab")
public class ApiController {

    @GetMapping(value = "dev/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getDevTeamMembers() {
        List<String> teamMembers = new ArrayList<>();
        teamMembers.add("Javier Nonis");
        teamMembers.add("Juan Duque");
        teamMembers.add("Duvan Lancheros");
        teamMembers.add("Carlos Palma");

        return ResponseEntity.ok(teamMembers);
    }
}
