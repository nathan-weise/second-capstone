package com.techelevator.tenmo.services;

import com.techelevator.tenmo.models.Account;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AccountService {

    private String baseURL;
    private String token;
    private RestTemplate restTemplate = new RestTemplate();

    public AccountService(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Account fetchAccount() {
        ResponseEntity<Account> response = restTemplate.exchange(baseURL + "user/account", HttpMethod.GET, makeAuthEntity(), Account.class);
        return response.getBody();
    }

    public HttpEntity<?> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        return new HttpEntity<>(headers);
    }


}
