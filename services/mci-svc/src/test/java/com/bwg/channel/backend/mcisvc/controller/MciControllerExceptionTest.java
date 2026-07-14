package com.bwg.channel.backend.mcisvc.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bwg.channel.backend.common.aop.GlobalRestExceptionAdvice;
import com.bwg.channel.backend.mcicommon.router.MciRouter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MciControllerExceptionTest {

    private final MciRouter mciRouter = new MciRouter(
            code -> Optional.empty(),
            List.of(),
            List.of()
    );
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MciController(mciRouter))
            .setControllerAdvice(new GlobalRestExceptionAdvice())
            .build();

    @Test
    void returnsTypedMciCodeAndStatusForUnknownTransaction() throws Exception {
        mockMvc.perform(post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"header\":{\"transactionCode\":\"UNKNOWN\",\"channelCode\":\"WEB\"},\"data\":{}}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("-6101"))
                .andExpect(jsonPath("$.msg").value("MCI transaction is not registered"));
    }
}
