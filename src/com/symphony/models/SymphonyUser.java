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

import com.symphony.api.pod.model.User;

/**
 * Created by ryan.dsouza on 7/26/16.
 *
 * Represents a Symphony User. Contains more information that just User from Symphony SDK
 * i.e.: "emailAddress" and "displayName". Class should be used for enriching a Symphony User
 */

public class SymphonyUser implements ISymphonyUser {

  private final Long userId;
  private final String emailAddress;
  private final String displayName;

  public SymphonyUser(Long userId, String emailAddress, String displayName) {
    this.userId = userId;
    this.emailAddress = emailAddress;
    this.displayName = displayName;
  }

  public SymphonyUser(User user, String displayName) {
    this.userId = user.getId();
    this.emailAddress = user.getEmailAddress();
    this.displayName = displayName;
  }

  public Long getUserId() {
    return userId;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return "SymphonyUser{" +
        "userId=" + userId +
        ", emailAddress='" + emailAddress + '\'' +
        ", displayName='" + displayName + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    return this.userId.intValue();
  }

  @Override
  public boolean equals(Object other) {
    if(!(other instanceof ISymphonyUser)) {
      return false;
    }

    ISymphonyUser otherUser = (ISymphonyUser) other;
    return otherUser.getUserId().equals(this.userId);
  }
}