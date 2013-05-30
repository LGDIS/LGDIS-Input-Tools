# -*- coding:utf-8 -*-

module Mrsfm
  # Enqueues a packet to the queue server.
  class QueueClientResque < QueueClient

    # Initializes default values of socket header
    def initialize(type_of_mode, sender, file_format)
      super
    end

    # Creates a packet and sends data to the queue server.
    # ==== Args
    # _data_ :: data to enqueue to the queue server.
    # Return
    # returns true if the packet has sent to the server
    def enqueue?(data)
      result = false

      begin
        Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(self.object_id.to_s) + "Send: " + data)
        Resque.enqueue(Mrsss::Parsers::Parser, data, @type_of_mode, @sender, @file_format)
        result = true
      rescue => exception
        result = false
        Mrsfm.logger.error(exception)	
      end
	return result
    end
  end
end
