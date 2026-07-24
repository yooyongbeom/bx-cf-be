package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeBridgeSupportTests {

    @Test
    void returnsInheritedFieldsWithParentFirst() {
        assertThat(TypeBridgeSupport.getAllFields(ChildDto.class))
                .extracting("name")
                .containsExactly("parentValue", "childValue");
    }

    @Test
    void detectsApiFieldsDeclaredOnParent() {
        assertThat(TypeBridgeSupport.hasApiFields(InheritedOnlyDto.class)).isTrue();
    }

    @Test
    void resolvesExplicitAndConventionBaseNames() {
        ApiDto explicitName = ExplicitNameReqDto.class.getAnnotation(ApiDto.class);
        ApiDto conventionName = ConventionRequestDto.class.getAnnotation(ApiDto.class);

        assertThat(TypeBridgeSupport.resolveBaseName(ExplicitNameReqDto.class, explicitName))
                .isEqualTo("Explicit");
        assertThat(TypeBridgeSupport.resolveBaseName(ConventionRequestDto.class, conventionName))
                .isEqualTo("Convention");
    }

    @Test
    void convertsHyphenAndUnderscoreSegmentsToPascalCase() {
        assertThat(TypeBridgeSupport.toPascalCase("common-code_list"))
                .isEqualTo("CommonCodeList");
    }

    static class ParentDto {

        @ApiField
        private String parentValue;
    }

    static class ChildDto extends ParentDto {

        private String childValue;
    }

    static class InheritedOnlyDto extends ParentDto {
    }

    @ApiDto(name = "Explicit")
    static class ExplicitNameReqDto {
    }

    @ApiDto
    static class ConventionRequestDto {
    }
}
