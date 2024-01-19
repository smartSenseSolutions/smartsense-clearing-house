package com.smartsense.dummy.ch.controller.service;

import com.smartsense.dummy.ch.dto.ClearinghouseResponseData;
import com.smartsense.dummy.ch.dto.ClearinghouseResponseStatus;
import com.smartsense.dummy.ch.dto.SelfDescriptionResponseData;
import com.smartsense.dummy.ch.dto.SelfDescriptionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DummyService {

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

    @Async
    public void callBack(Map<String,Object> map){
        log.info("==> Request : CallBack execute:");
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

    @Async
    public void portalCallBackRegistration(String externalId, Map<String,Object> map){
        log.info("==> Request : portalCallBackRegistration:{}",map);
        SelfDescriptionResponseData responseData = new SelfDescriptionResponseData(externalId,"DUMMT APPROVAL","{ \"test\": true }", SelfDescriptionStatus.Confirm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(generateToken());

        HttpEntity<Object> requestEntity =new HttpEntity<>(responseData, headers);
        restTemplate.exchange(portalRegSDUrl, HttpMethod.POST, requestEntity,Object.class);
    }

    public String generateToken(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        var data = new LinkedMultiValueMap();
        data.put("client_id", List.of(portalClientId));
        data.put("client_secret",List.of(portalClientSecret));
        data.put("grant_type",List.of("client_credentials"));

        HttpEntity<Object> requestEntity =new HttpEntity<>(data, headers);

        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(keycloakTokenUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
        });

        Map<String, Object> response = exchange.getBody();
        return response.get("access_token").toString();
    }
}
