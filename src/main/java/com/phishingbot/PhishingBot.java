package com.phishingbot;

import org.apache.commons.validator.routines.UrlValidator;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PhishingBot extends TelegramLongPollingBot {

    private final String BOT_NAME = "PhishingBot";
    private final String BOT_TOKEN = "";
    private final String[] VALID_SCHEME_VALIDATOR = { "http", "https" };

    private UrlValidator urlValidator;

    public PhishingBot() {
        this.urlValidator = new UrlValidator(VALID_SCHEME_VALIDATOR);
    }

    @Override
    public void onUpdateReceived(Update update) {
        
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.setChatId(update.getMessage().getChatId().toString());
            
            String url = update.getMessage().getText();

            if(IsValidUrl(url)) {

                CheckIfIsPhishing(url);

            } else {
                message.setText("Url Non Valido");
            }

            
            
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
           
    }

    private void CheckIfIsPhishing(String url) {

    }

    private boolean IsValidUrl(String url) {
        return urlValidator.isValid(url);
    }



    @Override
    public String getBotUsername() {
        return this.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return this.BOT_TOKEN;
    }
    
    
}
