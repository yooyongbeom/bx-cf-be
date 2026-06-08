package com.bwg.channel.backend.authsvc.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Map;

@Data
public class ErpLoginDto {
    private Header header;
    @JsonProperty("SSMAUTH00101In")
    private SSMAUTH00101In SSMAUTH00101In;

    @Data
    public static class Header {
        private String application;
        private String service;
        private String operation;
        private UserInfo userInfo;
        private String trUuid;

        @Data
        @JsonInclude(JsonInclude.Include.ALWAYS)       // 항상 포함
        @JsonSerialize
        public static class UserInfo {

        }
    }

    @Data
    public static class SSMAUTH00101In {
        private String id;
        private String password;
    }
}
