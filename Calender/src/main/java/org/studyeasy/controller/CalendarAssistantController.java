package org.studyeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.studyeasy.service.GoogleCalendarService;
import org.studyeasy.service.OpenAIService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

@Controller
public class CalendarAssistantController {

    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private GoogleCalendarService googleCalendarService;
    
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // Generate a user ID if not exists
        if (session.getAttribute("userId") == null) {
            session.setAttribute("userId", UUID.randomUUID().toString());
        }
        
        model.addAttribute("isAuthorized", googleCalendarService.isAuthorized());
        return "index";
    }
    
    @GetMapping("/authorize")
    public RedirectView authorize() throws GeneralSecurityException, IOException {
        String authUrl = googleCalendarService.getAuthorizationUrl();
        return new RedirectView(authUrl);
    }
    
    @GetMapping("/oauth2callback")
    public String oauth2Callback(@RequestParam("code") String code, Model model) {
        try {
            googleCalendarService.exchangeCodeForToken(code);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to authorize: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/chat")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String userInput = request.get("message");
        String response = openAIService.processUserInput(userId, userInput);
        
        return Map.of("response", response);
    }
    
    @PostMapping("/create-event")
    @ResponseBody
    public Map<String, String> createEvent(@RequestBody Map<String, Object> eventData) {
        if (!googleCalendarService.isAuthorized()) {
            return Map.of("result", "Please authorize with Google Calendar first");
        }
        
        String name = (String) eventData.get("name");
        String date = (String) eventData.get("date");
        String time = (String) eventData.get("time");
        Integer duration = (Integer) eventData.get("duration");
        @SuppressWarnings("unchecked")
        List<String> participants = (List<String>) eventData.get("participants");
        
        String result = googleCalendarService.createEvent(name, date, time, duration, participants);
        return Map.of("result", result);
    }
}
