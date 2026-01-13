#!/bin/bash
# ===================================
# SM-Caterer Deployment Script
# ===================================
# Usage: ./deploy.sh [war-file] [environment]
# Example: ./deploy.sh SM-Caterer-0.0.1-SNAPSHOT.war prod
# ===================================

set -e

# Configuration
APP_NAME="sm-caterer"
APP_USER="cloudcaters"
APP_GROUP="cloudcaters"
DEPLOY_DIR="/opt/cloudcaters"
BACKUP_DIR="/opt/cloudcaters/backups"
LOG_DIR="/var/log/cloudcaters"
WAR_FILE="${1:-SM-Caterer-0.0.1-SNAPSHOT.war}"
ENVIRONMENT="${2:-prod}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "This script must be run as root"
        exit 1
    fi
}

# Validate WAR file exists
validate_war() {
    if [[ ! -f "$WAR_FILE" ]]; then
        log_error "WAR file not found: $WAR_FILE"
        exit 1
    fi
    log_info "Found WAR file: $WAR_FILE"
}

# Create required directories
create_directories() {
    log_info "Creating directories..."
    mkdir -p "$DEPLOY_DIR"
    mkdir -p "$BACKUP_DIR"
    mkdir -p "$LOG_DIR"
    mkdir -p "$DEPLOY_DIR/uploads"

    # Set ownership
    chown -R "$APP_USER:$APP_GROUP" "$DEPLOY_DIR"
    chown -R "$APP_USER:$APP_GROUP" "$LOG_DIR"

    log_info "Directories created successfully"
}

# Backup current deployment
backup_current() {
    if [[ -f "$DEPLOY_DIR/app.war" ]]; then
        log_info "Backing up current deployment..."
        cp "$DEPLOY_DIR/app.war" "$BACKUP_DIR/app_${TIMESTAMP}.war"
        log_info "Backup saved to: $BACKUP_DIR/app_${TIMESTAMP}.war"
    else
        log_warn "No existing deployment found, skipping backup"
    fi
}

# Stop application
stop_application() {
    log_info "Stopping application..."
    if systemctl is-active --quiet "$APP_NAME"; then
        systemctl stop "$APP_NAME"
        sleep 5
        log_info "Application stopped"
    else
        log_warn "Application was not running"
    fi
}

# Deploy new version
deploy_war() {
    log_info "Deploying new version..."
    cp "$WAR_FILE" "$DEPLOY_DIR/app.war"
    chown "$APP_USER:$APP_GROUP" "$DEPLOY_DIR/app.war"
    chmod 640 "$DEPLOY_DIR/app.war"
    log_info "WAR file deployed to: $DEPLOY_DIR/app.war"
}

# Start application
start_application() {
    log_info "Starting application..."
    systemctl start "$APP_NAME"
    sleep 10

    if systemctl is-active --quiet "$APP_NAME"; then
        log_info "Application started successfully"
    else
        log_error "Application failed to start"
        log_error "Check logs: journalctl -u $APP_NAME -n 50"
        exit 1
    fi
}

# Health check
health_check() {
    log_info "Performing health check..."
    local max_attempts=30
    local attempt=1
    local health_url="http://localhost:8080/actuator/health"

    while [[ $attempt -le $max_attempts ]]; do
        if curl -s -f "$health_url" > /dev/null 2>&1; then
            log_info "Health check passed!"
            return 0
        fi
        log_warn "Health check attempt $attempt/$max_attempts failed, retrying..."
        sleep 2
        ((attempt++))
    done

    log_error "Health check failed after $max_attempts attempts"
    return 1
}

# Cleanup old backups (keep last 10)
cleanup_backups() {
    log_info "Cleaning up old backups..."
    cd "$BACKUP_DIR"
    ls -t app_*.war 2>/dev/null | tail -n +11 | xargs -r rm -f
    log_info "Old backups cleaned up"
}

# Main deployment flow
main() {
    echo "==========================================="
    echo "  SM-Caterer Deployment Script"
    echo "  Environment: $ENVIRONMENT"
    echo "  Timestamp: $TIMESTAMP"
    echo "==========================================="

    check_root
    validate_war
    create_directories
    backup_current
    stop_application
    deploy_war
    start_application

    if health_check; then
        cleanup_backups
        log_info "==========================================="
        log_info "Deployment completed successfully!"
        log_info "==========================================="
    else
        log_error "Deployment completed but health check failed"
        log_error "Consider rolling back: systemctl stop $APP_NAME"
        exit 1
    fi
}

# Run main function
main "$@"
