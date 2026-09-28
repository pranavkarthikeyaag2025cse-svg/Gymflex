# GymFlex

GymFlex is a Spring Boot gym membership and attendance tracker. The frontend is plain HTML, CSS, and JavaScript served by Spring Boot, so the browser and API share one origin. Java 17+ and MySQL 8+ are required; Java 21 and Maven are available in the current workspace.

## Project structure

```text
gymflex/
|-- pom.xml
|-- README.md
|-- .gitignore
|-- .vscode/
|   `-- launch.json
`-- src/main/
    |-- java/com/gymflex/
    |   |-- GymFlexApplication.java
    |   |-- config/ (AppConfig, DataInitializer)
    |   |-- controller/ (MemberController, PlanController, MembershipController,
    |   |                CheckInController, DashboardController)
    |   |-- dto/ (Dtos)
    |   |-- entity/ (Member, Plan, Membership, CheckIn)
    |   |-- exception/ (GlobalExceptionHandler, ResourceNotFoundException)
    |   |-- repository/ (MemberRepository, PlanRepository, MembershipRepository,
    |   |                CheckInRepository)
    |   `-- service/ (MemberService, PlanService, MembershipService,
    |                CheckInService, DashboardService)
    `-- resources/
        |-- application.properties
        `-- static/
            |-- index.html
            |-- members.html
            |-- add-member.html
            |-- plans.html
            |-- checkin.html
            |-- attendance.html
            |-- expiring.html
            |-- css/style.css
            `-- js/ (app, dashboard, members, plans, checkin, attendance, expiring).js
```

## Database and credentials

Create the local database in MySQL Workbench or the MySQL command line:

```sql
CREATE DATABASE gymflex_db;
```

Hibernate creates and updates the tables on startup. The connection defaults are in `src/main/resources/application.properties`. Change the password by setting `DB_PASSWORD` in the process environment; optional overrides are `DB_USERNAME` and `DB_URL`.

PowerShell, for the current terminal session:

```powershell
$env:DB_PASSWORD = "your-mysql-password"
```

Then run the application from that same terminal. Do not commit a real database password. The checked-in default `YOUR_MYSQL_PASSWORD` is only a reminder to configure the local password.

## Run in VS Code

1. Install a JDK 17 or newer, Maven, MySQL, and the VS Code Extension Pack for Java.
2. Create the `gymflex_db` database and configure `DB_PASSWORD` as shown above.
3. Open this project folder in VS Code. From the integrated terminal, run `mvn spring-boot:run`; or use **Run and Debug** with the included **Launch GymFlex** configuration.
4. Open [http://localhost:8080](http://localhost:8080). Spring Boot serves the dashboard and all frontend pages; do not open the HTML files directly from disk.

Default Monthly, Quarterly, and Yearly plans are inserted when the application starts if missing. Example prices are INR and can be changed from Membership Plans.

## REST API

Base URL: `http://localhost:8080`. JSON request bodies use `Content-Type: application/json`. Validation and business errors are returned as `{ "timestamp", "status", "message" }` without Java stack traces.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/members` | List members |
| GET | `/api/members/{id}` | Get a member |
| POST | `/api/members` | Create a member profile without a membership |
| PUT | `/api/members/{id}` | Update member details |
| PUT | `/api/members/{id}/details` | Atomically update member details, plan, start date, and expiry date |
| DELETE | `/api/members/{id}` | Delete member and their attendance/membership history |
| GET | `/api/plans` | List plans |
| GET | `/api/plans/{id}` | Get a plan |
| POST | `/api/plans` | Add a plan |
| PUT | `/api/plans/{id}` | Update a plan |
| DELETE | `/api/plans/{id}` | Delete an unused plan |
| POST | `/api/memberships/register` | Create member and initial membership together |
| POST | `/api/memberships/{id}/renew` | Renew a membership from its current expiry date |
| GET | `/api/memberships` | List memberships |
| GET | `/api/memberships/expiring-soon` | Memberships expiring today through day 7 |
| POST | `/api/checkins` | Check in a member |
| GET | `/api/checkins?date=YYYY-MM-DD` | List all check-ins, optionally filtered by date |
| GET | `/api/checkins/member/{memberId}` | Attendance history for one member |
| GET | `/api/checkins/monthly/{memberId}` | Current-month count and check-in records |
| GET | `/api/dashboard/stats` | Dashboard totals and recent check-ins |

Registration example:

```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "phone": "+91 98765 43210",
  "address": "12 Fitness Road, Pune",
  "planId": 1,
  "startDate": "2026-09-28"
}
```

Plan example:

```json
{
  "planName": "Monthly",
  "durationInMonths": 1,
  "price": 199.00,
  "description": "Flexible month-to-month access."
}
```

Renewal and check-in examples:

```json
{ "planId": 2 }
```

```json
{ "memberId": 1 }
```

A member may check in once per local calendar day. Check-in is refused if the latest membership expiry is before today; the expiry day itself remains valid. A renewal adds the selected duration to the existing expiry date, including when that date is already in the past. Plan duration and price, email, phone, and required fields are validated in the API as well as in the forms.
