node default {

	exec { 'apt-update':
		command => '/usr/bin/apt-get update',
	}

	# JAVA
	package { 'openjdk-7-jdk':
		ensure => present,
	}

	file { "/etc/profile.d/set_java_home.sh":
		ensure => present,
		content => "JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64",
		require => Package['openjdk-7-jdk']
	}

	# RabbitMQ
	class { 'rabbitmq':
	  service_manage    => true,
	  port              => '5672',
	}

	exec { 'setup' :
         require => [Class['rabbitmq'], File['/etc/profile.d/set_java_home.sh']],
         command =>  "/vagrant/guests/${endpoint}/setup.sh",
         logoutput => true,
         path => '/usr/bin:/sbin:/bin'}

}