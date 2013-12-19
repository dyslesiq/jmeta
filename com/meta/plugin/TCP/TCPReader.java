package com.meta.plugin.TCP;

import java.io.Serializable;
import java.util.HashMap;

import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Singleton who is listened to the request from DHT
 * @author Thomas LAVOCAT
 *
 */
public class TCPReader implements MessageHandler {

	private HashMap<String, AMPCommand> mapCommand = null;
	private static TCPReader instance = new TCPReader();
	
	private TCPReader() {
		mapCommand = new HashMap<String, AMPCommand>();
	}
	
	public static TCPReader getInstance() {
		return instance;
	}
	
	public void registerCommand(AMPCommand command){
		mapCommand.put(command.getCommandName(), command);
	}
	
	@Override
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		if(content instanceof AMPCommand){
			SerializableCommandParameters receivedCommand 
									= (SerializableCommandParameters) content;
			AMPCommand command = mapCommand.get(receivedCommand.getName());
			if(command != null){
				command.setParameters(receivedCommand);
				command.setCallingNode(from);
				command.execute();			
				if(command != null){
					command.setParameters(receivedCommand);
					command.setCallingNode(from);
					command.execute();
				}
			}
		}
	}

	@Override
	public Serializable onIncomingRequest(Node from, String tag,
			Serializable content) {		
		return null;
	}
	
}
