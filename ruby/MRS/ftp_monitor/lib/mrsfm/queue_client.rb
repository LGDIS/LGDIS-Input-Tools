# -*- coding:utf-8 -*-
require "socket"

module Mrsfm
  # Enqueues a packet to the queue server.
  # This is a super class of the client module which sends data to any queue server.
  # Inherit this class and set the class name on the configuration yaml.
  # [require_relative 'inherited class name'] must be added at the end of this script.
  class QueueClient

    # Initializes default values of socket header
    # ==== Args
    # _type_of_mode_ :: lgdis system mode
    # _sender_ :: sender of uploaded files
    # _file_format_ :: file format
    def initialize(type_of_mode, sender, file_format)
      @type_of_mode = type_of_mode
      @sender = sender
      @file_format = file_format
    end

    # Prepends header to send data.
    # ==== Args
    # _data_ :: data
    # ==== Return
    # packet to send to the queue server.
    def add_header(data)
      if data.nil?; raise NilError.new("data is nil");end
        packet = ""
        packet << @type_of_mode
        packet << @sender
        packet << @file_format
        packet << data

        return packet
      end
    end
end

require_relative 'queue_client_socket'
require_relative 'queue_client_resque'
