package org.studyeasy.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Calendar Assistant";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    @Value("${google.client.credentials-file-path}")
    private String credentialsFilePath;
    
    @Value("${google.oauth.callback.uri}")
    private String callbackUri;

    private Calendar service;
    private GoogleAuthorizationCodeFlow flow;

    public void initializeService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // Load client secrets
        Resource resource = new ClassPathResource(credentialsFilePath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, 
                new InputStreamReader(resource.getInputStream()));

        // Build flow
        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }
    
    public String getAuthorizationUrl() throws GeneralSecurityException, IOException {
        if (flow == null) {
            initializeService();
        }
        return flow.newAuthorizationUrl().setRedirectUri(callbackUri).build();
    }
    
    public void exchangeCodeForToken(String code) throws IOException, GeneralSecurityException {
        if (flow == null) {
            initializeService();
        }
        
        // Exchange auth code for tokens
        flow.newTokenRequest(code).setRedirectUri(callbackUri).execute();
        
        // Initialize the Calendar service
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = flow.loadCredential("user");
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String createEvent(String name, String date, String time, Integer duration, List<String> participants) {
        try {
            if (service == null) {
                return "Please authenticate with Google Calendar first";
            }
            
            Event event = new Event().setSummary(name);
            
            // Parse date and time
            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + time, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            LocalDateTime endDateTime = startDateTime.plusMinutes(duration != null ? duration : 60);
            
            DateTime start = new DateTime(startDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli());
            DateTime end = new DateTime(endDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli());
            
            event.setStart(new EventDateTime().setDateTime(start));
            event.setEnd(new EventDateTime().setDateTime(end));
            
            // Add participants if any
            if (participants != null && !participants.isEmpty()) {
                List<EventAttendee> attendees = participants.stream()
                        .map(email -> new EventAttendee().setEmail(email))
                        .collect(Collectors.toList());
                event.setAttendees(attendees);
            }
            
            // Insert the event
            event = service.events().insert("primary", event).execute();
            return "Event created: " + event.getHtmlLink();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to create event: " + e.getMessage();
        }
    }
    
    public boolean isAuthorized() {
        return service != null;
    }
}
