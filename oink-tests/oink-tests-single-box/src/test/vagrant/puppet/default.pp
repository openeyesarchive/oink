node default {

	exec { 'apt-update':
		command => '/usr/bin/apt-get update',
	}

	# RabbitMQ
	class { 'rabbitmq':
	  service_manage    => true,
	  port              => '5672',
	}

	exec { 'setup' :
         require => [Class['rabbitmq']],
         command =>  "/vagrant/guest/setup.sh",
         logoutput => true,
         path => '/usr/bin:/sbin:/bin'}

}