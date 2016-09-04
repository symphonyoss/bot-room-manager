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

import java.util.List;

/**
 * Created by ryan.dsouza on 8/8/16.
 *
 * Represents a simple chatroom with users - used for sales people
 */

public class ChatRoom {

  private final List<ISymphonyUser> symphonyUsers; //The users in this chat room
  private final String threadId;
  private final String roomName;

  public ChatRoom(List<ISymphonyUser> symphonyUsers, String threadId, String roomName) {
    this.symphonyUsers = symphonyUsers;
    this.threadId = threadId;
    this.roomName = roomName;
  }

  public List<ISymphonyUser> getSymphonyUsers() {
    return symphonyUsers;
  }

  public String getThreadId() {
    return threadId;
  }

  public String getRoomName() {
    return roomName;
  }

  @Override
  public String toString() {
    return "ChatRoom{" +
        "symphonyUsers=" + symphonyUsers +
        ", threadId='" + threadId + '\'' +
        ", roomName='" + roomName + '\'' +
        '}';
  }
}