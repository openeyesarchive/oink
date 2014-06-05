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
		content => "JAVA_HOME=$(readlink -f /usr/bin/java | sed \"s:bin/java::\")",
		require => Package['openjdk-7-jdk']
	}

}