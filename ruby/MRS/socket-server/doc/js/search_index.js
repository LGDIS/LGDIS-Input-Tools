var search_data = {"index":{"searchIndex":["mrsss","handler","parsers","jmaxml","ksnxml","parseutil","parser","pdf","redmine","txt","server","util","archive()","convert_point()","convert_point_array()","gen_extension()","gen_fileformat()","get_config_path()","get_jma_schema()","get_jma_xml_parse_rule()","get_ksn_xml_parse_rule()","get_mrsss_config()","get_parent_path()","get_redmine_config()","get_river_schema()","get_schemas_path()","get_yaml_config()","handle()","handle()","handle()","handle()","handle()","handle_data()","handle_request()","intensity_to_f()","is_japanese_datum()","load_log_config()","new()","new()","new()","new()","new()","new()","parser_logger()","perform()","post_issues()","post_uploads()","relay()","server_logger()","sjisfix()","start()","start_mrsss()","to_world_datum()","ungzip()","untar()","unzip()","gemfile","rakefile","mrsss"],"longSearchIndex":["mrsss","mrsss::handler","mrsss::parsers","mrsss::parsers::jmaxml","mrsss::parsers::ksnxml","mrsss::parsers::parseutil","mrsss::parsers::parser","mrsss::parsers::pdf","mrsss::parsers::redmine","mrsss::parsers::txt","mrsss::server","mrsss::util","mrsss::util::archive()","mrsss::parsers::parseutil::convert_point()","mrsss::parsers::parseutil::convert_point_array()","mrsss::handler#gen_extension()","mrsss::handler#gen_fileformat()","mrsss::util::get_config_path()","mrsss::get_jma_schema()","mrsss::get_jma_xml_parse_rule()","mrsss::get_ksn_xml_parse_rule()","mrsss::get_mrsss_config()","mrsss::util::get_parent_path()","mrsss::get_redmine_config()","mrsss::get_river_schema()","mrsss::util::get_schemas_path()","mrsss::util::get_yaml_config()","mrsss::handler#handle()","mrsss::parsers::jmaxml#handle()","mrsss::parsers::ksnxml#handle()","mrsss::parsers::pdf#handle()","mrsss::parsers::txt#handle()","mrsss::server#handle_data()","mrsss::server#handle_request()","mrsss::parsers::parseutil::intensity_to_f()","mrsss::parsers::parseutil::is_japanese_datum()","mrsss::load_log_config()","mrsss::handler::new()","mrsss::parsers::jmaxml::new()","mrsss::parsers::ksnxml::new()","mrsss::parsers::pdf::new()","mrsss::parsers::txt::new()","mrsss::server::new()","mrsss::parser_logger()","mrsss::parsers::parser::perform()","mrsss::parsers::redmine::post_issues()","mrsss::parsers::redmine::post_uploads()","mrsss::handler#relay()","mrsss::server_logger()","mrsss::util::sjisfix()","mrsss::server#start()","mrsss::start_mrsss()","mrsss::parsers::parseutil::to_world_datum()","mrsss::util::ungzip()","mrsss::util::untar()","mrsss::util::unzip()","","",""],"info":[["Mrsss","","Mrsss.html","","<p>Mrsssアプリケーションのベースとなるモジュールです。各種共通メソッドとアプリケーション開始メソッドを保持します。\n"],["Mrsss::Handler","","Mrsss/Handler.html","","<p>外部入力先より受信したデータを処理します。\n"],["Mrsss::Parsers","","Mrsss/Parsers.html","",""],["Mrsss::Parsers::JmaXml","","Mrsss/Parsers/JmaXml.html","","<p>JMAから受信したXMLデータを解析しRedmineへ登録するための処理を行うクラスです。\n"],["Mrsss::Parsers::KsnXml","","Mrsss/Parsers/KsnXml.html","","<p>河川から受信したXMLデータを解析しRedmineへ登録するための処理を行うクラスです。\n"],["Mrsss::Parsers::ParseUtil","","Mrsss/Parsers/ParseUtil.html","","<p>XML解析時に使用するユーティリティメソッドを定義します。\n"],["Mrsss::Parsers::Parser","","Mrsss/Parsers/Parser.html","","<p>MrsssアプリケーションにおいてResqueに登録されたデータを監視するワーカープロセスです。\n"],["Mrsss::Parsers::Pdf","","Mrsss/Parsers/Pdf.html","","<p>JMAから受信したPDFファイルをRedmineへ登録するための処理を行うクラスです。\n"],["Mrsss::Parsers::Redmine","","Mrsss/Parsers/Redmine.html","","<p>RedmineへRest要求する際に使用するモジュールです。\n"],["Mrsss::Parsers::Txt","","Mrsss/Parsers/Txt.html","","<p>J-Alertから受信したTxtファイルをRedmineへ登録するための処理を行うクラスです。\n"],["Mrsss::Server","","Mrsss/Server.html","","<p>JMAソケット手順に従ってデータを受信するサーバクラスです。1ポートに対して当クラスのインスタンスを1つ割り当ててください。\n"],["Mrsss::Util","","Mrsss/Util.html","","<p>アプリケーションで共通して使用するユーティリティメソッドを定義します。\n"],["archive","Mrsss::Util","Mrsss/Util.html#method-c-archive","(contents, archive_path, ext)","<p>引数データをとして保存します。ファイル名は現在日時を<code>YYYYMMDD_hhmmss</code>形式で表現したものになります。\n<p>Args\n<p>contents  &mdash; 保存するデータ(String)\n"],["convert_point","Mrsss::Parsers::ParseUtil","Mrsss/Parsers/ParseUtil.html#method-c-convert_point","(str)","<p>point情報をRest用文字列に変換します。緯度と経度の情報のみ使用し、深さの値は捨てます。ex.<code>+21.2+135.5/ -&gt;\n(135.5,21.2)</code>\n<p>Args\n<p>str  &mdash; 緯度経度情報 <code>+21.2</code> …\n"],["convert_point_array","Mrsss::Parsers::ParseUtil","Mrsss/Parsers/ParseUtil.html#method-c-convert_point_array","(str)","<p>point情報の配列表現をRest用文字列に変換します。 ex.<code>+35+135/+36+136/ -&gt;\n((+135,+35),(+136,+36))</code>\n<p>Args\n<p>str  &mdash; 緯度経度情報の配列表現 <code>+35+135/+36+136/</code> …\n"],["gen_extension","Mrsss::Handler","Mrsss/Handler.html#method-i-gen_extension","(message)","<p>拡張子を作成\n<p>Args\n<p>message  &mdash; 受信データ(Message)\n"],["gen_fileformat","Mrsss::Handler","Mrsss/Handler.html#method-i-gen_fileformat","(message)","<p>ファイルフォーマットを作成\n<p>Args\n<p>message  &mdash; 受信データ(Message)\n"],["get_config_path","Mrsss::Util","Mrsss/Util.html#method-c-get_config_path","(file_path)","<p>configファイルが保存されているディレクトリパスを取得します。 引数で指定されたパスから “../config”\nでたどったパスにconfigが保存されていることが前提です。\n<p>Args …\n"],["get_jma_schema","Mrsss","Mrsss.html#method-c-get_jma_schema","()","<p>Jmaから受信したXMLのスキーマ定義をロードして取得します。\n<p>Args\n<p>Return\n"],["get_jma_xml_parse_rule","Mrsss","Mrsss.html#method-c-get_jma_xml_parse_rule","()","<p>JMAから受信したXMLファイル用の解析ルール設定を取得します。\n<p>Args\n<p>Return\n"],["get_ksn_xml_parse_rule","Mrsss","Mrsss.html#method-c-get_ksn_xml_parse_rule","()","<p>河川から受信したXMLファイル用の解析ルール設定を取得します。\n<p>Args\n<p>Return\n"],["get_mrsss_config","Mrsss","Mrsss.html#method-c-get_mrsss_config","()","<p>Mrsssアプリケーション用の各種設定を取得します。\n<p>Args\n<p>Return\n"],["get_parent_path","Mrsss::Util","Mrsss/Util.html#method-c-get_parent_path","(file_path)","<p>引数で指定されたパスの親ディレクトリのパスを取得します。\n<p>Args\n<p>file_path  &mdash; ディレクトリのパス(String)\n"],["get_redmine_config","Mrsss","Mrsss.html#method-c-get_redmine_config","()","<p>RedmineとのRest通信用設定をロードして取得します。\n<p>Args\n<p>Return\n"],["get_river_schema","Mrsss","Mrsss.html#method-c-get_river_schema","()","<p>河川情報から受信したXMLのスキーマ定義をロードして取得します。\n<p>Args\n<p>Return\n"],["get_schemas_path","Mrsss::Util","Mrsss/Util.html#method-c-get_schemas_path","(file_path)","<p>XMLのスキーマファイルが保存されているディレクトリパスを取得します。 引数で指定されたパスから “../schemas”\nでたどったパスにスキーマファイルが保存されていることが前提です。 …\n"],["get_yaml_config","Mrsss::Util","Mrsss/Util.html#method-c-get_yaml_config","(config_file_name)","<p>configディレクトリのYamlファイルをロードしてHash形式で取得します。\n<p>Args\n<p>config_file_name  &mdash; configファイル名称(configディレクトリ内のファイルであることが前提) …\n"],["handle","Mrsss::Handler","Mrsss/Handler.html#method-i-handle","(message)","<p>受信データ処理を行います。データをヘッダ部とボディ部に分割しヘッダ部を解析します。\n<p>Args\n<p>message  &mdash; 受信データ\n"],["handle","Mrsss::Parsers::JmaXml","Mrsss/Parsers/JmaXml.html#method-i-handle","(contents)","<p>受信データの解析、Redmineへの送信処理を行います。\n<p>Args\n<p>contents  &mdash; 受信データ\n"],["handle","Mrsss::Parsers::KsnXml","Mrsss/Parsers/KsnXml.html#method-i-handle","(contents)","<p>受信データの解析、Redmineへの送信処理を行います。\n<p>Args\n<p>contents  &mdash; 受信データ\n"],["handle","Mrsss::Parsers::Pdf","Mrsss/Parsers/Pdf.html#method-i-handle","(contents)","<p>PDFファイルのRedmineへの送信処理を行います。\n<p>Args\n<p>contents  &mdash; PDFファイルデータ\n"],["handle","Mrsss::Parsers::Txt","Mrsss/Parsers/Txt.html#method-i-handle","(contents)","<p>PDFファイルのRedmineへの送信処理を行います。\n<p>Args\n<p>contents  &mdash; Txtファイル名とファイル内容を保持したHash\n"],["handle_data","Mrsss::Server","Mrsss/Server.html#method-i-handle_data","(data, session)","<p>受信したデータを解析します。\n<p>Args\n<p>data  &mdash; ソケットから受信したデータ\n"],["handle_request","Mrsss::Server","Mrsss/Server.html#method-i-handle_request","(session)","<p>接続が確立したソケットに対してデータ受信処理を行います。\n<p>Args\n<p>session  &mdash; クライアントと接続が確立したTCPSocketインスタンス\n"],["intensity_to_f","Mrsss::Parsers::ParseUtil","Mrsss/Parsers/ParseUtil.html#method-c-intensity_to_f","(str)","<p>震度を表す文字列を数値にして返却します。 ex1. “5+” -&gt; 5.75 ex.2 “5-” -&gt; 5.25\n<p>Args\n<p>str  &mdash; 震度を表す文字列\n"],["is_japanese_datum","Mrsss::Parsers::ParseUtil","Mrsss/Parsers/ParseUtil.html#method-c-is_japanese_datum","(str)","<p>引数の文字列(緯度)が日本測地系かを判定します。\n<p>Args\n<p>str  &mdash; 緯度経度情報\n"],["load_log_config","Mrsss","Mrsss.html#method-c-load_log_config","()","<p>ロガーインスタンス用Log4rインスタンスを作成します。\n<p>Args\n<p>Return\n"],["new","Mrsss::Handler","Mrsss/Handler.html#method-c-new","(channel_id, archive_path, mode, need_checksum, use_queue)","<p>初期化処理です。\n<p>Args\n<p>channel_id  &mdash; データの入力元を表す識別子\n"],["new","Mrsss::Parsers::JmaXml","Mrsss/Parsers/JmaXml.html#method-c-new","(mode, channel_id)","<p>初期化処理を行います。\n<p>Args\n<p>mode  &mdash; 動作モード (0:通常,1:訓練,2:試験)\n"],["new","Mrsss::Parsers::KsnXml","Mrsss/Parsers/KsnXml.html#method-c-new","(mode, channel_id)","<p>初期化処理を行います。\n<p>Args\n<p>mode  &mdash; 動作モード(0:通常,1:訓練,2:試験)\n"],["new","Mrsss::Parsers::Pdf","Mrsss/Parsers/Pdf.html#method-c-new","(mode, channel_id)","<p>初期化処理を行います。\n<p>Args\n<p>mode  &mdash; 動作モード (0:通常, 1:訓練, 2:試験)\n"],["new","Mrsss::Parsers::Txt","Mrsss/Parsers/Txt.html#method-c-new","(mode, channel_id)","<p>初期化処理\n<p>Args\n<p>mode  &mdash; 動作モード (0:通常, 1:訓練, 2:試験)\n"],["new","Mrsss::Server","Mrsss/Server.html#method-c-new","(channel_id, port, archive_path, mode, need_checksum, use_queue)","<p>初期化処理です。\n<p>Args\n<p>channel_id  &mdash; データの入力元を表す識別子\n"],["parser_logger","Mrsss","Mrsss.html#method-c-parser_logger","()","<p>Mrsssアプリケーションのパーサ機能用ロガーインスタンスを取得します。\n<p>Args\n<p>Return\n"],["perform","Mrsss::Parsers::Parser","Mrsss/Parsers/Parser.html#method-c-perform","(contents, mode, channel_id, file_format)","<p>ワーカー処理開始用メソッドです。\n<p>Args\n<p>contents  &mdash; 本文\n"],["post_issues","Mrsss::Parsers::Redmine","Mrsss/Parsers/Redmine.html#method-c-post_issues","(data)","<p>Redmineへissues発行依頼を行います。\n<p>Args\n<p>data  &mdash; 要求データ(JSON形式)\n"],["post_uploads","Mrsss::Parsers::Redmine","Mrsss/Parsers/Redmine.html#method-c-post_uploads","(data)","<p>Redmineへupload依頼を行います。\n<p>Args\n<p>data  &mdash; 要求データ(ファイルデータ)\n"],["relay","Mrsss::Handler","Mrsss/Handler.html#method-i-relay","(contents, fileformat)","<p>後続処理へ依頼\n<p>Args\n<p>contents  &mdash; 受信データ内容\n"],["server_logger","Mrsss","Mrsss.html#method-c-server_logger","()","<p>Mrsssアプリケーションのサーバ機能用ロガーインスタンスを取得します。\n<p>Args\n<p>Return\n"],["sjisfix","Mrsss::Util","Mrsss/Util.html#method-c-sjisfix","(str)","<p>ASCII-8bitと誤認されたShift-JIS文字列を修正します。\n(参考)blog.livedoor.jp/dormolin/archives/52016834.html\n<p>Args\n<p>str  &mdash; ASCII-8bitと誤認されたShift-JIS文字列 …\n"],["start","Mrsss::Server","Mrsss/Server.html#method-i-start","()","<p>サーバの受信待ち処理を開始します。サーバソケットを許可モードでオープンしクライアントからの接続を待ちます。\n<p>Args\n<p>Return\n"],["start_mrsss","Mrsss","Mrsss.html#method-c-start_mrsss","()","<p>Mrsssアプリケーションのサーバ機能を開始します。\n<p>Args\n<p>Return\n"],["to_world_datum","Mrsss::Parsers::ParseUtil","Mrsss/Parsers/ParseUtil.html#method-c-to_world_datum","(str)","<p>日本測地系を世界測地系の値に変換します。\n<p>Args\n<p>str  &mdash; 日本測地系による緯度経度情報の文字列表現\n"],["ungzip","Mrsss::Util","Mrsss/Util.html#method-c-ungzip","(str)","<p>gzip圧縮されたデータ(String)を解凍して返却します。\n<p>Args\n<p>str  &mdash; gzip圧縮されたデータ(String)\n"],["untar","Mrsss::Util","Mrsss/Util.html#method-c-untar","(str)","<p>tarパッケージされたデータ(String)を解凍して返却します。tar内の日本語ファイルの文字コードは“Shift-JIS”であることを前提とします。\n<p>Args\n<p>str  &mdash; tar圧縮されたデータ( …\n"],["unzip","Mrsss::Util","Mrsss/Util.html#method-c-unzip","(str)","<p>zip圧縮されたデータ(String)を解凍して返却します。\n<p>Args\n<p>str  &mdash; zip圧縮されたデータ(String)\n"],["Gemfile","","Gemfile.html","","<p>source ‘rubygems.org’\n<p>gem ‘daemons’ gem ‘nokogiri’ gem ‘resque’ gem ‘rest-client’ gem ‘log4r’ gem\n‘activesupport’ …\n"],["Rakefile","","Rakefile.html","","<p>require “resque/tasks” require “log4r” require “log4r/yamlconfigurator” …\n"],["mrsss","","bin/mrsss.html","","<p># get base directory  base_dir =\nFile.dirname(File.dirname(File.expand_path(__FILE__)))\n<p># load libraries …\n"]]}}