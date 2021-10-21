package com.phishingbot;

import org.apache.commons.validator.routines.UrlValidator;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class PhishingBot extends TelegramLongPollingBot {

    private final String BOT_NAME = "PhishingBot";
    private final String BOT_TOKEN = "";
    private final String[] VALID_SCHEME_VALIDATOR = { "http", "https" };
    private final String API_URL = "https://urlhaus-api.abuse.ch/v1/url/";

    private UrlValidator urlValidator;

    public PhishingBot() {
        this.urlValidator = new UrlValidator(VALID_SCHEME_VALIDATOR);
    }

    @Override
    public void onUpdateReceived(Update update) {
        
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.enableHtml(true);
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

    private String CheckIfIsPhishing(String url) {

        String message = "Non sono riuscito a fare i controlli. Riprova.";

        try {

            JSONObject json = MakeRequest(url);

            if(json.getString("query_status") == "no_results")  {
                message = "Non ho risultati per questo sito. Se sei sicuro che sia un sito Phishing aggiungilo <a href=\"https://urlhaus.abuse.ch/browse/\">qui</a>";    
            } else if(json.getString("query_status") == "ok")  {
                
                JSONObject blacklists = json.getJSONObject("blacklists");

                String spamhausDbl = blacklists.getString("spamhaus_dbl");
                String surbl = blacklists.getString("surbl");

                if(surbl == "not listed" && spamhausDbl == "not listed") {
                    message = "Il sito è sicuro";
                } else {
                    message = "L'url che hai inviato è un sito Phising, fai attenzione non cliccarlo o inserire dati all'interno";
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return message;

    }

    private JSONObject MakeRequest(String url) throws Exception{
        

        CloseableHttpClient httpClient = HttpClients.createDefault();
    
        HttpPost postRequest = new HttpPost(API_URL);
        
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.addTextBody("url", url);

        HttpEntity multipart = builder.build();

        postRequest.setEntity(multipart);

        CloseableHttpResponse response = httpClient.execute(postRequest);

        HttpEntity entityResponse = response.getEntity();

        if(entityResponse == null) {
            throw new Exception();
        }

        String responseString = EntityUtils.toString(entityResponse);

        JSONObject json = new JSONObject(responseString);

        return json;


        
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
