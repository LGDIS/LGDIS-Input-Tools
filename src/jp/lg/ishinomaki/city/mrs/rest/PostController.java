//
//  PostController.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 * サーバへPost要求を発行します。
 * 
 */
public class PostController implements ResponseHandler<String> {

    // ログ用
    private final Logger log = Logger.getLogger(PostController.class
            .getSimpleName());

    /**
     * プロトコル
     */
    private String protocol;

    /**
     * Post先のホスト名もしくはIPアドレス
     */
    private String targetHost;

    /**
     * Post先のポート番号(なければnull)
     */
    private String targetPort;

    /**
     * Post時に使用するAPI
     */
    private String postApi;

    /**
     * Post時に使用するAPIキー
     */
    private String apiKey;

    /**
     * Http接続のタイムアウト値<br>
     * この値は接続確立までのタイムアウトとPostリクエスト時のタイムアウトに使用されます
     */
    private int timeout;

    /**
     * Postエラー時のリトライ回数
     */
    private int retryCount = 0;

    /**
     * Basic認証時に必要なID
     */
    private String basicauthId;

    /**
     * Basic認証時に必要なパスワード
     */
    private String basicauthPassword;

    /**
     * コンストラクタ
     */
    public PostController() {

        // Redmine送信用の定義を取得
        Map<String, Object> redmine = ParserConfig.getInstance().getRedmine();
        protocol = (String) redmine.get(ParserConfig.PROTOCOL);
        targetHost = (String) redmine.get(ParserConfig.TARGET_HOST);
        targetPort = (String) redmine.get(ParserConfig.TARGET_PORT);
        postApi = (String) redmine.get(ParserConfig.POST_API);
        apiKey = (String) redmine.get(ParserConfig.API_KEY);
        basicauthId = (String) redmine.get(ParserConfig.BASICAUTH_ID);
        basicauthPassword = (String) redmine
                .get(ParserConfig.BASICAUTH_PASSWORD);
        timeout = (Integer) redmine.get(ParserConfig.TIMEOUT);
        retryCount = (Integer) redmine.get(ParserConfig.RETRY_COUNT);
    }

    /**
     * RestなサーバへのPost処理です。<br>
     * 送信するパラメータを引数にMap形式で渡してください。
     * 
     * @param sendData
     *            送信データです。チケット情報作成用のxml文字列を指定してください。
     */
    public void post(String sendData) {

        // 引数チェック
        if (sendData == null || sendData.length() == 0) {
            log.warning("送信データがありません");
            return;
        }

        // Http接続用クライアント
        HttpClient httpClient = new DefaultHttpClient();

        // Http接続用パラメータ
        HttpParams httpParams = httpClient.getParams();
        // --------------------------------------
        // http接続のタイムアウト設定
        // --------------------------------------
        // 接続確立のタイムアウトを設定（単位：ms）
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        // 接続後のタイムアウトを設定（単位：ms）
        HttpConnectionParams.setSoTimeout(httpParams, timeout);

        // RedmineのPostAPI呼び出し用URL作成
        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://").append(targetHost);
        if (targetPort != null && targetPort.length() > 0) {
            url.append(":").append(targetPort);
        }
        url.append(postApi).append("&key=").append(apiKey);

        // Postリクエストオブジェクト
        HttpPost httpPost = new HttpPost(url.toString());

        // RedmineのRestはxml形式のため
        httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");

        // Basic認証用
        if (basicauthId != null && basicauthId.length() > 0
                && basicauthPassword != null && basicauthPassword.length() > 0) {
            httpPost.setHeader(
                    "Authorization",
                    "Basic "
                            + new String(
                                    Base64.encodeBase64((basicauthId + ":" + basicauthPassword)
                                            .getBytes())));
        }
        try {
            // 送信データ(XML)を設定
            HttpEntity entity = new StringEntity(sendData, "UTF-8");
            httpPost.setEntity(entity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        // --------------------------------------
        // POST実行
        // 例外発生時はリトライ回数分リトライを行う
        // HTTPエラーの場合はリトライを行わない
        // --------------------------------------
        int count = 0;
        while (true) {
            try {
                log.info("Http Post Request !");
                httpClient.execute(httpPost, this);
                break;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                log.severe("Postでエラー発生！");
            } catch (IOException e) {
                e.printStackTrace();
                log.severe("Postでエラー発生！");
            }

            count++;

            if (count <= retryCount) {
                log.severe("2秒後にリトライします。");
                // 2秒スリープ
                try {
                    Thread.sleep(2 * 1000);
                } catch (Exception e) {
                }
            } else {
                log.severe("リトライ回数上限に達したためリトライを行いません。");
                break;
            }
        }

        // HttpClientの破棄
        httpClient.getConnectionManager().shutdown();

    }

    /**
     * HttpClient.executeのコールバック
     * 
     * @param response
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Override
    public String handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {

        // レスポンスのステータスコードを判定
        // 正常の場合はステータスコードが200
        switch (response.getStatusLine().getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_CREATED:
        case HttpStatus.SC_ACCEPTED:
            log.info("Httpリクエスト正常終了");
            // レスポンスデータを文字列として取得
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        default:
            log.severe("Httpリクエスト異常終了！リトライは行いません。Httpステータスコード -> ["
                    + response.getStatusLine().getStatusCode() + "]");
            return null;
        }
    }

}
