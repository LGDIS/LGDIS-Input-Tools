module Mrsfm
  # Utility methods for Mrsfm module
  class MrsfmUtil

    def initialize(base_directory)
      @base_directory = base_directory
    end

    # Retrieves the directory that contains configuration files 
    # Assumes the relative path of the directory from this module is "../config"
    # ==== Args
    # _file_path_ :: a file path to get the directory path which contains configuration files
    # ==== Return
    # returns the configuration directory
    def get_config_directory()
      config_directory= File.join(@base_directory, "config")
      return config_directory
    end

    # Retrieves yaml configuration hash values
    # ==== Args
    # yaml file name
    # ==== Return
    # configuration hash values
    def get_yaml_config(config_file_name)
      if config_file_name.nil?; raise NilError.new("config_file_name is nil");end
      file = File.join(get_config_directory, config_file_name)
      if !File.exists?(file); raise FileNotFoundError.new("the file does not exist");end

      yaml_config = {}
      File.open(file) do |io|
        yaml_config = YAML.load(io)
      end

      return yaml_config
    end

    # Retrieves a parent directory path
    # ==== Args
    # _file_path_ :: a file path to get a parent path
    # ==== Return
    # returns the parent directory 
    def self.get_parent_directory(file_path)
      if file_path.nil?; raise NilError.new("file_path is nil");end
      if !File.exists?(file_path); raise FileNotFoundError.new("the file does not exist");end

      parent_directory= File.dirname(File.dirname(File.expand_path(file_path)))
      return parent_directory
    end

    # Gets a bracketed string from the specified string
    # ==== Args
    # _string_ :: the specified string
    # ==== Return
    # a bracketed string
    def self.get_bracketed_string(string)
      if string.nil?; raise NilError.new("string is nil");end
        return "[" + string + "] "
    end
  end
end
