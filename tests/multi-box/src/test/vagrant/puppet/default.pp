node default {

	exec { 'apt-update':
		command => '/usr/bin/apt-get update',
	}

	include '::rabbitmq'
	include '::java'
            
    exec { 'setup' :
        require => Service['rabbitmq-server'],
        command =>  "/vagrant/guests/${endpoint}/setup.sh",
        path => '/usr/bin:/sbin:/bin'}
}