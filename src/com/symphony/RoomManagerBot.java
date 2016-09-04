/*
 *
 *
 * Copyright 2016 Symphony Communication Services, LLC
 *
 * Licensed to Symphony Communication Services, LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.symphony;

import com.symphony.api.pod.model.Stream;
import com.symphony.api.pod.model.User;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.clients.ISymphonyClient;
import com.symphony.clients.SymphonyClient;
import com.symphony.configurations.ConfigurationProvider;
import com.symphony.configurations.IConfigurationProvider;
import com.symphony.formatters.MessageML;
import com.symphony.models.CentralChatRoom;
import com.symphony.models.ChatRoom;
import com.symphony.models.ISymphonyMessage;
import com.symphony.models.ISymphonyUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan.dsouza on 7/27/16.
 *
 * The main class of the bot --> Run from here
 */

public class RoomManagerBot {

  private static final Logger LOG = LoggerFactory.getLogger(RoomManagerBot.class);

  //Triggers for sending a Help or Instructions message
  private static final List<String> helpCommands = new ArrayList<String>() {{
    add("help");
    add("instructions");
    add("guide");
  }};

  //Triggers for sending a message to the central chat room
  private static final List<String> centralChatRoomTriggers = new ArrayList<String>() {{
    add("central chat room");
    add("centralchatroom");
    add("ccr");
  }};

  //Triggers for sending a message to any of the side chat rooms
  //TODO: Make this more configurable
  private static final List<String> chatRoomTriggers = new ArrayList<String>() {{
    add("room 1");
    add("room1");
    add("room 2");
    add("room2");
    add("room 3");
    add("room3");
  }};

  //Triggers the bot to respond
  private static final List<String> botTriggerWords = new ArrayList<String>() {{
    add("roommanager");
    add("room manager");
    addAll(helpCommands);
    addAll(centralChatRoomTriggers);
    addAll(chatRoomTriggers);
  }};

  //Configuration information
  private final IConfigurationProvider configurationProvider;
  private final ISymphonyClient symphonyClient;
  private final long myUserId;

  private final List<ChatRoom> chatRooms;
  private final CentralChatRoom centralChatRoom;


  public RoomManagerBot(IConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
    this.symphonyClient = new SymphonyClient(this.configurationProvider);
    this.symphonyClient.authenticate();
    this.myUserId = configurationProvider.getBotUserId();

    this.chatRooms = this.getChatRooms();
    this.centralChatRoom = this.getCentralChatRoom();
  }

  /**
   * For testing purposes
   */
  public static void main(String[] ryan) {
    IConfigurationProvider configurationProvider = new ConfigurationProvider();
    RoomManagerBot browserBot = new RoomManagerBot(configurationProvider);
    browserBot.start();
  }

  /**
   * Starts the bot
   */
  public void start() {
    symphonyClient.authenticate();

    //Simple message to me saying the bot's up and running
    User userRyan = symphonyClient.getUserForEmailAddress("ryan.dsouza@symphony.com");
    Stream ourChat = symphonyClient.getStreamWithUsers(userRyan);
    symphonyClient.sendMessage(ourChat.getId(), "Up and running");

    //Infinite loop to always listen for responses
    while (true) {

      //Long poll the Symphony DataFeed API for messages
      List<ISymphonyMessage> messages = symphonyClient.getSymphonyMessages();

      //For each new message in all chatrooms
      for (ISymphonyMessage message : messages) {

        //If the message isn't from me (the bot), handle it
        if (message.getSymphonyUser().getUserId() != myUserId) {
          handleIncomingMessage(message);
        }
      }
    }
  }

  /**
   * Responsible for actually handling the incoming messages
   * @param message
   */
  private void handleIncomingMessage(ISymphonyMessage message) {

    //Message text in lowercase
    String messageText = message.getMessageText().toLowerCase();
    messageText = messageText.replace(">", "").replace("<", "");

    //If it doesn't contain a trigger word, it's not for the bot
    if (!containsTriggerWord(messageText)) {
      return;
    }

    //Handle each of the bot's possible actions
    if(shouldGiveHelp(messageText)) {
      LOG.debug("Giving help: " + messageText);
      handleGivingHelp(message);
    } else if(shouldSendToCentralChatRoom(messageText)) {
      LOG.debug("Sending to central chat room: " + messageText);
      handleSendingToCentralChatRoom(messageText, message);
    } else if(shouldSendToChatRoom(messageText)) {
      LOG.debug("Sending to chat room: " + messageText);
      handleSendingToChatRoom(messageText, message);
    }
  }

