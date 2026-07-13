package com.bwg.channel.backend.integrationsvc.notion.client;

import com.bwg.channel.backend.integrationsvc.notion.dto.NotionCreatePageReqDto;

/**
 * Notion 연동 client 경계.
 * <p>
 * webhook 서비스는 이 인터페이스에만 의존하므로, 테스트에서는 fake client로 대체할 수 있다.
 */
public interface NotionClient {
    /**
     * Notion database에 page(row)를 생성한다.
     *
     * @param request Notion pages API 요청 body.
     */
    void createPage(NotionCreatePageReqDto request);
}
