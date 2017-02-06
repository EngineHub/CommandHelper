######################################################
#
#   Sample Main file. This file is run when the server
#   starts up, or the plugin is reloaded. It can be used
#   as an opportunity to register events or do other things
#   when the server initially starts. Variables defined here
#   will go out of scope, so you can't assign something and
#   then use it in a command, for instance.
#
######################################################

@server_name = '<Server Name>';

# Echo out a server greeting to the console
console('Welcome to this server, which is running CommandHelper! (Change this message in plugins/CommandHelper/main.ms)');

# Bind an event to the player join, and tell ops hello.
# We send in @server_name, so we can use it inside the event handler
bind(player_join, null, null, @event, @server_name) {
	# If the player is an op, let's send them a welcome message
	if(pisop(@event['player'])) {
		msg('Welcome to '.@server_name.' (Change this message in plugins/CommandHelper/main.ms)');
	}
}
