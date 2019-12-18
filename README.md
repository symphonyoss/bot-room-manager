##RoomManager Bot

[![FINOS - Archived](https://cdn.jsdelivr.net/gh/finos/contrib-toolbox@master/images/badge-archived.svg)](https://finosfoundation.atlassian.net/wiki/display/FINOS/Archived)

##Created by Ryan D'souza and Susan Haimet

####Purpose
This comes from a real world use case provided by Groupama AM where they submit an offer and the sales people bid on it.

As detailed in the screenshots below, sales people in different chats cannot/should not communicate with each other - only the buyer/dealer can interact with the different sales groups (regulations).

This means that the buyer would need to switch between three+ different sales chats in order to find the best deal. This can take up a lot of time and result in them missing out on that trade.

RoomManager allows each sales chat to send a message to a room (Central Chat) with the buyer in it, allowing the buyer to view all sales offers in the same window/screen without switching between tabs and having different sales teams know what the other team is offering.

From that central chat, the buyer can communicate with each sales chat individually without switching tabs or chats. 

This will save the buyer a lot of time + effort, and time = money


####Run Instructions

- Download this ZIP 

- Uncompress this ZIP 

- Open project folder in IntelliJ 

- Install maven dependencies from pom.xml

- Run the main method of 'RoomManagerBot.java'




+ Our bot the Central Chat Room can launch an offer for a trade by sending out a message to Sales Chat Rooms (in our example 3 Sales Chat Rooms - Room 1, Room2 and Room 3,
---
+ Each Sales Shat rooms has Sales people waiting for info on a trade (in our case each Sales Chat Rooom has 2 users but it could be n users) + our bot.
___
+ Sales people in those Sales Chat Rooms will send a message if they are interested in the trade. The messaged is prefixed by ccr "Central Chat Room" and our bot will post that message in the "Central Chat Room" to say "yes I am interested in this trade".
---
ccr Hello Central Chat Room, I want to do this deal.
---
The message is displayed in the central chat room. In the use case provided by the our real life client this would be an offer for a trade.
---
The Central Chat Room can then send messages to any (or all) of the other chat rooms, for example, Chat Room 1 "Let's do the deal.", Chat Room 2' "You're too late, the deal is already done."
---
This comes from a real world use case provided by Groupama AM where they submit an offer and the sales people bid on it. They tell us that this is a killer functionality.
