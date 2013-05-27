# -*- coding:utf-8 -*-

# get base directory 
base_dir = File.dirname(File.dirname(File.expand_path(__FILE__)))

# load libraries path
$LOAD_PATH.unshift File.dirname(__FILE__) + '/../lib'
$LOAD_PATH.unshift '/home/mrsuser/MRS/socket-server/lib'

require "daemons"
require "mrsfm"


pid_dir = File.join(base_dir,"/tmp")
log_dir = File.join(base_dir,"/log")

# setup options
options = {
  :dir_mode => :script,
  :dir => pid_dir,
  :backtrace => true,
  :monitor => true,
  :log_dir => log_dir,
  :log_output => false
}

# start daemon
Daemons.run_proc('file_monitor', options) do
  Mrsfm.start_file_monitor
end

