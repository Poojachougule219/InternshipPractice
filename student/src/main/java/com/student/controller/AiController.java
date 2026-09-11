package com.student.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.student.dto.AiRequest;
import com.student.dto.AiResponse;
import com.student.service.AiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    public ResponseEntity<AiResponse> askAi(
            @RequestBody AiRequest request) {

        if (request == null
                || request.getQuestion() == null
                || request.getQuestion().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(new AiResponse(
                            "Please enter a question."
                    ));
        }

        String answer =
                aiService.askAi(
                        request.getQuestion().trim()
                );

        return ResponseEntity.ok(
                new AiResponse(answer)
        );
    }
}