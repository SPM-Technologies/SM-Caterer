/**
 * SM-Caterer Common JavaScript Functions
 */

// CSRF Token setup for AJAX requests
$(document).ready(function() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    if (token && header) {
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    }

    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        $('.alert-dismissible').fadeOut('slow');
    }, 5000);
});

/**
 * Initialize DataTable with common settings
 */
function initDataTable(tableId, options) {
    var defaultOptions = {
        responsive: true,
        pageLength: 10,
        lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "All"]],
        language: {
            search: "_INPUT_",
            searchPlaceholder: "Search...",
            lengthMenu: "Show _MENU_ entries",
            info: "Showing _START_ to _END_ of _TOTAL_ entries",
            infoEmpty: "No entries found",
            infoFiltered: "(filtered from _MAX_ total entries)",
            paginate: {
                first: '<i class="bi bi-chevron-double-left"></i>',
                previous: '<i class="bi bi-chevron-left"></i>',
                next: '<i class="bi bi-chevron-right"></i>',
                last: '<i class="bi bi-chevron-double-right"></i>'
            }
        },
        dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
             '<"row"<"col-sm-12"tr>>' +
             '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
        order: [[0, 'asc']]
    };

    return $('#' + tableId).DataTable($.extend({}, defaultOptions, options));
}

/**
 * Show loading spinner
 */
function showLoading() {
    if ($('.spinner-overlay').length === 0) {
        $('body').append('<div class="spinner-overlay"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>');
    }
    $('.spinner-overlay').show();
}

/**
 * Hide loading spinner
 */
function hideLoading() {
    $('.spinner-overlay').hide();
}

/**
 * Show toast notification
 */
function showToast(message, type) {
    type = type || 'info';
    var iconClass = {
        'success': 'bi-check-circle',
        'error': 'bi-exclamation-triangle',
        'warning': 'bi-exclamation-circle',
        'info': 'bi-info-circle'
    };

    var bgClass = {
        'success': 'bg-success',
        'error': 'bg-danger',
        'warning': 'bg-warning',
        'info': 'bg-info'
    };

    var toastHtml = '<div class="toast align-items-center text-white ' + bgClass[type] + ' border-0" role="alert">' +
        '<div class="d-flex">' +
        '<div class="toast-body"><i class="bi ' + iconClass[type] + '"></i> ' + message + '</div>' +
        '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>' +
        '</div></div>';

    if ($('.toast-container').length === 0) {
        $('body').append('<div class="toast-container"></div>');
    }

    var $toast = $(toastHtml);
    $('.toast-container').append($toast);

    var toast = new bootstrap.Toast($toast[0], { delay: 3000 });
    toast.show();

    $toast.on('hidden.bs.toast', function() {
        $(this).remove();
    });
}

/**
 * Confirm delete action
 */
function confirmDelete(url, itemName) {
    if (confirm('Are you sure you want to delete "' + itemName + '"? This action cannot be undone.')) {
        showLoading();
        $.ajax({
            url: url,
            type: 'DELETE',
            success: function(response) {
                hideLoading();
                showToast('Item deleted successfully', 'success');
                setTimeout(function() {
                    location.reload();
                }, 1000);
            },
            error: function(xhr) {
                hideLoading();
                var message = xhr.responseJSON ? xhr.responseJSON.message : 'An error occurred';
                showToast(message, 'error');
            }
        });
    }
}

/**
 * Format currency
 */
function formatCurrency(amount, currency) {
    currency = currency || 'INR';
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

/**
 * Format date
 */
function formatDate(dateString) {
    if (!dateString) return '-';
    var date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

/**
 * Validate form before submit
 */
function validateForm(formId) {
    var form = document.getElementById(formId);
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return false;
    }
    return true;
}

/**
 * Toggle status via AJAX
 */
function toggleStatus(url, currentStatus) {
    showLoading();
    $.ajax({
        url: url,
        type: 'PATCH',
        contentType: 'application/json',
        data: JSON.stringify({ status: currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' }),
        success: function(response) {
            hideLoading();
            showToast('Status updated successfully', 'success');
            setTimeout(function() {
                location.reload();
            }, 1000);
        },
        error: function(xhr) {
            hideLoading();
            var message = xhr.responseJSON ? xhr.responseJSON.message : 'An error occurred';
            showToast(message, 'error');
        }
    });
}
