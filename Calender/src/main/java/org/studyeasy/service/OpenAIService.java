package org.studyeasy.service;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    private final Map<String, List<ChatMessage>> conversations = new HashMap<>();
    
    public String processUserInput(String userId, String userInput) {
        OpenAiService service = new OpenAiService(apiKey);
        
        // Get or create conversation history
        List<ChatMessage> messages = conversations.getOrDefault(userId, new ArrayList<>());
        
        // Add system message if this is a new conversation
        if (messages.isEmpty()) {
            ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                "You are a helpful calendar assistant. Help users schedule events and manage their calendar.");
            messages.add(systemMessage);
        }
        
        // Add user message
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
        messages.add(userMessage);
        
        // Create chat completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(messages)
                .build();
        
        // Get response from OpenAI
        ChatCompletionResult result = service.createChatCompletion(completionRequest);
        ChatMessage responseMessage = result.getChoices().get(0).getMessage();
        
        // Add response to conversation history
        messages.add(responseMessage);
        conversations.put(userId, messages);
        
        return responseMessage.getContent();
    }
}
