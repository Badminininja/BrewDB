# BrewDB — Café Management System

A command-line café ordering and management system backed by a PostgreSQL
database. Customers can browse the menu, place and track orders, and manage
their profile; managers can maintain the menu. Built for **CS166 (Database
Management Systems), University of California, Riverside.**

> **Authors:** Joseph Mangapit <!-- add teammate name(s) here if this was a pair project -->

*Tech stack: Java (JDBC), PostgreSQL, Bash.*

## What it demonstrates

- **Relational schema design** — a normalized four-table model (users, menu,
  orders, and per-item order status) with primary/foreign keys and indexes.
- **A full application over a live database** — ~800 lines of Java talking to
  PostgreSQL through JDBC, with a role-based menu for customers vs. managers.
- **Working at realistic scale** — the project loads a seed dataset of roughly
  28,000 users, 87,000 orders, and 130,000 order-item status records.

## Features

**Customer**
- Create an account and log in
- View the full menu, or search it by item type or name
- Place an order and update an existing order
- Update profile: phone number, password, and favorite items

**Manager**
- Add, delete, and update menu items (type, price, description, image)

## Database schema

| Table | Purpose | Key columns |
|---|---|---|
| `Users` | Accounts and roles | `login` (PK), `phoneNum`, `password`, `favItems`, `type` |
| `Menu` | Items offered | `itemName` (PK), `type`, `price`, `description`, `imageURL` |
| `Orders` | Orders placed | `orderid` (PK), `login`, `paid`, `timeStampRecieved`, `total` |
| `ItemStatus` | Per-item status within an order | (`orderid`, `itemName`) (PK), `lastUpdated`, `status`, `comments` |

Indexes are defined on `Orders(timeStampRecieved)` and `ItemStatus(orderid)`
to speed up the most common lookups.

## Repository layout

```
project/
├── java/
│   ├── src/Cafe.java        # the application
│   ├── lib/                 # PostgreSQL JDBC driver
│   └── scripts/compile.sh   # compile + run
├── sql/
│   ├── src/                 # create_tables.sql, create_indexes.sql, load_data.sql
│   └── scripts/create_db.sh # build and populate the database
└── data/                    # seed CSVs (users, menu, orders, item status)
```

## Running it with Docker (recommended)

The whole thing — a PostgreSQL database loaded with the schema and seed data,
plus the Java app — runs in Docker, so you don't install PostgreSQL, a JDK, or
a JDBC driver yourself. You only need
[Docker Desktop](https://www.docker.com/products/docker-desktop/).

From the repository root:

```bash
docker compose run --rm app
```

That builds the app, starts the database (which auto-loads the schema and the
seed CSVs on first run), waits until it's ready, and then drops you into the
café menu. Exit with menu option `9` (or Ctrl+C). To stop the database
afterward, run `docker compose down`.

> The first run takes a few minutes: it downloads the PostgreSQL image and the
> JDBC driver and loads ~28k users / 87k orders / 130k status rows. Later runs
> are quick. The database keeps its data in a Docker volume between runs.

### Running it manually (without Docker)

The project was originally run in UCR's lab environment with a local PostgreSQL
server and a JDK. `project/sql/scripts/create_db.sh` loads the schema/data and
`project/java/scripts/compile.sh` compiles and runs the app — but note that
`compile.sh` hardcodes a UCR lab Java path and the repo ships an old JDBC
driver, so the Docker path above is the reliable way to run it today.

## Known limitations

- **SQL injection:** queries are assembled with string formatting over raw user
  input, so the app is not safe against malicious input. This was acceptable for
  the course's scope but would need parameterized queries (`PreparedStatement`)
  before any real use.
- **Passwords are stored and compared in plaintext.** A production version would
  hash them (e.g. bcrypt).
- The seed data is synthetic course data, not real users.
