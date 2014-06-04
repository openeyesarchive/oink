class { '::rabbitmq':
  service_manage    => true,
  port              => '5672',
}
  
# rabbitmq_user { 'oink':
#   admin    => true,
#   password => 'Test1571',
# }

# rabbitmq_user_permissions { 'oink@/':
#   configure_permission => '.*',
#   read_permission      => '.*',
#   write_permission     => '.*',
# }

# rabbitmq_plugin {'rabbitmq_management':
#   ensure => present,
# }
