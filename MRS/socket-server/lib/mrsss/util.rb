# coding: UTF-8

module Mrsss
  
  #
  # アプリケーションで共通して使用するユーティリティメソッドを定義します。
  #
  module Util

    # configファイルが保存されているディレクトリパスを取得します。
    # 引数で指定されたパスから "../config" でたどったパスにconfigが保存されていることが前提です。
    # ==== Args
    # _file_path_ :: ディレクトリのパス(String)
    # ==== Return
    # _String_ :: 親ディレクトリのパス
    # ==== Raise
    def self.get_config_path(file_path)
      @config_path ||= File.join(get_parent_path(file_path), "config")
      return @config_path
    end

    # XMLのスキーマファイルが保存されているディレクトリパスを取得します。
    # 引数で指定されたパスから "../schemas" でたどったパスにスキーマファイルが保存されていることが前提です。
    # ==== Args
    # _file_path_ :: ディレクトリのパス(String)
    # ==== Return
    # _String_ :: 親ディレクトリのパス
    # ==== Raise
    def self.get_schemas_path(file_path)
      @schemas_path ||= File.join(get_parent_path(file_path), "schemas")
      return @schemas_path
    end

    # 引数で指定されたパスの親ディレクトリのパスを取得します。
    # ==== Args
    # _file_path_ :: ディレクトリのパス(String)
    # ==== Return
    # _String_ :: 親ディレクトリのパス
    # ==== Raise
    def self.get_parent_path(file_path)
      @parent_path ||= File.dirname(File.dirname(File.expand_path(file_path)))
      return @parent_path
    end

    # configディレクトリのYamlファイルをロードしてHash形式で取得します。
    # ==== Args
    # _config_file_name_ :: configファイル名称(configディレクトリ内のファイルであることが前提)
    # ==== Return
    # _Hash_ :: yamlファイル内容
    # ==== Raise
    def self.get_yaml_config(config_file_name)
        yaml_config ||= YAML.load(File.open(File.join(get_config_path(__FILE__), config_file_name)))
        return yaml_config
    end
    
    # zip圧縮されたデータ(String)を解凍して返却します。
    # ==== Args
    # _str_ :: zip圧縮されたデータ(String)
    # ==== Return
    # _String_ :: 解凍されたデータ
    # ==== Raise
    def self.unzip(str)
      Zip::Archive.open_buffer(str) do |archive|
        archive.each do |entry|
          contents entry.read
        end
      end
      return contents
    end
    
    # gzip圧縮されたデータ(String)を解凍して返却します。
    # ==== Args
    # _str_ :: gzip圧縮されたデータ(String)
    # ==== Return
    # _String_ :: 解凍されたデータ
    # ==== Raise
    def self.ungzip(str)
      sio = StringIO.new(str)
      contents = Zlib::GzipReader.wrap(sio).read
      return contents
    end

    # 引数データをとして保存します。ファイル名は現在日時を+YYYYMMDD_hhmmss+形式で表現したものになります。
    # ==== Args
    # _contents_ :: 保存するデータ(String)
    # _archive_path_ :: データ保存ディレクトリ
    # _ext_ :: ファイルの拡張子
    # ==== Return
    # ==== Raise
    def self.archive(contents, archive_path, ext)
      # 現在日時.<拡張子>の形式でファイル名を作成
      now = Time.now
      file_name = now.strftime("%Y%m%d_%H%M%S") + '.' + ext
      
      # ファイル保存
      File.binwrite(File.join(archive_path, file_name), contents)
    end

    # 引数データをとして保存します。ファイル名は現在日時を+YYYYMMDD_hhmmss+形式で表現したものになります。
    # ==== Args
    # _contents_ :: 保存するデータ(String)
    # _archive_path_ :: データ保存ディレクトリ
    # _thread_id_ :: データ保存ディレクトリ
    # _ext_ :: ファイルの拡張子
    # ==== Return
    # ==== Raise
    def self.archive_ext(contents, archive_path, thread_id, ext)
      # 現在日時.<拡張子>の形式でファイル名を作成
      now = Time.now
      file_name = now.strftime("%Y%m%d_%H%M%S%3N") + '_' + thread_id + '.' + ext
      
      # ファイル保存
      File.binwrite(File.join(archive_path, file_name), contents)
      file_name
    end
    
    # tarパッケージされたデータ(String)を解凍して返却します。tar内の日本語ファイルの文字コードは"Shift-JIS"であることを前提とします。
    # ==== Args
    # _str_ :: tar圧縮されたデータ(String)
    # ==== Return
    # _String_ :: 解凍されたデータ
    # ==== Raise
    def self.untar(str)
      contents = []
      Archive::Tar::Minitar::Reader.open(StringIO.new(str)).each_entry do |entry|
        an_content = {}
        file = sjisfix(entry.read).force_encoding('sjis')
        name = entry.full_name
        an_content['file'] = file
        an_content['name'] = name
        contents.push(an_content)
      end
      contents
    end
    
    # ASCII-8bitと誤認されたShift-JIS文字列を修正します。
    # (参考)http://blog.livedoor.jp/dormolin/archives/52016834.html
    # ==== Args
    # _str_ :: ASCII-8bitと誤認されたShift-JIS文字列
    # ==== Return
    # _String_ :: 修正したデータ
    # ==== Raise
    def self.sjisfix(str)
      return str.gsub(/([\x83-\xFB])\//n, "\\1\\".force_encoding('ascii-8bit'))
    end

  end
end
