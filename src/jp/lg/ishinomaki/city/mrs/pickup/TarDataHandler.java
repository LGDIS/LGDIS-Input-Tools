//
//  TarDataHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.rest.IssuesPostController;
import jp.lg.ishinomaki.city.mrs.rest.PostController;
import jp.lg.ishinomaki.city.mrs.rest.UploadsPostController;
import jp.lg.ishinomaki.city.mrs.utils.ArchiveUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Tarファイルを取り扱うクラス
 * 
 */
public class TarDataHandler implements PickupDataHandler {

    // ログ用
    private static Logger log = Logger.getLogger(TarDataHandler.class
            .getSimpleName());

    /**
     * 動作モード 0:通常 1:訓練 2:試験
     */
    private int mode = 0;

    /**
     * コンストラクタ
     */
    public TarDataHandler() {
        this(0);
    }

    /**
     * コンストラクタ
     * 
     * @param mode
     *            動作モード
     */
    public TarDataHandler(int mode) {
        this.mode = mode;
    }

    /**
     * データハンドリングメソッド
     */
    @Override
    public void handle(byte[] data) {

        // Tarファイルを解凍する
        List<Map<String, Object>> fileMaps = ArchiveUtils.untar(data);
        if (fileMaps == null || fileMaps.size() == 0) {
            // 送信データがないため処理中断
            log.warning("送信データがないため処理を中断します");
            return;
        }

        // Tarファイル解凍後のファイル数分Redmineにupload要求
        for (int i = 0; i < fileMaps.size(); i++) {
            Map<String, Object> fileMap = fileMaps.get(i);
            // ファイルコンテンツ取得
            byte[] contents = (byte[]) fileMap.get("contents");

            // Uploads実行
            PostController uploads = new UploadsPostController();
            String response = uploads.post(contents);

            // 戻りデータがない場合は処理せず次のループへ
            if (response == null || response.length() == 0) {
                log.warning("Redmineへのupload要求の戻りがないため処理なしで次のデータへ");
                continue;
            }

            // 戻りデータログ表示
            log.finest(response);

            // xml解析
            String token = null;
            try {
                // XPathを使用してRedmineから返却されたトークン情報を取得
                Document doc = DocumentHelper.parseText(response);
                token = doc.valueOf("/upload/token/text()");
            } catch (DocumentException e) {
                log.warning("Redmineへのupload要求の戻りXMLを解析中に例外が発生したため処理なしで次のデータへ");
                e.printStackTrace();
                continue;
            }

            // トークンがない場合は処理終了
            if (token == null || token.length() == 0) {
                log.warning("tokenがないため処理なしで次のデータへ");
                continue;
            }

            log.finest("返却されたtoken -> " + token);

            // fileMap変数にtokenを設定
            fileMap.put("token", token);
        }

        // ----------------------------------------------------------------
        // このメソッドでは最初に uploads apiを実行しその戻りのtoken情報を用いて
        // issues apiを実行する
        // ----------------------------------------------------------------

        // トークンがある場合はIssues作成用XML作成
        String xml = createIssuesXmlAsString(fileMaps);
        // Issues作成
        IssuesPostController postController = new IssuesPostController();
        postController.post(xml);

    }

    /**
     * Issues登録用のxml文字列作成
     * 
     * @param token
     * @return
     */
    private String createIssuesXmlAsString(List<Map<String, Object>> fileMaps) {

        // XML作成のための設定を取得
        Map<String, String> map = ParserConfig.getInstance()
                .getTarAttachmentStatics();
        String subject = map.get(ParserConfig.SUBJECT);
        String trackerId = map.get(ParserConfig.TRACKER_ID);
        String projectId = null;
        // 動作モードによりプロジェクトIDを指定
        if (mode == 1) {
            projectId = ParserConfig.getInstance().getTrainingProjectId();
        } else if (mode == 2) {
            projectId = ParserConfig.getInstance().getTestProjectId();
        } else {
            projectId = map.get(ParserConfig.PROJECT_ID);
        }

        Document doc = DocumentHelper.createDocument();
        // ルートは"issue"
        Element issue = doc.addElement("issue");

        // XMLにsubjectを設定
        Element subjectElement = issue.addElement("subject");
        subjectElement.addText(subject);

        // XMLにproject_idを設定
        Element projectElement = issue.addElement("project_id");
        projectElement.addText(projectId);

        // XMLにtracker_idを設定
        Element trackerIdElement = issue.addElement("tracker_id");
        trackerIdElement.addText(trackerId);

        // uploads/upload要素を追加
        Element uploads = issue.addElement("uploads");
        uploads.addAttribute("type", "array");

        // ファイル数分upload要素を追加
        for (Map<String, Object> fileMap : fileMaps) {
            // upload要素を追加
            Element upload = uploads.addElement("upload");

            // upload要素にトークン等各種設定を追加
            String token = (String) fileMap.get("token");
            upload.addElement("token").addText(token);

            // filename
            String filename = (String) fileMap.get("name");
            if (filename != null) {
                upload.addElement("filename").addText(filename);
            }

            // description
            String description = map.get(ParserConfig.DESCRIPTION);
            if (description != null) {
                upload.addElement("description").addText(description);
            }

            // content_type
            String contentType = map.get(ParserConfig.CONTENT_TYPE);
            if (contentType != null) {
                upload.addElement("content_type").addText(contentType);
            }
        }

        return doc.asXML();
    }

}
