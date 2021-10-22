package com.phishingbot;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.validator.routines.UrlValidator;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.json.JSONObject;


public class PhishingBot extends TelegramLongPollingBot {

    private final String BOT_NAME = "PhishingBot";
    private final String BOT_TOKEN = "2097646910:AAFjCPbXYldU5CedABJ1IXqtO9pRRy8PgsM";
    private final String[] VALID_SCHEME_VALIDATOR = { "http", "https" };
    private final String API_URL = "https://urlhaus-api.abuse.ch/v1/url/";
    private OkHttpClient Client;

    private UrlValidator UrlValidator;

    public PhishingBot() {
        this.UrlValidator = new UrlValidator(VALID_SCHEME_VALIDATOR);
        this.Client = new OkHttpClient();
    }

    @Override
    public void onUpdateReceived(Update update) {
        
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.enableHtml(true);
            message.setChatId(update.getMessage().getChatId().toString());
            
            String url = update.getMessage().getText();

            if(IsValidUrl(url)) {

                String messageText = CheckIfIsPhishing(url);
                
                message.setText(messageText);

            } else {
                message.setText("Url Non Valido");
            }

            
            
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                System.out.println(e.getMessage());
            }
        }
           
    }

    private String CheckIfIsPhishing(String url) {

        String message = "Non sono riuscito a fare i controlli. Riprova.";

        try {

            JSONObject json = MakeRequest(url);

            if(json.getString("query_status").equals("no_results") )  {
                message = "Non ho risultati per questo sito. Se sei sicuro che sia un sito Phishing aggiungilo <a href=\"https://urlhaus.abuse.ch/browse/\">qui</a>";    
            } else if(json.getString("query_status").equals("ok"))  {
                
                JSONObject blacklists = json.getJSONObject("blacklists");

                String spamhausDbl = blacklists.getString("spamhaus_dbl");
                String surbl = blacklists.getString("surbl");

                if(this.IsNotListed(surbl) && this.IsNotListed(spamhausDbl)) {
                    message = "Il sito è sicuro";
                } else {
                    message = "L'url che hai inviato è un sito Phising, fai attenzione non cliccarlo o inserire dati all'interno";
                }
            }

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return message;

    }

    private JSONObject MakeRequest(String url) throws Exception{

        RequestBody body = new MultipartBuilder()
            .type(MultipartBuilder.FORM)
            .addFormDataPart("url", url).build();

        Request request = new Request.Builder()
            .url(this.API_URL)
            .post(body)
            .header("Content-Type", "multipart/form-data")
            .build();
        
        Response response = this.Client.newCall(request).execute();

        String jsonResponse = response.body().string();

        if(jsonResponse.equals("")) {
            return new JSONObject();
        }

        JSONObject json = new JSONObject(jsonResponse);

        return json;
    }

    private boolean IsNotListed(String property) {
        return property.equals("not listed");
    }

    private boolean IsValidUrl(String url) {
        return UrlValidator.isValid(url);
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
