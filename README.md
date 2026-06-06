# Assistant Scheduler
**Organization:** SummerProject2026

A user-friendly scheduling and employee management platform for dental offices that streamlines shift scheduling, availability management, and communication between staff and management.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [IDE Setup](#ide-setup-intellij-idea)
- [Cloning the Repository](#cloning-the-repository)
- [Backend Setup](#backend-setup-spring-boot)
- [Database Setup](#database-setup-postgresql)
- [Running the Project](#running-the-project)
- [User Roles](#user-roles)

---

## Project Overview

### Functional Requirements
- Assistants can view the monthly schedule
- Assistants can see other assistants' time off requests
- Assistants can submit time off requests or emergencies
- HR can approve time off requests
- HR is alerted to new requests or emergencies
- Schedule is automatically updated after approvals
- SRole can create and print the monthly schedule

### Non-Functional Requirements
- Schedule loads within 5 seconds
- Runtime complexity: O(n²)
- Schedule is printable

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Backend Framework** | Spring Boot 4.0.6 |
| **ORM** | Hibernate (via Spring Data JPA) |
| **Database** | PostgreSQL 16 |
| **Database UI** | TablePlus |
| **Build Tool** | Maven |
| **Frontend (Web)** | React |
| **Frontend (Mobile)** | React Native |
| **Cloud** | AWS |
| **IDE** | IntelliJ IDEA |
| **Version Control** | Git + GitHub |

### Spring Boot Dependencies
| Dependency | Purpose |
|---|---|
| `Spring Web` | REST API |
| `Spring Data JPA` | Database access via Hibernate |
| `Spring Security` | Login and role-based access control |
| `PostgreSQL Driver` | Connect to PostgreSQL database |
| `Lombok` | Reduces Java boilerplate |
| `Spring Mail` | HR alert emails and notifications |
| `Validation` | Input validation |

---

## Prerequisites

Before getting started make sure you have the following installed on your Mac:

### 1. Homebrew
If you don't have Homebrew installed, run this in your terminal:
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2. Git
```bash
brew install git
```
Verify:
```bash
git --version
```

### 3. Maven
```bash
brew install maven
```
Verify:
```bash
mvn --version
```

### 4. PostgreSQL 16
```bash
brew install postgresql@16
brew services start postgresql@16
```
Verify it's running:
```bash
brew services list
```
You should see `postgresql@16` with status `started`.

### 5. TablePlus (Database UI)
Download from [tableplus.com](https://tableplus.com) — free tier is sufficient.

---

## IDE Setup: IntelliJ IDEA

### Installation
1. Go to [jetbrains.com/idea/download](https://www.jetbrains.com/idea/download)
2. Select **Mac** and choose the correct chip:
   - **Apple Silicon** → M1/M2/M3/M4 Mac
   - **Intel** → Intel Mac
   > Not sure? Go to Apple menu → **About This Mac**
3. Download and open the `.dmg` file
4. Drag **IntelliJ IDEA** to your **Applications** folder

Alternatively, install via Homebrew:
```bash
# Community Edition (Free)
brew install --cask intellij-idea-ce

# Ultimate Edition (Paid, best for Spring Boot)
brew install --cask intellij-idea
```

### Set Up JDK in IntelliJ
1. Go to **File → Project Structure → Project**
2. Under **SDK** select **Java 21**
3. If not listed: **Add SDK → Download JDK → Amazon Corretto 21**
4. Click **Apply → OK**

### Recommended Plugins
Go to **IntelliJ IDEA → Settings → Plugins → Marketplace** and install:
- **Lombok** — required for the project
- **Rainbow Brackets** — easier to read nested code
- **GitToolBox** — better Git info in the editor

---

## Cloning the Repository

### Option 1: Using a Personal Access Token (Recommended for org repos)

**Step 1 — Create a GitHub Personal Access Token (PAT):**
1. Go to GitHub → Profile photo → **Settings**
2. Scroll to **Developer Settings → Personal Access Tokens → Tokens (classic)**
3. Click **Generate new token (classic)**
4. Name it `IntelliJ`, check `repo` and `read:org`, click **Generate token**
5. **Copy the token immediately** — you won't see it again

**Step 2 — Clone via terminal:**
```bash
mkdir ~/Documents/SummerProject2026
cd ~/Documents/SummerProject2026
git clone https://YOUR_PAT_TOKEN@github.com/SummerProject2026/repo-name.git
cd repo-name
```

**Step 3 — Open in IntelliJ:**
- **File → Open** → select the cloned folder → **Open**
- Click **"Trust Project"** if prompted

### Option 2: Clone via IntelliJ
1. Open IntelliJ → **Clone Repository**
2. Paste the GitHub repo URL directly
3. Choose save location → **Clone**

---

## Backend Setup: Spring Boot

### Load Maven Dependencies
When you open the project, IntelliJ should show a popup:
> *"Maven build script found"*

Click **"Load Maven Project"**.

If you don't see it:
- Right-click `pom.xml` → **"Add as Maven Project"**

### Verify the Build
```bash
./mvnw clean install
```
Or if `mvnw` is not present:
```bash
mvn clean install
```
You should see **BUILD SUCCESS**.

### application.properties
Located at `src/main/resources/application.properties`. Configure it as follows:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/assistantscheduler
spring.datasource.username=your_mac_username
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# App
spring.application.name=AssistantScheduler
```
> Replace `your_mac_username` with your Mac username (run `whoami` in terminal to find it).

---

## Database Setup: PostgreSQL

### Step 1: Start PostgreSQL
```bash
brew services start postgresql@16
```

### Step 2: Find Your Username
```bash
psql postgres -c "\du"
```
Your username is listed in the output (usually your Mac username).

### Step 3: Create the Database
```bash
psql postgres
```
Inside the postgres shell:
```sql
CREATE DATABASE assistantscheduler;
\q
```

### Step 4: Connect TablePlus
1. Open TablePlus → **Create a new connection** → select **PostgreSQL**
2. Fill in:

| Field | Value |
|---|---|
| **Name** | AssistantScheduler |
| **Host** | 127.0.0.1 |
| **Port** | 5432 |
| **User** | your Mac username |
| **Password** | leave blank |
| **Database** | assistantscheduler |

3. Click **Test** — should show green
4. Click **Connect**

### Useful PostgreSQL Commands
```bash
brew services start postgresql@16    # Start
brew services stop postgresql@16     # Stop
brew services restart postgresql@16  # Restart
brew services list                   # Check status
```

---

## Running the Project

Once setup is complete, run the application from IntelliJ:
- Open `src/main/java/.../AssistantSchedulerApplication.java`
- Click the green **Run** button

Or from the terminal:
```bash
./mvnw spring-boot:run
```

The app will start at `http://localhost:8080`

---

## User Roles

| Role | Permissions |
|---|---|
| **Assistant** | View monthly schedule, view other time off requests, submit time off requests and emergencies |
| **HR** | View schedule, approve/deny time off requests, receive alerts for new requests |
| **SRole** | View schedule, create monthly schedule, print monthly schedule |

---

## Project Structure

```
assistant-scheduler/
├── src/
│   ├── main/
│   │   ├── java/          ← Application source code
│   │   └── resources/
│   │       └── application.properties  ← DB and app config
│   └── test/              ← Unit and integration tests
├── pom.xml                ← Maven dependencies
├── mvnw                   ← Maven wrapper (Mac/Linux)
├── mvnw.cmd               ← Maven wrapper (Windows)
└── README.md
```

---

## Need Help?

- Spring Boot docs: [docs.spring.io](https://docs.spring.io)
- Maven Central (find dependencies): [search.maven.org](https://search.maven.org)
- PostgreSQL docs: [postgresql.org/docs](https://www.postgresql.org/docs/)
- TablePlus: [tableplus.com](https://tableplus.com)
