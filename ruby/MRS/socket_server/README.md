# Mrsss(mrs srver socket)

JMAソケット通信により受信したデータを解析しLGDISへチケット登録を行うアプリケーションです。


## 依存するライブラリ

* redis
* log4r
* active_support
* nokogiri
* rest-client
* resque
* archive-tar-minitar
* daemons

## ソケット通信コンポーネントの起動コマンド

デーモン起動

    ruby bin/mrsss start

デーモナイズしない場合

    ruby bin/mrsss start -t


## 解析コンポーネントの起動コマンド

バックグラウンドで稼働させる場合

    TERM_CHILD=1 QUEUES=mrsss rake resque:work BACKGROUND=yes

バックグラウンドで稼働させない場合

    TERM_CHILD=1 QUEUES=mrsss rake resque:work

詳細なログをコンソールに出力する場合

    TERM_CHILD=1 QUEUES=mrsss rake resque:work VERBOSE=true

