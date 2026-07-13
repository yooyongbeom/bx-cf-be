package com.bwg.channel.backend.mcisvc.registry;

import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcisvc.config.MciTransactionProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YamlTransactionRegistryTest {

    @Test
    void findsTransactionDefinitionLoadedFromProperties() {
        TransactionDefinition definition = new TransactionDefinition();
        definition.setCode("CIF001");
        definition.setName("고객 기본정보 조회");
        definition.setEnabled(true);
        definition.setChannels(List.of("WEB", "MOBILE"));
        definition.setTargetSystem("CORE_BANKING");
        definition.setAdapter("sampleCoreBankingAdapter");
        definition.setRequestMapper("sampleCustomerRequestMapper");
        definition.setResponseMapper("sampleCustomerResponseMapper");
        definition.setTimeoutMs(3000);
        definition.setRetry(false);
        definition.setLogPayload(true);
        definition.setMaskFields(List.of("data.customerName", "data.rrn"));

        MciTransactionProperties properties = new MciTransactionProperties();
        properties.setTransactions(List.of(definition));

        YamlTransactionRegistry registry = new YamlTransactionRegistry(properties);

        assertThat(registry.findByCode("CIF001"))
                .isPresent()
                .get()
                .extracting(TransactionDefinition::getAdapter)
                .isEqualTo("sampleCoreBankingAdapter");
    }
}
