# meetingscheduler
This is a simple Restful API for scheduling meetings where users can schedule meetings, cancel meetings, and query meetings.

# Screenshot
meeting-scheduler-controller:
<img width="1451" alt="image" src="https://github.com/RCXMAN/meetingscheduler/assets/36804741/1589b047-f19f-440a-9698-568ae231ff5c">

meeting-room-controller:
<img width="1475" alt="image" src="https://github.com/RCXMAN/meetingscheduler/assets/36804741/934cbfd7-501d-47f4-a8de-868b980d7bac">

# Features
* qucik schedule meeting: The meeting creator provides participants and meeting time. The system automatically arranges a suitable meeting room and create the meeting.
* normal schedule meeting: The meeting creator specifies a meeting room to create the meeting.
* query user meetings: Query meetings of a user by day, month or year.
* query room meetings: Query meetings of a room by day, month or year.
* canel a meeting: Cancel a meeting by meeting creator
* canel a meeting due to maintenance: If the conference room needs to be repaired during a certain time period, cancel all meetings for that time period and notify the participants

# Built With
* Java 17
* Spring Boot 3.1.1
* Postgres
