# SM-Caterer Deployment Guide

## Quick Start

### Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Linux server (Ubuntu/CentOS/RHEL)
- sudo/root access

### 1. Server Setup

```bash
# Create application user
sudo useradd -r -s /bin/false cloudcaters

# Create directories
sudo mkdir -p /opt/cloudcaters/{uploads,backups}
sudo mkdir -p /var/log/cloudcaters

# Set ownership
sudo chown -R cloudcaters:cloudcaters /opt/cloudcaters
sudo chown -R cloudcaters:cloudcaters /var/log/cloudcaters
```

### 2. Database Setup

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE `sm-caterer` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create application user
CREATE USER 'smcaterer_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON `sm-caterer`.* TO 'smcaterer_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Environment

```bash
# Copy environment template
sudo cp sm-caterer.env.template /opt/cloudcaters/sm-caterer.env

# Edit with your values
sudo nano /opt/cloudcaters/sm-caterer.env

# Secure the file
sudo chmod 600 /opt/cloudcaters/sm-caterer.env
sudo chown cloudcaters:cloudcaters /opt/cloudcaters/sm-caterer.env
```

### 4. Install Service

```bash
# Copy service file
sudo cp sm-caterer.service /etc/systemd/system/

# Reload systemd
sudo systemctl daemon-reload

# Enable on boot
sudo systemctl enable sm-caterer
```

### 5. Deploy Application

```bash
# Copy WAR file
sudo cp SM-Caterer-0.0.1-SNAPSHOT.war /opt/cloudcaters/app.war
sudo chown cloudcaters:cloudcaters /opt/cloudcaters/app.war

# Start service
sudo systemctl start sm-caterer

# Check status
sudo systemctl status sm-caterer
```

### 6. Verify Deployment

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# View logs
sudo journalctl -u sm-caterer -f
```

## Automated Deployment

Use the deployment script for automated deployments:

```bash
# Make script executable
chmod +x deploy.sh

# Run deployment
sudo ./deploy.sh SM-Caterer-0.0.1-SNAPSHOT.war prod
```

## Backup

### Manual Backup

```bash
# Full backup
sudo ./backup.sh full

# Database only
sudo ./backup.sh db

# Files only
sudo ./backup.sh files
```

### Automated Backup (Cron)

```bash
# Edit crontab
sudo crontab -e

# Add daily backup at 2 AM
0 2 * * * /opt/cloudcaters/deploy/backup.sh full >> /var/log/cloudcaters/backup.log 2>&1
```

## Common Commands

```bash
# Start/Stop/Restart
sudo systemctl start sm-caterer
sudo systemctl stop sm-caterer
sudo systemctl restart sm-caterer

# View status
sudo systemctl status sm-caterer

# View logs
sudo journalctl -u sm-caterer -f
sudo journalctl -u sm-caterer --since "1 hour ago"

# View application logs
tail -f /var/log/cloudcaters/sm-caterer.log
tail -f /var/log/cloudcaters/sm-caterer.log.error
```

## Troubleshooting

### Application won't start

1. Check Java version: `java -version`
2. Check environment file: `/opt/cloudcaters/sm-caterer.env`
3. Check database connectivity
4. View logs: `journalctl -u sm-caterer -n 100`

### Database connection issues

1. Verify MySQL is running: `systemctl status mysql`
2. Check credentials in environment file
3. Verify database exists: `mysql -u smcaterer_user -p -e "USE sm-caterer;"`

### Out of memory

Adjust JVM settings in service file:
```
-Xms512m -Xmx1024m
```

## Security Checklist

- [ ] Change all default passwords
- [ ] Generate secure JWT secret (64+ characters)
- [ ] Enable SSL/HTTPS
- [ ] Configure firewall (allow only 80/443)
- [ ] Set file permissions (600 for .env, 640 for .war)
- [ ] Enable fail2ban for SSH
- [ ] Regular security updates
