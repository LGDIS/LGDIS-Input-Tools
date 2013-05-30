# -*- coding:utf-8 -*-
module Mrsfm

  module Net

    # Represents an email message
    class MailMessage
      # to add a recipient, create a MailAddress for the recipient's address and add to "to" accessor.
      attr_accessor :to
      # to add a cc recipient, create a MailAddress for the recipient's address and add to cc accessor.
      attr_accessor :cc
      # to add a bcc recipient, create a MailAddress for the recipient's address and add to bcc accessor.
      attr_accessor :bcc
      # a MailAddress that contains the from address information.
      attr_accessor :from
      # a string that contains the subject content.
      attr_accessor :subject
      # a string value that contains the body text.
      attr_accessor :body

      # Initializes a new instance of the MailMessage class using the specified from and to address, subject, and body message. 
      # ==== Args
      # _from_ :: a MailAddress that contains the address of the sender of the email message.
      # _to_ :: a MailAddress that contains the address of the recipient of the email message.
      # _subject_ :: a string that contains the subject text.
      # _body_ :: a string that contains the message body.
      def initialize(from, to, subject = "", body = "")
        if from.nil?; raise NilError.new("from is null."); end
        if to.nil?; raise NilError.new("to is null."); end
        if !to.kind_of?(Array); raise ArgumentError.new("'to' address must be an array"); end
        if to == []; raise ArgumentError.new("'to' address array must not be empty"); end
        @from = from
        @to = to
        @cc = []
        @bcc = []
        @subject = subject
        @body = body
      end
    end
  end
end
