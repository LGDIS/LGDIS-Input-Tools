# -*- coding:utf-8 -*-
require "socket"

module Mrsfm
  # Enqueues a packet to the queue server.
  class QueueClientSocket < QueueClient
      # Initializes default values of socket header
      def initialize(type_of_mode, sender, file_format)
        super
        @sock_path = Mrsfm.utility.get_yaml_config("socket.yaml")["file_monitor"]["unix_socket_path"]
        if @sock_path.nil?;raise NilError.new("sock_path is nil."); end 
      end

      # Creates a packet and sends data to the queue server.
      # ==== Args
      # _data_ :: data to enqueue to the queue server.
      # Return
      # returns true if the packet has sent to the server
      def enqueue?(data)
        result = false

        begin
          # uses unix domain socket
          sock_path = @sock_path
          enq_sock = UNIXSocket.new(sock_path)
          
          packet = add_header(data)

          Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(self.object_id.to_s) + "Send: " + packet)
          enq_sock.send(packet,0)
          result = true
        rescue => exception
          result = false
          Mrsfm.logger.error(exception) 
        ensure
          if !enq_sock.nil?
            enq_sock.close 
            Mrsfm.logger.info(MrsfmUtil.get_bracketed_string(self.object_id.to_s) + "Socket closed.")
          end
        end

        return result
      end
  end
end
