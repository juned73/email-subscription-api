package com.example.subscription.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailValidationResponse {

    @JsonProperty("email_address")
    private String emailAddress;

    @JsonProperty("email_deliverability")
    private EmailDeliverability emailDeliverability;

    @JsonProperty("email_quality")
    private EmailQuality emailQuality;

    public boolean isDeliverable() {
        if (emailDeliverability == null) {
            return false;
        }
        if (emailDeliverability.getStatus() != null) {
            return "deliverable".equalsIgnoreCase(emailDeliverability.getStatus());
        }
        return Boolean.TRUE.equals(emailDeliverability.getIsFormatValid())
                && Boolean.TRUE.equals(emailDeliverability.getIsMxValid());
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmailDeliverability {

        private String status;

        @JsonProperty("status_detail")
        private String statusDetail;

        @JsonProperty("is_format_valid")
        private Boolean isFormatValid;

        @JsonProperty("is_smtp_valid")
        private Boolean isSmtpValid;

        @JsonProperty("is_mx_valid")
        private Boolean isMxValid;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmailQuality {

        private Double score;

        @JsonProperty("is_disposable")
        private Boolean isDisposable;

        @JsonProperty("is_free_email")
        private Boolean isFreeEmail;

        @JsonProperty("is_role")
        private Boolean isRole;
    }
}