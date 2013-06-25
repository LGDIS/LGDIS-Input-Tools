# coding: UTF-8

require "yaml"
require "log4r"
require "log4r/yamlconfigurator"
require "log4r/outputter/datefileoutputter"
require 'stringio'
require 'zlib'
require 'zipruby'
require 'resque'
require 'active_support/core_ext'
require 'archive/tar/minitar'
require 'json'
require 'nokogiri'
require 'rest_client'
require 'inifile'

require_relative "mrsss/server"
require_relative "mrsss/message"
require_relative "mrsss/util"
require_relative "mrsss/handler"
require_relative "mrsss/parsers/parser"
require_relative "mrsss/parsers/jma_xml"
require_relative "mrsss/parsers/ksn_xml"
require_relative "mrsss/parsers/parse_util"
require_relative "mrsss/parsers/pdf"
require_relative "mrsss/parsers/png"
require_relative "mrsss/parsers/txt"
require_relative "mrsss/parsers/bin"
require_relative "mrsss/parsers/redmine"


#
# Mrsssアプリケーションのベースとなるモジュールです。各種共通メソッドとアプリケーション開始メソッドを保持します。
#
module Mrsss
  
  # Mrsssアプリケーションのサーバ機能用ロガーインスタンスを取得します。
  # ==== Args
  # ==== Return
  # _Log4r_ :: 
  # ==== Raise
  def self.server_logger
    @server_logger ||= Log4r::Logger['Server']
    raise RuntimeError "log4rの設定が間違っているようです。確認してください。" if @server_logger.nil?
    return @server_logger
  end

  # Mrsssアプリケーションのパーサ機能用ロガーインスタンスを取得します。
  # ==== Args
  # ==== Return
  # _Log4r_ :: 
  # ==== Raise
  def self.parser_logger
    @parser_logger ||= Log4r::Logger['Parser']
    raise RuntimeError "log4rの設定が間違っているようです。確認してください。" if @parser_logger.nil?
    return @parser_logger
  end

  # Mrsssアプリケーション用の各種設定を取得します。
  # ==== Args
  # ==== Return
  # _Hash_ :: アプリケーション設定を保持します。
  # ==== Raise
  def self.get_mrsss_config
    @mrsss_config ||= Util.get_yaml_config("mrsss_config.yml")
    return @mrsss_config
  end

  # JMAから受信したXMLファイル用の解析ルール設定を取得します。
  # ==== Args
  # ==== Return
  # _Hash_ :: JmaのXML解析ルールを保持します。
  # ==== Raise
  def self.get_jma_xml_parse_rule
    @jma_xml_parse_rule ||= YAML.load(File.open(File.join(Util.get_config_path(__FILE__), "jma_xml_parse_rule.yml")))
    return @jma_xml_parse_rule
  end

  # 河川から受信したXMLファイル用の解析ルール設定を取得します。
  # ==== Args
  # ==== Return
  # _Hash_ :: 河川のXML解析ルールを保持します。
  # ==== Raise
  def self.get_ksn_xml_parse_rule
    @ksn_xml_parse_rule ||= YAML.load(File.open(File.join(Util.get_config_path(__FILE__), "ksn_xml_parse_rule.yml")))
    return @ksn_xml_parse_rule
  end

  # RedmineとのRest通信用設定をロードして取得します。
  # ==== Args
  # ==== Return
  # _Hash_ :: RedmineとのRest通信用設定を保持します。
  # ==== Raise
  def self.get_redmine_config
    @redmine_config ||= Util::get_yaml_config("redmine.yml")
    return @redmine_config
  end
  
  # Jmaから受信したXMLのスキーマ定義をロードして取得します。
  # ==== Args
  # ==== Return
  # _Nokogiri_ :: 
  # ==== Raise
  def self.get_jma_schema
    Dir.chdir(Util.get_schemas_path(__FILE__))
    @jma_schema ||= Nokogiri::XML::Schema(File.read("jmx.xsd"))
  end

  # 河川情報から受信したXMLのスキーマ定義をロードして取得します。
  # ==== Args
  # ==== Return
  # _Nokogiri_ :: 
  # ==== Raise
  def self.get_river_schema
    Dir.chdir(Util.get_schemas_path(__FILE__))
    @jma_schema ||= Nokogiri::XML::Schema(File.read("river.xsd"))
  end

  # ロガーインスタンス用Log4rインスタンスを作成します。
  # ==== Args
  # ==== Return
  # _Log4r_ :: 
  # ==== Raise
  def self.load_log_config
    if Log4r::Logger["Server"].nil?
      Log4r::YamlConfigurator.load_yaml_file(File.join(Util.get_config_path(__FILE__), "log4r.yml"))
    end
  end
  
  # Mrsssアプリケーションのサーバ機能を開始します。
  # ==== Args
  # ==== Return
  # ==== Raise
  def self.start_mrsss
    begin
      # アプリケーションに必要な設定をロード
      load_log_config
      config = get_mrsss_config
      
      # 設定されたchannels分スレッドを起動
      threads = []
      config['channels'].each do |channel_name, entry|
        thread = Thread.new do
          server = Server.new(channel_name, entry['channel_id'], entry['port'], entry['archive_path'], entry['mode'], config['need_checksum'], config['use_queue'])
          server.start
        end
        threads.push(thread)
        sleep 1
       end

    # Rubyはメインスレッドが停止するとサブスレッドも停止してしまうため
    # メインスレッドが停止しないようThread.joinメソッドを発行
    ensure
      if !threads.to_s.empty?
        threads.each do |t|
          begin
            t.join
          end
        end
      end
    end
  end

end # Mrsss
