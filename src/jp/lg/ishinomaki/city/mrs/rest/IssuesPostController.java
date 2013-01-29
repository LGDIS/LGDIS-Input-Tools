//
//  IssuesPostController.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.rest;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

/**
 * IssuesのPost要求を実行します。
 * 
 */
public class IssuesPostController extends PostController {

    /**
     * コンストラクタ
     */
    public IssuesPostController() {

        // Redmine送信用の定義を取得
        Map<String, Object> redmine = ParserConfig.getInstance().getRedmine();
        protocol = (String) redmine.get(ParserConfig.PROTOCOL);
        targetHost = (String) redmine.get(ParserConfig.TARGET_HOST);
        targetPort = (String) redmine.get(ParserConfig.TARGET_PORT);
        postApi = (String) redmine.get(ParserConfig.ISSUES_POST_API);
        apiKey = (String) redmine.get(ParserConfig.API_KEY);
        basicauthId = (String) redmine.get(ParserConfig.BASICAUTH_ID);
        basicauthPassword = (String) redmine
                .get(ParserConfig.BASICAUTH_PASSWORD);
        timeout = (Integer) redmine.get(ParserConfig.TIMEOUT);
        retryCount = (Integer) redmine.get(ParserConfig.RETRY_COUNT);
        contentType = (String) redmine
                .get(ParserConfig.ISSUES_POST_CONTENT_TYPE);
    }

    @Override
    protected HttpEntity createHttpEntity(Object data) {
        HttpEntity httpEntity = null;
        try {
            httpEntity = new StringEntity((String)data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpEntity;
    }
}
