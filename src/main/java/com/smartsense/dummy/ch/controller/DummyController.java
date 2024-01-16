package com.smartsense.dummy.ch.controller;

import com.smartsense.dummy.ch.dto.Oath2ClientDTO;
import com.smartsense.dummy.ch.dto.SelfDescriptionResponseData;
import com.smartsense.dummy.ch.dto.SelfDescriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class DummyController {

    @Autowired
    private RestTemplate restTemplate;


    @Value("${app.portal.reg.selfDescriptionUrl}")
    private String portalRegSDUrl;

    @Value("${app.portal.client.id}")
    private String portalClientId;

    @Value("${app.portal.client.tokenUrl}")
    private String keycloakTokenUrl;

    @Value("${app.portal.client.secret}")
    private String portalClientSecret;

    @PostMapping("/api/v1/validation")
    public void mockValidation(@RequestBody Map<String,Object> map){

    }

    @PostMapping("/api/v1/compliance")
    public void mockCompliance(@RequestParam("externalId") String externalId,@RequestBody Map<String,Object> map ) throws InterruptedException {
        Thread.sleep(5000L);
        portalCallBackRegistration(externalId);
    }





    public void portalCallBackRegistration(String externalId){
        SelfDescriptionResponseData responseData = new SelfDescriptionResponseData(externalId,"DUMMT APPROVAL","TEST DOCUMENT", SelfDescriptionStatus.Confirm);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(generateToken());

        HttpEntity<Object> requestEntity =new HttpEntity<>(responseData, headers);
        restTemplate.exchange(portalRegSDUrl, HttpMethod.POST, requestEntity,Object.class);
    }

    public String generateToken(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        Oath2ClientDTO tokenRequest = new Oath2ClientDTO(portalClientId,portalClientSecret,"client_credentials");
        HttpEntity<Object> requestEntity =new HttpEntity<>(tokenRequest, headers);
        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(keycloakTokenUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
        });

        Map<String, Object> response = exchange.getBody();
        return response.get("access_token").toString();
    }
}
