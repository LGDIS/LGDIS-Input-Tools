# -*- coding:utf-8 -*-
module Mrsfm

  module Net

    # Represents the address of an electronic mail sender or recipient.
    class MailAddress
      # the display name composed from the display name and address information specified when this instance was created.
      attr_reader:display_name
      # the email address specified when this instance was created.
      attr_reader:address

      # Initializes a new instance of the MailAddress class using the specified address and display name.
      # ==== Args
      # _address_ :: a string which contains an email address.
      # _display_name_ :: a string which contains the display name associated with address. This parameter can be nil.
      def initialize(address,display_name = "")
        if address.nil?; raise NilError.new("address is nil."); end 
        if address.empty?; raise ArgumentError.new("address is empty."); end
        if !MailAddress.valid?(address); raise FormatError.new("address format is not valid."); end
          @address = address
          @display_name = display_name
      end

      # Validates an email address.
      # ==== Args
      # _address_ :: a string which contains an email address.
      # ==== Return
      # returns true if the email address is valid.
      def self.valid?(address)
        email_address_regex = /\b[A-Z0-9._%a-z\-]+@(?:[A-Z0-9a-z\-]+\.)+[A-Za-z]{2,4}\z/
        address.match(email_address_regex) ? true : false
      end
    end

  end
end

