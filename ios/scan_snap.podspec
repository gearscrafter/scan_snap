#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint scan.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'scan_snap'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'scan_snap/Sources/scan_snap/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '13.0'

  s.swift_version = '5.0'

  s.dependency 'Flutter'

  # Required for Flutter plugins
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'IPHONEOS_DEPLOYMENT_TARGET' => '13.0',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }

  s.test_spec 'Tests' do |test_spec|
    test_spec.source_files = 'Tests/**/*.swift'
    test_spec.frameworks = 'XCTest'
  end
end
