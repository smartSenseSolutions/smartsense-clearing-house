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
import org.springframework.web.client.HttpClientErrorException;
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

    @Value("${app.portal.connector.selfDescriptionUrl}")
    private String portalConnectorSDUrl;

    @Value("${app.retryCount}")
    private Integer retryCount;

    @Value("${app.portal.client.id}")
    private String portalClientId;

    @Value("${app.portal.client.tokenUrl}")
    private String keycloakTokenUrl;

    @Value("${app.portal.client.secret}")
    private String portalClientSecret;

    @Async
    public void callBack(Map<String, Object> map) throws InterruptedException {
        DummyService.log.info("==> Request : CallBack execute:");
        Map<String, Object> callBack = (Map<String, Object>) map.get("callback");
        String callbackUrl = callBack.get("url").toString();
        Map<String, Object> participantDetailsMap = (Map<String, Object>) callBack.get("headers");
        String bpn = participantDetailsMap.get("Business-Partner-Number") + "";
        callBackForValidation(bpn, callbackUrl, 0);
    }

    @Async
    public void callBackForValidation(String bpn, String callBack, int currentRetry) throws InterruptedException {
        try {
            Thread.sleep(5000L);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(generateToken());
            headers.set("Business-Partner-Number", bpn);

            ClearinghouseResponseData callBackResponse = ClearinghouseResponseData.builder()
                    .validationMode("COMPLETED")
                    .validationUnits(List.of(
                            ClearinghouseResponseData.ValidationUnit.builder()
                                    .result("VALID")
                                    .type("vatId")
                                    .reason(null)
                                    .build()
                    ))
                    .build();

            HttpEntity<Object> requestEntity = new HttpEntity<>(callBackResponse, headers);
            ResponseEntity<Object> exchange = restTemplate.exchange(callBack, HttpMethod.POST, requestEntity, Object.class);
            if ((exchange.getStatusCode().isError()) && currentRetry <= retryCount) {
                currentRetry++;
                DummyService.log.info("==> Retrying  PortalBack for /api/v1/validation Retrycount: {}", currentRetry);
                Thread.sleep(5000L);
                callBackForValidation(bpn, callBack, currentRetry);
            } else if ((exchange.getStatusCode().isError()) && currentRetry > retryCount) {
                DummyService.log.info("==> Retrying  count exceed then limit currentRetryCount: {}, limit:{}", currentRetry, retryCount);
            }
        } catch (HttpClientErrorException ce) {
            if (ce.getStatusCode().isError() && currentRetry <= retryCount) {
                currentRetry++;
                DummyService.log.info("==> Retrying  PortalBack for /api/v1/validation Retrycount: {}", currentRetry);
                Thread.sleep(5000L);
                callBackForValidation(bpn, callBack, currentRetry);
            } else if (ce.getStatusCode().isError() && currentRetry > retryCount) {
                DummyService.log.info("==> Retrying  count exceed then limit currentRetryCount: {}, limit:{}", currentRetry, retryCount);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void portalCallBackRegistration(String externalId, Map<String, Object> map, int currentRetry) throws InterruptedException {
        DummyService.log.info("==> Request : portalCallBackRegistration:{} , CurrentRetry:{}", map, currentRetry);
        SelfDescriptionResponseData responseData = new SelfDescriptionResponseData(externalId, "DUMMT APPROVAL", "{ \"test\": true }", SelfDescriptionStatus.Confirm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(generateToken());
        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(responseData, headers);

            Map<String, Object> credentialMap = (Map<String, Object>) map.get("credentialSubject");
            String url = credentialMap != null && credentialMap.get("type").equals("ServiceOffering") ? portalConnectorSDUrl : portalRegSDUrl;
            ResponseEntity<Object> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);

            if ((exchange.getStatusCode().isError()) && currentRetry <= retryCount) {
                currentRetry++;
                DummyService.log.info("==> Retrying  PortalBack for /api/v1/compliance Retrycount: {}", currentRetry);
                Thread.sleep(5000L);
                portalCallBackRegistration(externalId, map, currentRetry);
            } else if ((exchange.getStatusCode().isError()) && currentRetry > retryCount) {
                DummyService.log.info("==> Retrying  count exceed then limit currentRetryCount: {}, limit:{}", currentRetry, retryCount);
            }
        } catch (HttpClientErrorException ce) {
            DummyService.log.info("error response :{}", ce.getMessage());
            if (ce.getStatusCode().isError() && currentRetry <= retryCount) {
                currentRetry++;
                DummyService.log.info("==> Retrying  PortalBack for /api/v1/compliance Retrycount: {}", currentRetry);
                Thread.sleep(5000L);
                portalCallBackRegistration(externalId, map, currentRetry);
            } else if (ce.getStatusCode().isError() && currentRetry > retryCount) {
                DummyService.log.info("==> Retrying  count exceed then limit currentRetryCount: {}, limit:{}", currentRetry, retryCount);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        var data = new LinkedMultiValueMap();
        data.put("client_id", List.of(portalClientId));
        data.put("client_secret", List.of(portalClientSecret));
        data.put("grant_type", List.of("client_credentials"));

        HttpEntity<Object> requestEntity = new HttpEntity<>(data, headers);
        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(keycloakTokenUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
        });

        Map<String, Object> response = exchange.getBody();
        return response.get("access_token").toString();
    }
}

