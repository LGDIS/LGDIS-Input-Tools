//
//  PdfDataHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.rest.IssuesPostController;
import jp.lg.ishinomaki.city.mrs.rest.PostController;
import jp.lg.ishinomaki.city.mrs.rest.UploadsPostController;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * PDF形式のデータをハンドリングするクラス
 * 
 */
public class PdfDataHandler implements PickupDataHandler {

    // ログ用
    private static Logger log = Logger.getLogger(PdfDataHandler.class
            .getSimpleName());

    /**
     * 動作モード 0:通常 1:訓練 2:試験
     */
    private int mode = 0;

    /**
     * コンストラクタ
     */
    public PdfDataHandler() {
        this(0);
    }

    /**
     * コンストラクタ
     * 
     * @param mode
     *            動作モード
     */
    public PdfDataHandler(int mode) {
        this.mode = mode;
    }

    /**
     * データハンドリングメソッド
     */
    @Override
    public void handle(byte[] data) {

        // ----------------------------------------------------------------
        // このメソッドでは最初に uploads apiを実行しその戻りのtoken情報を用いて
        // issues apiを実行する
        // ----------------------------------------------------------------
        // Uploads実行
        PostController uploads = new UploadsPostController();
        String response = uploads.post(data);

        // 戻りデータがない場合は処理しない
        if (response == null || response.length() == 0) {
            return;
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
            e.printStackTrace();
            return;
        }

        // トークンがない場合は処理終了
        if (token == null || token.length() == 0) {
            log.warning("tokenがないため処理中断");
            return;
        }

        log.finest("返却されたtoken -> " + token);

        // トークンがある場合はIssues作成用XML作成
        String xml = createIssuesXmlAsString(token);
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
    private String createIssuesXmlAsString(String token) {

        // XML作成のための設定を取得
        Map<String, String> map = ParserConfig.getInstance()
                .getPdfAttachmentStatics();
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
        Element upload = uploads.addElement("upload");

        // upload要素にトークン等各種設定を追加
        upload.addElement("token").addText(token);
        // filename
        String filename = map.get(ParserConfig.FILENAME);
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

        return doc.asXML();
    }

}
