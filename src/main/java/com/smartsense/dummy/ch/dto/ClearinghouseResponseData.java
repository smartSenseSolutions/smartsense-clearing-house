package com.smartsense.dummy.ch.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class ClearinghouseResponseData {

    private String validationMode;
    private List<ValidationUnit> validationUnits;

    public String getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    public List<ValidationUnit> getValidationUnits() {
        return validationUnits;
    }

    public void setValidationUnits(List<ValidationUnit> validationUnits) {
        this.validationUnits = validationUnits;
    }

    @Builder
    public static class ValidationUnit {
        private String result;
        private String type;
        private String reason;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

}
