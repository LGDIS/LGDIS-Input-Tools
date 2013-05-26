# -*- coding:utf-8 -*-
require "net/smtp"
require "nkf"

module Mrsfm

  module Net

		# Sends email by using SMTP.
		class SmtpClient
			# a string that contains the name or IP address of the computer for SMTP. the default value is localhost.
			attr_accessor :host
			# an integer that contains the port number on the SMTP host. the default value is 25. 
			attr_accessor :port

			# Initializes a new instance of the SmtpClient class that sends email by using the specified SMTP server and port.
			# ==== Args
			# _host_ :: a string that contains the name or IP address of the computer for SMTP.
			# _port_ :: an integer that contains the port number on the SMTP host. the default value is 25. 
			def initialize(host = "localhost", port = 25)
				if port < 0; raise ArgumentOutOfRangeError.new("port cannot be less than zero"); end
				if port > 65535; raise ArgumentOutOfRangeError.new("port cannot be less than zero"); end
				@host = host
				@port = port
			end

			# Creates a message to send to an SMTP server
			# ==== Args
			# _msg_ :: a MailMessage that contains the message to send
			# ==== Return
			# a message string that can be used to send email
			def create_message(msg)
				if msg.nil?; raise NilError.new("msg is nil");end
				if !msg.instance_of?(Mrsfm::Net::MailMessage); raise ArgumentError;end

				mail_message = ""
				mail_message << "From: #{NKF.nkf('-WjM',msg.from.display_name)} <#{msg.from.address}>\n"
				mail_message << "To: "
				msg.to.each_with_index do |mail_address,i|
					if i != 0 then
						mail_message << ";"
					end
					mail_message << NKF.nkf('-WjM',mail_address.display_name)
					mail_message << " <"
					mail_message << mail_address.address
					mail_message << ">"
				end
				mail_message << "\n"

				mail_message << "Subject: #{NKF.nkf('-WjM',msg.subject)}\n"
				mail_message << "Date: #{Time::now.strftime('%a, %d %b %Y %X %z')}\n"
				mail_message << "MIME-Version: 1.0\n"
				mail_message << "Content-Type: text/plain; charset=ISO-2022-JP\n"
				mail_message << "Content-Transfer-Encoding: 7bit\n"
				mail_message << NKF.nkf('-Wjm0',msg.body).force_encoding("UTF-8")
				return mail_message
			end

			# Sends the specified message to an SMTP server.
			# ==== Args
			# _msg_ :: a MailMessage that contains the message to send 
			def send(msg)
				if msg.nil?; raise NilError.new("msg is nil");end
				begin
					mail_from = msg.from
					mail_to = []
					msg.to.each do |mail_address|
						mail_to << mail_address.address 
					end

					mail_message = create_message(msg)

					hostname = system("hostname")
					::Net::SMTP.start(@host,@port,hostname) do |smtp|
						smtp.send_message(mail_message, mail_from, mail_to)
					end
				rescue => exception
					raise SmtpError.new(exception)
				end
			end

		end
  end
end

