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

## Running it

The project targets PostgreSQL and was originally run in UCR's lab
environment. To run it elsewhere you need a local PostgreSQL server and a JDK,
then:

1. **Create and load the database** (set `PGPORT` and `USER` for your setup):
   ```bash
   cd project/sql/scripts
   ./create_db.sh
   ```
2. **Compile and run the app:**
   ```bash
   cd project/java/scripts
   ./compile.sh
   ```

> **Note:** `compile.sh` currently hardcodes a Java path from the UCR lab
> (`/usr/csshare/pkgs/jdk1.7.0_17`) and the project ships an older PostgreSQL
> JDBC driver. You'll need to point it at your own JDK and, ideally, a current
> driver. A containerized setup (PostgreSQL + the app in Docker) is planned so
> it runs anywhere with one command.

## Known limitations

- **SQL injection:** queries are assembled with string formatting over raw user
  input, so the app is not safe against malicious input. This was acceptable for
  the course's scope but would need parameterized queries (`PreparedStatement`)
  before any real use.
- **Passwords are stored and compared in plaintext.** A production version would
  hash them (e.g. bcrypt).
- The seed data is synthetic course data, not real users.
