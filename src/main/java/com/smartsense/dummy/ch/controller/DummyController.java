package com.smartsense.dummy.ch.controller;

import com.smartsense.dummy.ch.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
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
        log.info("==> Request : Params:{}",map);
        String callBack = map.get("callbackUrl")+"";
        Map<String,Object> participantDetailsMap=(Map<String, Object>) map.get("participantDetails");
        String bpn =participantDetailsMap.get("bpn")+"";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(generateToken());
        ClearinghouseResponseData callBackResponse = new ClearinghouseResponseData(bpn, ClearinghouseResponseStatus.CONFIRM,"SUccess");

        HttpEntity<Object> requestEntity =new HttpEntity<>(callBackResponse, headers);
        restTemplate.exchange(callBack, HttpMethod.POST, requestEntity,Object.class);
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
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
        return mappingJackson2HttpMessageConverter;
    }
    public String generateToken(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        var data = new  LinkedMultiValueMap();
        data.put("client_id",List.of(portalClientId));
        data.put("client_secret",List.of(portalClientSecret));
        data.put("grant_type",List.of("client_credentials"));

        HttpEntity<Object> requestEntity =new HttpEntity<>(data, headers);

        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(keycloakTokenUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
        });

        Map<String, Object> response = exchange.getBody();
        return response.get("access_token").toString();
    }
}
