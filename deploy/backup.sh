#!/bin/bash
# ===================================
# SM-Caterer Backup Script
# ===================================
# Usage: ./backup.sh [backup-type]
# Types: full, db, files
# Example: ./backup.sh full
# ===================================

set -e

# Configuration
APP_NAME="sm-caterer"
BACKUP_BASE_DIR="/opt/cloudcaters/backups"
DEPLOY_DIR="/opt/cloudcaters"
LOG_DIR="/var/log/cloudcaters"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_TYPE="${1:-full}"

# Database credentials (from environment or defaults)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-sm-caterer}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASSWORD:-}"

# Retention settings
DAILY_RETENTION=7
WEEKLY_RETENTION=4
MONTHLY_RETENTION=12

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# Create backup directories
setup_directories() {
    mkdir -p "$BACKUP_BASE_DIR/daily"
    mkdir -p "$BACKUP_BASE_DIR/weekly"
    mkdir -p "$BACKUP_BASE_DIR/monthly"
    mkdir -p "$BACKUP_BASE_DIR/temp"
}

# Backup database
backup_database() {
    log_info "Starting database backup..."
    local backup_file="$BACKUP_BASE_DIR/temp/db_${TIMESTAMP}.sql"

    if [[ -z "$DB_PASS" ]]; then
        log_error "Database password not set. Export DB_PASSWORD environment variable."
        return 1
    fi

    mysqldump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" \
        --single-transaction \
        --routines \
        --triggers \
        --add-drop-table \
        "$DB_NAME" > "$backup_file"

    if [[ $? -eq 0 ]]; then
        gzip "$backup_file"
        log_info "Database backup completed: ${backup_file}.gz"
        echo "${backup_file}.gz"
    else
        log_error "Database backup failed"
        return 1
    fi
}

# Backup application files
backup_files() {
    log_info "Starting files backup..."
    local backup_file="$BACKUP_BASE_DIR/temp/files_${TIMESTAMP}.tar.gz"

    tar -czf "$backup_file" \
        -C "$DEPLOY_DIR" \
        --exclude='*.war' \
        --exclude='backups' \
        uploads \
        2>/dev/null || true

    if [[ -f "$backup_file" ]]; then
        log_info "Files backup completed: $backup_file"
        echo "$backup_file"
    else
        log_warn "No files to backup or backup failed"
    fi
}

# Backup logs
backup_logs() {
    log_info "Starting logs backup..."
    local backup_file="$BACKUP_BASE_DIR/temp/logs_${TIMESTAMP}.tar.gz"

    if [[ -d "$LOG_DIR" ]]; then
        tar -czf "$backup_file" -C "$LOG_DIR" . 2>/dev/null || true
        log_info "Logs backup completed: $backup_file"
        echo "$backup_file"
    else
        log_warn "Log directory not found"
    fi
}

# Create full backup archive
create_full_backup() {
    log_info "Creating full backup archive..."
    local final_backup="$BACKUP_BASE_DIR/daily/smcaterer_full_${TIMESTAMP}.tar.gz"

    cd "$BACKUP_BASE_DIR/temp"
    tar -czf "$final_backup" *.gz *.sql 2>/dev/null || true

    # Cleanup temp files
    rm -f "$BACKUP_BASE_DIR/temp"/*.gz "$BACKUP_BASE_DIR/temp"/*.sql

    log_info "Full backup created: $final_backup"
    echo "$final_backup"
}

# Rotate backups based on retention policy
rotate_backups() {
    log_info "Rotating backups..."

    # Daily cleanup (keep last N days)
    find "$BACKUP_BASE_DIR/daily" -name "*.tar.gz" -mtime +$DAILY_RETENTION -delete

    # Weekly backups (every Sunday)
    if [[ $(date +%u) -eq 7 ]]; then
        local latest=$(ls -t "$BACKUP_BASE_DIR/daily"/*.tar.gz 2>/dev/null | head -1)
        if [[ -n "$latest" ]]; then
            cp "$latest" "$BACKUP_BASE_DIR/weekly/"
            find "$BACKUP_BASE_DIR/weekly" -name "*.tar.gz" -mtime +$((WEEKLY_RETENTION * 7)) -delete
        fi
    fi

    # Monthly backups (first day of month)
    if [[ $(date +%d) -eq 01 ]]; then
        local latest=$(ls -t "$BACKUP_BASE_DIR/daily"/*.tar.gz 2>/dev/null | head -1)
        if [[ -n "$latest" ]]; then
            cp "$latest" "$BACKUP_BASE_DIR/monthly/"
            find "$BACKUP_BASE_DIR/monthly" -name "*.tar.gz" -mtime +$((MONTHLY_RETENTION * 30)) -delete
        fi
    fi

    log_info "Backup rotation completed"
}

# Calculate backup size
show_backup_stats() {
    log_info "Backup Statistics:"
    echo "  Daily backups:   $(ls -1 "$BACKUP_BASE_DIR/daily"/*.tar.gz 2>/dev/null | wc -l) files"
    echo "  Weekly backups:  $(ls -1 "$BACKUP_BASE_DIR/weekly"/*.tar.gz 2>/dev/null | wc -l) files"
    echo "  Monthly backups: $(ls -1 "$BACKUP_BASE_DIR/monthly"/*.tar.gz 2>/dev/null | wc -l) files"
    echo "  Total size:      $(du -sh "$BACKUP_BASE_DIR" 2>/dev/null | cut -f1)"
}

# Main backup flow
main() {
    echo "==========================================="
    echo "  SM-Caterer Backup Script"
    echo "  Type: $BACKUP_TYPE"
    echo "  Timestamp: $TIMESTAMP"
    echo "==========================================="

    setup_directories

    case $BACKUP_TYPE in
        full)
            backup_database
            backup_files
            backup_logs
            create_full_backup
            ;;
        db)
            backup_database
            ;;
        files)
            backup_files
            ;;
        *)
            log_error "Unknown backup type: $BACKUP_TYPE"
            log_info "Usage: $0 [full|db|files]"
            exit 1
            ;;
    esac

    rotate_backups
    show_backup_stats

    log_info "==========================================="
    log_info "Backup completed successfully!"
    log_info "==========================================="
}

# Run main function
main "$@"
