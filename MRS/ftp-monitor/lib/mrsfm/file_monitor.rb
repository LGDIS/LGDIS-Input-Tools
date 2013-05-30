# -*- coding:utf-8 -*-
require "fam"
require "fileutils"
require "rubygems/package"
require "zlib"
require_relative "queue_client"

module Mrsfm

  # Monitors uploaded files via ftp.
  class FileMonitor

    # Initializes a default upload directory.
    # ==== Args
    # _thread_id_ :: id to identify the thread 
    def initialize(thread_id, config)
      thread_config = config["threads"][thread_id]

      @thread_id = thread_id
      @type_of_mode = config["mode"]
      @queue_client_class = config["queue_client"]
      @monitor_dir = thread_config["monitor_path"]
      @archive_path = thread_config["archive_path"]
      @sender = thread_config["source_id"]
      @filters = thread_config["filters"]
      @format_header = config["format_header"]
    end

    # Start monitoring files.
    def start(loop_infinite = true)

    begin
      fam = Fam::Connection.new
      dir_request = fam.monitor_dir(@monitor_dir)

      Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(@thread_id) + "Start monitoring.")
      Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(@thread_id) + "monitoring directory:" + @monitor_dir)

      # Runs event loop.
      # If files are created and fam events pending exists, process the files.
      #while true
      begin
        # to avoid heavy load on CPU
        sleep(0.1)

        if fam.pending?
          ev = fam.next_event
          case ev.code
            when Fam::Event::CREATED 
              Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(@thread_id) + "File created: " + ev.file)

              # check if the file name matches the filter in the configuration yaml
              if !match_file_name_filter?(ev.file)
                Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(@thread_id) + "File format did not match: " + ev.file)
                next
              end

              file_path = File.join(@monitor_dir, ev.file)

              # check if the file exists
              if !File.exists?(file_path)
                Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(@thread_id) + "File might have been removed.")
                next
              end

              # send data
              success = false
              File.open(file_path, "r") do |io|
                extention = File.extname(file_path)
                format = get_file_format(extention)

                if extention == ".gz"
                  gzip_reader = Zlib::GzipReader.new(io)

                  if File.extname(file_path.sub(".gz","")) == ".tar"
                    Gem::Package::TarReader.new(gzip_reader).each do |entry|
                      data = entry.read
                      # reapply the format
                      extention = File.extname(entry.full_name)
                      format = get_file_format(extention)
                      success = send_message(data, format)
                    end
                  else
                    data = gzip_reader.read
                    extention = File.extname(gzip_reader.path)
                    format = get_file_format(extention)
                    success = send_message(data, format)
                  end
                elsif File.extname(file_path) == ".tar"
                  Gem::Package::TarReader.new(io).each do |entry|
                    data = entry.read
                    extention = File.extname(entry.full_name)
                    format = get_file_format(extention)
                    success = send_message(data, format)
                  end
                else
                  data = io.read
                  success = send_message(data, format)
                end
              end
              Mrsfm.logger.info("result of sending data to the queue: " + success.to_s)

              # archive the file
              if success && File.exists?(file_path)
                FileUtils.mv(file_path,@archive_path)

                archive_log_message = ""
                archive_log_message << MrsfmUtil.get_bracketed_string(@thread_id)
                archive_log_message << "Archived:"
                archive_log_message << file_path
                archive_log_message << " => "
                archive_log_message << @archive_path
                Mrsfm.logger.info(archive_log_message)
              end

            when Fam::Event::DELETED 
              # this case is written only for debugging purposes
              Mrsfm.logger.debug(MrsfmUtil.get_bracketed_string(@thread_id) + "File deleted: " + ev.file)
            when Fam::Event::CHANGED 
              # this case is written only for debugging purposes
              Mrsfm.logger.debug(MrsfmUtil.get_bracketed_string(@thread_id) + "File modified: " + ev.file)
            end
          end
        end while loop_infinite
      rescue => exception
        Mrsfm.logger.fatal(exception)	
        Mrsfm.send_error(exception)
      end
      Mrsfm.logger.info( MrsfmUtil.get_bracketed_string(@thread_id) + "Stop monitoring.")
    end

private
    def send_message(data, format)
      client_class = Object.const_get("Mrsfm").const_get(@queue_client_class)
      client = client_class.new(@type_of_mode, @sender, format)
      success =  client.enqueue?(data)
      return success
    end

    # Matches a file name with filters
    # Assume filters are defined in the configuration yaml.
    # ==== Args
    # _file_name_ :: a file name to be matched
    # ==== Return
    # returns true if the file name matches one of the filters, else returns false
    def match_file_name_filter?(filename)
      @filters.each do |filter|
        if File.fnmatch(filter, filename)
          return true
        end
      end
    return false
    end

    # Gets the file format
    # ==== Args
    # _file_path_ :: a file extention to determine the file format
    # ==== Return
    # returns file format
    def get_file_format(extention)
      format = @format_header[extention]
      format = format.nil? ? "XXX" : format;
    end
  end
end
