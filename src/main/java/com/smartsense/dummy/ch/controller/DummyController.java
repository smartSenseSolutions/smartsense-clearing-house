package com.smartsense.dummy.ch.controller;

import com.smartsense.dummy.ch.controller.service.DummyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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


    @Autowired
    private DummyService dummyService;

    @PostMapping("/api/v1/validation")
    public void mockValidation(@RequestBody Map<String,Object> map) throws InterruptedException {
        log.info("==> Request : Params:{}",map);
        Thread.sleep(10000L);
        dummyService.callBack(map);
    }

    @PostMapping("/api/v1/compliance")
    public void mockCompliance(@RequestParam("externalId") String externalId,@RequestBody Map<String,Object> map ) throws InterruptedException {
        Thread.sleep(10000L);
        dummyService.portalCallBackRegistration(externalId,map,0);
    }

}
