package com.bwg.channel.backend.mcicommon.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MCI 표준 처리 결과.
 * <p>
 * 고객사별 오류 코드는 mapper에서 product 표준 코드로 변환해 이 모델에 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MciResult {

    /** 거래 성공 여부. */
    private boolean success;

    /** 채널/backend에서 공통으로 해석할 표준 결과 코드. */
    private String code;

    /** 운영자와 개발자가 확인할 수 있는 결과 메시지. */
    private String message;

    /** 성공 결과를 만들 때 사용하는 기본 helper. */
    public static MciResult success() {
        return new MciResult(true, "0", "success");
    }

    /** 실패 결과를 만들 때 사용하는 기본 helper. */
    public static MciResult fail(String code, String message) {
        return new MciResult(false, code, message);
    }
}
