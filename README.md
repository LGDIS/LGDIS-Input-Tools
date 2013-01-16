# LGDIS-Input-Tools
=================

LGDIS (Local Government Disaster Infomation System) Input Tools.

## デプロイ環境で修正の必要がある設定ファイルについて

#### receiver.config
外部入力プロセスが使用するプロパティファイルを定義<br>
ファイルパスをデプロイ環境毎に変更する必要がある<br>
テンプレートはreceiver.config.example

#### parser.config
パーサープロセスが使用するプロパティファイルを定義<br>
ファイルパスをデプロイ環境毎に変更する必要がある<br>
テンプレートはparser.config.example

#### threads.properties
デプロイ環境のIPアドレス、ポート番号を定義<br>
テンプレートはthreads.properteis.example<br>

#### redmine.properties
デプロイ環境のIPアドレス、ポート番号、RedmineのAPIキーを定義<br>
テンプレートはredmine.properties.example
