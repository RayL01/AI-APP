package com.ray.aiapp.controller;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final OpenAiChatModel chatModel;

    @PostMapping
    public String chat(@RequestBody String prompt) {
        return chatModel.generate(prompt);
    }
}
