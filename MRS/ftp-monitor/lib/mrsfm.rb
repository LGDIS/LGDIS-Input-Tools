# -*- coding:utf-8 -*-
require 'resque'
require "yaml"
require "log4r"
require "log4r/yamlconfigurator"
require "log4r/outputter/datefileoutputter"
require "mrsss/parsers/parser"
require_relative "mrsfm/file_monitor"
require_relative "mrsfm/util/mrsfm_util"
require_relative "mrsfm/net/mail_address"
require_relative "mrsfm/net/mail_message"
require_relative "mrsfm/net/smtp_client"

# Main module of Mrsfm
module Mrsfm

  class MrsfmError < RuntimeError; end
  class ArgumentError < MrsfmError; end 
  class NilError < MrsfmError; end
  class FileNotFoundError < MrsfmError; end
  class FormatError < MrsfmError; end
  class ArgumentOutOfRangeError < MrsfmError; end
  class SmtpError < MrsfmError; end

  # Gets mrsfm system logger.
  # ==== Return
  # log4r logger
  def self.logger
    @logger ||= Log4r::Logger["Log4r"] 
    return @logger
  end

  # Gets mrsfm system utility.
  # ==== Return
  # system utility instance
  def self.utility
    @utility ||= MrsfmUtil.new(MrsfmUtil.get_parent_directory(__FILE__))
  end

  # Retrieves the mail configuration hash values
  # ==== Return
  # configuration hash values
  def self.get_mail_config
      @mail_config||= utility.get_yaml_config("mail_config.yml")
      return @mail_config
  end

  # Retrieves the message configuration hash values
  # ==== Return
  # configuration hash values
  def self.get_message_config
      @message_config ||= utility.get_yaml_config("messages.yml")
      return @message_config
  end

  # Sets up the configuration for log output.
  def self.load_log_config
    if Log4r::Logger["log4r"].nil?
      config_directory = utility.get_config_directory
      config_file = File.join(config_directory, "log4r.yml")
      Log4r::YamlConfigurator.load_yaml_file(config_file)
    end
  end

  # Sets up the email configuration.
  def self.load_file_monitor_email_config
    config = get_mail_config["file_monitor"]

    @host = config["host"]
    @port = config["port"].to_i
    from = Mrsfm::Net::MailAddress.new(config["from"]["address"],config["from"]["display_name"])

    to = []
    if !config["to"].to_s.empty?
      config["to"].each do |address|
        to << Mrsfm::Net::MailAddress.new(address["address"],address["display_name"])
      end
    end

    cc = []
    if !config["cc"].to_s.empty?
      config["cc"].each do |address|
        cc << Mrsfm::Net::MailAddress.new(address["address"],address["display_name"])
      end
    end

    bcc = []
    if !config["bcc"].to_s.empty?
      config["bcc"].each do |address|
        bcc << Mrsfm::Net::MailAddress.new(address["address"],address["display_name"])
      end
    end

    @msg = Mrsfm::Net::MailMessage.new(from, to)
    @msg.cc = cc
    @msg.bcc = bcc
    @msg.subject = config["subject"]
    @msg.body = config["body"]
  end

  # Sends an error message by email
  # ==== Args
  # _error_ :: an error message
  def self.send_error(error)
    if @send_error_email
      sc = Mrsfm::Net::SmtpClient.new(@host,@port)
      @msg.body.sub!("$1",error.to_s)

      sc.send(@msg)
    end
  end

  # Sets up configuration files and start file monitor threads. 
  def self.start_file_monitor
    begin
      load_log_config
      if Mrsfm.logger.nil?
        puts "the logger is nil."
        exit
      end
    rescue => exception
      puts "failed to load the logger :" + exception.to_s
    end

    begin
      Mrsfm.logger.info("loading config files.")
      load_file_monitor_email_config
      config = utility.get_yaml_config("file_monitor_config.yml")
      Mrsfm.logger.info("loading config files complete.")

      @send_error_email = config["send_error_email"]
      threads = []
      # create threads from each entry of "threads" in the configuration yaml
      config["threads"].each do |entry|
        thread = Thread.new do
          thread_id = entry[0]
          monitor = FileMonitor.new(thread_id,config)
          monitor.start
        end
        threads.push(thread) 
      end
    rescue => exception
      Mrsfm.logger.fatal(exception)
      send_error(exception)
    ensure
      if !threads.to_s.empty?
        threads.each do |t|
          t.join
        end
      end
    end
  end
end