  /**
   * Sends a message with help instructions
   * @param message
   */
  private void handleGivingHelp(final ISymphonyMessage message) {

    MessageML messageML = new MessageML();

    //If we are the central room, show the side rooms
    if(message.getStreamId().equals(this.centralChatRoom.getThreadId())) {
      messageML.addParagraph("As the central room, you can send messages to any of the following ");
      messageML.addParagraph("chat rooms by saying 'roomName:  messageText'");
      for(ChatRoom room : this.chatRooms) {
        messageML.addLineBreak();
        messageML.addBoldText(room.getRoomName());
        messageML.addParagraph(". Members");
        for(ISymphonyUser symphonyUser : room.getSymphonyUsers()) {
          messageML.addParagraph(": " + symphonyUser.getDisplayName());
        }
      }
    }

    //If we are a side room, say how to signal main room
    else {
      String roomName = this.centralChatRoom.roomNameForThreadId(message.getStreamId());
      messageML.addParagraph("As side room '" + roomName +
          "', you can message the main room by saying: ");
      messageML.addLineBreak();
      messageML.addBoldText("ccr messageText");
      messageML.addLineBreak();
      messageML.addBoldText("centralchatroom messageText");

      messageML.addLineBreak();
      messageML.addParagraph("Participants: ");
      for(ChatRoom room : this.chatRooms) {
        messageML.addLineBreak();
        messageML.addBoldText(room.getRoomName());
        messageML.addParagraph(". Members");
        for(ISymphonyUser symphonyUser : room.getSymphonyUsers()) {
          messageML.addParagraph(": " + symphonyUser.getDisplayName());
        }
      }
    }

    this.symphonyClient.sendMessage(message.getStreamId(), messageML);
  }

  /**
   * Sends a message to any of the side chat rooms from Central Chat Room
   * @param messageText
   * @param message
   */
  private void handleSendingToChatRoom(String messageText, ISymphonyMessage message) {

    String textToSend = messageText.toLowerCase();
    String roomName = null;

    //Remove all trigger words, while figuring out which side room to send to
    for(String triggerWord : botTriggerWords) {
      if(textToSend.toLowerCase().contains(triggerWord)) {
        textToSend = textToSend.substring(triggerWord.length() + 1);
        roomName = triggerWord;
      }
    }

    //The room to send a message to
    String chosenRoomThread = this.centralChatRoom.threadIdForRoomName(roomName);
    String officialRoomName = this.centralChatRoom.roomNameForThreadId(chosenRoomThread);

    MessageML messageML = new MessageML();

    //Could not find side chat room to send message to
    if(chosenRoomThread == null || chosenRoomThread.isEmpty()) {
      messageML.addParagraph("Could not find room '" + roomName + "'");
      messageML.addLineBreak();
      messageML.addParagraph("Possible options are: ");
      for(ChatRoom chatRoom : this.chatRooms) {
        messageML.addLineBreak();
        messageML.addBoldText(chatRoom.getRoomName());
      }
      this.symphonyClient.sendMessage(message.getStreamId(), messageML);
    }

    //Found side chat room to send message to
    else {
      messageML.addParagraph("Message from ");
      messageML.addBoldText("Central Chat Room");
      messageML.addParagraph(": " + textToSend);
      this.symphonyClient.sendMessage(chosenRoomThread, messageML);

      MessageML acknowledgement = new MessageML();
      acknowledgement.addParagraph("Successfully sent '" + textToSend + "' to " + officialRoomName);
      this.symphonyClient.sendMessage(message.getStreamId(), acknowledgement);
    }
  }

  /**
   * Handles send a message to the Central Chat Room from any of the side chat rooms
   * @param messageText
   * @param message
   */
  private void handleSendingToCentralChatRoom(String messageText, ISymphonyMessage message) {

    //Remove the trigger words so it's a clean message
    String textToSend = messageText;
    for(String triggerWord : botTriggerWords) {
      if(textToSend.contains(triggerWord)) {
        textToSend = textToSend.replace(triggerWord, "");
        break;
      }
    }

    MessageML messageML = new MessageML();
    messageML.addParagraph("Message from ");

    //Figure out the pretty name (i.e.: 'Room 1') of the room that's sending the message to the CCR
    V2RoomDetail senderRoom = this.symphonyClient.roomDetailForId(message.getStreamId());
    if(senderRoom != null) {
      messageML.addBoldText(senderRoom.getRoomAttributes().getName());
    } else {
      messageML.addBoldText("Unknown room");
    }

    //The actual message text
    messageML.addParagraph(": " + textToSend);
    this.symphonyClient.sendMessage(this.centralChatRoom.getThreadId(), messageML);

    MessageML acknowledgement = new MessageML();
    acknowledgement.addParagraph("Successfully sent '" + textToSend + "' to " + this.centralChatRoom.getRoomName());
    this.symphonyClient.sendMessage(senderRoom.getRoomSystemInfo().getId(), acknowledgement);
  }

