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

package com.symphony.models;

import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ryan.dsouza on 8/8/16.
 *
 * Represents the Central Chat Room (CCR), along with all the side chat rooms in it
 * The dealer should be in this room.
 * This class helps manage interactions between itself (CCR) and side chat rooms (with sellers)
 */

public class CentralChatRoom {

  //Maps roomNames with ChatRooms - used for getting threadIds given the name of a room
  private final Map<String, ChatRoom> chatRooms;

  //Unique side chat room ids
  private final Set<String> chatRoomIds;

  //CentralChatRoom specific information
  private final List<ISymphonyUser> centralChatRoomUsers;
  private final String threadId;
  private final String roomName;

  public CentralChatRoom(List<ChatRoom> chatRooms, List<ISymphonyUser> centralChatRoomUsers,
      String threadId, String roomName) {

    this.chatRooms = new HashMap<String, ChatRoom>();
    this.chatRoomIds = new HashSet<String>();

    //For each of the side chatrooms
    for(ChatRoom chatRoom : chatRooms) {

      //Store different versions of the room names i.e.: "Room 1", "Room1", "room 1", "room1"
      this.chatRooms.put(chatRoom.getThreadId(), chatRoom);
      this.chatRooms.put(chatRoom.getRoomName(), chatRoom);
      this.chatRooms.put(chatRoom.getRoomName().toLowerCase(), chatRoom);
      this.chatRooms.put(chatRoom.getRoomName().replace(" ", ""), chatRoom);
      this.chatRooms.put(chatRoom.getRoomName().toLowerCase().replace(" ", ""), chatRoom);

      //The set of unique chatroomIds
      this.chatRoomIds.add(chatRoom.getThreadId());
    }

    this.centralChatRoomUsers = centralChatRoomUsers;
    this.threadId = threadId;
    this.roomName = roomName;
  }

  /**
   * Returns the nice name of a chat room (i.e.: "Room 1") given the room's threadId
   * @param threadId
   * @return
   */
  public String roomNameForThreadId(String threadId) {

    //First try to get it from our HashMap of roomName, threadIds
    ChatRoom room = this.chatRooms.get(threadId);
    if(room != null) {
      return room.getRoomName();
    }

    //If that didn't work, repeat but encode with Base64 - sometimes threadId is Byte array
    String t1 = new String(Base64.encodeBase64(threadId.getBytes()));

    if(this.chatRooms.containsKey(t1)) {
      return this.chatRooms.get(t1).getRoomName();
    }

    return "Unknown Chatroom";
  }

  /**
   * Returns the threadId for a roomName - can fail if the roomName is incorrect
   * @param roomName
   * @return
   */
  public String threadIdForRoomName(String roomName) {

    if(roomName == null || roomName.isEmpty()) {
      return null;
    }

    //Basic check of our roomName to ChatRoom HashMap
    ChatRoom room = this.chatRooms.get(roomName);
    if(room != null) {
      return room.getThreadId();
    }

    //Last case, go through all keys and hope that one is the best fit
    for(String sideChatRoomName : this.chatRooms.keySet()) {
      if(sideChatRoomName.contains(roomName)) {
        return this.chatRooms.get(sideChatRoomName).getThreadId();
      }
    }

    return null;
  }

  /**
   * Checks to see if the threadId is for a side chatroom for this dealer
   * @param threadId
   * @return
   */
  public boolean hasChatRoom(String threadId) {
    return this.chatRoomIds.contains(threadId);
  }

  public Map<String, ChatRoom> getChatRooms() {
    return chatRooms;
  }

  public List<ISymphonyUser> getCentralChatRoomUsers() {
    return centralChatRoomUsers;
  }

  public String getThreadId() {
    return threadId;
  }

  public String getRoomName() {
    return roomName;
  }
}