require 'buildr/git_auto_version'
require 'buildr/gwt'

GWT_DEPS = [:gwt_websockets, :gwt_user]

desc 'A simple application demonstrating the use of the gwt-websockets library'
define 'gwt-websockets-example' do
  project.group = 'org.realityforge.gwt.websockets.example'

  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  compile.with :javax_annotation, :javax_javaee, GWT_DEPS

  gwt_dir = gwt(['org.realityforge.gwt.websockets.example.Example'],
                :java_args => ['-Xms512M', '-Xmx1024M'],
                :draft_compile => (ENV['FAST_GWT'] == 'true'))

  package(:war)

  clean { rm_rf "#{File.dirname(__FILE__)}/artifacts" }

  iml.add_gwt_facet({'org.realityforge.gwt.websockets.example.Example' => true},
                    :settings => {:compilerMaxHeapSize => '1024'},
                    :gwt_dev_artifact => :gwt_dev)

  # Hacke to remove GWT from path
  webroots = {}
  webroots[_(:source, :main, :webapp)] = '/' if File.exist?(_(:source, :main, :webapp))
  webroots[_(:source, :main, :webapp_local)] = '/'
  assets.paths.each { |path| webroots[path.to_s] = '/' if path.to_s != gwt_dir.to_s }
  iml.add_web_facet(:webroots => webroots)

  iml.add_jruby_facet

  ipr.add_gwt_configuration(project, :vm_parameters => '-Xmx3G', :shell_parameters => '-port 8888', :launch_page => 'http://127.0.0.1:8080/gwt-websockets-example')

  ipr.add_exploded_war_artifact(project,
                                :build_on_make => true,
                                :enable_war => true,
                                :dependencies => [project])

  ipr.extra_modules << '../gwt-websockets/gwt-websockets.iml'
end
