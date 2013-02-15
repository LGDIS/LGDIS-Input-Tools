//
//  TarDataHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.rest.IssuesPostController;
import jp.lg.ishinomaki.city.mrs.rest.UploadsPostController;
import jp.lg.ishinomaki.city.mrs.utils.ArchiveUtils;
import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

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
     * Restメソッドでuploadsを発行するためのインスタンス
     */
    UploadsPostController uploadsController;

    /**
     * Restメソッドでissuesを発行するためのインスタンス
     */
    IssuesPostController issuesController;

    /**
     * 動作モード 0:通常 1:訓練 2:試験
     */
    private int mode = 0;

    /**
     * コンストラクタ.<br>
     * 
     */
    public TarDataHandler() {
        this(0);
    }

    /**
     * コンストラクタ.<br>
     * 
     * @param mode
     *            動作モード 0:通常 1:訓練 2:試験
     */
    public TarDataHandler(int mode) {
        this.mode = mode;
        uploadsController = new UploadsPostController();
        issuesController = new IssuesPostController();
    }

    /**
     * TARデータに対する処理を行います。
     * 
     * @param data
     *            本文データ
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
        List<Map<String, String>> uploadedFiles = new ArrayList<Map<String, String>>();
        for (int i = 0; i < fileMaps.size(); i++) {
            Map<String, Object> fileMap = fileMaps.get(i);
            // ファイルコンテンツ取得
            byte[] contents = (byte[]) fileMap.get("contents");
            if (contents == null || contents.length == 0) {
                continue;
            }

            // Uploads実行
            String response = uploadsController.post(contents);

            // 戻りデータがない場合は処理せず次のループへ
            if (StringUtils.isBlank(response)) {
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
            if (StringUtils.isBlank(token)) {
                log.warning("tokenがないため処理なしで次のデータへ");
                continue;
            }

            log.finest("返却されたtoken -> " + token);

            // Mapに格納
            Map<String, String> uploadedFile = new HashMap<String, String>();
            uploadedFile.put("token", token);
            uploadedFile.put("filename", (String) fileMap.get("name"));
            uploadedFiles.add(uploadedFile);
        }

        if (uploadedFiles.size() == 0) {
            log.warning("ファイルのアップロードが1つも成功しなかったため処理中断");
            return;
        }

        // ----------------------------------------------------------------
        // このメソッドでは最初に uploads apiを実行しその戻りのtoken情報を用いて
        // issues apiを実行する
        // ----------------------------------------------------------------

        // トークンがある場合はIssues作成用XML作成
        String xml = createIssuesXmlAsString(uploadedFiles);
        // Issues作成
        issuesController.post(xml);

    }

    /**
     * Issues登録用のxml文字列作成
     * 
     * @param token uploadsメソッド発行後に取得したトークン番号
     * @return String Redmine送信用のXML文字列を作成
     */
    String createIssuesXmlAsString(List<Map<String, String>> uploadedFiles) {

        // XML作成のための設定を取得
        Map<String, String> map = ParserConfig.getInstance()
                .getTarAttachmentStatics();
        String subject = map.get(ParserConfig.SUBJECT);
        String trackerId = map.get(ParserConfig.TRACKER_ID);
        String description = map.get(ParserConfig.DESCRIPTION);
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
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
        for (Map<String, String> uploadedFile : uploadedFiles) {
            // upload要素を追加
            Element upload = uploads.addElement("upload");

            // upload要素にトークン等各種設定を追加
            String token = (String) uploadedFile.get("token");
            upload.addElement("token").addText(token);

            // filename
            String filename = (String) uploadedFile.get("filename");
            upload.addElement("filename").addText(filename);

            // description
            if (description != null) {
                upload.addElement("description").addText(description);
            }

            // content_type
            if (contentType != null) {
                upload.addElement("content_type").addText(contentType);
            }
        }

        return doc.asXML();
    }

}