  /**
   * Checks to see if the message contains a trigger word
   * @param messageText
   * @return
   */
  private static boolean containsTriggerWord(String messageText) {
    for (String triggerWord : botTriggerWords) {
      if (messageText.contains(triggerWord)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks to see if the user needs help
   * @param messageText
   * @return
   */
  private static boolean shouldGiveHelp(String messageText) {
    for(String helpWord : helpCommands) {
      if(messageText.contains(helpWord)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks to see if it should be a message to central chat room
   * @param messageText
   * @return
   */
  private static boolean shouldSendToCentralChatRoom(String messageText) {
    for(String triggerWord : centralChatRoomTriggers) {
      if(messageText.contains(triggerWord) && messageText.indexOf(triggerWord) == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks to see if it should be a message to one of the side rooms
   * @param messageText
   * @return
   */
  private static boolean shouldSendToChatRoom(String messageText) {
    for(String triggerWord : chatRoomTriggers) {
      if(messageText.contains(triggerWord)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets up and returns each of the chat rooms
   * //TODO: Make this configurable
   * @return
   */
  private List<ChatRoom> getChatRooms() {
    //ROOM 1
    List<String> room1Emails = new ArrayList<String>() {{
      add("Susan.haimet@symphony.com");
      add("rani.ibrahim@symphony.com");
      add("ryan.dsouza@symphony.com"); //TODO: FOR TESTING
    }};
    List<User> room1Users = this.symphonyClient.getUsersForEmailAddresses(room1Emails);
    List<ISymphonyUser> room1SymphonyUsers = this.symphonyClient.getSymphonyUsersForEmailAddresses(room1Emails);
    String stream1 = this.symphonyClient.getRoomWithUsers(room1Users, "Room 1");
    ChatRoom room1 = new ChatRoom(room1SymphonyUsers, stream1, "Room 1");
    //this.symphonyClient.sendMessage(stream1, "Test message. You are Room 1");

    //ROOM 2
    List<String> room2Emails = new ArrayList<String>() {{
      add("al@symphony.com");
      add("romana.escalera@symphony.com");
      add("ryan.dsouza@symphony.com"); //TODO: FOR TESTING
    }};
    List<User> room2Users = this.symphonyClient.getUsersForEmailAddresses(room2Emails);
    List<ISymphonyUser> room2SymphonyUsers = this.symphonyClient.getSymphonyUsersForEmailAddresses(room2Emails);
    String stream2 = this.symphonyClient.getRoomWithUsers(room2Users, "Room 2");
    ChatRoom room2 = new ChatRoom(room2SymphonyUsers, stream2, "Room 2");
    //this.symphonyClient.sendMessage(stream2, "Test message. You are Room 2");

    //ROOM 3
    List<String> room3Emails = new ArrayList<String>() {{
      add("richard.jowett@symphony.com");
      add("rosalina.gill@symphony.com");
      add("ryan.dsouza@symphony.com"); //TODO: FOR TESTING
    }};
    List<User> room3Users = this.symphonyClient.getUsersForEmailAddresses(room3Emails);
    List<ISymphonyUser> room3SymphonyUsers = this.symphonyClient.getSymphonyUsersForEmailAddresses(room3Emails);
    String stream3 = this.symphonyClient.getRoomWithUsers(room3Users, "Room 3");
    ChatRoom room3 = new ChatRoom(room3SymphonyUsers, stream3, "Room 3");
    //this.symphonyClient.sendMessage(stream3, "Test message. You are Room 3");

    List<ChatRoom> chatRooms = new ArrayList<ChatRoom>() {{
      add(room1);
      add(room2);
      add(room3);
    }};
    return chatRooms;
  }

  /**
   * Sets up and returns the central chat room
   * //TODO: Make this configurable
   * @return
   */
  private CentralChatRoom getCentralChatRoom() {
    List<String> centralRoomEmails = new ArrayList<String>() {{
      add("ryan.dsouza@symphony.com");
    }};
    List<User> centralRoomUsers = this.symphonyClient.getUsersForEmailAddresses(centralRoomEmails);
    List<ISymphonyUser> centralRoomSymphonyUsers = this.symphonyClient.getSymphonyUsersForEmailAddresses(centralRoomEmails);
    String centralRoomStream = this.symphonyClient.getRoomWithUsers(centralRoomUsers, "Central Chat Room");

    CentralChatRoom centralChatRoom = new CentralChatRoom(this.chatRooms, centralRoomSymphonyUsers, centralRoomStream, "Central Chat Room");
    return centralChatRoom;
  }
}