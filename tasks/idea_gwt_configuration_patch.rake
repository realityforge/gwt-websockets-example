raise "Patch applied in this version of Buildr" unless Buildr::VERSION == '1.4.20'

module Buildr
  module IntellijIdea
    class IdeaProject
      def add_gwt_configuration(project, options = {})
        launch_page = options[:launch_page]
        name = options[:name] || (launch_page ? "Run #{launch_page}" : "Run #{project.name} DevMode")
        shell_parameters = options[:shell_parameters]
        vm_parameters = options[:vm_parameters] || '-Xmx512m'
        singleton = options[:singleton].nil? ? true : !!options[:singleton]
        super_dev = options[:super_dev].nil? ? true : !!options[:super_dev]

        start_javascript_debugger = options[:start_javascript_debugger].nil? ? true : !!options[:start_javascript_debugger]

        add_configuration(name, 'GWT.ConfigurationType', 'GWT Configuration', false, :singleton => singleton) do |xml|
          xml.module(:name => project.iml.id)

          xml.option(:name => 'VM_PARAMETERS', :value => vm_parameters)
          xml.option(:name => 'RUN_PAGE', :value => launch_page) if launch_page

          xml.option(:name => 'START_JAVASCRIPT_DEBUGGER', :value => start_javascript_debugger)
          xml.option(:name => 'USE_SUPER_DEV_MODE', :value => super_dev)
          xml.option(:name => 'SHELL_PARAMETERS', :value => shell_parameters) if shell_parameters

          xml.RunnerSettings(:RunnerId => 'Debug') do |xml|
            xml.option(:name => 'DEBUG_PORT', :value => '')
            xml.option(:name => 'TRANSPORT', :value => 0)
            xml.option(:name => 'LOCAL', :value => true)
          end

          xml.RunnerSettings(:RunnerId => 'Run')
          xml.ConfigurationWrapper(:RunnerId => 'Run')
          xml.ConfigurationWrapper(:RunnerId => 'Debug')
          xml.method()
        end
      end

      def add_configuration(name, type, factory_name, default = false, options = {})
        add_to_composite_component(self.configurations) do |xml|
          params = options.dup
          params[:type] = type
          params[:factoryName] = factory_name
          params[:name] = name unless default
          params[:default] = true if default
          xml.configuration(params) do |xml|
            yield xml if block_given?
          end
        end
      end

    end
  end
end